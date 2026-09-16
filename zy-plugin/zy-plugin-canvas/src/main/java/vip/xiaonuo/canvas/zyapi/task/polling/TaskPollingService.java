package vip.xiaonuo.canvas.zyapi.task.polling;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.zyapi.client.NewApiClient;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiStatusResponse;
import vip.xiaonuo.canvas.zyapi.config.TaskWorkerConfig;
import vip.xiaonuo.canvas.zyapi.constant.TaskConstants;
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.generationtask.mapper.ZyGenerationTaskMapper;
import vip.xiaonuo.canvas.zyapi.service.ZyApiModelService;
import vip.xiaonuo.canvas.zyapi.task.TaskCompletionNotifier;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 任务轮询服务
 * 负责轮询异步任务状态并处理结果
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Service
public class TaskPollingService {

    private static final Logger log = LoggerFactory.getLogger(TaskPollingService.class);

    @Resource
    private NewApiClient newApiClient;

    @Resource
    private ZyGenerationTaskMapper zyGenerationTaskMapper;

    @Resource
    private ZyApiModelService zyModelService;

    @Resource
    private TaskResultHandler taskResultHandler;

    @Resource
    private TaskCompletionNotifier taskCompletionNotifier;

    @Resource
    private TaskWorkerConfig taskWorkerConfig;

    /** 轮询调度器（线程数按并发放大，避免多个慢上游查询占满少数线程导致其他任务轮询延迟） */
    private ScheduledExecutorService scheduler;

    /** 结果处理线程池：产物下载+落库在此执行，避免大文件下载阻塞轮询调度线程 */
    private ExecutorService resultExecutor;

    @jakarta.annotation.PostConstruct
    public void init() {
        // 轮询线程数：并发 50 时取 20，至少 5 个；getStatus 是同步 HTTP 调用，
        // 上游慢时线程被占住，线程太少会拖慢其他任务的轮询节奏
        int pollThreads = Math.min(20, Math.max(5, taskWorkerConfig.getConcurrency()));
        scheduler = Executors.newScheduledThreadPool(pollThreads);
        log.info("轮询调度器初始化: 线程数={}", pollThreads);
        resumeProcessingTasksOnRestart();
    }

