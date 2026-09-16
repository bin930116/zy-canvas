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
package vip.xiaonuo.canvas.zyapi.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 短剧项目资产保存参数
 *
 * @author xuyuxiang
 * @date 2026/9/5 21:30
 **/
@Getter
@Setter
public class ZyApiAssetParam {

    /** 主键(为空则新建) */
    @Schema(description = "主键")
    private String id;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 介质类型 */
    @Schema(description = "介质类型：image|video|audio")
    private String mediaType;

    /** 分类 */
    @Schema(description = "分类")
    private String category;

    /** 所属文件夹id */
    @Schema(description = "所属文件夹id")
    private String folderId;

    /** 状态 */
    @Schema(description = "状态")
    private String status;

    /** 存储key */
    @Schema(description = "存储key")
    private String storageKey;

    /** 时长(毫秒) */
    @Schema(description = "时长(毫秒)")
    private Long durationMs;

    /** 预览文本 */
    @Schema(description = "预览文本")
    private String previewText;

    /** 来源 */
    @Schema(description = "来源")
    private String source;

    /** 排序 */
    @Schema(description = "排序")
    private Integer position;
}
