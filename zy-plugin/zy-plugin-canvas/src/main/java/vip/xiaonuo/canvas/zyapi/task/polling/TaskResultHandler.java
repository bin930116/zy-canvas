package vip.xiaonuo.canvas.zyapi.task.polling;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.zyapi.client.NewApiClient;
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.modular.generationtask.mapper.ZyGenerationTaskMapper;
import vip.xiaonuo.canvas.modular.shotartifact.entity.ZyShotArtifact;
import vip.xiaonuo.canvas.modular.shotartifact.mapper.ZyShotArtifactMapper;
import vip.xiaonuo.canvas.zyapi.constant.TaskConstants;
import vip.xiaonuo.canvas.zyapi.service.ZyApiAssetService;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务结果处理器
 * 负责下载媒体、存储、构建结果JSON
 *
 * @author hanbin
 * @date 2026/09/15
 **/
@Service
public class TaskResultHandler {

    private static final Logger log = LoggerFactory.getLogger(TaskResultHandler.class);

    /** 各能力类型的结果 mimeType 兜底值（响应头缺失且 URL 无法推断时使用） */
    private static final String DEFAULT_VIDEO_MIME = "video/mp4";
    private static final String DEFAULT_IMAGE_MIME = "image/png";
    private static final String DEFAULT_AUDIO_MIME = "audio/mpeg";

    @Resource
    private NewApiClient newApiClient;

    @Resource
    private ZyApiAssetService zyAssetService;

    @Resource
    private ZyGenerationTaskMapper zyGenerationTaskMapper;

    @Resource
    private ZyShotArtifactMapper zyShotArtifactMapper;

    /**
     * 处理任务结果
     *
     * @param taskId    任务ID
     * @param resultUrl 结果URL
     * @param model     模型配置
     * @return 结果JSON
     */
    public Map<String, Object> handleResult(String taskId, String resultUrl, ZyModel model) {
        if (StrUtil.isEmpty(resultUrl)) {
            throw new RuntimeException("结果URL为空");
        }

        // 查询任务
        ZyGenerationTask task = zyGenerationTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String capability = task.getType();
        String userId = task.getUserId();
        String projectId = resolveTaskProjectId(task);

        // 根据能力类型处理（兼容前端提交的 canvas_ 前缀，如 canvas_video）
        if (TaskConstants.Type.VIDEO.equals(capability) || TaskConstants.Type.CANVAS_VIDEO.equals(capability)) {
            return handleVideoResult(projectId, userId, resultUrl, model);
        } else if (TaskConstants.Type.IMAGE.equals(capability) || TaskConstants.Type.CANVAS_IMAGE.equals(capability)) {
            return handleImageResult(projectId, userId, resultUrl, model);
        } else if (TaskConstants.Type.AUDIO.equals(capability) || TaskConstants.Type.CANVAS_AUDIO.equals(capability)) {
            return handleAudioResult(projectId, userId, resultUrl, model);
        } else if (TaskConstants.Type.TEXT.equals(capability) || TaskConstants.Type.CANVAS_TEXT.equals(capability)) {
            return handleTextResult(resultUrl);
        }

        throw new RuntimeException("不支持的能力类型: " + capability);
    }

    /**
     * 生成任务可能未写 project_id（画布/全局生成），从 metadata.domainProjectId 兜底；
     * 仍为空则落个人素材库（personal），避免 zy_asset 非空约束失败。
     */
    private String resolveTaskProjectId(ZyGenerationTask task) {
        if (StrUtil.isNotEmpty(task.getProjectId())) {
            return task.getProjectId();
        }
        String inputJson = task.getInputJson();
        if (StrUtil.isEmpty(inputJson)) {
            return "personal";
        }
        try {
            JSONObject input = JSONUtil.parseObj(inputJson);
            JSONObject metadata = input.getJSONObject("metadata");
            String fromMeta = metadata != null ? metadata.getStr("domainProjectId") : null;
            if (StrUtil.isNotEmpty(fromMeta)) {
                return fromMeta;
            }
            String fromInput = input.getStr("projectId");
            if (StrUtil.isNotEmpty(fromInput)) {
                return fromInput;
            }
        } catch (Exception e) {
            log.warn("解析任务 projectId 失败，回退 personal: taskId={}, {}", task.getId(), e.getMessage());
        }
        return "personal";
    }

