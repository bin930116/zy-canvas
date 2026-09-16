package vip.xiaonuo.canvas.zyapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 任务Worker配置
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Configuration
@ConfigurationProperties(prefix = "task.worker")
public class TaskWorkerConfig {

    /**
     * Worker并发数（中型应用推荐50-100）
     */
    private int concurrency = 50;

    /**
     * 任务租约超时时间（秒）
     * 长任务需要调大，避免租约过期
     */
    private int leaseTimeout = 120;

    /**
     * 任务轮询间隔（毫秒）：TaskPollingService 查询上游任务状态的频率
     */
    private long pollInterval = 10000;

    /**
     * 任务轮询超时（毫秒）：视频生成通常需要 5~20 分钟，默认 30 分钟
     */
    private long pollTimeoutMs = 1800000;

    /**
     * 结果处理并发数：产物下载+落库在独立线程池执行，避免占用轮询调度线程
     * （大文件下载会阻塞线程较长时间，数量不宜过大）
     */
    private int resultConcurrency = 4;

    /**
     * 默认最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 是否启用Worker
     */
    private boolean enabled = true;

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public int getLeaseTimeout() {
        return leaseTimeout;
    }

    public void setLeaseTimeout(int leaseTimeout) {
        this.leaseTimeout = leaseTimeout;
    }

    public long getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }

    public long getPollTimeoutMs() {
        return pollTimeoutMs;
    }

    public void setPollTimeoutMs(long pollTimeoutMs) {
        this.pollTimeoutMs = pollTimeoutMs;
    }

    public int getResultConcurrency() {
        return resultConcurrency;
    }

    public void setResultConcurrency(int resultConcurrency) {
        this.resultConcurrency = resultConcurrency;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}