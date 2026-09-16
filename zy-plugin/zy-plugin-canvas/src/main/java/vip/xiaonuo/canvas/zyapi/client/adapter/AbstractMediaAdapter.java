package vip.xiaonuo.canvas.zyapi.client.adapter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.zyapi.client.request.NewApiVideoRequest;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiStatusResponse;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiTaskResponse;
import vip.xiaonuo.canvas.zyapi.constant.TaskConstants;

import java.util.Map;

/**
 * 媒体渠道适配器基类
 * <p>
 * 收敛各渠道适配器的公共能力：URL 归一化、HTTP 执行（POST/GET/DELETE）、
 * 任务创建 / 状态响应解析、错误提取、参考图定位、时长解析与超时解析。
 * 子类只需实现协议差异：请求体结构、接口路径、产物 URL 回退策略。
 * <p>
 * 新增渠道 = 继承本基类并实现 {@link MediaModelAdapter} 的协议差异部分，注册为 Spring Bean。
 *
 * @author hanbin
 * @date 2026/09/15
 **/
public abstract class AbstractMediaAdapter implements MediaModelAdapter {

    protected static final Logger log = LoggerFactory.getLogger(AbstractMediaAdapter.class);

    /** 默认提交超时：5 分钟（base64 图片较大需要更长时间） */
    private static final int DEFAULT_SUBMIT_TIMEOUT_MS = 300000;

    /** 默认状态查询超时：30 秒（轮询场景不应长时间阻塞） */
    private static final int DEFAULT_QUERY_TIMEOUT_MS = 30000;

    // ============ URL 构建 ============

    /**
     * 归一化 baseUrl：去尾部斜杠；为空时抛异常
     */
    protected String normalizeBaseUrl(String baseUrl) {
        if (StrUtil.isEmpty(baseUrl)) {
            throw new RuntimeException("模型配置缺少 baseUrl");
        }
        return StrUtil.removeSuffix(baseUrl, "/");
    }

    /**
     * 拼接完整 URL：baseUrl 若已以 /v1 结尾则自动去重
     *
     * @param baseUrl 模型 baseUrl
     * @param path    接口路径（必须以 / 开头，如 /v1/videos）
     */
    protected String buildUrl(String baseUrl, String path) {
        String base = normalizeBaseUrl(baseUrl);
        if (base.endsWith("/v1") && path.startsWith("/v1")) {
            return base + path.substring("/v1".length());
        }
        return base + path;
    }

    // ============ HTTP 执行 ============

