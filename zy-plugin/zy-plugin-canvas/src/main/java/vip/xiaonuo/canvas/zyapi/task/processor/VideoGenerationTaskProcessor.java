package vip.xiaonuo.canvas.zyapi.task.processor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vip.xiaonuo.canvas.zyapi.client.NewApiClient;
import vip.xiaonuo.canvas.zyapi.client.request.NewApiVideoRequest;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiTaskResponse;
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.modular.generationtask.mapper.ZyGenerationTaskMapper;
import vip.xiaonuo.canvas.zyapi.constant.TaskConstants;
import vip.xiaonuo.canvas.zyapi.service.ZyApiModelService;
import vip.xiaonuo.canvas.zyapi.task.TaskProcessor;
import vip.xiaonuo.canvas.zyapi.task.polling.TaskPollingService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 视频生成任务处理器
 *
 * @author hanbin
 * @date 2026/09/15
 **/
@Component
public class VideoGenerationTaskProcessor implements TaskProcessor {

    private static final Logger log = LoggerFactory.getLogger(VideoGenerationTaskProcessor.class);

    @Resource
    private ZyApiModelService zyModelService;

    @Resource
    private NewApiClient newApiClient;

    @Resource
    private TaskPollingService taskPollingService;

    @Resource
    private ZyGenerationTaskMapper zyGenerationTaskMapper;

    @Override
    public void process(ZyGenerationTask task) throws Exception {
        log.info("开始处理视频任务: id={}, type={}", task.getId(), task.getType());

        // 1. 解析输入JSON
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

        // 2. 查询模型配置
        ZyModel model = zyModelService.getByModelKey(modelKey);
        if (model == null) {
            throw new RuntimeException("模型不存在: " + modelKey);
        }

        if (StrUtil.isEmpty(model.getBaseUrl()) || StrUtil.isEmpty(model.getApiKey())) {
            throw new RuntimeException("模型配置不完整，缺少baseUrl或apiKey");
        }

        // 3. 更新任务阶段
        updateTaskStage(task.getId(), "调用生成模型", 35);

        // 4. 构建请求
        NewApiVideoRequest request = buildVideoRequest(task, input, model);

        // 5. 调用NewAPI创建任务
        NewApiTaskResponse response = newApiClient.createVideoTask(model, request);

        if (response.getTaskIdValue() == null) {
            String error = StrUtil.isNotEmpty(response.getError()) ? response.getError() : "创建任务失败";
            throw new RuntimeException(error);
        }

        // 6. 更新任务providerRequestId，并将状态置为 processing（非终态）：
        //    避免 TaskWorkerCoordinator 的兜底逻辑把仍为 running 的任务误标记为 succeeded，
        //    导致轮询结果（视频下载/产物回填）永不执行。
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", task.getId())
                .eq("status", TaskConstants.Status.RUNNING)
                .set("status", TaskConstants.Status.PROCESSING)
                .set("provider_request_id", response.getTaskIdValue())
                .set("stage", "等待生成")
                .set("progress", 40);

        zyGenerationTaskMapper.update(null, updateWrapper);

        // 7. 注册轮询任务
        taskPollingService.registerPolling(task.getId(), model.getId());

        log.info("视频任务已提交: taskId={}, providerTaskId={}", task.getId(), response.getTaskIdValue());
    }

    @Override
    public boolean supports(String taskType) {
        return TaskConstants.Type.VIDEO.equals(taskType) || TaskConstants.Type.CANVAS_VIDEO.equals(taskType) ||
               (taskType != null && taskType.startsWith("video_"));
    }

    @Override
    public String getTaskType() {
        return TaskConstants.Type.VIDEO;
    }

