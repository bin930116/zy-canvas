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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.asset.mapper.ZyAssetMapper;
import vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject;
import vip.xiaonuo.canvas.modular.shot.entity.ZyShot;
import vip.xiaonuo.canvas.modular.shot.entity.ZyShotAsset;
import vip.xiaonuo.canvas.modular.shotrevision.entity.ZyShotRevision;
import vip.xiaonuo.canvas.modular.shot.mapper.ZyShotAssetMapper;
import vip.xiaonuo.canvas.modular.shot.mapper.ZyShotMapper;
import vip.xiaonuo.canvas.modular.shotrevision.mapper.ZyShotRevisionMapper;
import vip.xiaonuo.canvas.zyapi.param.ZyApiShotParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiShotRevisionInputParam;
import vip.xiaonuo.canvas.zyapi.service.ZyApiDramaProjectService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiShotService;
import vip.xiaonuo.common.exception.CommonException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 短剧项目分镜Service实现
 *
 * @author xuyuxiang
 * @date 2026/9/5 20:00
 **/
@Service
public class ZyApiShotServiceImpl extends ServiceImpl<ZyShotMapper, ZyShot> implements ZyApiShotService {

    @Resource
    private ZyShotMapper zyShotMapper;

    @Resource
    private ZyShotRevisionMapper zyShotRevisionMapper;

    @Resource
    private ZyShotAssetMapper zyShotAssetMapper;

    @Resource
    private ZyAssetMapper zyAssetMapper;

    @Resource
    private ZyApiDramaProjectService zyDramaProjectService;

    @Override
    public List<ZyShot> listByUnit(String userId, String projectId, String unitId) {
        requireProject(userId, projectId);
        return zyShotMapper.selectList(new QueryWrapper<ZyShot>()
                .eq("project_id", projectId)
                .eq("unit_id", unitId)
                .orderByAsc("position")
                .orderByAsc("create_time"));
    }

    @Override
    public ZyShot save(String userId, String projectId, ZyApiShotParam param) {
        requireProject(userId, projectId);
        ZyShot entity;
        if (ObjectUtil.isNotEmpty(param.getId())) {
            // 更新已存在的镜头
            entity = zyShotMapper.selectOne(new QueryWrapper<ZyShot>().eq("id", param.getId()).eq("project_id", projectId));
            if (ObjectUtil.isEmpty(entity)) {
                throw new CommonException("分镜不存在");
            }
            // 检查编辑权限：只能编辑自己创建的镜头
            if (!ZyApiDramaProjectServiceImpl.canEditResource(entity.getCreateUser(), userId)) {
                throw new CommonException("无权限编辑该分镜，只能编辑自己创建的内容");
            }
        } else {
            // 创建新镜头
            entity = new ZyShot();
            entity.setId(IdUtil.fastSimpleUUID());
            entity.setProjectId(projectId);
            entity.setUnitId(param.getUnitId());
            entity.setStatus(ObjectUtil.isNotEmpty(param.getStatus()) ? param.getStatus() : "draft");
            entity.setPosition(param.getPosition() != null ? param.getPosition() : (int) this.count(new QueryWrapper<ZyShot>().eq("project_id", projectId).eq("unit_id", param.getUnitId())));
        }
        if (param.getUnitId() != null) entity.setUnitId(param.getUnitId());
        if (param.getTitle() != null) entity.setTitle(param.getTitle());
        if (param.getDescription() != null) entity.setDescription(param.getDescription());
        if (param.getPosition() != null) entity.setPosition(param.getPosition());
        if (param.getDurationMs() != null) entity.setDurationMs(param.getDurationMs().intValue());
        if (param.getStatus() != null) entity.setStatus(param.getStatus());
        if (ObjectUtil.isNotEmpty(param.getRevision())) {
            int nextVersion = nextRevisionVersion(entity.getId()) + 1;
            ZyShotRevision revision = buildRevision(param.getRevision(), entity.getId(), nextVersion, userId);
            zyShotRevisionMapper.insert(revision);
            entity.setCurrentRevisionId(revision.getId());
        }
        if (ObjectUtil.isEmpty(param.getId())) {
            zyShotMapper.insert(entity);
        } else {
            zyShotMapper.updateById(entity);
        }
        return entity;
    }