    /**
     * 服务重启接管：轮询注册表（activePollings）是进程内内存，重启即丢失；
     * 已提交上游的 processing 任务若不接管会永久卡住（除非手动 query-provider）。
     * 启动时扫描这些任务重新注册轮询，保证重启后任务仍能走到终态。
     */
    private void resumeProcessingTasksOnRestart() {
        try {
            List<ZyGenerationTask> stuck = zyGenerationTaskMapper.selectList(
                    new QueryWrapper<ZyGenerationTask>()
                            .eq("status", TaskConstants.Status.PROCESSING)
                            .isNotNull("provider_request_id")
                            .ne("provider_request_id", ""));
            if (stuck.isEmpty()) {
                return;
            }
            log.info("检测到 {} 个 processing 任务需要重启接管", stuck.size());
            for (ZyGenerationTask task : stuck) {
                try {
                    ZyModel model = zyModelService.getByModelKey(task.getModel());
                    if (model == null) {
                        log.warn("重启接管跳过（模型不存在）: taskId={}, model={}", task.getId(), task.getModel());
                        continue;
                    }
                    registerPolling(task.getId(), model.getId());
                } catch (Exception e) {
                    log.warn("重启接管失败: taskId={}, error={}", task.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("重启接管扫描失败（不影响服务启动）: {}", e.getMessage());
        }
    }

    /** 活跃的轮询任务 */
    private final ConcurrentHashMap<String, ScheduledFuture<?>> activePollings = new ConcurrentHashMap<>();

    /** 连续缺失 providerRequestId 的轮询次数（防御瞬态读不到上游任务ID的情况） */
    private final ConcurrentHashMap<String, Integer> missingProviderCounts = new ConcurrentHashMap<>();

    /** 连续缺失 providerRequestId 达到该次数后标记失败，避免空转到轮询超时 */
    private static final int MAX_MISSING_PROVIDER_POLLS = 3;

    /** 上游查询连续失败次数（用于指数退避，避免上游异常时每 10s 空打） */
    private final ConcurrentHashMap<String, Integer> pollFailures = new ConcurrentHashMap<>();

    /** 下次允许查询上游的时间戳（退避期间跳过查询） */
    private final ConcurrentHashMap<String, Long> nextPollTimes = new ConcurrentHashMap<>();

    /**
     * 注册轮询任务
     *
     * @param taskId  任务ID
     * @param modelId 模型ID
     */
    public void registerPolling(String taskId, String modelId) {
        if (activePollings.containsKey(taskId)) {
            log.warn("任务 {} 已在轮询中", taskId);
            return;
        }

        log.info("注册任务轮询: taskId={}, modelId={}", taskId, modelId);

        long pollIntervalMs = taskWorkerConfig.getPollInterval();
        long pollTimeoutMs = taskWorkerConfig.getPollTimeoutMs();

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                doPoll(taskId, modelId);
            } catch (Exception e) {
                log.error("轮询任务 {} 失败", taskId, e);
            }
        }, pollIntervalMs, pollIntervalMs, TimeUnit.MILLISECONDS);

        activePollings.put(taskId, future);

        // 设置超时
        scheduler.schedule(() -> {
            if (activePollings.containsKey(taskId)) {
                handleTimeout(taskId);
            }
        }, pollTimeoutMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行轮询
     */
    private void doPoll(String taskId, String modelId) {
        // 退避检查：上游查询连续失败时按指数退避跳过本次查询，避免固定 10s 高频空打上游
        Long nextPollAt = nextPollTimes.get(taskId);
        if (nextPollAt != null && System.currentTimeMillis() < nextPollAt) {
            return;
        }

        // 查询任务
        ZyGenerationTask task = zyGenerationTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("任务 {} 不存在，停止轮询", taskId);
            stopPolling(taskId);
            return;
        }

        // 检查任务状态
        if (TaskConstants.Status.CANCELLED.equals(task.getStatus()) || TaskConstants.Status.SUCCEEDED.equals(task.getStatus()) || TaskConstants.Status.FAILED.equals(task.getStatus())) {
            log.info("任务 {} 已终态，停止轮询", taskId);
            stopPolling(taskId);
            return;
        }

        // 查询模型配置（走进程内缓存，轮询每 10s 一次/任务，避免高频查库）
        ZyModel model = zyModelService.getById(modelId);
        if (model == null) {
            log.error("模型 {} 不存在", modelId);
            handleFailed(taskId, "模型配置不存在");
            return;
        }

        // 查询上游任务状态
        String providerTaskId = task.getProviderRequestId();
        if (providerTaskId == null) {
            // 防御：registerPolling 前处理器已写入 providerRequestId，正常情况下不会缺失；
            // 若连续缺失说明任务状态异常，尽早失败而不是空转到轮询超时
            int missingCount = missingProviderCounts.merge(taskId, 1, Integer::sum);
            if (missingCount >= MAX_MISSING_PROVIDER_POLLS) {
                log.error("任务 {} 连续 {} 次无 providerRequestId，标记失败", taskId, missingCount);
                missingProviderCounts.remove(taskId);
                handleFailed(taskId, "上游任务ID缺失，无法轮询");
            } else {
                log.warn("任务 {} 没有providerRequestId（第 {}/{} 次）", taskId, missingCount, MAX_MISSING_PROVIDER_POLLS);
            }
            return;
        }

        NewApiStatusResponse status;
        try {
            status = newApiClient.getStatus(model, providerTaskId);
            // 查询成功：重置失败计数与退避
            pollFailures.remove(taskId);
            nextPollTimes.remove(taskId);
        } catch (Exception e) {
            // 上游查询异常：指数退避（10s→20s→40s→80s→160s→320s 封顶），避免持续高频重打
            int failures = pollFailures.merge(taskId, 1, Integer::sum);
            long pollIntervalMs = taskWorkerConfig.getPollInterval();
            long backoffMs = Math.min(pollIntervalMs * (1L << Math.min(failures, 5)), pollIntervalMs * 32L);
            nextPollTimes.put(taskId, System.currentTimeMillis() + backoffMs);
            log.warn("任务 {} 查询上游异常（第{}次），退避 {}ms 后重试: {}", taskId, failures, backoffMs, e.getMessage());
            return;
        }

        // 更新进度
        if (status.getProgress() != null && status.getProgress() > 0) {
            updateProgress(taskId, status.getProgress());
        }

        // 处理状态
        if (status.isSuccess()) {
            handleSuccess(taskId, status.getResultUrlValue(), model);
        } else if (status.isFailed()) {
            handleFailed(taskId, status.getErrorMessage());
        } else if (status.isCancelled()) {
            handleCancelled(taskId);
        }
    }

    /**
     * 处理成功
     */
    private void handleSuccess(String taskId, String resultUrl, ZyModel model) {
        log.info("任务 {} 成功，结果URL: {}", taskId, resultUrl);
        stopPolling(taskId);

        // 结果下载+落库移到独立线程池执行（产物可能较大，下载耗时可达分钟级），
        // 避免占用轮询调度线程导致其他任务轮询被阻塞
        ensureResultExecutor();
        resultExecutor.submit(() -> {
            try {
                // 前置检查：任务若已被取消则不再下载/回填（与终态更新的状态条件双保险）
                ZyGenerationTask pre = zyGenerationTaskMapper.selectById(taskId);
                if (pre == null || TaskConstants.Status.CANCELLED.equals(pre.getStatus())) {
                    log.info("任务 {} 已取消，跳过结果处理", taskId);
                    return;
                }

                // 下载媒体并存储
                Map<String, Object> result = taskResultHandler.handleResult(taskId, resultUrl, model);

                // 更新任务状态（仅当仍处于活跃状态：避免取消后覆盖回 succeeded）
                UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                        .eq("id", taskId)
                        .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING, TaskConstants.Status.PROCESSING)
                        .set("status", TaskConstants.Status.SUCCEEDED)
                        .set("progress", 100)
                        .set("completed_at", new Date())
                        .set("result_json", JSONUtil.toJsonStr(result));

                int updated = zyGenerationTaskMapper.update(null, updateWrapper);
                if (updated == 0) {
                    log.info("任务 {} 状态已变化（可能已取消），跳过回填与成功通知", taskId);
                    return;
                }

                // 分镜产物回填（与 query-provider 恢复路径共用同一实现）
                ZyGenerationTask currentTask = zyGenerationTaskMapper.selectById(taskId);
                taskResultHandler.backfillShotArtifact(currentTask != null ? currentTask : pre, result);

                ZyGenerationTask notified = zyGenerationTaskMapper.selectById(taskId);
                taskCompletionNotifier.notifySuccess(notified != null ? notified : pre);
            } catch (Exception e) {
                log.error("处理任务 {} 结果失败", taskId, e);
                handleFailed(taskId, "结果处理失败: " + e.getMessage());
            }
        });
    }

