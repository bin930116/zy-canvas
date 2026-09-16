package vip.xiaonuo.canvas.zyapi.client.adapter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.zyapi.client.request.NewApiImageRequest;
import vip.xiaonuo.canvas.zyapi.client.request.NewApiVideoRequest;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiStatusResponse;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiTaskResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容 / NewAPI 渠道适配器
 * <p>
 * 处理：OpenAI 兼容协议（/v1/images/generations、/v1/video/generations）、
 * newapi / newapi-channel-1 / newapi-channel-2 私有协议。
 * 除 ComfyUI 系列外全部由本适配器兜底处理。
 * <p>
 * 公共能力（HTTP 执行、响应解析、错误提取、参考图定位、时长解析）继承自
 * {@link AbstractMediaAdapter}，本类只保留协议差异：URL 路径、请求体结构。
 *
 * @author hanbin
 * @date 2026/09/15
 **/
@Component
public class NewApiMediaAdapter extends AbstractMediaAdapter {

    private static final Logger log = LoggerFactory.getLogger(NewApiMediaAdapter.class);

    @Override
    public boolean supports(ZyModel model) {
        // ComfyUI 系列模型（minimax / comfyui / comfy）由 ComfyUIMediaAdapter 专属处理，避免重复路由
        if (model != null && model.getModelKey() != null) {
            String modelKey = model.getModelKey().toLowerCase();
            if (modelKey.contains("minimax") || modelKey.contains("comfyui") || modelKey.contains("comfy")) {
                return false;
            }
        }
        // 其余模型统一由本适配器兜底
        return true;
    }

    @Override
    public NewApiTaskResponse createVideoTask(ZyModel model, NewApiVideoRequest request) {
        String protocol = model.getProtocol();
        String url = buildCreateUrl(model.getBaseUrl(), protocol, "video");
        Map<String, Object> body = buildVideoRequestBody(request, protocol);

        log.info("创建视频任务: url={}, protocol={}, model={}", url, protocol, request.getModel());

        HttpResponse response = executePost(url, model.getApiKey(), body, submitTimeoutMs(model));
        return parseTaskResponse(response);
    }

    @Override
    public NewApiTaskResponse createImageTask(ZyModel model, NewApiImageRequest request) {
        String protocol = model.getProtocol();
        String url = buildCreateUrl(model.getBaseUrl(), protocol, "image");
        Map<String, Object> body = buildImageRequestBody(request, protocol);

        log.info("创建图片任务: url={}, protocol={}, model={}", url, protocol, request.getModel());

        HttpResponse response = executePost(url, model.getApiKey(), body, submitTimeoutMs(model));
        return parseTaskResponse(response);
    }

    @Override
    public NewApiStatusResponse getStatus(ZyModel model, String taskId) {
        String protocol = model.getProtocol();
        String url = buildStatusUrl(model.getBaseUrl(), protocol, taskId);

        log.debug("查询任务状态: url={}, taskId={}", url, taskId);

        HttpResponse response = executeGet(url, model.getApiKey(), queryTimeoutMs(model));
        return parseStatusResponse(response, model.getBaseUrl(), taskId);
    }

    @Override
    public void cancelTask(ZyModel model, String taskId) {
        String protocol = model.getProtocol();
        String url = buildCancelUrl(model.getBaseUrl(), protocol, taskId);

        log.info("取消任务: url={}, taskId={}", url, taskId);

        try {
            try (HttpResponse response = executeDelete(url, model.getApiKey(), queryTimeoutMs(model))) {
                // 取消接口无响应体需要消费，仅需关闭连接
            }
        } catch (Exception e) {
            log.warn("取消任务失败: {}", e.getMessage());
        }
    }

    // ============ 私有方法：URL 构建 ============

    private String buildCreateUrl(String baseUrl, String protocol, String type) {
        String path;
        if ("video".equals(type)) {
            // NewAPI 视频生成统一走 /v1/video/generations
            path = "/v1/video/generations";
        } else {
            path = "newapi-channel-2".equals(protocol) ? "/v1/video/generations" : "/v1/images/generations";
        }
        return buildUrl(baseUrl, path);
    }

    private String buildStatusUrl(String baseUrl, String protocol, String taskId) {
        // NewAPI 官方：GET /v1/video/generations/{task_id}
        // newapi-channel-2 兼容旧路径 /v1/video/generations/{id}；其余仍回退 /v1/videos/{id}
        String path = ("newapi-channel-2".equals(protocol) || "newapi".equals(protocol) || StrUtil.isEmpty(protocol))
                ? "/v1/video/generations/" + taskId
                : "/v1/videos/" + taskId;
        return buildUrl(baseUrl, path);
    }

    private String buildCancelUrl(String baseUrl, String protocol, String taskId) {
        return buildStatusUrl(baseUrl, protocol, taskId);
    }