    /**
     * 处理视频结果
     */
    private Map<String, Object> handleVideoResult(String projectId, String userId, String resultUrl, ZyModel model) {
        log.info("处理视频结果: userId={}, url={}", userId, resultUrl);

        // 下载视频（流式写临时文件，避免大视频全量入内存）
        NewApiClient.DownloadFileResult download = newApiClient.downloadMediaToFile(resultUrl, model.getApiKey(), NewApiClient.resolveDownloadTimeoutMs(model));
        String mimeType = resolveMimeType(download, resultUrl, DEFAULT_VIDEO_MIME);

        try {
            // 存储为资产
            ZyAsset asset = zyAssetService.storeMediaFile(userId, projectId, download.file(), TaskConstants.MediaKind.VIDEO, mimeType);

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("mode", TaskConstants.MediaKind.VIDEO);

            Map<String, Object> videoInfo = new HashMap<>();
            videoInfo.put("dataUrl", asset.getStorageKey());
            videoInfo.put("resourceId", asset.getId());
            videoInfo.put("mimeType", mimeType);
            videoInfo.put("bytes", download.bytes());

            result.put("video", videoInfo);

            return result;
        } finally {
            cn.hutool.core.io.FileUtil.del(download.file());
        }
    }

    /**
     * 处理图片结果
     */
    private Map<String, Object> handleImageResult(String projectId, String userId, String resultUrl, ZyModel model) {
        log.info("处理图片结果: userId={}, url={}", userId, resultUrl);

        // 下载图片（流式写临时文件）
        NewApiClient.DownloadFileResult download = newApiClient.downloadMediaToFile(resultUrl, model.getApiKey(), NewApiClient.resolveDownloadTimeoutMs(model));
        String mimeType = resolveMimeType(download, resultUrl, DEFAULT_IMAGE_MIME);

        try {
            // 存储为资产
            ZyAsset asset = zyAssetService.storeMediaFile(userId, projectId, download.file(), TaskConstants.MediaKind.IMAGE, mimeType);

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("mode", TaskConstants.MediaKind.IMAGE);

            Map<String, Object> imageInfo = new HashMap<>();
            imageInfo.put("dataUrl", asset.getStorageKey());
            imageInfo.put("resourceId", asset.getId());
            imageInfo.put("mimeType", mimeType);
            imageInfo.put("bytes", download.bytes());

            // 图片结果是一个数组
            result.put("images", new Object[]{imageInfo});

            return result;
        } finally {
            cn.hutool.core.io.FileUtil.del(download.file());
        }
    }

    /**
     * 处理音频结果
     */
    private Map<String, Object> handleAudioResult(String projectId, String userId, String resultUrl, ZyModel model) {
        log.info("处理音频结果: userId={}, url={}", userId, resultUrl);

        // 下载音频（流式写临时文件）
        NewApiClient.DownloadFileResult download = newApiClient.downloadMediaToFile(resultUrl, model.getApiKey(), NewApiClient.resolveDownloadTimeoutMs(model));
        String mimeType = resolveMimeType(download, resultUrl, DEFAULT_AUDIO_MIME);

        try {
            // 存储为资产
            ZyAsset asset = zyAssetService.storeMediaFile(userId, projectId, download.file(), TaskConstants.MediaKind.AUDIO, mimeType);

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("mode", TaskConstants.MediaKind.AUDIO);

            Map<String, Object> audioInfo = new HashMap<>();
            audioInfo.put("dataUrl", asset.getStorageKey());
            audioInfo.put("resourceId", asset.getId());
            audioInfo.put("mimeType", mimeType);
            audioInfo.put("bytes", download.bytes());

            result.put("audio", audioInfo);

            return result;
        } finally {
            cn.hutool.core.io.FileUtil.del(download.file());
        }
    }

    /**
     * 处理文本结果（通常不需要下载）
     */
    private Map<String, Object> handleTextResult(String resultUrl) {
        // 文本结果通常直接返回，这里暂不处理
        Map<String, Object> result = new HashMap<>();
        result.put("mode", "text");
        result.put("text", "生成成功");
        return result;
    }

    /**
     * 推断产物 mimeType：优先上游响应 Content-Type，其次 URL 后缀，最后兜底默认值。
     * 响应头可能带 "; charset=..." 等参数，需要截断清理；
     * application/octet-stream 视为"无类型信息"（CDN 常统一返回），继续走 URL 后缀/兜底。
     */
    private String resolveMimeType(NewApiClient.DownloadFileResult download, String url, String fallback) {
        String contentType = download.contentType();
        if (StrUtil.isNotEmpty(contentType)) {
            int semi = contentType.indexOf(';');
            String clean = semi > 0 ? contentType.substring(0, semi).trim() : contentType.trim();
            if (StrUtil.isNotEmpty(clean) && !"application/octet-stream".equalsIgnoreCase(clean)) {
                return clean;
            }
        }
        String guessed = guessMimeFromUrl(url);
        return StrUtil.isNotEmpty(guessed) ? guessed : fallback;
    }

