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
package vip.xiaonuo.canvas.modular.generationtask.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 任务队列信息实体
 *
 * @author hanbin
 * @date  2026/09/08 14:24
 **/
@Getter
@Setter
@TableName("zy_generation_task")
public class ZyGenerationTask {

    /** 主键 */
    @TableId
    @Schema(description = "主键")
    private String id;

    /** 用户 */
    @Schema(description = "用户")
    private String userId;

    /** 项目 */
    @Schema(description = "项目")
    private String projectId;

    /** 任务类型 */
    @Schema(description = "任务类型")
    private String type;

    /** 进度 */
    @Schema(description = "进度")
    private Integer progress;

    /** 删除标志 */
    @Schema(description = "删除标志")
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private String deleteFlag;

    /** 状态 */
    @Schema(description = "状态")
    private String status;

    /** 阶段 */
    @Schema(description = "阶段")
    private String stage;

    /** 提示词 */
    @Schema(description = "提示词")
    private String prompt;

    /** 操作 */
    @Schema(description = "操作")
    private String operation;

    /** 提供商 */
    @Schema(description = "提供商")
    private String provider;

    /** 模型 */
    @Schema(description = "模型")
    private String model;

    /** 提供商请求id */
    @Schema(description = "提供商请求id")
    private String providerRequestId;

    /** 提供商取消状态 */
    @Schema(description = "提供商取消状态")
    private String providerCancelStatus;

    /** 提供商取消错误 */
    @Schema(description = "提供商取消错误")
    private String providerCancelError;

    /** 提供商取消尝试次数 */
    @Schema(description = "提供商取消尝试次数")
    private Integer providerCancelAttempts;

    /** 提供商取消请求时间 */
    @Schema(description = "提供商取消请求时间")
    private Date providerCancelRequestedAt;

    /** 提供商取消时间 */
    @Schema(description = "提供商取消时间")
    private Date providerCancelledAt;

    /** 错误码 */
    @Schema(description = "错误码")
    private String errorCode;

    /** 官方状态 */
    @Schema(description = "官方状态")
    private String officialStatus;

    /** 预览URL */
    @Schema(description = "预览URL")
    private String previewUrl;

    /** 预览类型 */
    @Schema(description = "预览类型")
    private String previewKind;

    /** 预览海报URL */
    @Schema(description = "预览海报URL")
    private String previewPosterUrl;

    /** 输入JSON */
    @Schema(description = "输入JSON")
    private String inputJson;

    /** 结果JSON */
    @Schema(description = "结果JSON")
    private String resultJson;

    /** 结果状态 */
    @Schema(description = "结果状态")
    private String resultState;

    /** 文本草稿 */
    @Schema(description = "文本草稿")
    private String textDraft;

    /** 错误信息 */
    @Schema(description = "错误信息")
    private String error;

    /** 尝试次数 */
    @Schema(description = "尝试次数")
    private Integer attempts;

    /** 开始时间 */
    @Schema(description = "开始时间")
    private Date startedAt;

    /** 完成时间 */
    @Schema(description = "完成时间")
    private Date completedAt;

    /** 客户端上下文JSON */
    @Schema(description = "客户端上下文JSON")
    private String clientContextJson;

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

    /** 处理该任务的Worker ID */
    @Schema(description = "处理该任务的Worker ID")
    private String workerId;

    /** 任务租约过期时间 */
    @Schema(description = "任务租约过期时间")
    private Date leaseExpiresAt;

    /** 已重试次数 */
    @Schema(description = "已重试次数")
    private Integer retryCount;

    /** 最大重试次数 */
    @Schema(description = "最大重试次数")
    private Integer maxRetries;
}
