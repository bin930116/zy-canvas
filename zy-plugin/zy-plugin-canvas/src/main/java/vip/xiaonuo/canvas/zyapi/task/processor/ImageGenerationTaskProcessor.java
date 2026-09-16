package vip.xiaonuo.canvas.zyapi.task.processor;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.shotartifact.entity.ZyShotArtifact;
import vip.xiaonuo.canvas.modular.shotartifact.mapper.ZyShotArtifactMapper;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.modular.generationtask.mapper.ZyGenerationTaskMapper;
import vip.xiaonuo.canvas.zyapi.service.ZyApiAssetService;
import vip.xiaonuo.canvas.zyapi.constant.TaskConstants;
import vip.xiaonuo.canvas.zyapi.service.impl.ZyApiAssetServiceImpl;
import vip.xiaonuo.canvas.zyapi.service.ZyApiCharacterService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiModelService;
import vip.xiaonuo.canvas.zyapi.task.TaskProcessor;
import vip.xiaonuo.canvas.zyapi.task.TaskCompletionNotifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片生成任务处理器
 * 从数据库模型配置（或前端 config 回退）获取 baseUrl/apiKey，直连 OpenAI 兼容图片接口
 * （/v1/images/generations），同步返回图片并存储为资产；角色三视图任务自动绑定到角色卡
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Component
public class ImageGenerationTaskProcessor implements TaskProcessor {

    private static final Logger log = LoggerFactory.getLogger(ImageGenerationTaskProcessor.class);

    @Resource
    private ZyApiModelService zyModelService;

    @Resource
    private ZyApiAssetService zyAssetService;

    @Resource
    private ZyApiCharacterService zyCharacterService;

    @Resource
    private ZyGenerationTaskMapper zyGenerationTaskMapper;

    @Resource
    private ZyShotArtifactMapper zyShotArtifactMapper;

    @Resource
    private TaskCompletionNotifier taskCompletionNotifier;

