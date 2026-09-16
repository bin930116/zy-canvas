package vip.xiaonuo.canvas.zyapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.generationtask.mapper.ZyGenerationTaskMapper;
import vip.xiaonuo.canvas.zyapi.constant.TaskConstants;
import vip.xiaonuo.canvas.zyapi.service.ZyApiGenerationTaskService;
import vip.xiaonuo.canvas.zyapi.task.TaskWorkerCoordinator;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 生成任务接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 23:00
 **/
@Tag(name = "生成任务")
@RestController
@RequestMapping("/tasks")
public class ZyApiGenerationTaskController {

    @Resource
    private ZyApiGenerationTaskService zyGenerationTaskService;

    @Resource
    private TaskWorkerCoordinator taskWorkerCoordinator;

    @Resource
    private ZyGenerationTaskMapper zyGenerationTaskMapper;

    /**
     * 获取生成任务列表
     *
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     **/
    @Operation(summary = "获取生成任务列表")
    @GetMapping
    public CommonResult<List<Map<String, Object>>> listTasks(@RequestParam(required = false) Integer limit,
                                                             @RequestParam(required = false) String projectId,
                                                             @RequestParam(required = false) Boolean activeOnly) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyGenerationTaskService.listTasks(userId, projectId, activeOnly, limit));
    }

    /**
     * 创建生成任务
     *
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     **/
    @Operation(summary = "创建生成任务")
    @PostMapping
    public CommonResult<Map<String, Object>> createTask(@RequestBody Map<String, Object> input) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyGenerationTaskService.createTask(userId, input));
    }

    /**
     * 获取生成任务详情
     *
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     **/
    @Operation(summary = "获取生成任务详情")
    @GetMapping("/{id}")
    public CommonResult<Map<String, Object>> getTask(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyGenerationTaskService.getTask(userId, id));
    }

    /**
     * 重试任务
     *
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     **/
    @Operation(summary = "重试任务")
    @PostMapping("/{id}/retry")
    public CommonResult<Map<String, Object>> retryTask(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyGenerationTaskService.retryTask(userId, id));
    }

    /**
     * 取消任务
     *
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     **/
    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    public CommonResult<Map<String, Object>> cancelTask(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyGenerationTaskService.cancelTask(userId, id));
    }

    /**
     * 查询上游渠道状态（失败视频任务恢复）
     *
     * @author xuyuxiang
     * @date 2026/9/8
     **/
    @Operation(summary = "查询上游任务状态")
    @PostMapping("/{id}/query-provider")
    public CommonResult<Map<String, Object>> queryProvider(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyGenerationTaskService.queryProviderTask(userId, id));
    }

    /**
     * 获取Worker状态（监控接口）
     *
     * @author Your Name
     * @date 2026/09/06
     **/
    @Operation(summary = "获取Worker状态")
    @GetMapping("/worker/status")
    public CommonResult<TaskWorkerCoordinator.WorkerStatus> getWorkerStatus() {
        return CommonResult.data(taskWorkerCoordinator.getWorkerStatus());
    }

    /**
     * SSE文本流接口
     * 前端轮询任务文本输出
     *
     * @param id    任务ID
     * @param after 上次事件ID（游标）
     * @return SSE事件流
     */
    @Operation(summary = "任务文本流SSE")
    @GetMapping("/{id}/text-events")
    public SseEmitter textEvents(@PathVariable String id,
                                 @RequestParam(required = false, defaultValue = "0") Long after,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        // 手动设置 CORS 头（SSE 响应不经过 SaServletFilter）
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", origin != null ? origin : "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        // 查询任务
        ZyGenerationTask task = zyGenerationTaskMapper.selectById(id);
        if (task == null) {
            emitter.completeWithError(new RuntimeException("任务不存在"));
            return emitter;
        }

        // 如果任务已完成或失败，直接返回最终结果
        String status = task.getStatus();
        if (TaskConstants.Status.SUCCEEDED.equals(status)
                || TaskConstants.Status.FAILED.equals(status)
                || TaskConstants.Status.CANCELLED.equals(status)) {
            try {
                String text = task.getTextDraft() != null ? task.getTextDraft() : "";
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name("delta")
                        .data(Map.of("sequence", System.currentTimeMillis(), "content", text)));
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data(Map.of("status", status)));
                emitter.send(SseEmitter.event()
                        .name("terminal")
                        .data(Map.of("finalText", text, "status", status)));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 任务进行中，轮询等待结果
        final String taskId = id;
        final SseEmitter finalEmitter = emitter;
        new Thread(() -> {
            try {
                long lastSequence = after;
                String lastStatus = null;
                String lastStage = null;
                Integer lastProgress = null;
                int sentLength = 0; // 已推送的 text_draft 长度，下次只发增量
                while (true) {
                    Thread.sleep(500); // 500ms 轮询，流式更跟手
                    ZyGenerationTask currentTask = zyGenerationTaskMapper.selectById(taskId);
                    if (currentTask == null) break;

                    String currentStatus = currentTask.getStatus();
                    String textDraft = currentTask.getTextDraft() != null ? currentTask.getTextDraft() : "";

                    // 只推送新增片段（前端 onTextDelta 做 fullText += content）
                    if (textDraft.length() > sentLength) {
                        String delta = textDraft.substring(sentLength);
                        sentLength = textDraft.length();
                        long sequence = System.currentTimeMillis();
                        if (sequence > lastSequence) {
                            finalEmitter.send(SseEmitter.event()
                                    .id(String.valueOf(sequence))
                                    .name("delta")
                                    .data(Map.of("sequence", sequence, "content", delta)));
                            lastSequence = sequence;
                        }
                    }

                    // 发送状态/阶段/进度变化
                    String currentStage = currentTask.getStage();
                    Integer currentProgress = currentTask.getProgress();
                    if (!Objects.equals(currentStatus, lastStatus)
                            || !Objects.equals(currentStage, lastStage)
                            || !Objects.equals(currentProgress, lastProgress)) {
                        Map<String, Object> progressData = new LinkedHashMap<>();
                        if (currentStatus != null) progressData.put("status", currentStatus);
                        if (currentStage != null) progressData.put("stage", currentStage);
                        if (currentProgress != null) progressData.put("progress", currentProgress);
                        finalEmitter.send(SseEmitter.event().name("progress").data(progressData));
                        lastStatus = currentStatus;
                        lastStage = currentStage;
                        lastProgress = currentProgress;
                    }

                    // 检查是否完成
                    if (TaskConstants.Status.SUCCEEDED.equals(currentStatus)
                            || TaskConstants.Status.FAILED.equals(currentStatus)
                            || TaskConstants.Status.CANCELLED.equals(currentStatus)) {
                        finalEmitter.send(SseEmitter.event()
                                .name("done")
                                .data(Map.of("status", currentStatus)));
                        finalEmitter.send(SseEmitter.event()
                                .name("terminal")
                                .data(Map.of("finalText", textDraft, "status", currentStatus)));
                        finalEmitter.complete();
                        break;
                    }
                }
            } catch (Exception e) {
                finalEmitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}