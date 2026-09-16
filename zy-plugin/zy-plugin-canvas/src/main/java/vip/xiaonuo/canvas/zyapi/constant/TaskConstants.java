package vip.xiaonuo.canvas.zyapi.constant;

/**
 * 任务调度通用常量
 * <p>
 * 收敛散落在任务调度链路中的魔法字符串：
 * 任务状态、任务/能力类型、参考媒体类型与下游媒资大类。
 *
 * @author hanbin
 * @date 2026/09/15
 **/
public final class TaskConstants {

    private TaskConstants() {
    }

    /**
     * 任务状态（zy_generation_task.status）
     */
    public static final class Status {

        /** 排队中 */
        public static final String QUEUED = "queued";

        /** 执行中（Worker 已领取） */
        public static final String RUNNING = "running";

        /** 已提交上游、等待生成（非终态，用于视频等异步任务） */
        public static final String PROCESSING = "processing";

        /** 成功（终态） */
        public static final String SUCCEEDED = "succeeded";

        /** 失败（终态） */
        public static final String FAILED = "failed";

        /** 已取消（终态） */
        public static final String CANCELLED = "cancelled";

        private Status() {
        }
    }

    /**
     * 任务/能力类型（zy_generation_task.type，兼容 canvas_ 前缀）
     */
    public static final class Type {

        /** 视频生成 */
        public static final String VIDEO = "video";

        /** 图片生成 */
        public static final String IMAGE = "image";

        /** 音频生成 */
        public static final String AUDIO = "audio";

        /** 文本生成 */
        public static final String TEXT = "text";

        /** 画布视频生成 */
        public static final String CANVAS_VIDEO = "canvas_video";

        /** 画布图片生成 */
        public static final String CANVAS_IMAGE = "canvas_image";

        /** 画布音频生成 */
        public static final String CANVAS_AUDIO = "canvas_audio";

        /** 画布文本生成 */
        public static final String CANVAS_TEXT = "canvas_text";

        private Type() {
        }
    }

    /**
     * 参考媒体类型（input.media[].type）
     */
    public static final class MediaType {

        /** 参考图 */
        public static final String REFERENCE_IMAGE = "reference_image";

        /** 参考视频 */
        public static final String REFERENCE_VIDEO = "reference_video";

        /** 首帧图 */
        public static final String FIRST_FRAME = "first_frame";

        /** 末帧图 */
        public static final String LAST_FRAME = "last_frame";

        private MediaType() {
        }
    }

    /**
     * 下游媒资大类（zy_asset.media_type / zy_shot_artifact.media_type）
     */
    public static final class MediaKind {

        /** 视频 */
        public static final String VIDEO = "video";

        /** 图片 */
        public static final String IMAGE = "image";

        /** 音频 */
        public static final String AUDIO = "audio";

        private MediaKind() {
        }
    }
}
