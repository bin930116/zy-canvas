package vip.xiaonuo.canvas.modular.workflow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 项目工作流实例实体
 *
 * @author hanbin
 * @date 2026/09/09 16:50
 **/
@Getter
@Setter
@TableName("zy_workflow")
public class ZyWorkflow {

    /** 主键 */
    @TableId
    @Schema(description = "主键")
    private String id;

    /** 删除标志 */
    @Schema(description = "删除标志")
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private String deleteFlag;

    /** 项目 */
    @Schema(description = "项目")
    private String projectId;

    /** 章节 */
    @Schema(description = "章节")
    private String unitId;

    /** 范围 */
    @Schema(description = "范围")
    private String scope;

    /** 状态 */
    @Schema(description = "状态")
    private String status;

    /** 版本 */
    @Schema(description = "版本")
    private Integer revision;

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