    /**
     * 分镜产物回填：按 metadata.shotId + artifactType 写入 zy_shot_artifact。
     * 与图片任务 bindToShotArtifactIfPresent 同逻辑（版本号 = 同镜头同类型最大版本 + 1），
     * 供轮询成功路径与 query-provider 恢复路径共用，保证两条路径行为一致。
     */
    public void backfillShotArtifact(ZyGenerationTask task, Map<String, Object> result) {
        if (task == null || StrUtil.isEmpty(task.getInputJson())) {
            return;
        }
        try {
            JSONObject input = JSONUtil.parseObj(task.getInputJson());
            JSONObject metadata = input.getJSONObject("metadata");
            if (metadata == null) {
                return;
            }
            String shotId = metadata.getStr("shotId");
            if (StrUtil.isEmpty(shotId)) {
                return;
            }
            String artifactType = metadata.getStr("artifactType");
            String type = StrUtil.isNotEmpty(artifactType) ? artifactType : TaskConstants.Type.VIDEO;
            Object media = result.get(TaskConstants.MediaKind.VIDEO);
            String mediaKind = TaskConstants.MediaKind.VIDEO;
            if (!(media instanceof Map)) {
                media = result.get(TaskConstants.MediaKind.IMAGE);
                mediaKind = TaskConstants.MediaKind.IMAGE;
            }
            if (!(media instanceof Map)) {
                return;
            }
            Map<?, ?> mediaInfo = (Map<?, ?>) media;
            Object resourceIdObj = mediaInfo.get("resourceId");
            if (resourceIdObj == null) {
                return;
            }
            String resourceId = resourceIdObj.toString();
            Object dataUrlObj = mediaInfo.get("dataUrl");
            String url = dataUrlObj != null ? dataUrlObj.toString() : null;

            // 同镜头同类型旧产物标记为过期（保留历史）
            zyShotArtifactMapper.update(null, new UpdateWrapper<ZyShotArtifact>()
                    .eq("shot_id", shotId)
                    .eq("type", type)
                    .set("selected", false));
            ZyShotArtifact artifact = new ZyShotArtifact();
            artifact.setId(IdUtil.fastSimpleUUID());
            String artifactProjectId = task.getProjectId();
            if (StrUtil.isEmpty(artifactProjectId)) {
                artifactProjectId = metadata.getStr("domainProjectId");
            }
            artifact.setProjectId(StrUtil.isNotEmpty(artifactProjectId) ? artifactProjectId : "personal");
            artifact.setUnitId(metadata.getStr("unitId"));
            artifact.setShotId(shotId);
            artifact.setTaskId(task.getId());
            artifact.setType(type);
            // 版本号 = 同镜头同类型最大版本 + 1，与图片回填保持一致，避免版本重复
            Integer maxVersion = zyShotArtifactMapper.selectList(new QueryWrapper<ZyShotArtifact>()
                            .eq("shot_id", shotId)
                            .eq("type", type)
                            .orderByDesc("version"))
                    .stream()
                    .map(ZyShotArtifact::getVersion)
                    .filter(v -> v != null)
                    .findFirst()
                    .orElse(0);
            artifact.setVersion(maxVersion + 1);
            artifact.setResourceId(resourceId);
            artifact.setUrl(url);
            artifact.setMediaType(mediaKind);
            artifact.setStatus("ready");
            artifact.setSelected(true);
            Map<String, Object> meta = new HashMap<>();
            meta.put("url", url != null ? url : "");
            meta.put("mode", mediaKind);
            artifact.setMetadataJson(JSONUtil.toJsonStr(meta));
            artifact.setCreateUser(task.getUserId());
            artifact.setUpdateUser(task.getUserId());
            zyShotArtifactMapper.insert(artifact);
            log.info("分镜产物已回填: shotId={}, type={}, artifactId={}", shotId, type, artifact.getId());
        } catch (Exception e) {
            log.warn("分镜产物回填失败: {}", e.getMessage());
        }
    }

    /**
     * 按 URL 后缀猜测 mimeType
     */
    private String guessMimeFromUrl(String url) {
        if (StrUtil.isEmpty(url)) {
            return null;
        }
        String lower = url.toLowerCase();
        if (lower.contains(".mp4")) {
            return "video/mp4";
        }
        if (lower.contains(".webm")) {
            return "video/webm";
        }
        if (lower.contains(".png")) {
            return "image/png";
        }
        if (lower.contains(".jpg") || lower.contains(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.contains(".webp")) {
            return "image/webp";
        }
        if (lower.contains(".gif")) {
            return "image/gif";
        }
        if (lower.contains(".mp3")) {
            return "audio/mpeg";
        }
        if (lower.contains(".wav")) {
            return "audio/wav";
        }
        return null;
    }
}
