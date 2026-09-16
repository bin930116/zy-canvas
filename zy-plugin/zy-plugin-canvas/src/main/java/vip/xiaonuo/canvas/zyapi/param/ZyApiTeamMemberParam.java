package vip.xiaonuo.canvas.zyapi.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZyApiTeamMemberParam {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "角色：admin/member")
    private String role;
}