    /**
     * 处理失败
     */
    private void handleFailed(String taskId, String error) {
        log.error("任务 {} 失败: {}", taskId, error);
        stopPolling(taskId);

        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", taskId)
                .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING, TaskConstants.Status.PROCESSING)
                .set("status", TaskConstants.Status.FAILED)
                .set("error", error)
                .set("completed_at", new Date());

        int updated = zyGenerationTaskMapper.update(null, updateWrapper);
        if (updated == 0) {
            log.info("任务 {} 状态已变化（可能已取消），跳过失败通知", taskId);
            return;
        }
        ZyGenerationTask task = zyGenerationTaskMapper.selectById(taskId);
        taskCompletionNotifier.notifyFailed(task, error);
    }

    /**
     * 处理取消
     */
    private void handleCancelled(String taskId) {
        log.info("任务 {} 已取消", taskId);
        stopPolling(taskId);

        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", taskId)
                .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING, TaskConstants.Status.PROCESSING)
                .set("status", TaskConstants.Status.CANCELLED)
                .set("completed_at", new Date());

        zyGenerationTaskMapper.update(null, updateWrapper);
    }

    /**
     * 处理超时
     * 标记失败前先尝试通知上游取消：轮询超时通常意味着任务卡在上游，
     * 若不取消上游会继续生成并产生费用。
     */
    private void handleTimeout(String taskId) {
        log.warn("任务 {} 轮询超时", taskId);
        try {
            ZyGenerationTask task = zyGenerationTaskMapper.selectById(taskId);
            if (task != null && StrUtil.isNotEmpty(task.getProviderRequestId())) {
                ZyModel model = zyModelService.getByModelKey(task.getModel());
                if (model != null) {
                    newApiClient.cancelTask(model, task.getProviderRequestId());
                    log.info("轮询超时已通知上游取消: taskId={}, providerTaskId={}", taskId, task.getProviderRequestId());
                }
            }
        } catch (Exception e) {
            log.warn("轮询超时取消上游失败（不影响标记失败）: taskId={}, error={}", taskId, e.getMessage());
        }
        handleFailed(taskId, "任务轮询超时");
    }

    /**
     * 惰性初始化结果处理线程池（线程数取 task.worker.result-concurrency，默认 4）
     */
    private synchronized void ensureResultExecutor() {
        if (resultExecutor == null) {
            int threads = Math.max(1, taskWorkerConfig.getResultConcurrency());
            resultExecutor = Executors.newFixedThreadPool(threads);
            log.info("结果处理线程池初始化: 并发数={}", threads);
        }
    }

    /**
     * 停止轮询
     */
    private void stopPolling(String taskId) {
        ScheduledFuture<?> future = activePollings.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }
        missingProviderCounts.remove(taskId);
        pollFailures.remove(taskId);
        nextPollTimes.remove(taskId);
    }

    /**
     * 更新进度（仅活跃状态：避免取消/失败后轮询竞态把 progress 覆盖回去）
     */
    private void updateProgress(String taskId, int progress) {
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", taskId)
                .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING, TaskConstants.Status.PROCESSING)
                .set("progress", progress);

        zyGenerationTaskMapper.update(null, updateWrapper);
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        activePollings.forEach((taskId, future) -> future.cancel(false));
        activePollings.clear();
        missingProviderCounts.clear();
        pollFailures.clear();
        nextPollTimes.clear();
        scheduler.shutdown();
        if (resultExecutor != null) {
            resultExecutor.shutdown();
        }
    }
}