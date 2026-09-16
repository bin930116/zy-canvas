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
package vip.xiaonuo.canvas.modular.asset.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 短剧项目资产添加参数
 *
 * @author hanbin
 * @date  2026/09/08 14:46
 **/
@Getter
@Setter
public class ZyAssetAddParam {

    /** 项目 */
    @Schema(description = "项目")
    private String projectId;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 介质类型 */
    @Schema(description = "介质类型")
    private String mediaType;

    /** 分类 */
    @Schema(description = "分类")
    private String category;

    /** 状态 */
    @Schema(description = "状态")
    private String status;

    /** 主版本 */
    @Schema(description = "主版本")
    private String primaryVersionId;

    /** 版本数 */
    @Schema(description = "版本数")
    private Integer versionCount;

    /** 用途 */
    @Schema(description = "用途")
    private String usages;

    /** 所属文件夹id */
    @Schema(description = "所属文件夹id")
    private String folderId;

    /** 排序 */
    @Schema(description = "排序")
    private Integer position;

    /** 存储key */
    @Schema(description = "存储key")
    private String storageKey;

    /** 时长(毫秒) */
    @Schema(description = "时长(毫秒)")
    private Integer durationMs;

    /** 预览文本 */
    @Schema(description = "预览文本")
    private String previewText;

    /** 关联角色id */
    @Schema(description = "关联角色id")
    private String characterId;

    /** 来源 */
    @Schema(description = "来源")
    private String source;

    /** 扩展JSON */
    @Schema(description = "扩展JSON")
    private String extJson;

    /** 封面图片URL */
    @Schema(description = "封面图片URL")
    private String coverUrl;

    /** 标签(JSON数组) */
    @Schema(description = "标签(JSON数组)")
    private String tags;

    /** 备注信息 */
    @Schema(description = "备注信息")
    private String note;

}
