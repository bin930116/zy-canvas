package vip.xiaonuo.canvas.modular.shotartifact.entity;

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
 * 分镜产物实体
 *
 * @author hanbin
 * @date 2026/09/09 17:20
 **/
@Getter
@Setter
@TableName("zy_shot_artifact")
public class ZyShotArtifact {

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

    /** 分镜 */
    @Schema(description = "分镜")
    private String shotId;

    /** 分镜版本 */
    @Schema(description = "分镜版本")
    private String revisionId;

    /** 生成任务 */
    @Schema(description = "生成任务")
    private String taskId;

    /** 产物类型 */
    @Schema(description = "产物类型")
    private String type;

    /** 版本号 */
    @Schema(description = "版本号")
    private Integer version;

    /** 资源id */
    @Schema(description = "资源id")
    private String resourceId;

    /** 图直链 */
    @Schema(description = "图直链")
    private String url;

    /** 媒体类型 */
    @Schema(description = "媒体类型")
    private String mediaType;

    /** 状态 */
    @Schema(description = "状态")
    private String status;

    /** 是否当前选中 */
    @Schema(description = "是否当前选中")
    private Boolean selected;

    /** 元数据json */
    @Schema(description = "元数据json")
    private String metadataJson;

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
