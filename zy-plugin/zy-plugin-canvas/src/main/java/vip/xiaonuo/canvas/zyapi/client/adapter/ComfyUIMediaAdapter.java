package vip.xiaonuo.canvas.zyapi.client.adapter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.zyapi.client.request.NewApiImageRequest;
import vip.xiaonuo.canvas.zyapi.client.request.NewApiVideoRequest;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiStatusResponse;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiTaskResponse;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * ComfyUI Adapter / NewAPI 视频网关适配器
 * <p>
 * 对应 ComfyUI Adapter API 手册：
 * - 提交视频：POST /v1/videos（NewAPI 推荐入口；兼容入口 /v1/video/generations）
 * - 请求体：{ model, prompt, duration, resolution, ratio, image_url }
 * - 状态查询：GET /v1/videos/{task_id}
 * - 产物下载：GET /v1/videos/{task_id}/content
 * - 鉴权：Authorization: Bearer &lt;key&gt;
 * <p>
 * 关键约定：参考图以 URL 形式提交，但适配器/NewAPI 网关需拿到图片内容，
 * 因此本适配器在提交前将参考图 URL 下载并转为 base64 data URI，放入 image_url 字段。
 * 业务层（任务处理器 / 轮询 / 结果回填）零改动，仅新增本 Bean。
 * <p>
 * 公共能力（HTTP 执行、响应解析、错误提取、参考图定位、时长解析）继承自
 * {@link AbstractMediaAdapter}，本类只保留协议差异与图片压缩逻辑。
 *
 * @author hanbin
 * @date 2026/09/15
 **/
@Component
public class ComfyUIMediaAdapter extends AbstractMediaAdapter {

    private static final Logger log = LoggerFactory.getLogger(ComfyUIMediaAdapter.class);

    /** 参考图压缩：最长边像素（控制 base64 体积，避免大请求体触发网关 504） */
    private static final int MAX_REFERENCE_SIDE = 1024;

    /** 参考图压缩：JPEG 质量（0.8 档在体积与参考图准确度间平衡） */
    private static final float REFERENCE_JPEG_QUALITY = 0.8f;

    /** 参考图下载超时（毫秒） */
    private static final int REFERENCE_DOWNLOAD_TIMEOUT_MS = 60000;

    @Override
    public boolean supports(ZyModel model) {
        if (model == null || StrUtil.isEmpty(model.getModelKey())) {
            return false;
        }
        String modelKey = model.getModelKey().toLowerCase();
        // 命中 ComfyUI 系列模型：minimax / comfyui / comfy
        return modelKey.contains("minimax") || modelKey.contains("comfyui") || modelKey.contains("comfy");
    }

    @Override
    public NewApiTaskResponse createVideoTask(ZyModel model, NewApiVideoRequest request) {
        String url = buildUrl(model.getBaseUrl(), "/v1/videos");
        Map<String, Object> body = buildVideoRequestBody(request);

        log.info("创建 ComfyUI 视频任务: url={}, model={}", url, request.getModel());

        HttpResponse response = executePost(url, model.getApiKey(), body, submitTimeoutMs(model));
        return parseTaskResponse(response);
    }

    @Override
    public NewApiTaskResponse createImageTask(ZyModel model, NewApiImageRequest request) {
        // ComfyUI 图像走 /v1/images/generations（本适配器主要处理视频；图像任务交由通用适配器）
        throw new UnsupportedOperationException("ComfyUI Adapter 暂不支持图像任务，请使用通用渠道");
    }

    @Override
    public NewApiStatusResponse getStatus(ZyModel model, String taskId) {
        String baseUrl = normalizeBaseUrl(model.getBaseUrl());
        String url = baseUrl + "/v1/videos/" + taskId;

        log.debug("查询 ComfyUI 任务状态: url={}, taskId={}", url, taskId);

        HttpResponse response = executeGet(url, model.getApiKey(), queryTimeoutMs(model));
        return parseStatusResponse(response, baseUrl, taskId);
    }

    @Override
    public void cancelTask(ZyModel model, String taskId) {
        String baseUrl = normalizeBaseUrl(model.getBaseUrl());
        String url = baseUrl + "/v1/videos/" + taskId;
        log.info("取消 ComfyUI 任务: url={}, taskId={}", url, taskId);
        try {
            try (HttpResponse response = executeDelete(url, model.getApiKey(), queryTimeoutMs(model))) {
                // 取消接口无响应体需要消费，仅需关闭连接
            }
        } catch (Exception e) {
            log.warn("取消任务失败: {}", e.getMessage());
        }
    }

