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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.canvasunitlink.entity.ZyCanvasUnitLink;
import vip.xiaonuo.canvas.modular.canvasunitlink.mapper.ZyCanvasUnitLinkMapper;
import vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject;
import vip.xiaonuo.canvas.modular.project.entity.ZyProject;
import vip.xiaonuo.canvas.modular.project.mapper.ZyProjectMapper;
import vip.xiaonuo.canvas.zyapi.service.ZyApiDramaProjectService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiProjectCanvasService;
import vip.xiaonuo.common.exception.CommonException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 画布项目 Service 实现类
 *
 * @author xuyuxiang
 * @date 2026/9/5 22:30
 **/
@Service
public class ZyApiProjectCanvasServiceImpl implements ZyApiProjectCanvasService {

    @Resource
    private ZyProjectMapper zyProjectMapper;

    @Resource
    private ZyCanvasUnitLinkMapper zyCanvasUnitLinkMapper;

    @Resource
    private ZyApiDramaProjectService zyDramaProjectService;

    @Override
    public Map<String, Object> listCanvases(String userId, String projectId, Integer page, Integer pageSize) {
        // 校验项目存在且归属当前用户
        requireProject(userId, projectId);

        // 查询项目的画布列表（只显示当前用户创建的画布，不共享给团队成员）
        QueryWrapper<ZyProject> canvasWrapper = new QueryWrapper<ZyProject>()
                .eq("project_id", projectId)
                .eq("user_id", userId)  // 只查询当前用户创建的画布
                .orderByDesc("update_time");
        Page<ZyProject> canvasPage = zyProjectMapper.selectPage(new Page<>(page, pageSize), canvasWrapper);

        // 转换画布列表
        List<Map<String, Object>> canvases = new ArrayList<>();
        for (ZyProject canvas : canvasPage.getRecords()) {
            canvases.add(toCanvas(canvas));
        }

        // 查询画布与章节的关联关系（只查询当前用户的画布关联）
        List<String> canvasIds = new ArrayList<>();
        for (ZyProject canvas : canvasPage.getRecords()) {
            canvasIds.add(canvas.getId());
        }
        
        List<ZyCanvasUnitLink> links = new ArrayList<>();
        if (!canvasIds.isEmpty()) {
            links = zyCanvasUnitLinkMapper.selectList(
                    new QueryWrapper<ZyCanvasUnitLink>()
                            .eq("project_id", projectId)
                            .in("canvas_id", canvasIds)
                            .orderByDesc("create_time")
            );
        }

        // 转换关联关系列表
        List<Map<String, Object>> canvasUnitLinks = new ArrayList<>();
        for (ZyCanvasUnitLink link : links) {
            canvasUnitLinks.add(toCanvasUnitLink(link));
        }

        // 构造返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("canvases", canvases);
        result.put("canvasUnitLinks", canvasUnitLinks);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", canvasPage.getTotal());
        result.put("hasMore", (long) (page * pageSize) < canvasPage.getTotal());
        return result;
    }

    @Override
    public Map<String, Object> linkCanvasUnit(String userId, String projectId, String canvasId, String unitId, String role) {
        // 校验项目存在且归属当前用户
        requireProject(userId, projectId);

        // 查询画布
        ZyProject canvas = zyProjectMapper.selectOne(
                new QueryWrapper<ZyProject>().eq("id", canvasId).eq("project_id", projectId)
        );
        if (ObjectUtil.isEmpty(canvas)) {
            throw new CommonException("画布不存在或未关联到此项目");
        }

        // 检查是否已存在关联
        ZyCanvasUnitLink existing = zyCanvasUnitLinkMapper.selectOne(
                new QueryWrapper<ZyCanvasUnitLink>()
                        .eq("project_id", projectId)
                        .eq("canvas_id", canvasId)
                        .eq("unit_id", unitId)
        );
        if (ObjectUtil.isNotEmpty(existing)) {
            throw new CommonException("画布已关联到此章节");
        }

        // 创建关联
        ZyCanvasUnitLink link = new ZyCanvasUnitLink();
        link.setId(IdUtil.fastSimpleUUID());
        link.setProjectId(projectId);
        link.setCanvasId(canvasId);
        link.setUnitId(unitId);
        link.setRole(role != null ? role : "storyboard");
        zyCanvasUnitLinkMapper.insert(link);

        // 返回关联信息
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("link", toCanvasUnitLink(link));
        return result;
    }

    @Override
    public void unlinkCanvasUnit(String userId, String projectId, String canvasId, String unitId) {
        // 校验项目存在且归属当前用户
        requireProject(userId, projectId);

        // 删除关联
        zyCanvasUnitLinkMapper.delete(
                new QueryWrapper<ZyCanvasUnitLink>()
                        .eq("project_id", projectId)
                        .eq("canvas_id", canvasId)
                        .eq("unit_id", unitId)
        );
    }

    @Override
    public void unlinkCanvasProject(String userId, String projectId, String canvasId) {
        // 校验项目存在且归属当前用户
        requireProject(userId, projectId);

        // 查询画布
        ZyProject canvas = zyProjectMapper.selectOne(
                new QueryWrapper<ZyProject>().eq("id", canvasId).eq("project_id", projectId)
        );
        if (ObjectUtil.isEmpty(canvas)) {
            throw new CommonException("画布不存在或未关联到此项目");
        }

        // 清除画布的项目关联
        canvas.setProjectId(null);
        zyProjectMapper.updateById(canvas);

        // 删除画布与章节的所有关联
        zyCanvasUnitLinkMapper.delete(
                new QueryWrapper<ZyCanvasUnitLink>()
                        .eq("project_id", projectId)
                        .eq("canvas_id", canvasId)
        );
    }

    /**
     * 校验项目存在且归属当前用户
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     */
    private void requireProject(String userId, String projectId) {
        ZyDramaProject project = zyDramaProjectService.getById(userId, projectId);
        if (ObjectUtil.isEmpty(project)) {
            throw new CommonException("短剧项目不存在");
        }
    }

    /**
     * 画布实体转对象
     *
     * @param canvas 画布实体
     * @return 画布对象
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     */
    private Map<String, Object> toCanvas(ZyProject canvas) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", canvas.getId());
        result.put("projectId", canvas.getProjectId());
        result.put("title", canvas.getTitle());
        result.put("createdAt", canvas.getCreateTime() != null ? DateUtil.formatDateTime(canvas.getCreateTime()) : null);
        result.put("updatedAt", canvas.getUpdateTime() != null ? DateUtil.formatDateTime(canvas.getUpdateTime()) : null);
        return result;
    }

    /**
     * 画布与章节关联实体转对象
     *
     * @param link 关联实体
     * @return 关联对象
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     */
    private Map<String, Object> toCanvasUnitLink(ZyCanvasUnitLink link) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", link.getId());
        result.put("projectId", link.getProjectId());
        result.put("canvasId", link.getCanvasId());
        result.put("unitId", link.getUnitId());
        result.put("role", link.getRole());
        result.put("createdAt", link.getCreateTime() != null ? DateUtil.formatDateTime(link.getCreateTime()) : null);
        return result;
    }
}