    /**
     * POST JSON 请求（携带 Bearer 鉴权）
     */
    protected HttpResponse executePost(String url, String apiKey, Map<String, Object> body, int timeoutMs) {
        return HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(body))
                .timeout(timeoutMs)
                .execute();
    }

    /**
     * GET 请求（携带 Bearer 鉴权）
     */
    protected HttpResponse executeGet(String url, String apiKey, int timeoutMs) {
        return HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .timeout(timeoutMs)
                .execute();
    }

    /**
     * DELETE 请求（携带 Bearer 鉴权）
     */
    protected HttpResponse executeDelete(String url, String apiKey, int timeoutMs) {
        return HttpRequest.delete(url)
                .header("Authorization", "Bearer " + apiKey)
                .timeout(timeoutMs)
                .execute();
    }

    // ============ 超时解析 ============

    /**
     * 提交请求超时：优先取模型配置 timeout_seconds，缺省 5 分钟
     */
    protected int submitTimeoutMs(ZyModel model) {
        return resolveTimeoutMs(model, DEFAULT_SUBMIT_TIMEOUT_MS);
    }

    /**
     * 状态查询超时：优先取模型配置 timeout_seconds，但封顶 30 秒
     */
    protected int queryTimeoutMs(ZyModel model) {
        return Math.min(resolveTimeoutMs(model, DEFAULT_QUERY_TIMEOUT_MS), DEFAULT_QUERY_TIMEOUT_MS);
    }

    private int resolveTimeoutMs(ZyModel model, int fallbackMs) {
        if (model != null && model.getTimeoutSeconds() != null && model.getTimeoutSeconds() > 0) {
            return model.getTimeoutSeconds() * 1000;
        }
        return fallbackMs;
    }

    // ============ 任务创建响应解析 ============

    /**
     * 解析任务创建响应：兼容 NewAPI 官方 { id / task_id, status } 与
     * output 包裹风格 { output: { task_id, task_status } }
     */
    protected NewApiTaskResponse parseTaskResponse(HttpResponse response) {
        try (HttpResponse resp = response) {
            if (!resp.isOk()) {
                NewApiTaskResponse errorResponse = new NewApiTaskResponse();
                errorResponse.setError("请求失败: " + resp.getStatus() + " " + StrUtil.sub(resp.body(), 0, 300));
                return errorResponse;
            }

            String body = resp.body();
            log.debug("任务创建响应: {}", body);

            JSONObject json = JSONUtil.parseObj(body);
            NewApiTaskResponse taskResponse = new NewApiTaskResponse();
            taskResponse.setId(json.getStr("id"));
            taskResponse.setTaskId(json.getStr("task_id"));
            taskResponse.setStatus(json.getStr("status"));

            JSONObject output = json.getJSONObject("output");
            if (output != null) {
                if (StrUtil.isEmpty(taskResponse.getTaskIdValue())) {
                    taskResponse.setTaskId(output.getStr("task_id"));
                }
                if (StrUtil.isEmpty(taskResponse.getStatus())) {
                    taskResponse.setStatus(output.getStr("task_status"));
                }
                if (StrUtil.isNotEmpty(output.getStr("message"))) {
                    taskResponse.setError(output.getStr("message"));
                }
            }
            String error = extractError(json);
            if (StrUtil.isNotEmpty(error)) {
                taskResponse.setError(error);
            }
            taskResponse.setMessage(json.getStr("message"));
            return taskResponse;
        }
    }

    // ============ 状态响应解析 ============

    /**
     * 解析状态响应：兼容 NewAPI 官方与 output 包裹风格；解析完成后调用
     * {@link #buildFallbackResultUrl} 提供产物 URL 回退（默认无）。
     */
    protected NewApiStatusResponse parseStatusResponse(HttpResponse response, String baseUrl, String taskId) {
        try (HttpResponse resp = response) {
            if (!resp.isOk()) {
                NewApiStatusResponse errorResponse = new NewApiStatusResponse();
                errorResponse.setError("请求失败: " + resp.getStatus() + " " + StrUtil.sub(resp.body(), 0, 300));
                return errorResponse;
            }

            String body = resp.body();
            log.debug("状态查询响应: {}", body);

            JSONObject json = JSONUtil.parseObj(body);
            NewApiStatusResponse statusResponse = new NewApiStatusResponse();
            statusResponse.setId(json.getStr("id"));
            statusResponse.setStatus(json.getStr("status"));
            statusResponse.setResultUrl(json.getStr("result_url"));
            statusResponse.setOutputUrl(json.getStr("output_url"));
            statusResponse.setVideoUrl(json.getStr("video_url"));
            statusResponse.setUrl(json.getStr("url"));
            statusResponse.setImageUrl(json.getStr("image_url"));
            statusResponse.setPosterUrl(json.getStr("poster_url"));
            statusResponse.setFailReason(json.getStr("fail_reason"));
            statusResponse.setProgress(json.getInt("progress"));

            // output 包裹风格：output.task_status / output.task_id / output.video_url / output.message
            JSONObject output = json.getJSONObject("output");
            if (output != null) {
                if (StrUtil.isEmpty(statusResponse.getStatus())) {
                    statusResponse.setStatus(output.getStr("task_status"));
                }
                if (StrUtil.isEmpty(statusResponse.getId())) {
                    statusResponse.setId(output.getStr("task_id"));
                }
                if (StrUtil.isEmpty(statusResponse.getResultUrlValue())) {
                    String videoUrl = output.getStr("video_url");
                    if (StrUtil.isNotEmpty(videoUrl)) {
                        statusResponse.setVideoUrl(videoUrl);
                    }
                }
                if (StrUtil.isNotEmpty(output.getStr("message"))) {
                    statusResponse.setMessage(output.getStr("message"));
                }
                if (StrUtil.isNotEmpty(output.getStr("code")) && "FAILED".equalsIgnoreCase(output.getStr("task_status"))) {
                    statusResponse.setError(output.getStr("code") + ": " + output.getStr("message"));
                }
            }

            String error = extractError(json);
            if (StrUtil.isNotEmpty(error)) {
                statusResponse.setError(error);
            }
            if (StrUtil.isEmpty(statusResponse.getMessage())) {
                statusResponse.setMessage(json.getStr("message"));
            }

            // images 数组
            Object imagesObj = json.get("images");
            if (imagesObj != null) {
                statusResponse.setImages(JSONUtil.parseArray(imagesObj).toArray(new String[0]));
            }

            // 成功但无直接产物 URL → 交由子类回退策略
            if (statusResponse.isSuccess() && StrUtil.isEmpty(statusResponse.getResultUrlValue())) {
                String fallback = buildFallbackResultUrl(baseUrl, taskId);
                if (StrUtil.isNotEmpty(fallback)) {
                    statusResponse.setResultUrl(fallback);
                }
            }

            return statusResponse;
        }
    }

    /**
     * 产物 URL 回退策略：默认无；子类可按协议覆写（如 ComfyUI 的 content 下载端点）
     */
    protected String buildFallbackResultUrl(String baseUrl, String taskId) {
        return null;
    }

    /**
     * 提取上游错误：error 可能是字符串或 { message, type, param, code }
     */
    protected String extractError(JSONObject json) {
        Object error = json.get("error");
        if (error == null) {
            return null;
        }
        if (error instanceof JSONObject) {
            JSONObject err = (JSONObject) error;
            String message = err.getStr("message");
            if (StrUtil.isEmpty(message)) {
                message = err.getStr("msg");
            }
            String code = err.getStr("code");
            if (StrUtil.isNotEmpty(code) && StrUtil.isNotEmpty(message)) {
                return code + ": " + message;
            }
            return StrUtil.isNotEmpty(message) ? message : err.toString();
        }
        return error.toString();
    }

    // ============ 请求辅助 ============

    /**
     * 取第一张参考图 URL：优先 imageUrls，其次 input.media 中的 reference_image / first_frame
     */
    protected String firstImageUrl(NewApiVideoRequest request) {
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            return request.getImageUrls().get(0);
        }
        if (request.getInput() != null && request.getInput().getMedia() != null) {
            for (NewApiVideoRequest.Media m : request.getInput().getMedia()) {
                if (isReferenceImage(m.getType())) {
                    return m.getUrl();
                }
            }
        }
        return null;
    }

    /**
     * 是否为参考图类型媒体
     */
    protected boolean isReferenceImage(String mediaType) {
        return TaskConstants.MediaType.REFERENCE_IMAGE.equals(mediaType)
                || TaskConstants.MediaType.FIRST_FRAME.equals(mediaType);
    }

    /**
     * 解析时长：优先 seconds，其次 parameters.duration
     */
    protected Integer parseDurationSeconds(NewApiVideoRequest request) {
        if (StrUtil.isNotEmpty(request.getSeconds())) {
            try {
                return Integer.parseInt(request.getSeconds().trim());
            } catch (Exception ignore) {
                // 非数字时长忽略
            }
        }
        if (request.getParameters() != null && request.getParameters().getDuration() != null) {
            return request.getParameters().getDuration();
        }
        return null;
    }
}
