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
package vip.xiaonuo.canvas.modular.shot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 短剧项目分镜实体
 *
 * @author hanbin
 * @date  2026/09/07 18:55
 **/
@Getter
@Setter
@TableName("zy_shot")
public class ZyShot {

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

    /** 所属章节 */
    @Schema(description = "所属章节")
    private String unitId;

    /** 当前版本 */
    @Schema(description = "当前版本")
    private String currentRevisionId;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 描述 */
    @Schema(description = "描述")
    private String description;

    /** 排序 */
    @Schema(description = "排序")
    private Integer position;

    /** 时长(毫秒) */
    @Schema(description = "时长(毫秒)")
    private Integer durationMs;

    /** 状态 */
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
