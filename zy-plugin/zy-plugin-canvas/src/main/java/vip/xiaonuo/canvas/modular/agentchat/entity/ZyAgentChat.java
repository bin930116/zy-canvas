package vip.xiaonuo.canvas.modular.agentchat.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * Agent对话会话实体
 *
 * @author xuyuxiang
 * @date 2026/9/14
 **/
@Getter
@Setter
@TableName("zy_agent_chat")
public class ZyAgentChat {

    /** 主键 */
    @TableId
    @Schema(description = "主键")
    private String id;

    /** 删除标志 */
    @Schema(description = "删除标志")
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private String deleteFlag;

    /** 用户 */
    @Schema(description = "用户")
    private String userId;

    /** 画布ID */
    @Schema(description = "画布ID")
    private String canvasId;

    /** 会话标题 */
    @Schema(description = "会话标题")
    private String title;

    /** 消息列表(JSON) */
    @Schema(description = "消息列表(JSON)")
    private String messagesJson;

    /** 状态: active/archived */
    @Schema(description = "状态")
    private String status;

    /** 创建人 */
    @Schema(description = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    /** 创建时间 */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新人 */
    @Schema(description = "更新人")
    @TableField(fill = FieldFill.UPDATE)
    private String updateUser;

    /** 更新时间 */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;
}
