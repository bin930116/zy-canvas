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
package vip.xiaonuo.canvas.modular.model.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 模型配置添加参数
 *
 * @author hanbin
 * @date  2026/09/07 18:27
 **/
@Getter
@Setter
public class ZyModelAddParam {

    /** 模型标识 */
    @Schema(description = "模型标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "modelKey不能为空")
    private String modelKey;

    /** 模型名称 */
    @Schema(description = "模型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "modelName不能为空")
    private String modelName;

    /** 提供商 */
    @Schema(description = "提供商")
    private String providerName;

    /** 能力类型 */
    @Schema(description = "能力类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "capability不能为空")
    private String capability;

    /** 协议类型 */
    @Schema(description = "协议类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "protocol不能为空")
    private String protocol;

    /** baseUrl */
    @Schema(description = "baseUrl", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "baseUrl不能为空")
    private String baseUrl;

    /** API密钥 */
    @Schema(description = "API密钥", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "apiKey不能为空")
    private String apiKey;

    /** 并发数 */
    @Schema(description = "并发数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "maxConcurrency不能为空")
    private Integer maxConcurrency;

    /** 超时时间（秒） */
    @Schema(description = "超时时间（秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "timeoutSeconds不能为空")
    private Integer timeoutSeconds;

    /** 能力规格JSON */
    @Schema(description = "能力规格JSON")
    private String capabilitySpecJson;

    /** 状态 */
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "status不能为空")
    private String status;

    /** 是否默认 */
    @Schema(description = "是否默认", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "isDefault不能为空")
    private String isDefault;

    /** 排序码 */
    @Schema(description = "排序码")
    private Integer sortCode;

}
