package vip.xiaonuo.canvas.zyapi.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZyApiTeamParam {

    @NotBlank(message = "团队名称不能为空")
    @Schema(description = "团队名称")
    private String name;

    @Schema(description = "团队描述")
    private String description;

    @Schema(description = "团队头像URL")
    private String avatarUrl;
}
