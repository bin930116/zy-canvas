package vip.xiaonuo.canvas.zyapi.client.request;

import lombok.Data;

import java.util.List;

/**
 * NewAPI图片生成请求
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Data
public class NewApiImageRequest {

    /** 模型标识 */
    private String model;

    /** 提示词 */
    private String prompt;

    /** 图片尺寸 */
    private String size;

    /** 图片质量 */
    private String quality;

    /** 生成数量 */
    private Integer n;

    /** 参考图片URL（图生图） */
    private String image;

    /** 参考图片URL列表 */
    private List<String> imageUrls;

    /** 负面提示词 */
    private String negativePrompt;

    /** 种子值 */
    private Long seed;

    /** 引导系数 */
    private Double guidanceScale;

    /** 推理步数 */
    private Integer numInferenceSteps;
}