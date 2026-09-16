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
import vip.xiaonuo.canvas.modular.generationtask.mapper.ZyGenerationTaskMapper;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.zyapi.service.ZyApiModelService;
import vip.xiaonuo.canvas.zyapi.task.TaskProcessor;

import java.util.*;

/**
 * 分镜脚本任务处理器
 * 接收章节文本，调用 LLM 生成分镜脚本
 */
@Component
public class StoryboardRowsTaskProcessor implements TaskProcessor {

    private static final Logger log = LoggerFactory.getLogger(StoryboardRowsTaskProcessor.class);

    @Resource
    private ZyGenerationTaskMapper zyGenerationTaskMapper;

    @Resource
    private ZyApiModelService zyModelService;

    @Override
    public String getTaskType() {
        return "agent_storyboard_rows";
    }

    @Override
    public boolean supports(String taskType) {
        return "agent_storyboard_rows".equals(taskType);
    }

    @Override
    public void process(ZyGenerationTask task) throws Exception {
        log.info("开始处理分镜脚本任务: id={}", task.getId());

        // 1. 解析输入
        String inputJson = task.getInputJson();
        if (StrUtil.isEmpty(inputJson)) {
            throw new RuntimeException("任务输入为空");
        }

        JSONObject input = JSONUtil.parseObj(inputJson);
        String prompt = task.getPrompt();
        String modelKey = task.getModel();

        if (StrUtil.isEmpty(modelKey)) {
            throw new RuntimeException("模型标识为空");
        }

        // 2. 获取模型配置（进程内缓存读取，优先级高于前端传入的 config）
        String baseUrl = null;
        String apiKey = null;

        // 先尝试从数据库获取模型配置
        ZyModel modelConfig = zyModelService.getByModelKey(modelKey);
        if (modelConfig != null) {
            baseUrl = modelConfig.getBaseUrl();
            apiKey = modelConfig.getApiKey();
            log.info("从数据库获取模型配置: model={}, baseUrl={}", modelKey, baseUrl);
        }

        // 如果数据库没有，从前端 config 获取
        if (StrUtil.isEmpty(baseUrl) || StrUtil.isEmpty(apiKey)) {
            JSONObject config = input.getJSONObject("config");
            if (baseUrl == null) baseUrl = config != null ? config.getStr("baseUrl") : null;
            if (apiKey == null) apiKey = config != null ? config.getStr("apiKey") : null;
            log.info("从前端config获取模型配置: model={}, baseUrl={}", modelKey, baseUrl);
        }

        if (StrUtil.isEmpty(baseUrl) || StrUtil.isEmpty(apiKey)) {
            throw new RuntimeException("模型配置不完整（缺少 baseUrl 或 apiKey）");
        }

        // 3. 更新任务阶段
        updateTaskStage(task.getId(), "构建分镜提示词", 20);

        // 4. 构建提示词
        String systemPrompt = buildSystemPrompt();
        String fullPrompt = systemPrompt + "\n\n" + (prompt != null ? prompt : "");

        // 5. 更新任务阶段
        updateTaskStage(task.getId(), "调用生成模型", 40);

        // 6. 调用 LLM API
        String responseText = callLlmApi(baseUrl, apiKey, modelKey, fullPrompt);

        // 7. 更新任务阶段
        updateTaskStage(task.getId(), "解析分镜结果", 70);

        // 8. 解析响应 JSON
        JSONObject result = parseStoryboardResponse(responseText);

        // 9. 更新任务阶段
        updateTaskStage(task.getId(), "完成", 90);

        // 10. 更新任务成功
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", task.getId())
                .set("status", "succeeded")
                .set("progress", 100)
                .set("result_json", result.toString())
                .set("completed_at", new Date());
        zyGenerationTaskMapper.update(null, updateWrapper);

        log.info("分镜脚本任务完成: id={}", task.getId());
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return "你是影视分镜导演和 AI 视频提示词专家。先理解故事目标、人物动机、冲突、情绪曲线和结尾，再把剧情转译为可执行、可拍摄的连续镜头，不要把剧情段落直接改写成镜头摘要。\n\n" +
                "每个镜头先确定观众此刻跟随谁，再明确 narrativeIntent、viewerPOV 和 performanceBlocking，然后选择景别、机位、焦段、构图和运镜。narrativeIntent 用一句话说明本镜头在叙事上的作用；performanceBlocking 用可执行的动作动词描述人物走位、手势与节奏（如“从画面左侧入画，绕过长椅，在路灯下停住”），不得用抽象形容词代替；lightingAndAtmosphere 说明光线来源与场景氛围；audioEffects 说明环境声、音效或留白。摄影机设计必须说明叙事动机，不得用术语数量代替导演判断；一个镜头只保留一个主运镜，必须有起点、动机和停止点，并为人物表演让出注意力。\n\n" +
                "大远景只承担空间、规模和处境建立；人物对白与喜剧反应优先使用中景、近景、过肩或独立反应镜头。按叙事需要使用反应、停顿、空镜、插入和匹配剪辑，避免每镜都推进、环绕、航拍或慢动作。\n\n" +
                "description 只写可见画面与动作；把意识到、回忆起、感到等不可见信息转译成眼神、停顿、手部动作、走位、道具反应或环境变化。visualPrompt 明确人物左右位置、视线、前中后景、遮挡、视觉焦点、可信光源、材质和色彩；videoPrompt 只补充主体运动、环境运动和结尾状态。\n\n" +
                "保持角色五官、年龄、发型、服装、道具、损伤位置、空间关系和项目媒介一致。视觉媒介、光线和材质严格服从项目画风，不得默认改写为真人摄影，也不得把 3D、动画或卡通项目强制改成写实。负面要求必须针对当前镜头的换脸、服装变化、手部错误、乱码、闪烁、穿模、风格突变和动作僵硬风险。\n\n" +
                "你必须返回一个 JSON 对象，格式如下：\n" +
                "{\n" +
                "  \"title\": \"章节标题\",\n" +
                "  \"rows\": [\n" +
                "    {\n" +
                "      \"shotNumber\": 1,\n" +
                "      \"plotDescription\": \"镜头描述（可见画面与动作）\",\n" +
                "      \"dialogue\": \"台词\",\n" +
                "      \"characters\": [\"角色名\"],\n" +
                "      \"shotSize\": \"景别（如：中景、近景、特写）\",\n" +
                "      \"emotion\": \"情绪\",\n" +
                "      \"narrativeIntent\": \"叙事意图\",\n" +
                "      \"performanceBlocking\": \"表演调度（走位、动作、手势与节奏）\",\n" +
                "      \"lightingAndAtmosphere\": \"光线与氛围\",\n" +
                "      \"audioEffects\": \"声音设计（环境声与音效）\",\n" +
                "      \"camera\": \"机位与焦段\",\n" +
                "      \"motion\": \"运镜\",\n" +
                "      \"timeBeats\": \"时间节拍\",\n" +
                "      \"imageGenerationPrompt\": \"英文图片生成提示词\",\n" +
                "      \"videoMotionPrompt\": \"中文视频运动提示词\",\n" +
                "      \"mustHave\": [\"必须包含的元素\"],\n" +
                "      \"optionalDetails\": [\"可选细节\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "只返回 JSON，不要返回其他内容。";
    }

