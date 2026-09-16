package vip.xiaonuo.canvas.zyapi.client.response;

import lombok.Data;

/**
 * NewAPI任务创建响应
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Data
public class NewApiTaskResponse {

    /** 任务ID */
    private String id;

    /** 任务ID（别名） */
    private String taskId;

    /** 状态 */
    private String status;

    /** 错误信息 */
    private String error;

    /** 错误消息 */
    private String message;

    /**
     * 获取任务ID（兼容不同协议）
     */
    public String getTaskIdValue() {
        if (id != null) {
            return id;
        }
        return taskId;
    }
}