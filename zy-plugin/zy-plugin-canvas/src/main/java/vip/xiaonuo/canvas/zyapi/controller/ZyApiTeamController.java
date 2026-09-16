package vip.xiaonuo.canvas.zyapi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.team.entity.ZyTeam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiTeamMemberParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiTeamParam;
import vip.xiaonuo.canvas.zyapi.service.ZyApiTeamService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.Map;

@Tag(name = "团队管理")
@RestController
@RequestMapping("/teams")
public class ZyApiTeamController {

    @Resource
    private ZyApiTeamService zyTeamService;

    @Operation(summary = "团队列表")
    @GetMapping
    public CommonResult<Page<ZyTeam>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyTeamService.listPage(userId, page, pageSize));
    }

    @Operation(summary = "创建团队")
    @PostMapping
    public CommonResult<ZyTeam> create(@RequestBody ZyApiTeamParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyTeamService.create(userId, param));
    }

    @Operation(summary = "团队详情")
    @GetMapping("/{id}")
    public CommonResult<ZyTeam> detail(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyTeamService.getById(userId, id));
    }

    @Operation(summary = "更新团队")
    @PatchMapping("/{id}")
    public CommonResult<ZyTeam> update(@PathVariable String id, @RequestBody ZyApiTeamParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyTeamService.update(userId, id, param));
    }

    @Operation(summary = "删除团队")
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        zyTeamService.delete(userId, id);
        return CommonResult.ok();
    }

    @Operation(summary = "成员列表")
    @GetMapping("/{id}/members")
    public CommonResult<Map<String, Object>> listMembers(@PathVariable String id) {
        return CommonResult.data(zyTeamService.listMembers(id));
    }

    @Operation(summary = "邀请成员")
    @PostMapping("/{id}/members")
    public CommonResult<Void> addMember(@PathVariable String id, @RequestBody ZyApiTeamMemberParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        zyTeamService.addMember(id, param.getUserId(), param.getRole(), userId);
        return CommonResult.ok();
    }

    @Operation(summary = "修改成员角色")
    @PatchMapping("/{id}/members/{memberId}")
    public CommonResult<Void> updateMemberRole(@PathVariable String id, @PathVariable String memberId,
                                                @RequestBody ZyApiTeamMemberParam param) {
        zyTeamService.updateMemberRole(id, memberId, param.getRole());
        return CommonResult.ok();
    }

    @Operation(summary = "移除成员")
    @DeleteMapping("/{id}/members/{memberId}")
    public CommonResult<Void> removeMember(@PathVariable String id, @PathVariable String memberId) {
        zyTeamService.removeMember(id, memberId);
        return CommonResult.ok();
    }

    @Operation(summary = "搜索用户（按账号）")
    @GetMapping("/search-users")
    public CommonResult<Map<String, Object>> searchUsers(@RequestParam String keyword) {
        return CommonResult.data(zyTeamService.searchUsers(keyword));
    }
}
