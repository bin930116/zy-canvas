package vip.xiaonuo.canvas.zyapi.task;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.zyapi.config.TaskWorkerConfig;
import vip.xiaonuo.canvas.zyapi.constant.TaskConstants;
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.generationtask.mapper.ZyGenerationTaskMapper;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 任务Worker协调器
 * 负责任务领取、并发控制、租约续约
 *
 * 支持中型规模并发处理（50-100并发任务）
 *
 * @author hanbin
 * @date 2026/09/15
 **/
@Service
public class TaskWorkerCoordinator {

    private static final Logger log = LoggerFactory.getLogger(TaskWorkerCoordinator.class);

    @Resource
    private ZyGenerationTaskMapper zyGenerationTaskMapper;

    @Resource
    private TaskProcessorRegistry taskProcessorRegistry;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TaskWorkerConfig taskWorkerConfig;

    private final String workerId = IdUtil.fastSimpleUUID();

    /** 租约续约周期（秒） */
    private static final long LEASE_RENEWAL_PERIOD_SECONDS = 30;

    // 用于异步处理任务的线程池：固定大小（与配置并发数一致，配合 Redis 槽位计数不超发）
    private ExecutorService taskExecutor;

    // 租约续约调度器
    private final ScheduledExecutorService leaseRenewalScheduler = Executors.newScheduledThreadPool(2);

