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
package vip.xiaonuo.canvas.zyapi.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.generationtask.mapper.ZyGenerationTaskMapper;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.zyapi.client.NewApiClient;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiStatusResponse;
import vip.xiaonuo.canvas.zyapi.constant.TaskConstants;
import vip.xiaonuo.canvas.zyapi.service.ZyApiGenerationTaskService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiModelService;
import vip.xiaonuo.canvas.zyapi.task.polling.TaskResultHandler;
import vip.xiaonuo.canvas.zyapi.task.TaskCompletionNotifier;
import vip.xiaonuo.common.exception.CommonException;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成任务 Service 实现类
 *
 * @author xuyuxiang
 * @date 2026/9/5 23:00
 **/
@Service
public class ZyApiGenerationTaskServiceImpl implements ZyApiGenerationTaskService {

    private static final Logger log = LoggerFactory.getLogger(ZyApiGenerationTaskServiceImpl.class);

    @Resource
    private ZyGenerationTaskMapper zyGenerationTaskMapper;

    @Resource
    private NewApiClient newApiClient;

    @Resource
    private ZyApiModelService zyModelService;

    @Resource
    private TaskResultHandler taskResultHandler;

    @Resource
    private TaskCompletionNotifier taskCompletionNotifier;

    @Override
    public List<Map<String, Object>> listTasks(String userId, String projectId, Boolean activeOnly, Integer limit) {
        // 构造查询条件
        QueryWrapper<ZyGenerationTask> wrapper = new QueryWrapper<ZyGenerationTask>()
                .eq("user_id", userId)
                .orderByDesc("create_time");

        if (projectId != null && !projectId.isEmpty()) {
            wrapper.eq("project_id", projectId);
        }

        if (activeOnly != null && activeOnly) {
            // 活跃状态含 processing：视频任务提交上游后即 processing，漏掉会导致"进行中"列表看不到生成中的视频任务
            wrapper.in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING, TaskConstants.Status.PROCESSING);
        }

        // 限制数量
        int boundedLimit = Math.min(limit != null ? limit : 30, 100);
        wrapper.last("LIMIT " + boundedLimit);

        // 查询任务列表
        List<ZyGenerationTask> tasks = zyGenerationTaskMapper.selectList(wrapper);

        // 转换任务列表
        List<Map<String, Object>> taskList = new ArrayList<>();
        for (ZyGenerationTask task : tasks) {
            taskList.add(toTask(task));
        }

