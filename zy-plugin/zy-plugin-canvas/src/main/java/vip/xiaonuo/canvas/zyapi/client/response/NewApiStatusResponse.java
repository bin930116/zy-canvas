package vip.xiaonuo.canvas.zyapi.client.response;

import lombok.Data;

/**
 * NewAPI任务状态响应
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Data
public class NewApiStatusResponse {

    /** 任务ID */
    private String id;

    /** 状态：SUCCESS, FAILURE, SUBMITTED, QUEUED, IN_PROGRESS, pending, processing, completed, failed, cancelled */
    private String status;

    /** 结果URL（视频/图片下载地址） */
    private String resultUrl;

    /** 输出URL */
    private String outputUrl;

    /** 视频URL */
    private String videoUrl;

    /** 图片URL */
    private String imageUrl;

    /** 图片列表 */
    private String[] images;

    /** 海报URL */
    private String posterUrl;

    /** NewAPI 统一协议顶层视频地址字段 */
    private String url;

    /** 失败原因 */
    private String failReason;

    /** 错误信息 */
    private String error;

    /** 错误消息 */
    private String message;

    /** 进度（0-100） */
    private Integer progress;

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(status) || "SUCCEEDED".equalsIgnoreCase(status)
                || "completed".equalsIgnoreCase(status);
    }

    /**
     * 判断是否失败
     */
    public boolean isFailed() {
        return "FAILURE".equalsIgnoreCase(status) || "failed".equalsIgnoreCase(status);
    }

    /**
     * 判断是否取消
     */
    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(status);
    }

    /**
     * 判断是否还在处理中
     */
    public boolean isPending() {
        return !isSuccess() && !isFailed() && !isCancelled();
    }

    /**
     * 获取结果URL（兼容不同协议）
     */
    public String getResultUrlValue() {
        if (resultUrl != null) {
            return resultUrl;
        }
        if (outputUrl != null) {
            return outputUrl;
        }
        if (videoUrl != null) {
            return videoUrl;
        }
        if (url != null) {
            return url;
        }
        if (imageUrl != null) {
            return imageUrl;
        }
        if (images != null && images.length > 0) {
            return images[0];
        }
        return null;
    }

    /**
     * 获取错误信息
     */
    public String getErrorMessage() {
        if (failReason != null) {
            return failReason;
        }
        if (error != null) {
            return error;
        }
        return message;
    }
}