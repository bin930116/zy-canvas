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
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.project.entity.ZyProject;
import vip.xiaonuo.canvas.modular.project.mapper.ZyProjectMapper;
import vip.xiaonuo.canvas.zyapi.result.ZyApiProjectSummaryResult;
import vip.xiaonuo.canvas.zyapi.service.ZyApiProjectService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 画布项目Service实现
 *
 * @author xuyuxiang
 * @date 2026/9/5 18:30
 **/
@Service
public class ZyApiProjectServiceImpl extends ServiceImpl<ZyProjectMapper, ZyProject> implements ZyApiProjectService {

    @Resource
    private ZyProjectMapper zyProjectMapper;

    @Override
    public List<ZyApiProjectSummaryResult> listSummary(String userId) {
        QueryWrapper<ZyProject> queryWrapper = new QueryWrapper<ZyProject>()
                .eq("user_id", userId)
                .orderByDesc("update_time");
        // 走分页插件，避免 easy-trans 在 PG 下因严格 GROUP BY 报错(与 Snowy 一致)
        List<ZyProject> projectList = this.page(new Page<>(1, 1000, false), queryWrapper).getRecords();
        if (ObjectUtil.isEmpty(projectList)) {
            return Collections.emptyList();
        }
        return projectList.stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Override
    public ZyProject getById(String userId, String id) {
        return zyProjectMapper.selectOne(new QueryWrapper<ZyProject>()
                .eq("id", id)
                .eq("user_id", userId));
    }

    @Override
    public ZyProject upsert(String userId, String id, Map<String, Object> project) {
        String projectId = ObjectUtil.isNotEmpty(project.get("id")) ? String.valueOf(project.get("id")) : id;
        String title = project.get("title") != null ? String.valueOf(project.get("title")) : "";
        String refProjectId = project.get("projectId") != null ? String.valueOf(project.get("projectId")) : null;
        String dataJson = JSONUtil.toJsonStr(project);

        ZyProject exist = this.getById(userId, projectId);
        if (ObjectUtil.isEmpty(exist)) {
            ZyProject entity = new ZyProject();
            entity.setId(projectId);
            entity.setUserId(userId);
            entity.setTitle(title);
            entity.setProjectId(refProjectId);
            entity.setDataJson(dataJson);
            entity.setRevision(1);
            entity.setStatus("active");
            zyProjectMapper.insert(entity);
            return entity;
        }
        exist.setTitle(title);
        exist.setProjectId(refProjectId);
        exist.setDataJson(dataJson);
        exist.setStatus(project.get("status") != null ? String.valueOf(project.get("status")) : exist.getStatus());
        exist.setRevision(exist.getRevision() == null ? 1 : exist.getRevision() + 1);
        zyProjectMapper.updateById(exist);
        return exist;
    }

    @Override
    public void delete(String userId, String id) {
        zyProjectMapper.delete(new QueryWrapper<ZyProject>().eq("id", id).eq("user_id", userId));
    }

    /**
     * 实体转摘要结果
     *
     * @param entity 项目实体
     * @return 摘要结果
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    private ZyApiProjectSummaryResult toSummary(ZyProject entity) {
        ZyApiProjectSummaryResult result = new ZyApiProjectSummaryResult();
        result.setId(entity.getId());
        result.setProjectId(entity.getProjectId());
        result.setTitle(entity.getTitle());
        result.setCreatedAt(entity.getCreateTime() != null ? DateUtil.formatDateTime(entity.getCreateTime()) : null);
        result.setUpdatedAt(entity.getUpdateTime() != null ? DateUtil.formatDateTime(entity.getUpdateTime()) : entity.getCreateTime() != null ? DateUtil.formatDateTime(entity.getCreateTime()) : null);
        return result;
    }
}
