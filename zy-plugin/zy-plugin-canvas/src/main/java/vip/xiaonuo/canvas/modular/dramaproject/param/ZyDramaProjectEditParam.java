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
package vip.xiaonuo.canvas.modular.dramaproject.param;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 短剧项目编辑参数
 *
 * @author hanbin
 * @date  2026/09/08 14:26
 **/
@Getter
@Setter
public class ZyDramaProjectEditParam {

    /** 主键 */
    @ExcelProperty("主键")
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "id不能为空")
    private String id;

    /** 用户 */
    @ExcelProperty("用户")
    @Schema(description = "用户")
    private String userId;

    /** 名称 */
    @ExcelProperty("名称")
    @Schema(description = "名称")
    private String name;

    /** 类型 */
    @ExcelProperty("类型")
    @Schema(description = "类型")
    private String type;

    /** 画幅比 */
    @ExcelProperty("画幅比")
    @Schema(description = "画幅比")
    private String aspectRatio;

    /** 来源类型 */
    @ExcelProperty("来源类型")
    @Schema(description = "来源类型")
    private String sourceType;

    /** 描述 */
    @ExcelProperty("描述")
    @Schema(description = "描述")
    private String description;

    /** 封面资源id */
    @ExcelProperty("封面资源id")
    @Schema(description = "封面资源id")
    private String coverResourceId;

    /** 风格预设id */
    @ExcelProperty("风格预设id")
    @Schema(description = "风格预设id")
    private String stylePresetId;

    /** 风格配置JSON */
    @ExcelProperty("风格配置JSON")
    @Schema(description = "风格配置JSON")
    private String styleProfileJson;

    /** 默认图片模型 */
    @ExcelProperty("默认图片模型")
    @Schema(description = "默认图片模型")
    private String defaultImageModel;

    /** 默认视频模型 */
    @ExcelProperty("默认视频模型")
    @Schema(description = "默认视频模型")
    private String defaultVideoModel;

    /** 状态 */
    @ExcelProperty("状态")
    @Schema(description = "状态")
    private String status;

    /** 乐观锁版本号 */
    @ExcelProperty("乐观锁版本号")
    @Schema(description = "乐观锁版本号")
    private Integer revision;

}
