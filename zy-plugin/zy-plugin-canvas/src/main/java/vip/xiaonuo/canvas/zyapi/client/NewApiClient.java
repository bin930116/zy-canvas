package vip.xiaonuo.canvas.zyapi.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.zyapi.client.adapter.MediaModelAdapter;
import vip.xiaonuo.canvas.zyapi.client.request.NewApiImageRequest;
import vip.xiaonuo.canvas.zyapi.client.request.NewApiVideoRequest;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiStatusResponse;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiTaskResponse;

import java.util.List;

/**
 * 模型渠道门面（Facade）
 * <p>
 * 统一入口：按模型配置（baseUrl 域名）分发给对应 {@link MediaModelAdapter}。
 * 业务层（任务处理器 / 轮询服务 / 结果处理）只依赖本类，不感知协议差异。
 * <p>
 * 新增模型渠道 = 新增一个 {@link MediaModelAdapter} 实现并注册为 Spring Bean，
 * 业务层零改动。
 *
 * @author Your Name
 * @date 2026/09/09
 **/
@Component
public class NewApiClient {

    private static final Logger log = LoggerFactory.getLogger(NewApiClient.class);

    /** 所有已注册的渠道适配器（Spring 自动注入） */
    @Resource
    private List<MediaModelAdapter> mediaModelAdapters;

    /**
     * 按模型配置选择适配器
     */
    private MediaModelAdapter resolveAdapter(ZyModel model) {
        for (MediaModelAdapter adapter : mediaModelAdapters) {
            if (adapter.supports(model)) {
                return adapter;
            }
        }
        throw new RuntimeException("未找到模型 " + model.getModelKey() + " 的渠道适配器: " + model.getBaseUrl());
    }

    /**
     * 创建视频生成任务
     *
     * @param model   模型配置
     * @param request 视频请求
     * @return 任务响应
     */
    public NewApiTaskResponse createVideoTask(ZyModel model, NewApiVideoRequest request) {
        return resolveAdapter(model).createVideoTask(model, request);
    }

    /**
     * 创建图片生成任务
     *
     * @param model   模型配置
     * @param request 图片请求
     * @return 任务响应
     */
    public NewApiTaskResponse createImageTask(ZyModel model, NewApiImageRequest request) {
        return resolveAdapter(model).createImageTask(model, request);
    }

    /**
     * 查询任务状态
     *
     * @param model  模型配置
     * @param taskId 任务ID
     * @return 状态响应
     */
    public NewApiStatusResponse getStatus(ZyModel model, String taskId) {
        return resolveAdapter(model).getStatus(model, taskId);
    }

    /**
     * 取消任务
     *
     * @param model  模型配置
     * @param taskId 任务ID
     */
    public void cancelTask(ZyModel model, String taskId) {
        resolveAdapter(model).cancelTask(model, taskId);
    }

    /**
     * 下载媒体文件（协议无关）
     *
     * @param url 媒体URL
     * @return 文件字节数组
     */
    public byte[] downloadMedia(String url) {
        return downloadMedia(url, null);
    }

    /**
     * 下载媒体文件（协议无关，可选携带 Bearer 鉴权）
     *
     * @param url    媒体URL
     * @param apiKey 可选鉴权Key（如 content 下载端点需要 Bearer 时传入）
     * @return 文件字节数组
     */
    public byte[] downloadMedia(String url, String apiKey) {
        return downloadMediaWithType(url, apiKey).bytes();
    }

    /**
     * 下载媒体文件并返回响应 Content-Type（协议无关，可选携带 Bearer 鉴权）
     * <p>
     * 供结果处理按上游实际类型落库 mimeType（video/mp4、image/png 等），
     * 避免硬编码类型与真实产物不符。
     *
     * @param url    媒体URL
     * @param apiKey 可选鉴权Key（如 content 下载端点需要 Bearer 时传入）
     * @return 字节内容 + Content-Type（可能为空）
     */
    public DownloadResult downloadMediaWithType(String url, String apiKey) {
        return downloadMediaWithType(url, apiKey, DEFAULT_DOWNLOAD_TIMEOUT_MS);
    }