    /**
     * 产物 URL 回退：ComfyUI 状态响应成功但无直接产物 URL 时，回退为 content 下载端点
     */
    @Override
    protected String buildFallbackResultUrl(String baseUrl, String taskId) {
        String fallback = baseUrl + "/v1/videos/" + taskId + "/content";
        log.info("ComfyUI 状态无直接产物URL，回退 content 端点: {}", fallback);
        return fallback;
    }

    // ============ 私有方法 ============

    /**
     * 构建请求体：{ model, prompt, duration, resolution, ratio, image_url }
     */
    private Map<String, Object> buildVideoRequestBody(NewApiVideoRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("prompt", request.getPrompt());

        Integer duration = parseDurationSeconds(request);
        if (duration != null) {
            body.put("duration", duration);
        }
        if (StrUtil.isNotEmpty(request.getResolution())) {
            body.put("resolution", request.getResolution());
        }
        if (StrUtil.isNotEmpty(request.getAspectRatio())) {
            body.put("ratio", request.getAspectRatio());
        }

        // 参考图：取第一张，下载并转 base64 data URI 放入 image_url（文档单图字段）
        String imageUrl = firstImageUrl(request);
        if (StrUtil.isNotEmpty(imageUrl)) {
            String dataUri = toBase64DataUri(imageUrl);
            body.put("image_url", dataUri);
            log.info("参考图已转 base64: url={}, dataUriLength={}", imageUrl, dataUri.length());
        } else {
            log.warn("ComfyUI 视频任务没有参考图");
        }

        return body;
    }

    /**
     * 下载图片字节，压缩后转为 base64 data URI（data:image/jpeg;base64,...）
     * <p>
     * 上游对 image_url 支持 http/https/data 三种 scheme，data URI 必须显式带
     * "data:image/xxx;base64," 前缀（纯 base64 会返回 "image URL scheme must be
     * http, https or data"）。为避免超大请求体导致网关 504，先压缩：
     * 最长边限制 {@value #MAX_REFERENCE_SIDE}px、JPEG 质量 {@value #REFERENCE_JPEG_QUALITY}，
     * base64 体积从 MB 级降到百 KB 级。
     */
    private String toBase64DataUri(String imageUrl) {
        byte[] bytes;
        try (HttpResponse response = HttpRequest.get(imageUrl)
                .timeout(REFERENCE_DOWNLOAD_TIMEOUT_MS)
                .execute()) {
            if (!response.isOk()) {
                throw new RuntimeException("下载参考图失败: " + response.getStatus());
            }
            bytes = response.bodyBytes();
        } catch (Exception e) {
            throw new RuntimeException("下载参考图失败: " + imageUrl + " -> " + e.getMessage());
        }
        if (bytes == null || bytes.length == 0) {
            throw new RuntimeException("参考图内容为空: " + imageUrl);
        }
        byte[] compressed = compressImage(bytes);
        String base64 = Base64.getEncoder().encodeToString(compressed);
        return "data:image/jpeg;base64," + base64;
    }

    /**
     * 压缩图片：最长边 {@value #MAX_REFERENCE_SIDE}px、JPEG 质量 {@value #REFERENCE_JPEG_QUALITY}。
     * PNG 透明底填充白色后再转 JPEG。
     */
    private byte[] compressImage(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return bytes; // 无法解析则原样返回
            }
            int w = image.getWidth();
            int h = image.getHeight();

            if (w > MAX_REFERENCE_SIDE || h > MAX_REFERENCE_SIDE) {
                double scale = Math.min(1.0 * MAX_REFERENCE_SIDE / w, 1.0 * MAX_REFERENCE_SIDE / h);
                int nw = Math.max(1, (int) Math.round(w * scale));
                int nh = Math.max(1, (int) Math.round(h * scale));
                BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = scaled.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, nw, nh);
                g.drawImage(image, 0, 0, nw, nh, null);
                g.dispose();
                image = scaled;
            } else if (image.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage rgb = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgb.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, w, h);
                g.drawImage(image, 0, 0, null);
                g.dispose();
                image = rgb;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (!writers.hasNext()) {
                return bytes;
            }
            ImageWriter writer = writers.next();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(REFERENCE_JPEG_QUALITY);
                writer.write(null, new IIOImage(image, null, null), param);
            } finally {
                writer.dispose();
            }
            byte[] result = out.toByteArray();
            // 压缩后反而更大则用原字节
            return result.length < bytes.length ? result : bytes;
        } catch (IOException | RuntimeException e) {
            log.warn("压缩参考图失败，使用原图: {}", e.getMessage());
            return bytes;
        }
    }
}
