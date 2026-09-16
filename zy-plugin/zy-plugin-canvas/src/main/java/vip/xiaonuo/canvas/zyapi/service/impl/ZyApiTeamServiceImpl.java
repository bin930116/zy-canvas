package vip.xiaonuo.canvas.zyapi.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import cn.hutool.json.JSONObject;
import vip.xiaonuo.canvas.modular.team.entity.ZyTeam;
import vip.xiaonuo.canvas.modular.team.mapper.ZyTeamMapper;
import vip.xiaonuo.canvas.modular.teammember.entity.ZyTeamMember;
import vip.xiaonuo.canvas.modular.teammember.mapper.ZyTeamMemberMapper;
import vip.xiaonuo.canvas.zyapi.param.ZyApiTeamParam;
import vip.xiaonuo.canvas.zyapi.service.ZyApiTeamService;
import vip.xiaonuo.client.ClientUserApi;
import vip.xiaonuo.common.exception.CommonException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ZyApiTeamServiceImpl extends ServiceImpl<ZyTeamMapper, ZyTeam> implements ZyApiTeamService {

    @Resource
    private ZyTeamMapper zyTeamMapper;

    @Resource
    private ZyTeamMemberMapper zyTeamMemberMapper;

    @Resource
    private ClientUserApi clientUserApi;

    @Override
    public Page<ZyTeam> listPage(String userId, Integer page, Integer pageSize) {
        QueryWrapper<ZyTeam> queryWrapper = new QueryWrapper<ZyTeam>()
                .and(w -> w
                    .eq("create_user", userId)
                    .or()
                    .inSql("id", "SELECT team_id FROM zy_team_member WHERE user_id = '" + userId + "' AND status = 'active'")
                )
                .orderByDesc("update_time");
        if (ObjectUtil.isNotEmpty(page) && ObjectUtil.isNotEmpty(pageSize)) {
            return this.page(new Page<>(page, pageSize), queryWrapper);
        }
        return this.page(new Page<>(1, 1000, false), queryWrapper);
    }

    @Override
    public ZyTeam create(String userId, ZyApiTeamParam param) {
        ZyTeam entity = new ZyTeam();
        entity.setId(IdUtil.fastSimpleUUID());
        entity.setName(param.getName());
        entity.setDescription(param.getDescription());
        entity.setAvatarUrl(param.getAvatarUrl());
        entity.setStatus("active");
        zyTeamMapper.insert(entity);
        // 自动将创建者加入团队（owner）
        addMember(entity.getId(), userId, "owner", null);
        return entity;
    }

    @Override
    public ZyTeam getById(String userId, String id) {
        ZyTeam team = zyTeamMapper.selectById(id);
        if (team == null) {
            return null;
        }
        if (userId.equals(team.getCreateUser()) || isMember(id, userId)) {
            return team;
        }
        return null;
    }

    @Override
    public ZyTeam update(String userId, String id, ZyApiTeamParam param) {
        ZyTeam entity = this.getById(userId, id);
        if (ObjectUtil.isEmpty(entity)) {
            return null;
        }
        if (param.getName() != null) entity.setName(param.getName());
        if (param.getDescription() != null) entity.setDescription(param.getDescription());
        if (param.getAvatarUrl() != null) entity.setAvatarUrl(param.getAvatarUrl());
        zyTeamMapper.updateById(entity);
        return entity;
    }

    @Override
    public void delete(String userId, String id) {
        ZyTeam team = zyTeamMapper.selectById(id);
        if (team == null || !userId.equals(team.getCreateUser())) {
            throw new CommonException("无权删除该团队");
        }
        zyTeamMapper.deleteById(id);
        zyTeamMemberMapper.delete(new QueryWrapper<ZyTeamMember>().eq("team_id", id));
    }

    @Override
    public Map<String, Object> listMembers(String teamId) {
        // 只查询活跃状态的团队成员
        List<ZyTeamMember> members = zyTeamMemberMapper.selectList(
                new QueryWrapper<ZyTeamMember>()
                        .eq("team_id", teamId)
                        .orderByAsc("create_time"));
        List<Map<String, Object>> list = new ArrayList<>();
        for (ZyTeamMember m : members) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("userId", m.getUserId());
            // 查询用户名称
            try {
                JSONObject user = clientUserApi.getUserByIdWithoutException(m.getUserId());
                if (user != null) {
                    item.put("userName", user.getStr("name"));
                } else {
                    item.put("userName", "未知用户");
                }
            } catch (Exception ignored) {
            }
            item.put("role", m.getRole());
            item.put("status", m.getStatus());
            item.put("invitedBy", m.getInvitedBy());
            item.put("createdAt", m.getCreateTime());
            list.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("members", list);
        result.put("total", list.size());
        return result;
    }

    @Override
    public void addMember(String teamId, String userId, String role, String invitedBy) {
        ZyTeamMember existing = zyTeamMemberMapper.selectOne(
                new QueryWrapper<ZyTeamMember>().eq("team_id", teamId).eq("user_id", userId));
        if (existing != null) {
            throw new CommonException("该用户已是团队成员");
        }
        ZyTeamMember member = new ZyTeamMember();
        member.setId(IdUtil.fastSimpleUUID());
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setRole(ObjectUtil.isNotEmpty(role) ? role : "member");
        member.setStatus("active");
        member.setInvitedBy(invitedBy);
        zyTeamMemberMapper.insert(member);
    }

    @Override
    public void removeMember(String teamId, String userId) {
        ZyTeamMember member = zyTeamMemberMapper.selectOne(
                new QueryWrapper<ZyTeamMember>().eq("team_id", teamId).eq("user_id", userId));
        if (member != null && "owner".equals(member.getRole())) {
            throw new CommonException("不能移除所有者");
        }
        // 物理删除：直接从数据库删除记录
        zyTeamMemberMapper.delete(new QueryWrapper<ZyTeamMember>().eq("team_id", teamId).eq("user_id", userId));
    }

    @Override
    public void updateMemberRole(String teamId, String userId, String role) {
        ZyTeamMember member = zyTeamMemberMapper.selectOne(
                new QueryWrapper<ZyTeamMember>().eq("team_id", teamId).eq("user_id", userId));
        if (member == null) {
            throw new CommonException("成员不存在");
        }
        if ("owner".equals(member.getRole())) {
            throw new CommonException("不能修改所有者角色");
        }
        member.setRole(role);
        zyTeamMemberMapper.updateById(member);
    }

    @Override
    public boolean isMember(String teamId, String userId) {
        long count = zyTeamMemberMapper.selectCount(
                new QueryWrapper<ZyTeamMember>()
                        .eq("team_id", teamId)
                        .eq("user_id", userId)
                        .eq("status", "active"));
        return count > 0;
    }

    @Override
    public String getMemberRole(String teamId, String userId) {
        ZyTeamMember member = zyTeamMemberMapper.selectOne(
                new QueryWrapper<ZyTeamMember>()
                        .eq("team_id", teamId)
                        .eq("user_id", userId)
                        .eq("status", "active"));
        if (member != null) {
            return member.getRole();
        }
        // 创建人兜底：团队成员表中没有记录时，团队创建者视为 owner
        ZyTeam team = zyTeamMapper.selectById(teamId);
        if (team != null && userId.equals(team.getCreateUser())) {
            return "owner";
        }
        return null;
    }

    @Override
    public Map<String, Object> searchUsers(String keyword) {
        List<JSONObject> users = clientUserApi.userSelector(keyword);
        List<Map<String, Object>> list = new ArrayList<>();
        for (JSONObject user : users) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", user.getStr("id"));
            item.put("name", user.getStr("name"));
            item.put("account", user.getStr("account"));
            list.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("users", list);
        return result;
    }
}
