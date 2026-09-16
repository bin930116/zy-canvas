/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议
 */
package vip.xiaonuo.canvas.modular.skill.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * 技能库实体
 *
 * @author hanbin
 * @date  2026/09/11
 **/
@Getter
@Setter
@TableName("zy_skill")
public class ZySkill {

    /** 主键 */
    @TableId
    @Schema(description = "主键")
    private String id;

    /** 技能ID */
    @Schema(description = "技能ID")
    private String skillId;

    /** 技能名称 */
    @Schema(description = "技能名称")
    private String skillName;

    /** 技能描述 */
    @Schema(description = "技能描述")
    private String description;

    /** 技能指令 */
    @Schema(description = "技能指令")
    private String instruction;

    /** 分类 */
    @Schema(description = "分类")
    private String tag;

    /** 排序权重 */
    @Schema(description = "排序权重")
    private Integer sortWeight;

    /** 状态 */
    @Schema(description = "状态")
    private Integer status;

    /** 是否私有 */
    @Schema(description = "是否私有")
    private Integer isPrivate;

    /** 是否测试 */
    @Schema(description = "是否测试")
    private Integer isTest;

    /** 点赞数 */
    @Schema(description = "点赞数")
    private Integer likeCount;

    /** 被添加数 */
    @Schema(description = "被添加数")
    private Integer addedCount;

    /** 所有者UID */
    @Schema(description = "所有者UID")
    private String ownerUid;

    /** 作者信息JSON */
    @Schema(description = "作者信息JSON")
    private String effectiveUser;

    /** 展示媒体JSON */
    @Schema(description = "展示媒体JSON")
    private String showcaseMedia;

    /** 来源类型 */
    @Schema(description = "来源类型")
    private String sourceType;

    /** 来源地址 */
    @Schema(description = "来源地址")
    private String sourceUrl;

    /** markdown地址 */
    @Schema(description = "markdown地址")
    private String markdownUrl;

    /** 额外信息 */
    @Schema(description = "额外信息")
    private String extraInfo;

    /** 版本号 */
    @Schema(description = "版本号")
    private String version;

    /** 版本ID */
    @Schema(description = "版本ID")
    private String versionId;

    /** 内容哈希 */
    @Schema(description = "内容哈希")
    private String contentHash;

    /** 文件数 */
    @Schema(description = "文件数")
    private Integer fileCount;

    /** 总字节数 */
    @Schema(description = "总字节数")
    private Long totalBytes;

    /** 原技能ID */
    @Schema(description = "原技能ID")
    private String originalSkillId;

    /** 来源 */
    @Schema(description = "来源")
    private Integer source;

    /** 同步状态 */
    @Schema(description = "同步状态")
    private String syncStatus;

    /** 自动更新 */
    @Schema(description = "自动更新")
    private Integer autoUpdate;

    /** 最后检查时间 */
    @Schema(description = "最后检查时间")
    private Long lastCheckedAt;

    /** 最后同步时间 */
    @Schema(description = "最后同步时间")
    private Long lastSyncedAt;

    /** 删除标志 */
    @Schema(description = "删除标志")
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private String deleteFlag;

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