    @Override
    public void process(ZyGenerationTask task) throws Exception {
        log.info("开始处理图片任务: id={}", task.getId());

        // 1. 解析输入
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

        // 2. 获取模型配置（优先数据库，回退前端 config）
        String baseUrl = null;
        String apiKey = null;
        try {
            ZyModel model = zyModelService.getByModelKey(modelKey);
            if (model != null) {
                baseUrl = model.getBaseUrl();
                apiKey = model.getApiKey();
                log.info("从数据库获取模型配置: model={}, baseUrl={}", modelKey, baseUrl);
            }
        } catch (Exception e) {
            log.warn("从数据库查询模型配置失败，尝试前端 config: {}", e.getMessage());
        }
        if (StrUtil.isEmpty(baseUrl) || StrUtil.isEmpty(apiKey)) {
            JSONObject config = input.getJSONObject("config");
            if (baseUrl == null) baseUrl = config != null ? config.getStr("baseUrl") : null;
            if (apiKey == null) apiKey = config != null ? config.getStr("apiKey") : null;
            log.info("从前端 config 获取模型配置: model={}, baseUrl={}", modelKey, baseUrl);
        }
        if (StrUtil.isEmpty(baseUrl) || StrUtil.isEmpty(apiKey)) {
            throw new RuntimeException("模型配置不完整（缺少 baseUrl 或 apiKey）");
        }

        // 3. 更新任务阶段
        updateTaskStage(task.getId(), "调用生成模型", 30);

        try {
            // 4. 调用 CM 协议（ComfyUI Adapter）图片接口，返回图片字节
            byte[] imageData = callImageApiCm(baseUrl, apiKey, modelKey, prompt, input);

            // 4.1 魔数嗅探真实图片格式（上游 base64 响应不携带类型，硬编码 image/png 可能存错格式）
            String imageMime = ZyApiAssetServiceImpl.sniffImageMimeType(imageData);
            if (imageMime == null) {
                imageMime = "image/png";
            }

            // 5. 存储为资产
            ZyAsset asset = zyAssetService.storeMedia(task.getUserId(), task.getProjectId(), imageData, "image", imageMime);

            // 6. 角色三视图任务：自动绑定到角色卡
            bindToCharacterIfPresent(task, input, asset);

            // 6.1 分镜图/动作预演任务：回填镜头产物（按 metadata.shotId + artifactType）
            bindToShotArtifactIfPresent(task, input, asset);

            // 7. 构建结果 JSON（与前端 images 契约一致）
            Map<String, Object> result = new HashMap<>();
            result.put("mode", "image");
            List<Map<String, Object>> images = new ArrayList<>();
            Map<String, Object> imageInfo = new HashMap<>();
            imageInfo.put("dataUrl", asset.getStorageKey());
            imageInfo.put("resourceId", asset.getId());
            imageInfo.put("mimeType", imageMime);
            imageInfo.put("bytes", imageData.length);
            images.add(imageInfo);
            result.put("images", images);

            // 8. 更新任务成功（仅当任务仍处于活跃状态：避免取消后覆盖回 succeeded）
            UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                    .eq("id", task.getId())
                    .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING)
                    .set("status", TaskConstants.Status.SUCCEEDED)
                    .set("progress", 100)
                    .set("stage", "完成")
                    .set("completed_at", new Date())
                    .set("result_json", JSONUtil.toJsonStr(result));
            int updated = zyGenerationTaskMapper.update(null, updateWrapper);
            if (updated == 0) {
                log.info("图片任务 {} 状态已变化（可能已取消），跳过成功通知", task.getId());
                return;
            }

            log.info("图片任务完成: id={}, bytes={}, assetId={}", task.getId(), imageData.length, asset.getId());
            taskCompletionNotifier.notifySuccess(task);
        } catch (Exception e) {
            log.error("图片任务处理失败: id={}, error={}", task.getId(), e.getMessage(), e);
            // 仅当任务仍处于活跃状态才标记失败，避免覆盖已取消/已成功的状态
            UpdateWrapper<ZyGenerationTask> failWrapper = new UpdateWrapper<ZyGenerationTask>()
                    .eq("id", task.getId())
                    .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING)
                    .set("status", TaskConstants.Status.FAILED)
                    .set("progress", 100)
                    .set("stage", "失败")
                    .set("error", e.getMessage() != null ? e.getMessage() : "图片生成失败")
                    .set("completed_at", new Date());
            int failUpdated = zyGenerationTaskMapper.update(null, failWrapper);
            if (failUpdated == 0) {
                log.info("图片任务 {} 状态已变化（可能已取消），跳过失败通知", task.getId());
                return;
            }
            taskCompletionNotifier.notifyFailed(task, e.getMessage());
        }
    }

    @Override
    public boolean supports(String taskType) {
        return "image".equals(taskType) || "canvas_image".equals(taskType);
    }

    @Override
    public String getTaskType() {
        return "image";
    }

    /**
     * CM 协议（ComfyUI Adapter）图片生成（图片任务默认协议）
     * 格式：{ model, prompt, size, seed, response_format }
     * 返回 data[].url 或 data[].b64_json
     */
    private byte[] callImageApiCm(String baseUrl, String apiKey, String model, String prompt, JSONObject input) {
        // 拼接 URL：baseUrl + /v1/images/generations
        String url = baseUrl;
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (!url.endsWith("/v1")) {
            url += "/v1";
        }
        url += "/images/generations";

        // 构建 CM 协议请求体
        JSONObject body = new JSONObject();
        body.set("model", model);
        body.set("prompt", prompt);

        JSONObject config = input.getJSONObject("config");
        if (config != null) {
            String normalizedSize = normalizeImageSize(config.getStr("size"));
            if (normalizedSize != null) {
                body.set("size", normalizedSize);
            }
        }
        // 请求 base64 返回，避免下载需要鉴权的 URL
        body.set("response_format", "b64_json");

        log.info("调用 CM 协议图片 API: url={}, model={}, size={}", url, model, body.getStr("size"));

        try (HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .body(body.toString())
                .timeout(300000) // 5 分钟超时
                .execute()) {

            if (!response.isOk()) {
                String responseBody = response.body();
                log.error("CM 协议图片 API 调用失败: status={}, body={}", response.getStatus(), responseBody);
                throw new RuntimeException("图片任务提交失败: " + response.getStatus() + " " + StrUtil.sub(responseBody, 0, 300));
            }

            String responseBody = response.body();
            log.info("CM 协议图片 API 响应: {}", StrUtil.sub(responseBody, 0, 500));
            JSONObject responseJson = JSONUtil.parseObj(responseBody);

            // 解析 data[].b64_json 或 data[].url
            JSONArray data = responseJson.getJSONArray("data");
            if (data != null && !data.isEmpty()) {
                JSONObject first = data.getJSONObject(0);
                // 优先 base64
                String b64 = first.getStr("b64_json");
                if (StrUtil.isNotEmpty(b64)) {
                    return Base64.decode(b64);
                }
                // 其次 URL（相对路径需要拼 baseUrl）
                String imageUrl = first.getStr("url");
                if (StrUtil.isNotEmpty(imageUrl)) {
                    if (imageUrl.startsWith("/")) {
                        String root = baseUrl;
                        while (root.endsWith("/")) {
                            root = root.substring(0, root.length() - 1);
                        }
                        if (root.endsWith("/v1")) {
                            root = root.substring(0, root.length() - 3);
                        }
                        imageUrl = root + imageUrl;
                    }
                    return downloadBytes(imageUrl);
                }
            }

            throw new RuntimeException("CM 协议图片生成成功但未解析到图片: " + StrUtil.sub(responseBody, 0, 300));
        }
    }

    /**
     * 归一化图片尺寸：兼容"1024x1024"与"9:16"两种写法，未知格式返回 null（不传，用服务默认）
     */
    private String normalizeImageSize(String size) {
        if (StrUtil.isEmpty(size)) return null;
        String trimmed = size.trim();
        // 已是宽x高（如 1024x1024 / 1280X720）
        if (trimmed.matches("\\d+[xX]\\d+")) {
            return trimmed.toLowerCase();
        }
        // 比例映射（常见画幅 → 常用像素）
        switch (trimmed) {
            case "1:1": return "1024x1024";
            case "9:16": return "720x1280";
            case "16:9": return "1280x720";
            case "3:4": return "768x1024";
            case "4:3": return "1024x768";
            case "2:3": return "768x1152";
            case "3:2": return "1152x768";
            case "21:9": return "1344x576";
            default: return null;
        }
    }

    private byte[] downloadBytes(String imageUrl) {
        try (HttpResponse response = HttpRequest.get(imageUrl).timeout(120000).execute()) {
            if (!response.isOk()) {
                throw new RuntimeException("下载图片失败: " + response.getStatus());
            }
            return response.bodyBytes();
        }
    }

    /**
     * 角色三视图任务（metadata.characterAssetId）生成后自动绑定为角色卡 primary 形象
     */
    private void bindToCharacterIfPresent(ZyGenerationTask task, JSONObject input, ZyAsset asset) {
        JSONObject metadata = input.getJSONObject("metadata");
        if (metadata == null) return;
        String characterAssetId = metadata.getStr("characterAssetId");
        if (StrUtil.isEmpty(characterAssetId)) return;

        List<Map<String, Object>> representations = new ArrayList<>();
        Map<String, Object> representation = new HashMap<>();
        representation.put("role", "primary");
        representation.put("resourceId", asset.getId());
        // 图 URL（dev 下载链接或 MINIO 直链），前端可直接加载，避免走 /resources/{id}/file 404
        representation.put("url", asset.getStorageKey());
        representations.add(representation);

        try {
            zyCharacterService.putRepresentations(task.getUserId(), asset.getProjectId(), characterAssetId, representations);
            log.info("角色三视图已绑定: characterAssetId={}, imageAssetId={}", characterAssetId, asset.getId());
        } catch (Exception e) {
            log.warn("角色三视图绑定失败: {}", e.getMessage());
        }
    }

    /**
     * 分镜产物任务（metadata.shotId）生成后回填为镜头产物
     */
    private void bindToShotArtifactIfPresent(ZyGenerationTask task, JSONObject input, ZyAsset asset) {
        JSONObject metadata = input.getJSONObject("metadata");
        if (metadata == null) return;
        String shotId = metadata.getStr("shotId");
        if (StrUtil.isEmpty(shotId)) return;
        String artifactType = metadata.getStr("artifactType");
        String type = StrUtil.isNotEmpty(artifactType) ? artifactType : "storyboard";
        String url = asset.getStorageKey();
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
        // 版本号 = 同镜头同类型最大版本 + 1，避免重复 v1
        Integer maxVersion = zyShotArtifactMapper.selectList(new QueryWrapper<ZyShotArtifact>()
                        .eq("shot_id", shotId)
                        .eq("type", type)
                        .orderByDesc("version"))
                .stream()
                .map(ZyShotArtifact::getVersion)
                .filter(v -> v != null)
                .findFirst()
                .orElse(0);
        artifact.setVersion(maxVersion == null ? 1 : maxVersion + 1);
        artifact.setResourceId(asset.getId());
        artifact.setUrl(url);
        artifact.setMediaType("image");
        artifact.setStatus("ready");
        artifact.setSelected(true);
        artifact.setMetadataJson(JSONUtil.toJsonStr(new java.util.HashMap<String, Object>() {{
            put("url", url != null ? url : "");
            put("mode", "image");
        }}));
        artifact.setCreateUser(task.getUserId());
        artifact.setUpdateUser(task.getUserId());
        zyShotArtifactMapper.insert(artifact);
        log.info("分镜产物已回填: shotId={}, type={}, artifactId={}", shotId, type, artifact.getId());
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
