package vip.xiaonuo.canvas.zyapi.task.processor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.modular.generationtask.mapper.ZyGenerationTaskMapper;
import vip.xiaonuo.canvas.zyapi.constant.TaskConstants;
import vip.xiaonuo.canvas.zyapi.service.ZyApiModelService;
import vip.xiaonuo.canvas.zyapi.task.TaskCompletionNotifier;
import vip.xiaonuo.canvas.zyapi.task.TaskProcessor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 文本生成任务处理器
 * <p>
 * 仅处理普通文本生成（章节提取、角色拆解等）。
 * 画布网站 Agent 走同步接口 /api/agent/chat，不经过任务队列。
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Component
public class TextGenerationTaskProcessor implements TaskProcessor {

    private static final Logger log = LoggerFactory.getLogger(TextGenerationTaskProcessor.class);

    @Resource
    private ZyApiModelService zyModelService;

    @Resource
    private ZyGenerationTaskMapper zyGenerationTaskMapper;

    @Resource
    private TaskCompletionNotifier taskCompletionNotifier;

    @Override
    public void process(ZyGenerationTask task) throws Exception {
        log.info("开始处理文本任务: id={}", task.getId());

        String inputJson = task.getInputJson();
        if (StrUtil.isEmpty(inputJson)) {
            throw new RuntimeException("任务输入为空");
        }

        JSONObject input = JSONUtil.parseObj(inputJson);
        String modelKey = task.getModel();
        String prompt = task.getPrompt();

        if (StrUtil.isEmpty(modelKey)) {
            throw new RuntimeException("模型标识为空");
        }

        if (StrUtil.isEmpty(prompt)) {
            throw new RuntimeException("任务提示词为空");
        }

        // 获取模型配置（优先数据库，回退前端 config）
        String baseUrl = null;
        String apiKey = null;
        try {
            ZyModel model = zyModelService.getByModelKey(modelKey);
            if (model != null) {
                baseUrl = model.getBaseUrl();
                apiKey = model.getApiKey();
            }
        } catch (Exception e) {
            log.warn("从数据库查询模型配置失败: {}", e.getMessage());
        }
        if (StrUtil.isEmpty(baseUrl) || StrUtil.isEmpty(apiKey)) {
            JSONObject config = input.getJSONObject("config");
            if (baseUrl == null) baseUrl = config != null ? config.getStr("baseUrl") : null;
            if (apiKey == null) apiKey = config != null ? config.getStr("apiKey") : null;
        }
        if (StrUtil.isEmpty(baseUrl) || StrUtil.isEmpty(apiKey)) {
            throw new RuntimeException("模型配置不完整（缺少 baseUrl 或 apiKey）");
        }

        updateTaskStage(task.getId(), "调用生成模型", 30);

        try {
            String generatedText = callLlmApi(baseUrl, apiKey, modelKey, input, buildEffectivePrompt(input, prompt));

            Map<String, Object> result = new HashMap<>();
            result.put("mode", "text");
            result.put("text", generatedText);

            UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                    .eq("id", task.getId())
                    .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING)
                    .set("status", TaskConstants.Status.SUCCEEDED)
                    .set("progress", 100)
                    .set("stage", "完成")
                    .set("completed_at", new Date())
                    .set("text_draft", generatedText)
                    .set("result_json", JSONUtil.toJsonStr(result));
            int updated = zyGenerationTaskMapper.update(null, updateWrapper);
            if (updated == 0) {
                log.info("文本任务 {} 状态已变化（可能已取消），跳过成功通知", task.getId());
                return;
            }

            log.info("文本任务完成: id={}, textLength={}", task.getId(), generatedText.length());
        } catch (Exception e) {
            log.error("文本任务处理失败: id={}, error={}", task.getId(), e.getMessage(), e);
            UpdateWrapper<ZyGenerationTask> failWrapper = new UpdateWrapper<ZyGenerationTask>()
                    .eq("id", task.getId())
                    .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING)
                    .set("status", TaskConstants.Status.FAILED)
                    .set("progress", 100)
                    .set("stage", "失败")
                    .set("error", e.getMessage() != null ? e.getMessage() : "文本生成失败")
                    .set("completed_at", new Date());
            int failUpdated = zyGenerationTaskMapper.update(null, failWrapper);
            if (failUpdated == 0) {
                log.info("文本任务 {} 状态已变化（可能已取消），跳过失败通知", task.getId());
                return;
            }
            taskCompletionNotifier.notifyFailed(task, e.getMessage());
        }
    }

    @Override
    public boolean supports(String taskType) {
        return "text".equals(taskType) || "canvas_text".equals(taskType);
    }

    @Override
    public String getTaskType() {
        return "text";
    }

    private String buildEffectivePrompt(JSONObject input, String prompt) {
        JSONObject metadata = input.getJSONObject("metadata");
        if (metadata == null) return prompt;
        String operation = metadata.getStr("promptTemplateOperation");
        JSONObject variables = metadata.getJSONObject("promptTemplateVariables");
        if (variables == null || variables.isEmpty()) return prompt;
        if ("character_extract".equals(operation)) {
            StringBuilder sb = new StringBuilder();
            sb.append("你是短剧角色拆解专家。请仔细阅读章节正文，提取出现的所有角色，返回 JSON 对象，格式：")
              .append("{\"characters\":[{\"name\":\"角色名\",\"role\":\"剧情定位\",\"appearance\":\"外貌特征\",\"clothing\":\"服装造型\",\"physique\":\"体型特征\",\"personality\":\"性格特点\",\"consistencyPrompt\":\"角色一致性设定\",\"multiViewPrompt\":\"多视角描述\",\"voiceLanguage\":\"配音语言（如：普通话）\",\"voiceAge\":\"声音年龄段（如：青年）\",\"voiceTimbre\":\"音色描述（如：清亮磁性男声）\"}]}。")
              .append("没有明确名字的角色用其在文中出现的称谓命名（如\"跑步者\"、\"女孩\"）。每个字段都必须填写具体内容不得为空；voiceLanguage、voiceAge、voiceTimbre 是角色配音必需信息；consistencyPrompt 要描述可复用的外观与风格设定。只返回 JSON，不要返回其他内容。");
            appendPromptVariable(sb, "项目名称", variables);
            appendPromptVariable(sb, "章节名称", variables);
            appendPromptVariable(sb, "项目画风", variables);
            appendPromptVariable(sb, "章节正文", variables);
            return sb.toString();
        }
        return prompt;
    }

    private void appendPromptVariable(StringBuilder sb, String key, JSONObject variables) {
        String value = variables.getStr(key);
        if (StrUtil.isNotEmpty(value)) {
            sb.append('\n').append(key).append("：").append(value);
        }
    }

    private String callLlmApi(String baseUrl, String apiKey, String model, JSONObject input, String prompt) {
        String url = baseUrl;
        if (!url.endsWith("/")) url += "/";
        if (url.endsWith("/v1/")) {
            url += "chat/completions";
        } else {
            url += "v1/chat/completions";
        }

        String systemPrompt = null;
        JSONObject config = input.getJSONObject("config");
        if (config != null) {
            systemPrompt = config.getStr("systemPrompt");
        }

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model);
        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content",
                StrUtil.blankToDefault(systemPrompt, "You are a helpful assistant.")));
        messages.add(new JSONObject().set("role", "user").set("content", prompt));
        requestBody.set("messages", messages);
        requestBody.set("temperature", 0.7);
        requestBody.set("max_tokens", 8000);

        log.info("调用 LLM API: url={}, model={}", url, model);

        try (HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .body(requestBody.toString())
                .timeout(300000)
                .execute()) {

            if (!response.isOk()) {
                String body = response.body();
                log.error("LLM API 调用失败: status={}, body={}", response.getStatus(), body);
                throw new RuntimeException("LLM API 调用失败: " + response.getStatus());
            }

            String responseBody = response.body();
            JSONObject responseJson = JSONUtil.parseObj(responseBody);
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("LLM API 返回为空");
            }
            String content = choices.getJSONObject(0).getJSONObject("message").getStr("content");
            if (StrUtil.isEmpty(content)) {
                throw new RuntimeException("LLM API 返回内容为空");
            }
            return content.trim();
        }
    }

    private void updateTaskStage(String taskId, String stage, int progress) {
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", taskId)
                .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING)
                .set("stage", stage)
                .set("progress", progress);
        zyGenerationTaskMapper.update(null, updateWrapper);
    }
}
