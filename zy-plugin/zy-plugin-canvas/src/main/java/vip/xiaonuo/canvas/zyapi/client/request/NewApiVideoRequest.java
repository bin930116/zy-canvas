package vip.xiaonuo.canvas.zyapi.client.request;

import lombok.Data;

import java.util.List;

/**
 * NewAPI视频生成请求
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Data
public class NewApiVideoRequest {

    /** 模型标识 */
    private String model;

    /** 提示词 */
    private String prompt;

    // ============ newapi-channel-2 协议字段 ============

    /** 时长（秒） */
    private String seconds;

    /** 宽高比 */
    private String aspectRatio;

    /** 分辨率 */
    private String resolution;

    /** 是否生成音频 */
    private Boolean generateAudio;

    /** 参考图片URL列表 */
    private List<String> imageUrls;

    /** 参考视频URL列表 */
    private List<String> videoUrls;

    /** 参考音频URL列表 */
    private List<String> audioUrls;

    // ============ newapi-channel-1 协议字段 ============

    /** 输入参数 */
    private Input input;

    /** 参数配置 */
    private Parameters parameters;

    @Data
    public static class Input {
        private String prompt;
        private List<Media> media;
    }

    @Data
    public static class Media {
        /** 类型：first_frame, last_frame, reference_image, reference_video */
        private String type;
        private String url;
        /** 该主体的音色参考 URL（WAV/MP3），可与 reference_image / reference_video 同用 */
        private String referenceVoice;
    }

    @Data
    public static class Parameters {
        /** 分辨率：480P, 720P, 1080P */
        private String resolution;
        /** 宽高比：16:9, 9:16, 1:1 */
        private String ratio;
        /** 时长 */
        private Integer duration;
        /** 是否带水印 */
        private Boolean watermark;
    }
}