    @Override
    public void delete(String userId, String projectId, String shotId) {
        requireProject(userId, projectId);
        ZyShot entity = zyShotMapper.selectOne(new QueryWrapper<ZyShot>().eq("id", shotId).eq("project_id", projectId));
        if (ObjectUtil.isEmpty(entity)) {
            throw new CommonException("分镜不存在");
        }
        // 检查删除权限：只能删除自己创建的镜头
        if (!ZyApiDramaProjectServiceImpl.canDeleteResource(entity.getCreateUser(), userId)) {
            throw new CommonException("无权限删除该分镜，只能删除自己创建的内容");
        }
        zyShotMapper.deleteById(shotId);
        zyShotRevisionMapper.delete(new QueryWrapper<ZyShotRevision>().eq("shot_id", shotId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ZyShot> replaceByUnit(String userId, String projectId, String unitId, List<ZyApiShotParam> params) {
        requireProject(userId, projectId);
        List<ZyShot> existing = zyShotMapper.selectList(new QueryWrapper<ZyShot>().eq("project_id", projectId).eq("unit_id", unitId));
        for (ZyShot shot : existing) {
            zyShotMapper.deleteById(shot.getId());
            zyShotRevisionMapper.delete(new QueryWrapper<ZyShotRevision>().eq("shot_id", shot.getId()));
        }
        List<ZyShot> result = new ArrayList<>();
        int index = 0;
        for (ZyApiShotParam param : params) {
            ZyShot entity = new ZyShot();
            entity.setId(ObjectUtil.isNotEmpty(param.getId()) ? param.getId() : IdUtil.fastSimpleUUID());
            entity.setProjectId(projectId);
            entity.setUnitId(unitId);
            entity.setTitle(param.getTitle());
            entity.setDescription(param.getDescription());
            entity.setPosition(index++);
            entity.setDurationMs(param.getDurationMs() != null ? param.getDurationMs().intValue() : 0);
            entity.setStatus(ObjectUtil.isNotEmpty(param.getStatus()) ? param.getStatus() : "draft");
            if (ObjectUtil.isNotEmpty(param.getRevision())) {
                ZyShotRevision revision = buildRevision(param.getRevision(), entity.getId(), 1, userId);
                zyShotRevisionMapper.insert(revision);
                entity.setCurrentRevisionId(revision.getId());
            }
            if (ObjectUtil.isEmpty(param.getId())) {
                zyShotMapper.insert(entity);
            } else {
                zyShotMapper.updateById(entity);
            }
            result.add(entity);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createRevision(String userId, String projectId, String shotId, ZyApiShotRevisionInputParam input) {
        requireProject(userId, projectId);
        ZyShot shot = zyShotMapper.selectOne(new QueryWrapper<ZyShot>().eq("id", shotId).eq("project_id", projectId));
        if (ObjectUtil.isEmpty(shot)) {
            throw new CommonException("分镜不存在");
        }
        int nextVersion = nextRevisionVersion(shotId) + 1;
        ZyShotRevision revision = buildRevision(input, shotId, nextVersion, userId);
        zyShotRevisionMapper.insert(revision);
        shot.setCurrentRevisionId(revision.getId());
        zyShotMapper.updateById(shot);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shot", shot);
        result.put("revision", revision);
        return result;
    }

    @Override
    public List<ZyShotRevision> listRevisionsByShot(String shotId) {
        return zyShotRevisionMapper.selectList(new QueryWrapper<ZyShotRevision>()
                .eq("shot_id", shotId)
                .orderByAsc("version")
                .orderByDesc("create_time"));
    }

    /**
     * 构建分镜版本实体
     *
     * @param input   版本输入
     * @param shotId  分镜id
     * @param version 版本号
     * @param userId  用户id
     * @return 版本实体
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     */
    private ZyShotRevision buildRevision(ZyApiShotRevisionInputParam input, String shotId, int version, String userId) {
        ZyShotRevision revision = new ZyShotRevision();
        revision.setId(IdUtil.fastSimpleUUID());
        revision.setShotId(shotId);
        revision.setVersion(version);
        revision.setPlotDescription(input.getPlotDescription());
        revision.setAction(input.getAction());
        revision.setDialogue(input.getDialogue());
        revision.setShotSize(input.getShotSize());
        revision.setCameraAngle(input.getCameraAngle());
        revision.setCameraMovement(input.getCameraMovement());
        revision.setDurationMs(input.getDurationMs() != null ? input.getDurationMs().intValue() : null);
        revision.setImagePrompt(input.getImagePrompt());
        revision.setVideoPrompt(input.getVideoPrompt());
        revision.setNegativePrompt(input.getNegativePrompt());
        revision.setContinuityNotes(input.getContinuityNotes());
        revision.setActionBeatsJson(input.getActionBeats() != null ? JSONUtil.toJsonStr(input.getActionBeats()) : null);
        revision.setCreatedBy(userId);
        return revision;
    }

    /**
     * 获取分镜当前最高版本号
     *
     * @param shotId 分镜id
     * @return 最高版本号
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     */
    private int nextRevisionVersion(String shotId) {
        Long count = zyShotRevisionMapper.selectCount(new QueryWrapper<ZyShotRevision>().eq("shot_id", shotId));
        return count == null ? 0 : count.intValue();
    }

    /**
     * 校验项目存在且归属当前用户
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     */
    private void requireProject(String userId, String projectId) {
        ZyDramaProject project = zyDramaProjectService.getById(userId, projectId);
        if (ObjectUtil.isEmpty(project)) {
            throw new CommonException("短剧项目不存在");
        }
    }

    @Override
    public Map<String, Object> linkAsset(String userId, String projectId, String shotId, String assetVersionId, String role) {
        requireProject(userId, projectId);
        ZyShot shot = zyShotMapper.selectOne(new QueryWrapper<ZyShot>().eq("id", shotId).eq("project_id", projectId));
        if (ObjectUtil.isEmpty(shot)) {
            throw new CommonException("分镜不存在");
        }
        if (ObjectUtil.isEmpty(assetVersionId)) {
            throw new CommonException("资产版本不存在");
        }
        // 按资产id或主版本id反查资产
        ZyAsset asset = zyAssetMapper.selectOne(new QueryWrapper<ZyAsset>().eq("id", assetVersionId));
        if (ObjectUtil.isEmpty(asset)) {
            asset = zyAssetMapper.selectOne(new QueryWrapper<ZyAsset>().eq("primary_version_id", assetVersionId));
        }
        if (ObjectUtil.isEmpty(asset)) {
            throw new CommonException("资产不存在");
        }
        // 已绑定则直接返回已有引用
        ZyShotAsset existing = zyShotAssetMapper.selectOne(new QueryWrapper<ZyShotAsset>()
                .eq("shot_id", shotId)
                .eq("asset_version_id", assetVersionId)
                .eq("role", role)
                .last("limit 1"));
        if (ObjectUtil.isNotEmpty(existing)) {
            return toAssetReference(existing);
        }
        ZyShotAsset entity = new ZyShotAsset();
        entity.setId(IdUtil.fastSimpleUUID());
        entity.setProjectId(projectId);
        entity.setShotId(shotId);
        entity.setAssetVersionId(assetVersionId);
        entity.setAssetId(asset.getId());
        entity.setRole(role);
        entity.setStatus("linked");
        entity.setCreateUser(userId);
        entity.setUpdateUser(userId);
        zyShotAssetMapper.insert(entity);
        return toAssetReference(entity);
    }

    @Override
    public Map<String, Object> unlinkAsset(String userId, String projectId, String shotId, String referenceId) {
        requireProject(userId, projectId);
        ZyShotAsset existing = zyShotAssetMapper.selectOne(new QueryWrapper<ZyShotAsset>()
                .eq("id", referenceId)
                .eq("shot_id", shotId)
                .eq("project_id", projectId));
        if (ObjectUtil.isEmpty(existing)) {
            throw new CommonException("绑定引用不存在");
        }
        zyShotAssetMapper.deleteById(existing.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unlinked", true);
        return result;
    }

    @Override
    public List<Map<String, Object>> listAssetsByProject(String projectId) {
        List<ZyShotAsset> list = zyShotAssetMapper.selectList(new QueryWrapper<ZyShotAsset>()
                .eq("project_id", projectId)
                .orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ZyShotAsset item : list) {
            result.add(toAssetReference(item));
        }
        return result;
    }

    private Map<String, Object> toAssetReference(ZyShotAsset item) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", item.getId());
        result.put("shotId", item.getShotId());
        result.put("assetVersionId", item.getAssetVersionId());
        result.put("role", item.getRole());
        result.put("status", item.getStatus());
        result.put("createdAt", item.getCreateTime() != null ? DateUtil.formatDateTime(item.getCreateTime()) : null);
        return result;
    }
}