    // 活跃任务的续约任务映射
    private final ConcurrentHashMap<String, ScheduledFuture<?>> activeRenewals = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        int concurrency = Math.max(1, taskWorkerConfig.getConcurrency());
        taskExecutor = Executors.newFixedThreadPool(concurrency);
        log.info("任务Worker初始化: workerId={}, 并发线程数={}", workerId, concurrency);
    }

    /**
     * 轮询并处理任务
     * <p>
     * 每轮循环领取多个任务：定时器每 2 秒触发一次，若每次只领取 1 个，
     * 批量任务场景（如一次提交 50 个图片）需要 2s×50=100s 才能全部开工，
     * 50 并发线程池会长期喂不饱。单轮上限 10 个，避免长时间占住定时器线程。
     */
    public void pollAndProcess() {
        int maxClaimPerRound = 10;
        for (int i = 0; i < maxClaimPerRound; i++) {
            // 1. 领取任务（不需要提前检查并发，领取时再检查）
            ZyGenerationTask task = claimNextTask();
            if (ObjectUtil.isEmpty(task)) {
                return;
            }

            // 2. 检查并发槽位（领取成功后再检查，避免浪费）
            if (!acquireConcurrencySlot()) {
                // 没有可用槽位，释放任务（带短退避让位，避免队头任务反复占用领取）
                releaseTask(task);
                return;
            }

            // 3. 异步处理任务（提交被拒绝时释放槽位并重新入队，避免槽位泄漏/任务丢失）
            try {
                taskExecutor.submit(() -> {
                    try {
                        processTask(task);
                    } finally {
                        releaseConcurrencySlot();
                    }
                });
            } catch (RejectedExecutionException e) {
                releaseConcurrencySlot();
                releaseTask(task);
                log.warn("任务 {} 提交线程池被拒绝，已释放槽位并重新入队", task.getId());
            }
        }
    }

    /**
     * 领取下一个待处理任务
     *
     * @return 任务实体，如果没有任务返回null
     */
    private ZyGenerationTask claimNextTask() {
        // 查询待处理任务（按创建时间排序）
        // 说明：不额外加分布式锁——乐观锁（update 带 status=queued 条件）已保证同一任务只被一个 Worker 领取，
        // 加全局锁反而增加每 2 秒一次的锁竞争延迟。
        // lease_expires_at 语义：queued 任务 = 重试退避截止时间（未到时间不领取）；running 任务 = 租约到期时间
        QueryWrapper<ZyGenerationTask> queryWrapper = new QueryWrapper<ZyGenerationTask>()
                .eq("status", TaskConstants.Status.QUEUED)
                .and(w -> w.isNull("lease_expires_at").or().le("lease_expires_at", new Date()))
                .orderByAsc("create_time")
                .last("LIMIT 1");

        ZyGenerationTask task = zyGenerationTaskMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(task)) {
            return null;
        }

        // 检查是否有对应的处理器；没有则直接标记失败，避免任务永久滞留 queued
        if (!taskProcessorRegistry.hasProcessor(task.getType())) {
            log.warn("没有找到任务类型 {} 的处理器，标记任务 {} 失败", task.getType(), task.getId());
            UpdateWrapper<ZyGenerationTask> failWrapper = new UpdateWrapper<ZyGenerationTask>()
                    .eq("id", task.getId())
                    .eq("status", TaskConstants.Status.QUEUED)
                    .set("status", TaskConstants.Status.FAILED)
                    .set("error", "没有找到任务类型 " + task.getType() + " 的处理器")
                    .set("completed_at", new Date());
            zyGenerationTaskMapper.update(null, failWrapper);
            return null;
        }

        // 更新任务状态为running，设置Worker ID和租约过期时间
        Date now = new Date();
        Date leaseExpiresAt = new Date(now.getTime() + taskWorkerConfig.getLeaseTimeout() * 1000L);

        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", task.getId())
                .eq("status", TaskConstants.Status.QUEUED)  // 乐观锁：只更新状态为queued的任务
                .set("status", TaskConstants.Status.RUNNING)
                .set("worker_id", workerId)
                .set("started_at", now)
                .set("lease_expires_at", leaseExpiresAt);

        int updated = zyGenerationTaskMapper.update(null, updateWrapper);
        if (updated == 0) {
            // 任务已被其他Worker领取
            return null;
        }

        log.info("Worker {} 领取任务 {}", workerId, task.getId());
        return task;
    }

    /**
     * 释放任务（将状态改回queued）
     * <p>
     * 加 5 秒短退避：并发满时若不退避，队头任务会被每 2 秒反复领取→释放，
     * 导致排在它后面的任务永远无法被领取（队头饥饿）。
     */
    private void releaseTask(ZyGenerationTask task) {
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", task.getId())
                .eq("worker_id", workerId)
                .set("status", TaskConstants.Status.QUEUED)
                .set("worker_id", null)
                .set("lease_expires_at", new Date(System.currentTimeMillis() + RELEASE_BACKOFF_MS));

        zyGenerationTaskMapper.update(null, updateWrapper);
        log.info("Worker {} 释放任务 {}（{}ms 后可重领）", workerId, task.getId(), RELEASE_BACKOFF_MS);
    }

    /** 释放任务后的让位退避（毫秒）：避免队头任务在并发满时反复占用领取 */
    private static final long RELEASE_BACKOFF_MS = 5000L;

    /**
     * 处理任务（带自动租约续约）
     *
     * @param task 任务实体
     */
    private void processTask(ZyGenerationTask task) {
        TaskProcessor processor = taskProcessorRegistry.getProcessor(task.getType());
        if (ObjectUtil.isEmpty(processor)) {
            log.error("任务 {} 没有找到处理器", task.getId());
            markTaskFailed(task, "没有找到对应的任务处理器");
            return;
        }

        // 启动租约自动续约（每30秒续约一次）
        ScheduledFuture<?> renewalFuture = leaseRenewalScheduler.scheduleAtFixedRate(() -> {
            try {
                renewLease(task.getId());
            } catch (Exception e) {
                log.error("任务 {} 租约续约失败", task.getId(), e);
            }
        }, LEASE_RENEWAL_PERIOD_SECONDS, LEASE_RENEWAL_PERIOD_SECONDS, TimeUnit.SECONDS);
        activeRenewals.put(task.getId(), renewalFuture);

        try {
            log.info("开始处理任务 {}, 类型: {}", task.getId(), task.getType());
            
            // 执行任务处理器（处理器内部负责更新任务为 succeeded/failed 并写入 result_json/error）
            processor.process(task);

            // 兜底：若处理器未更新任务状态（仍为 running），则由协调器标记成功
            markTaskSucceededIfStillRunning(task);
            
            log.info("任务 {} 处理成功", task.getId());
            
        } catch (Exception e) {
            log.error("任务 {} 处理失败", task.getId(), e);
            
            // 检查是否需要重试
            if (shouldRetry(task)) {
                markTaskForRetry(task, e.getMessage());
            } else {
                markTaskFailed(task, e.getMessage());
            }
        } finally {
            // 停止租约续约
            ScheduledFuture<?> future = activeRenewals.remove(task.getId());
            if (future != null) {
                future.cancel(false);
            }
        }
    }

    /**
     * 判断是否应该重试
     */
    private boolean shouldRetry(ZyGenerationTask task) {
        Integer retryCount = task.getRetryCount();
        Integer maxRetries = task.getMaxRetries();
        
        if (retryCount == null) retryCount = 0;
        if (maxRetries == null) maxRetries = taskWorkerConfig.getMaxRetries();
        
        return retryCount < maxRetries;
    }

    /**
     * 标记任务重试。
     * 带乐观条件（仅 running 可重试，避免覆盖已取消/已成功的状态）；
     * 同时按重试次数指数退避（lease_expires_at 作为"最早可重新领取时间"，
     * 对 queued 任务不再表示租约，claimNextTask 会排除未到时间的任务）。
     */
    private void markTaskForRetry(ZyGenerationTask task, String error) {
        Date now = new Date();
        int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();
        // 退避：10s → 20s → 40s → 80s → 160s → 320s → 封顶 600s，避免上游临时故障时反复重打
        long backoffMs = Math.min(600_000L, 10_000L << Math.min(retryCount, 5));
        Date retryAt = new Date(now.getTime() + backoffMs);

        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", task.getId())
                .eq("status", TaskConstants.Status.RUNNING)
                .set("status", TaskConstants.Status.QUEUED)
                .set("error", error)
                .setSql("retry_count = retry_count + 1")
                .set("worker_id", null)
                .set("lease_expires_at", retryAt);

        int updated = zyGenerationTaskMapper.update(null, updateWrapper);
        if (updated > 0) {
            log.info("任务 {} 已标记为重试（第 {} 次，{}s 后可重领）", task.getId(), retryCount + 1, backoffMs / 1000);
        } else {
            log.info("任务 {} 状态已变化（可能已取消），跳过重试标记", task.getId());
        }
    }

    /**
     * 仅当任务仍处于 running 时标记成功（避免覆盖处理器已写入的失败状态/结果）
     */
    private void markTaskSucceededIfStillRunning(ZyGenerationTask task) {
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", task.getId())
                .eq("status", TaskConstants.Status.RUNNING)
                .set("status", TaskConstants.Status.SUCCEEDED)
                .set("completed_at", new Date())
                .set("progress", 100);

        zyGenerationTaskMapper.update(null, updateWrapper);
    }

    /**
     * 标记任务失败（仅当任务仍处于活跃状态，避免覆盖已取消状态）
     */
    private void markTaskFailed(ZyGenerationTask task, String error) {
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", task.getId())
                .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING)
                .set("status", TaskConstants.Status.FAILED)
                .set("error", error)
                .set("completed_at", new Date());

        zyGenerationTaskMapper.update(null, updateWrapper);
    }

    /**
     * 获取并发槽位
     */
    private boolean acquireConcurrencySlot() {
        int maxConcurrency = taskWorkerConfig.getConcurrency();
        // 使用Redis原子计数器实现并发控制
        String counterKey = "task:concurrency:counter";
        Long current = redissonClient.getAtomicLong(counterKey).incrementAndGet();

        // 刷新计数器TTL：活跃期间由领取/租约续约持续续期；Worker 崩溃后计数自动过期归零，
        // 避免计数虚高导致新任务永远无法领取槽位
        refreshConcurrencyCounterTtl(counterKey);

        if (current > maxConcurrency) {
            redissonClient.getAtomicLong(counterKey).decrementAndGet();
            return false;
        }

        return true;
    }

    /**
     * 刷新并发计数器 TTL：TTL = 2 倍租约时长。
     * 处理中任务的租约续约（每 30s）也会刷新，因此只要任一 Worker 存活，计数保持；
     * 所有 Worker 崩溃/空闲后，计数最迟 2 个租约周期自动过期归零。
     */
    private void refreshConcurrencyCounterTtl(String counterKey) {
        long ttlSeconds = Math.max(60L, taskWorkerConfig.getLeaseTimeout() * 2L);
        try {
            redissonClient.getAtomicLong(counterKey).expire(ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("刷新并发计数器TTL失败: {}", e.getMessage());
        }
    }

    /**
     * 释放并发槽位
     */
    private void releaseConcurrencySlot() {
        String counterKey = "task:concurrency:counter";
        redissonClient.getAtomicLong(counterKey).decrementAndGet();
    }

    /**
     * 续约任务租约
     */
    public void renewLease(String taskId) {
        int leaseTimeoutSeconds = taskWorkerConfig.getLeaseTimeout();
        Date leaseExpiresAt = new Date(System.currentTimeMillis() + leaseTimeoutSeconds * 1000L);
        
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", taskId)
                .eq("worker_id", workerId)
                .eq("status", TaskConstants.Status.RUNNING)
                .set("lease_expires_at", leaseExpiresAt);

        int updated = zyGenerationTaskMapper.update(null, updateWrapper);
        if (updated > 0) {
            log.debug("任务 {} 租约续约成功", taskId);
            // 活跃任务续约时同步刷新并发计数器 TTL，保证处理中的槽位计数不会过期
            refreshConcurrencyCounterTtl("task:concurrency:counter");
        }
    }

    /**
     * 清理过期租约的任务
     */
    public void cleanupExpiredLeases() {
        // 查找租约过期但仍处于running状态的任务
        QueryWrapper<ZyGenerationTask> queryWrapper = new QueryWrapper<ZyGenerationTask>()
                .eq("status", TaskConstants.Status.RUNNING)
                .isNotNull("lease_expires_at")
                .lt("lease_expires_at", new Date());

        List<ZyGenerationTask> expiredTasks = zyGenerationTaskMapper.selectList(queryWrapper);
        
        for (ZyGenerationTask task : expiredTasks) {
            log.warn("任务 {} 租约过期，重新入队", task.getId());

            // 乐观条件：仅当仍处于 running 且 worker 未变化时才 re-queue，
            // 避免清理瞬间任务已被其他 Worker 重新领取时被错误改回 queued
            UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                    .eq("id", task.getId())
                    .eq("status", TaskConstants.Status.RUNNING)
                    .eq("worker_id", task.getWorkerId())
                    .set("status", TaskConstants.Status.QUEUED)
                    .set("worker_id", null)
                    .set("lease_expires_at", null);

            int updated = zyGenerationTaskMapper.update(null, updateWrapper);
            if (updated == 0) {
                log.info("任务 {} 已被重新处理，跳过租约清理", task.getId());
            }
        }
    }

    /**
     * 获取Worker状态信息
     */
    public WorkerStatus getWorkerStatus() {
        String counterKey = "task:concurrency:counter";
        long currentConcurrency = redissonClient.getAtomicLong(counterKey).get();
        
        // 查询各状态任务数量
        long queuedCount = countTasksByStatus(TaskConstants.Status.QUEUED);
        long runningCount = countTasksByStatus(TaskConstants.Status.RUNNING);
        long succeededCount = countTasksByStatus(TaskConstants.Status.SUCCEEDED);
        long failedCount = countTasksByStatus(TaskConstants.Status.FAILED);
        
        return new WorkerStatus(
            workerId,
            taskWorkerConfig.getConcurrency(),
            (int) currentConcurrency,
            activeRenewals.size(),
            queuedCount,
            runningCount,
            succeededCount,
            failedCount
        );
    }

    private long countTasksByStatus(String status) {
        QueryWrapper<ZyGenerationTask> queryWrapper = new QueryWrapper<ZyGenerationTask>()
                .eq("status", status);
        return zyGenerationTaskMapper.selectCount(queryWrapper);
    }

    /**
     * 优雅停机
     */
    public void shutdown() {
        log.info("Worker {} 开始优雅停机...", workerId);
        
        // 停止所有租约续约
        activeRenewals.forEach((taskId, future) -> {
            future.cancel(false);
            log.info("已停止任务 {} 的租约续约", taskId);
        });
        activeRenewals.clear();
        
        // 关闭线程池
        taskExecutor.shutdown();
        leaseRenewalScheduler.shutdown();
        
        try {
            if (!taskExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
            }
            if (!leaseRenewalScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                leaseRenewalScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            taskExecutor.shutdownNow();
            leaseRenewalScheduler.shutdownNow();
        }
        
        log.info("Worker {} 已完成停机", workerId);
    }

    /**
     * Worker状态信息类
     */
    public static class WorkerStatus {
        public final String workerId;
        public final int maxConcurrency;
        public final int currentConcurrency;
        public final int activeTasks;
        public final long queuedCount;
        public final long runningCount;
        public final long succeededCount;
        public final long failedCount;
        
        public WorkerStatus(String workerId, int maxConcurrency, int currentConcurrency, 
                          int activeTasks, long queuedCount, long runningCount,
                          long succeededCount, long failedCount) {
            this.workerId = workerId;
            this.maxConcurrency = maxConcurrency;
            this.currentConcurrency = currentConcurrency;
            this.activeTasks = activeTasks;
            this.queuedCount = queuedCount;
            this.runningCount = runningCount;
            this.succeededCount = succeededCount;
            this.failedCount = failedCount;
        }
    }
}