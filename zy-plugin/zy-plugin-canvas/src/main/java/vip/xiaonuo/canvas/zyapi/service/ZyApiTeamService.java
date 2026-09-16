package vip.xiaonuo.canvas.zyapi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import vip.xiaonuo.canvas.modular.team.entity.ZyTeam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiTeamParam;

import java.util.Map;

public interface ZyApiTeamService {

    Page<ZyTeam> listPage(String userId, Integer page, Integer pageSize);

    ZyTeam create(String userId, ZyApiTeamParam param);

    ZyTeam getById(String userId, String id);

    ZyTeam update(String userId, String id, ZyApiTeamParam param);

    void delete(String userId, String id);

    Map<String, Object> listMembers(String teamId);

    void addMember(String teamId, String userId, String role, String invitedBy);

    void removeMember(String teamId, String userId);

    void updateMemberRole(String teamId, String userId, String role);

    boolean isMember(String teamId, String userId);

    /**
     * 查询用户在团队中的角色(owner/admin/member)，不在团队中返回null
     *
     * @param teamId 团队id
     * @param userId 用户id
     * @return 角色或null
     */
    String getMemberRole(String teamId, String userId);

    Map<String, Object> searchUsers(String keyword);
}
