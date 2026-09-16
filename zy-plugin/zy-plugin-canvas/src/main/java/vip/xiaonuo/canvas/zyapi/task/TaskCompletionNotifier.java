package vip.xiaonuo.canvas.zyapi.task;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.dev.api.DevMessageApi;

import java.util.List;

/**
 * 生成任务完成 → 站内信「任务提醒」
 * category 固定 task，通知中心「提醒消息」页会显示为任务提醒。
 *
 * @author xuyuxiang
 * @date 2026/9/10
 **/
@Service
public class TaskCompletionNotifier {

    private static final Logger log = LoggerFactory.getLogger(TaskCompletionNotifier.class);

    private static final String CATEGORY = "task";

    @Resource
    private DevMessageApi devMessageApi;

    public void notifySuccess(ZyGenerationTask task) {
        if (task == null || StrUtil.isEmpty(task.getUserId())) {
            return;
        }
        String subject = subjectFor(task, "已完成");
        String content = contentFor(task, "生成成功。", "shot");
        sendQuiet(task.getUserId(), subject, content);
    }

    public void notifyFailed(ZyGenerationTask task, String error) {
        if (task == null || StrUtil.isEmpty(task.getUserId())) {
            return;
        }
        String reason = StrUtil.blankToDefault(error, "生成失败");
        String subject = subjectFor(task, "失败");
        String content = contentFor(task, "生成失败：" + reason, "shot");
        sendQuiet(task.getUserId(), subject, content);
    }

    private String subjectFor(ZyGenerationTask task, String verb) {
        String typeLabel = typeLabel(task.getType());
        String stage = StrUtil.isNotEmpty(task.getStage()) ? task.getStage() : typeLabel;
        String prompt = task.getPrompt();
        String title = StrUtil.isNotBlank(prompt) ? StrUtil.maxLength(StrUtil.trim(prompt), 24) : typeLabel;
        return typeLabel + "·" + stage + verb + "：" + title;
    }

    private String contentFor(ZyGenerationTask task, String summary, String defaultCategoryHint) {
        StringBuilder sb = new StringBuilder();
        sb.append(summary);
        String model = task.getModel();
        if (StrUtil.isNotEmpty(model)) {
            sb.append("\n模型：").append(model);
        }
        String type = task.getType();
        if (StrUtil.isNotEmpty(type)) {
            sb.append("\n类型：").append(typeLabel(type));
        }
        if (StrUtil.isNotBlank(task.getPrompt())) {
            sb.append("\n提示词：").append(StrUtil.maxLength(StrUtil.trim(task.getPrompt()), 120));
        }
        sb.append("\n任务ID：").append(task.getId());
        return sb.toString();
    }

    private String typeLabel(String type) {
        if (StrUtil.isEmpty(type)) {
            return "生成任务";
        }
        if (type.contains("video")) {
            return "镜头视频";
        }
        if (type.contains("image") || type.contains("storyboard")) {
            return "分镜图";
        }
        if (type.contains("audio")) {
            return "音频";
        }
        if (type.contains("text")) {
            return "文本";
        }
        return type;
    }

    private void sendQuiet(String userId, String subject, String content) {
        try {
            devMessageApi.sendMessageWithContent(List.of(userId), CATEGORY, subject, content);
            log.info("已发送任务站内信: userId={}, subject={}", userId, subject);
        } catch (Exception e) {
            log.warn("任务站内信发送失败（不影响任务结果）: {}", e.getMessage());
        }
    }
}