    /**
     * 构建视频请求
     */
    private NewApiVideoRequest buildVideoRequest(ZyGenerationTask task, JSONObject input, ZyModel model) {
        NewApiVideoRequest request = new NewApiVideoRequest();
        request.setModel(model.getModelKey());
        request.setPrompt(task.getPrompt());

        // 配置参数只解析一次，同时映射到协议字段（seconds/aspectRatio/resolution/generateAudio）
        // 与 parameters 结构，避免同一字段解析两遍
        VideoConfig config = parseConfig(input);

        if (config.duration != null) {
            request.setSeconds(String.valueOf(config.duration));
        }
        if (StrUtil.isNotEmpty(config.aspectRatio)) {
            request.setAspectRatio(config.aspectRatio);
        }
        if (StrUtil.isNotEmpty(config.resolution)) {
            request.setResolution(config.resolution);
        }
        if (config.generateAudio != null) {
            request.setGenerateAudio(config.generateAudio);
        }

        // 解析参考媒体：兼容前端提交的 referenceImages（含 url），以及旧协议 images/videos/audios
        List<String> imageUrls = parseMediaUrls(input.getJSONArray("referenceImages"));
        if (imageUrls == null) {
            imageUrls = parseMediaUrls(input.getJSONArray("images"));
        }
        List<String> videoUrls = parseMediaUrls(input.getJSONArray("referenceVideos"));
        if (videoUrls == null) {
            videoUrls = parseMediaUrls(input.getJSONArray("videos"));
        }
        List<String> audioUrls = parseMediaUrls(input.getJSONArray("audios"));
        String referenceVoice = input.getStr("referenceVoice");

        if (imageUrls != null && !imageUrls.isEmpty()) {
            request.setImageUrls(imageUrls);
            log.info("视频任务参考图: count={}, urls={}", imageUrls.size(), imageUrls);
        } else {
            log.warn("视频任务没有参考图! input.referenceImages={}, input.images={}", input.getJSONArray("referenceImages"), input.getJSONArray("images"));
        }
        if (videoUrls != null && !videoUrls.isEmpty()) {
            request.setVideoUrls(videoUrls);
            log.info("视频任务参考视频: count={}, urls={}", videoUrls.size(), videoUrls);
        }

        // 构造参考媒体列表（reference_image / reference_video + 可选 reference_voice），
        // 供适配器按协议取参考图（如 ComfyUI 渠道转 base64 内嵌提交）
        List<NewApiVideoRequest.Media> mediaList = new ArrayList<>();
        if (imageUrls != null) {
            for (String url : imageUrls) {
                NewApiVideoRequest.Media media = new NewApiVideoRequest.Media();
                media.setType(TaskConstants.MediaType.REFERENCE_IMAGE);
                media.setUrl(url);
                if (StrUtil.isNotEmpty(referenceVoice)) {
                    media.setReferenceVoice(referenceVoice);
                }
                mediaList.add(media);
            }
        }
        if (videoUrls != null) {
            for (String url : videoUrls) {
                NewApiVideoRequest.Media media = new NewApiVideoRequest.Media();
                media.setType(TaskConstants.MediaType.REFERENCE_VIDEO);
                media.setUrl(url);
                mediaList.add(media);
            }
        }
        // 首帧图（若有）
        String firstFrame = input.getStr("firstFrame");
        if (StrUtil.isEmpty(firstFrame)) {
            JSONObject configObj = input.getJSONObject("config");
            firstFrame = configObj != null ? configObj.getStr("firstFrame") : null;
        }
        if (StrUtil.isNotEmpty(firstFrame)) {
            NewApiVideoRequest.Media media = new NewApiVideoRequest.Media();
            media.setType(TaskConstants.MediaType.FIRST_FRAME);
            media.setUrl(firstFrame);
            mediaList.add(media);
        }
        if (!mediaList.isEmpty()) {
            NewApiVideoRequest.Input inputObj = new NewApiVideoRequest.Input();
            inputObj.setPrompt(task.getPrompt());
            inputObj.setMedia(mediaList);
            request.setInput(inputObj);
        }

        // 设置生成参数（时长/分辨率/画幅）
        NewApiVideoRequest.Parameters params = new NewApiVideoRequest.Parameters();
        if (config.duration != null) {
            params.setDuration(config.duration);
        }
        if (StrUtil.isNotEmpty(config.resolution)) {
            params.setResolution(config.resolution);
        }
        if (StrUtil.isNotEmpty(config.aspectRatio)) {
            params.setRatio(config.aspectRatio);
        }
        request.setParameters(params);

        if (audioUrls != null && !audioUrls.isEmpty()) {
            request.setAudioUrls(audioUrls);
        }

        return request;
    }

    /**
     * 解析媒体URL列表
     */
    private List<String> parseMediaUrls(Object mediaArray) {
        if (mediaArray == null) {
            return null;
        }

        List<String> urls = new ArrayList<>();

        if (mediaArray instanceof List) {
            for (Object item : (List<?>) mediaArray) {
                if (item instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) item;
                    Object url = map.get("url");
                    if (url != null) {
                        urls.add(url.toString());
                    }
                } else if (item instanceof String) {
                    urls.add((String) item);
                }
            }
        }

        return urls.isEmpty() ? null : urls;
    }

    /**
     * 更新任务阶段（仅活跃状态：避免任务被取消后 stage/progress 被覆盖）
     */
    private void updateTaskStage(String taskId, String stage, int progress) {
        UpdateWrapper<ZyGenerationTask> updateWrapper = new UpdateWrapper<ZyGenerationTask>()
                .eq("id", taskId)
                .in("status", TaskConstants.Status.QUEUED, TaskConstants.Status.RUNNING, TaskConstants.Status.PROCESSING)
                .set("stage", stage)
                .set("progress", progress);

        zyGenerationTaskMapper.update(null, updateWrapper);
    }

    /**
     * 视频配置参数中间对象：把前端/旧协议字段名差异收敛到一处，
     * 供协议字段与 parameters 复用，避免重复解析。
     */
    private static class VideoConfig {
        Integer duration;
        String aspectRatio;
        String resolution;
        Boolean generateAudio;
    }

    /**
     * 解析 config 配置（兼容前端 backendProviderConfig 字段名）
     */
    private VideoConfig parseConfig(JSONObject input) {
        VideoConfig config = new VideoConfig();
        JSONObject configObj = input.getJSONObject("config");
        if (configObj == null) {
            return config;
        }

        // 时长：前端写 videoSeconds，旧协议写 duration
        config.duration = configObj.getInt("videoSeconds");
        if (config.duration == null) {
            config.duration = configObj.getInt("duration");
        }

        // 宽高比：前端写 size，旧协议写 aspectRatio
        config.aspectRatio = configObj.getStr("size");
        if (StrUtil.isEmpty(config.aspectRatio)) {
            config.aspectRatio = configObj.getStr("aspectRatio");
        }

        // 分辨率：前端写 vquality，旧协议写 resolution
        config.resolution = configObj.getStr("vquality");
        if (StrUtil.isEmpty(config.resolution)) {
            config.resolution = configObj.getStr("resolution");
        }

        // 是否生成音频
        config.generateAudio = configObj.getBool("videoGenerateAudio");
        if (config.generateAudio == null) {
            config.generateAudio = configObj.getBool("generateAudio");
        }

        return config;
    }
}
