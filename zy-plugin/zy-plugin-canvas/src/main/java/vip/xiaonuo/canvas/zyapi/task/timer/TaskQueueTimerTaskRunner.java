/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Snowy源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://www.xiaonuo.vip
 * 5.不可二次分发开源参与同类竞品，如有想法可联系团队xiaonuobase@qq.com商议合作。
 * 6.若您的项目无法满足以上几点，需要更多功能代码，获取Snowy商业授权许可，请在官网购买授权，地址为 https://www.xiaonuo.vip
 */
package vip.xiaonuo.canvas.zyapi.task.timer;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vip.xiaonuo.canvas.zyapi.task.TaskWorkerCoordinator;
import vip.xiaonuo.common.timer.CommonTimerTaskRunner;

/**
 * 任务队列定时任务
 * 每2秒轮询一次任务队列
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Component
public class TaskQueueTimerTaskRunner implements CommonTimerTaskRunner {

    private static final Logger log = LoggerFactory.getLogger(TaskQueueTimerTaskRunner.class);

    @Resource
    private TaskWorkerCoordinator taskWorkerCoordinator;

    /** 租约清理最小间隔（毫秒）：与租约续约周期一致，避免每 2 秒全表扫描 running 任务 */
    private static final long CLEANUP_INTERVAL_MS = 30000L;

    /** 上次执行租约清理的时间戳（volatile 保证定时器线程可见） */
    private volatile long lastCleanupAt = 0L;

    @Override
    public void action(String extJson) {
        try {
            // 解析扩展参数
            boolean enabled = true;
            if (ObjectUtil.isNotEmpty(extJson)) {
                JSONObject config = new JSONObject(extJson);
                enabled = config.getBool("enabled", true);
            }

            if (!enabled) {
                log.debug("任务队列已禁用，跳过轮询");
                return;
            }

            // 轮询并处理任务
            taskWorkerCoordinator.pollAndProcess();

            // 清理过期租约（按间隔降频执行，避免高频全表扫描）
            long now = System.currentTimeMillis();
            if (now - lastCleanupAt >= CLEANUP_INTERVAL_MS) {
                taskWorkerCoordinator.cleanupExpiredLeases();
                lastCleanupAt = now;
            }

        } catch (Exception e) {
            log.error("任务队列轮询失败", e);
        }
    }
}