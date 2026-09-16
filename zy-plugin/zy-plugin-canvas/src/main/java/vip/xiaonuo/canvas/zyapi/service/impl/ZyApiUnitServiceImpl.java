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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject;
import vip.xiaonuo.canvas.modular.unit.entity.ZyUnit;
import vip.xiaonuo.canvas.modular.unit.mapper.ZyUnitMapper;
import vip.xiaonuo.canvas.zyapi.param.ZyApiUnitParam;
import vip.xiaonuo.canvas.zyapi.service.ZyApiDramaProjectService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiUnitService;
import vip.xiaonuo.common.exception.CommonException;

import java.util.ArrayList;
import java.util.List;

/**
 * 短剧项目章节Service实现
 *
 * @author xuyuxiang
 * @date 2026/9/5 19:30
 **/
@Service
public class ZyApiUnitServiceImpl extends ServiceImpl<ZyUnitMapper, ZyUnit> implements ZyApiUnitService {

    @Resource
    private ZyUnitMapper zyUnitMapper;

    @Resource
    private ZyApiDramaProjectService zyDramaProjectService;

    @Override
    public List<ZyUnit> listByProject(String userId, String projectId) {
        requireProject(userId, projectId);
        return zyUnitMapper.selectList(new QueryWrapper<ZyUnit>()
                .eq("project_id", projectId)
                .orderByAsc("position")
                .orderByAsc("create_time"));
    }

    @Override
    public ZyUnit get(String userId, String projectId, String unitId) {
        requireProject(userId, projectId);
        return zyUnitMapper.selectOne(new QueryWrapper<ZyUnit>().eq("id", unitId).eq("project_id", projectId));
    }

    @Override
    public ZyUnit create(String userId, String projectId, ZyApiUnitParam param) {
        requireProject(userId, projectId);
        ZyUnit entity = new ZyUnit();
        entity.setId(IdUtil.fastSimpleUUID());
        entity.setProjectId(projectId);
        entity.setKind(param.getKind());
        entity.setTitle(param.getTitle());
        entity.setSourceText(param.getSourceText());
        entity.setWordCount(countWord(param.getSourceText()));
        entity.setStatus(ObjectUtil.isNotEmpty(param.getStatus()) ? param.getStatus() : "draft");
        entity.setPosition(param.getPosition() != null ? param.getPosition() : (int) this.count(new QueryWrapper<ZyUnit>().eq("project_id", projectId)));
        zyUnitMapper.insert(entity);
        return entity;
    }

    @Override
    public List<ZyUnit> importUnits(String userId, String projectId, List<ZyApiUnitParam> params) {
        requireProject(userId, projectId);
        int basePosition = (int) this.count(new QueryWrapper<ZyUnit>().eq("project_id", projectId));
        List<ZyUnit> result = new ArrayList<>();
        for (ZyApiUnitParam param : params) {
            ZyUnit entity = new ZyUnit();
            entity.setId(IdUtil.fastSimpleUUID());
            entity.setProjectId(projectId);
            entity.setKind(param.getKind());
            entity.setTitle(param.getTitle());
            entity.setSourceText(param.getSourceText());
            entity.setWordCount(countWord(param.getSourceText()));
            entity.setStatus(ObjectUtil.isNotEmpty(param.getStatus()) ? param.getStatus() : "draft");
            entity.setPosition(basePosition++);
            zyUnitMapper.insert(entity);
            result.add(entity);
        }
        return result;
    }

    @Override
    public ZyUnit update(String userId, String projectId, String unitId, ZyApiUnitParam param) {
        requireProject(userId, projectId);
        ZyUnit entity = zyUnitMapper.selectOne(new QueryWrapper<ZyUnit>().eq("id", unitId).eq("project_id", projectId));
        if (ObjectUtil.isEmpty(entity)) {
            throw new CommonException("章节不存在");
        }
        // 检查编辑权限：只能编辑自己创建的章节
        if (!ZyApiDramaProjectServiceImpl.canEditResource(entity.getCreateUser(), userId)) {
            throw new CommonException("无权限编辑该章节，只能编辑自己创建的内容");
        }
        if (param.getKind() != null) entity.setKind(param.getKind());
        if (param.getTitle() != null) entity.setTitle(param.getTitle());
        if (param.getSourceText() != null) {
            entity.setSourceText(param.getSourceText());
            entity.setWordCount(countWord(param.getSourceText()));
        }
        if (param.getStatus() != null) entity.setStatus(param.getStatus());
        if (param.getPosition() != null) entity.setPosition(param.getPosition());
        zyUnitMapper.updateById(entity);
        return entity;
    }

    @Override
    public void delete(String userId, String projectId, String unitId) {
        requireProject(userId, projectId);
        ZyUnit entity = zyUnitMapper.selectOne(new QueryWrapper<ZyUnit>().eq("id", unitId).eq("project_id", projectId));
        if (ObjectUtil.isEmpty(entity)) {
            throw new CommonException("章节不存在");
        }
        // 检查删除权限：只能删除自己创建的章节
        if (!ZyApiDramaProjectServiceImpl.canDeleteResource(entity.getCreateUser(), userId)) {
            throw new CommonException("无权限删除该章节，只能删除自己创建的内容");
        }
        zyUnitMapper.deleteById(unitId);
    }

    @Override
    public List<String> reorder(String userId, String projectId, List<String> unitIds) {
        requireProject(userId, projectId);
        if (ObjectUtil.isNotEmpty(unitIds)) {
            int index = 0;
            for (String unitId : unitIds) {
                ZyUnit entity = zyUnitMapper.selectOne(new QueryWrapper<ZyUnit>().eq("id", unitId).eq("project_id", projectId));
                if (ObjectUtil.isNotEmpty(entity)) {
                    entity.setPosition(index++);
                    zyUnitMapper.updateById(entity);
                }
            }
        }
        return unitIds == null ? new ArrayList<>() : unitIds;
    }

    /**
     * 校验项目存在且归属当前用户
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    private void requireProject(String userId, String projectId) {
        ZyDramaProject project = zyDramaProjectService.getById(userId, projectId);
        if (ObjectUtil.isEmpty(project)) {
            throw new CommonException("短剧项目不存在");
        }
    }

    /**
     * 统计字数(按字符长度)
     *
     * @param sourceText 正文
     * @return 字数
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    private int countWord(String sourceText) {
        return sourceText == null ? 0 : sourceText.length();
    }
}
