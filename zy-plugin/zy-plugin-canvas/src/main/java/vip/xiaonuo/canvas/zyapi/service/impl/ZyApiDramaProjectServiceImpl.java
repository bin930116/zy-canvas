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
package vip.xiaonuo.canvas.zyapi.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject;
import vip.xiaonuo.canvas.modular.dramaproject.mapper.ZyDramaProjectMapper;
import vip.xiaonuo.canvas.zyapi.param.ZyApiDramaProjectParam;
import vip.xiaonuo.canvas.zyapi.service.ZyApiDramaProjectService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiTeamService;

/**
 * 短剧项目Service实现
 *
 * @author xuyuxiang
 * @date 2026/9/5 19:00
 **/
@Service
public class ZyApiDramaProjectServiceImpl extends ServiceImpl<ZyDramaProjectMapper, ZyDramaProject> implements ZyApiDramaProjectService {

    @Resource
    private ZyDramaProjectMapper zyDramaProjectMapper;

    @Resource
    private ZyApiTeamService zyTeamService;

    @Override
    public Page<ZyDramaProject> listPage(String userId, Integer page, Integer pageSize) {
        QueryWrapper<ZyDramaProject> queryWrapper = new QueryWrapper<ZyDramaProject>()
                .and(w -> w
                    .eq("user_id", userId)
                    .or()
                    .inSql("team_id", "SELECT team_id FROM zy_team_member WHERE user_id = '" + userId + "' AND status = 'active'")
                )
                .orderByDesc("update_time");
        if (ObjectUtil.isNotEmpty(page) && ObjectUtil.isNotEmpty(pageSize)) {
            return this.page(new Page<>(page, pageSize), queryWrapper);
        }
        // 无分页参数时取前1000(与 Snowy 一致走分页插件)
        return this.page(new Page<>(1, 1000, false), queryWrapper);
    }

    @Override
    public ZyDramaProject getById(String userId, String id) {
        // 允许项目创建者和团队成员查看和编辑项目内容
        return this.getOne(new QueryWrapper<ZyDramaProject>()
                .eq("id", id)
                .and(w -> w
                    .eq("user_id", userId)
                    .or()
                    .inSql("team_id", "SELECT team_id FROM zy_team_member WHERE user_id = '" + userId + "' AND status = 'active'")
                ));
    }

    @Override
    public ZyDramaProject getByIdForOwner(String userId, String id) {
        // 仅限项目创建者，用于项目管理操作（删除、修改团队关联等）
        return this.getOne(new QueryWrapper<ZyDramaProject>().eq("id", id).eq("user_id", userId));
    }

    @Override
    public ZyDramaProject create(String userId, ZyApiDramaProjectParam param) {
        ZyDramaProject entity = new ZyDramaProject();
        entity.setId(IdUtil.fastSimpleUUID());
        entity.setUserId(userId);
        entity.setName(param.getName());
        entity.setType(param.getType());
        entity.setAspectRatio(param.getAspectRatio());
        entity.setSourceType(param.getSourceType());
        entity.setDescription(param.getDescription());
        entity.setCoverResourceId(param.getCoverResourceId());
        entity.setStylePresetId(param.getStylePresetId());
        entity.setStyleProfileJson(param.getStyleProfileJson());
        entity.setDefaultImageModel(param.getDefaultImageModel());
        entity.setDefaultVideoModel(param.getDefaultVideoModel());
        entity.setStatus(ObjectUtil.isNotEmpty(param.getStatus()) ? param.getStatus() : "active");
        entity.setRevision(1);
        zyDramaProjectMapper.insert(entity);
        return entity;
    }

    @Override
    public ZyDramaProject update(String userId, String id, ZyApiDramaProjectParam param) {
        // 仅项目创建者可以修改项目设置
        ZyDramaProject entity = this.getByIdForOwner(userId, id);
        if (ObjectUtil.isEmpty(entity)) {
            return null;
        }
        if (param.getName() != null) entity.setName(param.getName());
        if (param.getType() != null) entity.setType(param.getType());
        if (param.getAspectRatio() != null) entity.setAspectRatio(param.getAspectRatio());
        if (param.getSourceType() != null) entity.setSourceType(param.getSourceType());
        if (param.getDescription() != null) entity.setDescription(param.getDescription());
        if (param.getCoverResourceId() != null) entity.setCoverResourceId(param.getCoverResourceId());
        if (param.getStylePresetId() != null) entity.setStylePresetId(param.getStylePresetId());
        if (param.getStyleProfileJson() != null) entity.setStyleProfileJson(param.getStyleProfileJson());
        if (param.getDefaultImageModel() != null) entity.setDefaultImageModel(param.getDefaultImageModel());
        if (param.getDefaultVideoModel() != null) entity.setDefaultVideoModel(param.getDefaultVideoModel());
        if (param.getStatus() != null) entity.setStatus(param.getStatus());
        entity.setRevision(entity.getRevision() == null ? 1 : entity.getRevision() + 1);
        zyDramaProjectMapper.updateById(entity);
        return entity;
    }

    @Override
    public void delete(String userId, String id) {
        // 仅项目创建者可以删除项目
        ZyDramaProject entity = this.getByIdForOwner(userId, id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new IllegalArgumentException("无权限删除项目，只有项目创建者可以操作");
        }
        zyDramaProjectMapper.deleteById(id);
    }

    @Override
    public ZyDramaProject updateTeam(String userId, String id, String teamId) {
        // 仅项目创建者可以修改团队关联
        ZyDramaProject entity = this.getByIdForOwner(userId, id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new IllegalArgumentException("无权限修改项目团队关联，只有项目创建者可以操作");
        }
        // 关联到新团队时，要求在该团队有 owner/admin 角色
        if (ObjectUtil.isNotEmpty(teamId) && !ObjectUtil.equals(teamId, entity.getTeamId())) {
            String role = zyTeamService.getMemberRole(teamId, userId);
            if (!"owner".equals(role) && !"admin".equals(role)) {
                throw new IllegalArgumentException("只有团队所有者或管理员可以关联项目");
            }
        }
        entity.setTeamId(ObjectUtil.isNotEmpty(teamId) ? teamId : null);
        entity.setRevision(entity.getRevision() == null ? 1 : entity.getRevision() + 1);
        zyDramaProjectMapper.updateById(entity);
        return entity;
    }

    /**
     * 检查用户是否可以编辑资源（基于 create_user）
     *
     * @param createUserId  资源创建者ID
     * @param currentUserId 当前用户ID
     * @return 是否可以编辑
     */
    public static boolean canEditResource(String createUserId, String currentUserId) {
        return createUserId != null && createUserId.equals(currentUserId);
    }

    /**
     * 检查用户是否可以删除资源（基于 create_user）
     *
     * @param createUserId  资源创建者ID
     * @param currentUserId 当前用户ID
     * @return 是否可以删除
     */
    public static boolean canDeleteResource(String createUserId, String currentUserId) {
        return createUserId != null && createUserId.equals(currentUserId);
    }
}
