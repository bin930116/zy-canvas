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
package vip.xiaonuo.canvas.modular.teammember.param;

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
 * 团队成员关系表编辑参数
 *
 * @author hanbin
 * @date  2026/09/07 18:44
 **/
@Getter
@Setter
public class ZyTeamMemberEditParam {

    /** 主键ID */
    @ExcelProperty("主键ID")
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "id不能为空")
    private String id;

    /** 团队 */
    @ExcelProperty("团队")
    @Schema(description = "团队", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "teamId不能为空")
    private String teamId;

    /** 成员 */
    @ExcelProperty("成员")
    @Schema(description = "成员", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "userId不能为空")
    private String userId;

    /** 成员角色 */
    @ExcelProperty("成员角色")
    @Schema(description = "成员角色", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "role不能为空")
    private String role;

    /** 状态 */
    @ExcelProperty("状态")
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "status不能为空")
    private String status;

    /** 邀请人 */
    @ExcelProperty("邀请人")
    @Schema(description = "邀请人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "invitedBy不能为空")
    private String invitedBy;

}
