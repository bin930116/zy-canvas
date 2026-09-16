package vip.xiaonuo.canvas.zyapi.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.zyapi.service.ZyApiModelService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.Map;

/**
 * 提示词优化接口
 *
 * @author xuyuxiang
 * @date 2026/9/15
 **/
@Tag(name = "提示词优化")
@RestController
@RequestMapping("/api/agent")
public class ZyApiPromptOptimizerController {

    private static final Logger log = LoggerFactory.getLogger(ZyApiPromptOptimizerController.class);

    @Resource
    private ZyApiModelService zyModelService;

    /**
     * 优化提示词：用户输入简单描述 → LLM 生成专业影视提示词
     */
    @Operation(summary = "优化提示词")
    @PostMapping("/optimize-prompt")
    public CommonResult<Map<String, Object>> optimizePrompt(@RequestBody Map<String, Object> body) {
        String userId = StpClientUtil.getLoginIdAsString();
        String prompt = body.get("prompt") != null ? String.valueOf(body.get("prompt")).trim() : "";
        String mode = body.get("mode") != null ? String.valueOf(body.get("mode")).trim() : "image";
        String modelKey = body.get("model") != null ? String.valueOf(body.get("model")).trim() : "";
        String styleHint = body.get("styleHint") != null ? String.valueOf(body.get("styleHint")).trim() : "";

        if (StrUtil.isEmpty(prompt)) {
            return CommonResult.error("请输入要优化的提示词");
        }

        // 获取文本模型配置
        ZyModel model = null;
        if (StrUtil.isNotEmpty(modelKey)) {
            model = zyModelService.getByModelKey(modelKey);
        }
        if (model == null) {
            // 默认取第一个可用的文本模型
            model = zyModelService.getDefaultModel("text");
        }
        if (model == null || StrUtil.isEmpty(model.getBaseUrl()) || StrUtil.isEmpty(model.getApiKey())) {
            return CommonResult.error("没有可用的文本模型，请先在模型管理中配置");
        }

        try {
            String optimized = callLlmForOptimization(model, prompt, mode, styleHint);
            return CommonResult.data(Map.of(
                    "original", prompt,
                    "optimized", optimized,
                    "mode", mode
            ));
        } catch (Exception e) {
            log.error("提示词优化失败: {}", e.getMessage(), e);
            return CommonResult.error("提示词优化失败: " + e.getMessage());
        }
    }

    /**
     * 调用 LLM 生成专业提示词
     */
    private String callLlmForOptimization(ZyModel model, String userPrompt, String mode, String styleHint) throws Exception {
        String url = model.getBaseUrl();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (!url.endsWith("/v1")) {
            url += "/v1";
        }
        url += "/chat/completions";

        String systemPrompt = buildSystemPrompt(mode, styleHint);

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model.getModelKey());
        requestBody.set("temperature", 0.7);
        requestBody.set("max_tokens", 2000);

        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
        messages.add(new JSONObject().set("role", "user").set("content", userPrompt));
        requestBody.set("messages", messages);

        log.info("提示词优化调用: url={}, model={}, mode={}", url, model.getModelKey(), mode);

        HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + model.getApiKey())
                .body(requestBody.toString())
                .timeout(120000)
                .execute();

        if (!response.isOk()) {
            throw new RuntimeException("LLM API 调用失败: " + response.getStatus() + " " + StrUtil.sub(response.body(), 0, 200));
        }

        JSONObject responseJson = JSONUtil.parseObj(response.body());
        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("LLM 返回为空");
        }

        String content = choices.getJSONObject(0).getJSONObject("message").getStr("content");
        if (StrUtil.isEmpty(content)) {
            throw new RuntimeException("LLM 返回内容为空");
        }
        return content.trim();
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String mode, String styleHint) {
        boolean isVideo = "video".equals(mode);

        StringBuilder sb = new StringBuilder();
        sb.append("你是专业的 AI 影视提示词工程师。用户会给你一段简单的画面描述，你需要将其扩展为专业、详细、可执行的");
        sb.append(isVideo ? "视频生成" : "图片生成");
        sb.append("提示词。\n\n");

        if (isVideo) {
            sb.append("## 视频提示词要求\n");
            sb.append("1. 使用中文撰写\n");
            sb.append("2. 结构包含：主体描述、运动方式、镜头运动、光线氛围、结尾状态\n");
            sb.append("3. 运动描述要具体：方向、速度、节奏\n");
            sb.append("4. 镜头运动明确：推、拉、摇、移、跟、升、降、固定\n");
            sb.append("5. 避免抽象词汇，用可见的画面语言\n");
            sb.append("6. 长度控制在 100-200 字\n\n");
            sb.append("## 输出格式\n");
            sb.append("直接输出优化后的提示词，不要添加任何解释或前缀。\n\n");
            sb.append("## 示例\n");
            sb.append("输入：一个女孩在海边\n");
            sb.append("输出：镜头缓慢向前推进。一位穿白色连衣裙的年轻女孩赤脚站在黄昏的沙滩上，海浪轻轻拍打她的脚踝。她微微低头，长发被海风吹起。远处海平面上夕阳西沉，天空被染成橘粉色。光线柔和温暖，带有明显的逆光轮廓。结尾状态：女孩抬起头望向远方，画面定格。\n");
        } else {
            sb.append("## 图片提示词要求\n");
            sb.append("1. 使用英文撰写（英文提示词对图片模型更准确）\n");
            sb.append("2. 结构包含：主体、环境、构图、光线、材质、风格\n");
            sb.append("3. 包含摄影术语：景别、镜头、光影\n");
            sb.append("4. 包含风格标签：渲染引擎、画质、艺术风格\n");
            sb.append("5. 负面提示词放在最后，用 --no 前缀\n");
            sb.append("6. 长度控制在 80-150 词\n\n");
            sb.append("## 输出格式\n");
            sb.append("直接输出优化后的英文提示词，不要添加任何解释或前缀。\n\n");
            sb.append("## 示例\n");
            sb.append("输入：一个未来科幻城市\n");
            sb.append("输出：Wide establishing shot of a futuristic megacity at dusk, towering glass skyscrapers with holographic advertisements, flying vehicles streaking across the sky, neon reflections on wet streets below, cinematic lighting with volumetric fog, PBR materials, photorealistic, Unreal Engine 5 render, 8k resolution, ultra detailed. --no cartoon, low quality, blurry\n");
        }

        if (StrUtil.isNotEmpty(styleHint)) {
            sb.append("\n## 项目画风约束\n");
            sb.append(styleHint);
            sb.append("\n优化后的提示词必须符合以上画风规范。\n");
        }

        return sb.toString();
    }
}