    /**
     * 下载媒体文件并返回响应 Content-Type（可指定读超时）
     *
     * @param url       媒体URL
     * @param apiKey    可选鉴权Key（如 content 下载端点需要 Bearer 时传入）
     * @param timeoutMs 读超时（毫秒），模型配置 timeout_seconds 未覆盖时用默认 60 秒
     * @return 字节内容 + Content-Type（可能为空）
     */
    public DownloadResult downloadMediaWithType(String url, String apiKey, int timeoutMs) {
        log.info("下载媒体文件: {}", url);

        HttpRequest request = HttpRequest.get(url).timeout(timeoutMs);
        if (StrUtil.isNotEmpty(apiKey)) {
            request.header("Authorization", "Bearer " + apiKey);
        }
        try (HttpResponse response = request.execute()) {
            if (!response.isOk()) {
                throw new RuntimeException("下载媒体失败: " + response.getStatus());
            }
            return new DownloadResult(response.bodyBytes(), response.header("Content-Type"));
        }
    }

    /**
     * 下载媒体文件到临时文件（流式，避免大视频/大音频全量入内存导致 OOM）。
     * 调用方负责在 finally 中删除临时文件。
     *
     * @param url       媒体URL
     * @param apiKey    可选鉴权Key（如 content 下载端点需要 Bearer 时传入）
     * @param timeoutMs 读超时（毫秒）
     * @return 临时文件 + 响应 Content-Type + 字节数
     */
    public DownloadFileResult downloadMediaToFile(String url, String apiKey, int timeoutMs) {
        log.info("流式下载媒体文件: {}", url);

        HttpRequest request = HttpRequest.get(url).timeout(timeoutMs);
        if (StrUtil.isNotEmpty(apiKey)) {
            request.header("Authorization", "Bearer " + apiKey);
        }
        try (HttpResponse response = request.execute()) {
            if (!response.isOk()) {
                throw new RuntimeException("下载媒体失败: " + response.getStatus());
            }

            try {
                java.io.File temp = java.io.File.createTempFile("zy-media-dl-", ".tmp");
                try (java.io.InputStream in = response.bodyStream();
                     java.io.FileOutputStream out = new java.io.FileOutputStream(temp)) {
                    long bytes = cn.hutool.core.io.IoUtil.copy(in, out);
                    return new DownloadFileResult(temp, response.header("Content-Type"), bytes);
                } catch (Exception e) {
                    cn.hutool.core.io.FileUtil.del(temp);
                    throw e;
                }
            } catch (java.io.IOException e) {
                throw new RuntimeException("下载媒体失败: " + e.getMessage(), e);
            }
        }
    }

    /** 默认下载读超时（毫秒）：60 秒 */
    private static final int DEFAULT_DOWNLOAD_TIMEOUT_MS = 60000;

    /** 下载读超时上限（毫秒）：10 分钟，避免模型超时配置过大时下载阻塞过久 */
    private static final int MAX_DOWNLOAD_TIMEOUT_MS = 600000;

    /**
     * 按模型配置解析下载读超时：优先 timeout_seconds，缺省 60 秒，封顶 10 分钟
     */
    public static int resolveDownloadTimeoutMs(ZyModel model) {
        if (model != null && model.getTimeoutSeconds() != null && model.getTimeoutSeconds() > 0) {
            return Math.min(model.getTimeoutSeconds() * 1000, MAX_DOWNLOAD_TIMEOUT_MS);
        }
        return DEFAULT_DOWNLOAD_TIMEOUT_MS;
    }

    /**
     * 下载结果：字节内容 + 响应 Content-Type（可能为空）
     */
    public record DownloadResult(byte[] bytes, String contentType) {
    }

    /**
     * 流式下载结果：临时文件 + 响应 Content-Type + 字节数（调用方负责删除临时文件）
     */
    public record DownloadFileResult(java.io.File file, String contentType, long bytes) {
    }
}