    // ============ 私有方法：视频请求体 ============

    private Map<String, Object> buildVideoRequestBody(NewApiVideoRequest request, String protocol) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("prompt", request.getPrompt());

        log.info("构建视频请求体: protocol={}, hasInput={}, hasMedia={}", protocol, request.getInput() != null, request.getInput() != null && request.getInput().getMedia() != null);

        if ("newapi-channel-2".equals(protocol)) {
            if (request.getSeconds() != null) {
                body.put("seconds", request.getSeconds());
            }
            if (request.getAspectRatio() != null) {
                body.put("aspect_ratio", request.getAspectRatio());
            }
            if (request.getResolution() != null) {
                body.put("resolution", request.getResolution());
            }
            if (request.getGenerateAudio() != null) {
                body.put("generate_audio", request.getGenerateAudio());
            }
            if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                body.put("image_urls", request.getImageUrls());
            }
            if (request.getVideoUrls() != null && !request.getVideoUrls().isEmpty()) {
                body.put("video_urls", request.getVideoUrls());
            }
            if (request.getAudioUrls() != null && !request.getAudioUrls().isEmpty()) {
                body.put("audio_urls", request.getAudioUrls());
            }
        } else if ("newapi-channel-1".equals(protocol)) {
            // NewAPI 视频接口标准格式：image 单字符串 + duration + width/height
            if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                body.put("image", request.getImageUrls().get(0));
            }
            Integer duration = parseDurationSeconds(request);
            if (duration != null) {
                body.put("duration", duration);
            }
            Map<String, Integer> size = parseWidthHeight(request.getAspectRatio());
            if (size != null) {
                body.put("width", size.get("width"));
                body.put("height", size.get("height"));
            }
        } else {
            // NewAPI 官方统一协议 POST /v1/video/generations
            // https://docs.newapi.pro/zh/docs/api/ai-model/videos/createvideogeneration
            Integer duration = parseDurationSeconds(request);
            if (duration != null) {
                body.put("duration", duration);
            }
            String imageUrl = firstImageUrl(request);
            if (StrUtil.isNotEmpty(imageUrl)) {
                body.put("image", imageUrl);
            }
            Map<String, Integer> size = parseWidthHeight(request.getAspectRatio());
            if (size != null) {
                body.put("width", size.get("width"));
                body.put("height", size.get("height"));
            }
            if (request.getGenerateAudio() != null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("generate_audio", request.getGenerateAudio());
                body.put("metadata", metadata);
            }
        }

        return body;
    }

    // ============ 私有方法：图片请求体 ============

    private Map<String, Object> buildImageRequestBody(NewApiImageRequest request, String protocol) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("prompt", request.getPrompt());

        if (request.getSize() != null) {
            body.put("size", request.getSize());
        }
        if (request.getQuality() != null) {
            body.put("quality", request.getQuality());
        }
        if (request.getN() != null) {
            body.put("n", request.getN());
        }
        if (request.getImage() != null) {
            body.put("image", request.getImage());
        }
        if (request.getNegativePrompt() != null) {
            body.put("negative_prompt", request.getNegativePrompt());
        }
        if (request.getSeed() != null) {
            body.put("seed", request.getSeed());
        }
        if (request.getGuidanceScale() != null) {
            body.put("guidance_scale", request.getGuidanceScale());
        }
        if (request.getNumInferenceSteps() != null) {
            body.put("num_inference_steps", request.getNumInferenceSteps());
        }

        return body;
    }

    // ============ 私有方法：参数工具 ============

    /**
     * 按宽高比换算 width/height（NewAPI 部分协议用宽高而非 ratio 字符串）
     */
    private Map<String, Integer> parseWidthHeight(String aspectRatio) {
        if (StrUtil.isEmpty(aspectRatio)) {
            return null;
        }
        switch (aspectRatio.trim()) {
            case "16:9":
                return Map.of("width", 1280, "height", 720);
            case "9:16":
                return Map.of("width", 720, "height", 1280);
            case "1:1":
                return Map.of("width", 720, "height", 720);
            case "4:3":
                return Map.of("width", 960, "height", 720);
            case "3:4":
                return Map.of("width", 720, "height", 960);
            default:
                String[] parts = aspectRatio.split(":");
                if (parts.length == 2) {
                    try {
                        int ratioW = Integer.parseInt(parts[0].trim());
                        int ratioH = Integer.parseInt(parts[1].trim());
                        if (ratioW <= 0 || ratioH <= 0) {
                            return null;
                        }
                        if (ratioW >= ratioH) {
                            return Map.of("width", 1280, "height", 1280 * ratioH / ratioW);
                        }
                        return Map.of("height", 1280, "width", 1280 * ratioW / ratioH);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                }
                return null;
        }
    }
}
