/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Snowy源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://www.xiaonuo.vip
 * 5.不可二次分发开源参与同类竞品，如有想法可联系团队xiaonuobase@qq.com商议合作。
 * 6.若您的项目无法满足以上几点，需要更多功能代码，获取Snowy商业授权许可，请在官网购买授权，地址为 https://www.xiaonuo.vip
 */
package vip.xiaonuo.canvas.modular.shotrevision.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 短剧项目分镜版本实体
 *
 * @author hanbin
 * @date  2026/09/07 18:51
 **/
@Getter
@Setter
@TableName("zy_shot_revision")
public class ZyShotRevision {

    /** 主键 */
    @TableId
    @Schema(description = "主键")
    private String id;

    /** 分镜id */
    @Schema(description = "分镜id")
    private String shotId;

    /** 版本号 */
    @Schema(description = "版本号")
    private Integer version;

    /** 剧情描述 */
    @Schema(description = "剧情描述")
    private String plotDescription;

    /** 动作 */
    @Schema(description = "动作")
    private String action;

    /** 台词 */
    @Schema(description = "台词")
    private String dialogue;

    /** 景别 */
    @Schema(description = "景别")
    private String shotSize;

    /** 机位角度 */
    @Schema(description = "机位角度")
    private String cameraAngle;

    /** 删除标志 */
    @Schema(description = "删除标志")
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private String deleteFlag;

    /** 运镜 */
    @Schema(description = "运镜")
    private String cameraMovement;

    /** 时长(毫秒) */
    @Schema(description = "时长(毫秒)")
    private Integer durationMs;

    /** 图片提示词 */
    @Schema(description = "图片提示词")
    private String imagePrompt;

    /** 视频提示词 */
    @Schema(description = "视频提示词")
    private String videoPrompt;

    /** 负面提示词 */
    @Schema(description = "负面提示词")
    private String negativePrompt;

    /** 连续性说明 */
    @Schema(description = "连续性说明")
    private String continuityNotes;

    /** 动作节拍JSON */
    @Schema(description = "动作节拍JSON")
    private String actionBeatsJson;

    /** 创建人 */
    @Schema(description = "创建人")
    private String createdBy;

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