    /**
     * 调用 LLM API
     */
    private String callLlmApi(String baseUrl, String apiKey, String model, String prompt) {
        // 构建请求 URL（OpenAI 兼容接口格式：baseUrl/v1/chat/completions，baseUrl 已含 /v1 时去重）
        String url = baseUrl;
        if (!url.endsWith("/")) url += "/";
        if (url.endsWith("/v1/")) {
            url += "chat/completions";
        } else {
            url += "v1/chat/completions";
        }

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model);

        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content", "You are a helpful assistant."));
        messages.add(new JSONObject().set("role", "user").set("content", prompt));
        requestBody.set("messages", messages);

        requestBody.set("temperature", 0.7);
        requestBody.set("max_tokens", 32000);

        log.info("调用 LLM API: url={}, model={}", url, model);

        // 发送请求
        try (HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .body(requestBody.toString())
                .timeout(300000) // 5 分钟超时
                .execute()) {

            if (!response.isOk()) {
                String body = response.body();
                log.error("LLM API 调用失败: status={}, body={}", response.getStatus(), body);
                throw new RuntimeException("LLM API 调用失败: " + response.getStatus());
            }

            // 解析响应
            String responseBody = response.body();
            log.info("LLM API 响应: {}", responseBody.substring(0, Math.min(500, responseBody.length())));

            // 检查是否是 XML/HTML 错误响应
            if (responseBody.trim().startsWith("<") || responseBody.trim().startsWith("<?xml")) {
                log.error("LLM API 返回了 XML/HTML 而非 JSON: {}", responseBody.substring(0, Math.min(500, responseBody.length())));
                throw new RuntimeException("LLM API 返回了非 JSON 响应，请检查 API 地址和配置");
            }

            JSONObject responseJson = JSONUtil.parseObj(responseBody);
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("LLM API 返回为空");
            }

            return choices.getJSONObject(0).getJSONObject("message").getStr("content");
        }
    }

    /**
     * 解析分镜响应
     */
    private JSONObject parseStoryboardResponse(String responseText) {
        try {
            // 尝试直接解析 JSON
            String jsonText = responseText.trim();

            // 如果被 Markdown 代码块包裹，提取 JSON
            if (jsonText.startsWith("```json")) {
                jsonText = jsonText.substring(7);
            }
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.substring(3);
            }
            if (jsonText.endsWith("```")) {
                jsonText = jsonText.substring(0, jsonText.length() - 3);
            }
            jsonText = jsonText.trim();

            JSONObject result = JSONUtil.parseObj(jsonText);

            // 验证 rows 存在
            JSONArray rows = result.getJSONArray("rows");
            if (rows == null || rows.isEmpty()) {
                throw new RuntimeException("分镜结果没有 rows");
            }

            // 设置默认 title
            if (!result.containsKey("title") || StrUtil.isEmpty(result.getStr("title"))) {
                result.set("title", "影视分镜");
            }

            return result;
        } catch (Exception e) {
            log.error("解析分镜响应失败: {}", e.getMessage());
            throw new RuntimeException("分镜响应格式错误: " + e.getMessage());
        }
    }

    /**
     * 更新任务阶段
     */
    private void updateTaskStage(String taskId, String stage, int progress) {
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", taskId)
                .set("stage", stage)
                .set("progress", progress);
        zyGenerationTaskMapper.update(null, updateWrapper);
    }
}