        return taskList;
    }

    @Override
    public Map<String, Object> createTask(String userId, Map<String, Object> input) {
        // 创建任务实体
        ZyGenerationTask task = new ZyGenerationTask();
        task.setId(IdUtil.fastSimpleUUID());
        task.setUserId(userId);
        task.setProjectId((String) input.get("projectId"));
        task.setType((String) input.get("type"));
        task.setStatus(TaskConstants.Status.QUEUED);
        task.setProgress(0);
        task.setPrompt((String) input.get("prompt"));
        task.setOperation((String) input.get("operation"));
        task.setProvider((String) input.get("provider"));
        task.setModel((String) input.get("model"));
        task.setAttempts(1);

        // 客户端上下文
        if (input.get("clientContext") != null) {
            task.setClientContextJson(JSONUtil.toJsonStr(input.get("clientContext")));
        }

        // 输入 JSON
        if (input.get("input") != null) {
            task.setInputJson(JSONUtil.toJsonStr(input.get("input")));
        }

        // 保存任务
        zyGenerationTaskMapper.insert(task);

        // 返回任务对象（直接返回，不包装）
        return toTask(task);
    }

    @Override
    public Map<String, Object> getTask(String userId, String taskId) {
        // 查询任务
        ZyGenerationTask task = zyGenerationTaskMapper.selectOne(
                new QueryWrapper<ZyGenerationTask>().eq("id", taskId).eq("user_id", userId)
        );
        if (task == null) {
            throw new CommonException("任务不存在");
        }

        // 直接返回任务对象
        return toTask(task);
    }

    @Override
    public Map<String, Object> retryTask(String userId, String taskId) {
        // 查询任务
        ZyGenerationTask task = zyGenerationTaskMapper.selectOne(
                new QueryWrapper<ZyGenerationTask>().eq("id", taskId).eq("user_id", userId)
        );
        if (task == null) {
            throw new CommonException("任务不存在");
        }

        // 只有失败的任务可以重试
        if (!TaskConstants.Status.FAILED.equals(task.getStatus()) && !TaskConstants.Status.CANCELLED.equals(task.getStatus())) {
            throw new CommonException("只有失败或已取消的任务可以重试");
        }

        // 重置任务状态（带乐观条件：仅失败/已取消可重试，避免双端并发时覆盖其他终态）
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", taskId)
                .in("status", TaskConstants.Status.FAILED, TaskConstants.Status.CANCELLED)
                .set("status", TaskConstants.Status.QUEUED)
                .set("progress", 0)
                // 置空旧结果/取消残留（setSql 强制赋 NULL，避免旧产物误导前端）
                .setSql("stage = NULL, error = NULL, error_code = NULL, official_status = NULL, result_state = NULL, "
                        + "result_json = NULL, preview_url = NULL, preview_kind = NULL, preview_poster_url = NULL, "
                        + "text_draft = NULL, provider_request_id = NULL, provider_cancel_status = NULL, "
                        + "provider_cancel_error = NULL, provider_cancel_attempts = NULL, "
                        + "provider_cancel_requested_at = NULL, provider_cancelled_at = NULL, "
                        + "attempts = attempts + 1");

        int updated = zyGenerationTaskMapper.update(null, updateWrapper);
        if (updated == 0) {
            throw new CommonException("任务状态已变化，无法重试");
        }

        // 直接返回任务对象
        return toTask(zyGenerationTaskMapper.selectById(taskId));
    }

    @Override
    public Map<String, Object> cancelTask(String userId, String taskId) {
        // 查询任务
        ZyGenerationTask task = zyGenerationTaskMapper.selectOne(
                new QueryWrapper<ZyGenerationTask>().eq("id", taskId).eq("user_id", userId)
        );
        if (task == null) {
            throw new CommonException("任务不存在");
        }

        // 只有排队中、运行中或处理中（已提交上游等待生成）的任务可以取消
        if (!TaskConstants.Status.QUEUED.equals(task.getStatus())
                && !TaskConstants.Status.RUNNING.equals(task.getStatus())
                && !TaskConstants.Status.PROCESSING.equals(task.getStatus())) {
            throw new CommonException("只有排队中、运行中或处理中的任务可以取消");
        }

        Date now = new Date();
        // 通知上游渠道取消（仅当任务已提交上游、有 providerRequestId 时）
        String providerCancelStatus = null;
        String providerCancelError = null;
        Date providerCancelledAt = null;
        if (StrUtil.isNotEmpty(task.getProviderRequestId())) {
            try {
                ZyModel model = zyModelService.getByModelKey(task.getModel());
                if (model == null) {
                    providerCancelError = "模型不存在: " + task.getModel();
                } else {
                    newApiClient.cancelTask(model, task.getProviderRequestId());
                    providerCancelStatus = "cancelled";
                    providerCancelledAt = now;
                    log.info("已通知上游取消任务: taskId={}, providerTaskId={}", taskId, task.getProviderRequestId());
                }
            } catch (Exception e) {
                providerCancelError = e.getMessage();
                log.warn("通知上游取消任务失败: taskId={}, error={}", taskId, e.getMessage());
            }
        }

        // 更新任务状态（带乐观条件：仅活跃状态可取消，避免校验后与轮询/重试并发时覆盖终态）
        UpdateWrapper<ZyGenerationTask> cancelWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", taskId)
                .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING, TaskConstants.Status.PROCESSING)
                .set("status", TaskConstants.Status.CANCELLED)
                .set("provider_cancel_status", providerCancelStatus != null ? providerCancelStatus : "requested")
                .set("provider_cancel_error", providerCancelError)
                .setSql("provider_cancel_attempts = COALESCE(provider_cancel_attempts, 0) + 1")
                .set("provider_cancel_requested_at", now)
                .set("provider_cancelled_at", providerCancelledAt);

        int updated = zyGenerationTaskMapper.update(null, cancelWrapper);
        if (updated == 0) {
            throw new CommonException("任务状态已变化，无法取消");
        }

        // 直接返回任务对象
        return toTask(zyGenerationTaskMapper.selectById(taskId));
    }

    @Override
    public Map<String, Object> queryProviderTask(String userId, String taskId) {
        // 查询任务
        ZyGenerationTask task = zyGenerationTaskMapper.selectOne(
                new QueryWrapper<ZyGenerationTask>().eq("id", taskId).eq("user_id", userId)
        );
        if (task == null) {
            throw new CommonException("任务不存在");
        }

        // 只有失败的任务可以查询上游（与前端按钮条件一致）
        if (!TaskConstants.Status.FAILED.equals(task.getStatus())) {
            throw new CommonException("只有失败的任务可以查询上游状态");
        }

        String providerTaskId = task.getProviderRequestId();
        if (StrUtil.isEmpty(providerTaskId)) {
            throw new CommonException("任务没有上游渠道标识，无法查询");
        }

        // 查询模型配置
        ZyModel model = zyModelService.getByModelKey(task.getModel());
        if (model == null) {
            throw new CommonException("模型不存在: " + task.getModel());
        }

        // 查询上游渠道状态
        NewApiStatusResponse status = newApiClient.getStatus(model, providerTaskId);

        // 上游已成功：恢复任务并处理结果
        if (status.isSuccess()) {
            String resultUrl = status.getResultUrlValue();
            // 同步下载+落库（恢复场景低频，保持接口同步返回契约）；若后续需要彻底异步化，
            // 可改为提交 resultExecutor 异步处理并让前端轮询任务状态
            Map<String, Object> result = taskResultHandler.handleResult(taskId, resultUrl, model);
            UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                    .eq("id", taskId)
                    .eq("status", TaskConstants.Status.FAILED)
                    .set("status", TaskConstants.Status.SUCCEEDED)
                    .set("progress", 100)
                    .set("completed_at", new Date())
                    .set("result_json", JSONUtil.toJsonStr(result));
            int updated = zyGenerationTaskMapper.update(null, updateWrapper);
            if (updated > 0) {
                // 与轮询成功路径保持一致：分镜产物回填 + 成功通知
                ZyGenerationTask current = zyGenerationTaskMapper.selectById(taskId);
                taskResultHandler.backfillShotArtifact(current != null ? current : task, result);
                taskCompletionNotifier.notifySuccess(current != null ? current : task);
            } else {
                log.info("任务 {} 恢复成功但状态已变化（可能已被重试/取消），跳过回填与通知", taskId);
            }

            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("task", toTask(zyGenerationTaskMapper.selectById(taskId)));
            resultMap.put("providerStatus", status.getStatus() != null ? status.getStatus() : "");
            resultMap.put("recovered", true);
            resultMap.put("billingSettled", false);
            return resultMap;
        }

        // 上游未成功：返回未恢复状态
        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("task", toTask(task));
        resultMap.put("providerStatus", status.getStatus() != null ? status.getStatus() : "");
        resultMap.put("recovered", false);
        resultMap.put("billingSettled", false);
        return resultMap;
    }

    @Override
    public void updateTaskStatus(String taskId, String status, Integer progress, String error) {
        // 查询任务
        ZyGenerationTask task = zyGenerationTaskMapper.selectOne(
                new QueryWrapper<ZyGenerationTask>().eq("id", taskId)
        );
        if (task == null) {
            return;
        }

        // 更新状态
        task.setStatus(status);
        if (progress != null) {
            task.setProgress(progress);
        }
        if (error != null) {
            task.setError(error);
        }

        // 根据状态设置时间
        if (TaskConstants.Status.RUNNING.equals(status) && task.getStartedAt() == null) {
            task.setStartedAt(new Date());
        }
        if (TaskConstants.Status.SUCCEEDED.equals(status)
                || TaskConstants.Status.FAILED.equals(status)
                || TaskConstants.Status.CANCELLED.equals(status)) {
            task.setCompletedAt(new Date());
        }

        // 更新任务
        zyGenerationTaskMapper.updateById(task);
    }

    /**
     * 任务实体转对象
     *
     * @param task 任务实体
     * @return 任务对象
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     */
    private Map<String, Object> toTask(ZyGenerationTask task) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", task.getId());
        result.put("projectId", task.getProjectId());
        result.put("type", task.getType() != null ? task.getType() : TaskConstants.Type.IMAGE);
        result.put("status", task.getStatus() != null ? task.getStatus() : TaskConstants.Status.QUEUED);
        result.put("progress", task.getProgress() != null ? task.getProgress() : 0);
        result.put("stage", task.getStage());
        result.put("prompt", task.getPrompt());
        result.put("operation", task.getOperation());
        result.put("provider", task.getProvider());
        result.put("model", task.getModel());
        result.put("providerRequestId", task.getProviderRequestId());
        result.put("providerCancelStatus", task.getProviderCancelStatus());
        result.put("providerCancelError", task.getProviderCancelError());
        result.put("providerCancelAttempts", task.getProviderCancelAttempts());
        result.put("providerCancelRequestedAt", task.getProviderCancelRequestedAt());
        result.put("providerCancelledAt", task.getProviderCancelledAt());
        result.put("errorCode", task.getErrorCode());
        result.put("officialStatus", task.getOfficialStatus());
        result.put("previewUrl", task.getPreviewUrl());
        result.put("previewKind", task.getPreviewKind());
        result.put("previewPosterUrl", task.getPreviewPosterUrl());
        result.put("resultState", task.getResultState());
        result.put("textDraft", task.getTextDraft());
        result.put("error", task.getError());
        result.put("attempts", task.getAttempts());
        result.put("createdAt", task.getCreateTime() != null ? DateUtil.formatDateTime(task.getCreateTime()) : null);
        result.put("updatedAt", task.getUpdateTime() != null ? DateUtil.formatDateTime(task.getUpdateTime()) : task.getCreateTime() != null ? DateUtil.formatDateTime(task.getCreateTime()) : null);
        result.put("startedAt", task.getStartedAt() != null ? DateUtil.formatDateTime(task.getStartedAt()) : null);
        result.put("completedAt", task.getCompletedAt() != null ? DateUtil.formatDateTime(task.getCompletedAt()) : null);
        
        // 添加resultJson（前端解析结果需要）
        result.put("resultJson", task.getResultJson());

        // 添加客户端上下文
        if (task.getClientContextJson() != null && !task.getClientContextJson().isEmpty()) {
            try {
                result.put("clientContext", JSONUtil.parseObj(task.getClientContextJson()));
            } catch (Exception ignored) {
            }
        }

        return result;
    }
}