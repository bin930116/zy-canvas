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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject;
import vip.xiaonuo.canvas.modular.workflow.entity.ZyWorkflow;
import vip.xiaonuo.canvas.modular.workflow.entity.ZyWorkflowStep;
import vip.xiaonuo.canvas.modular.workflow.mapper.ZyWorkflowMapper;
import vip.xiaonuo.canvas.modular.workflow.mapper.ZyWorkflowStepMapper;
import vip.xiaonuo.canvas.zyapi.service.ZyApiDramaProjectService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiWorkflowService;
import vip.xiaonuo.common.exception.CommonException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 短剧项目工作流Service实现
 *
 * @author hanbin
 * @date 2026/09/09 16:50
 **/
@Service
public class ZyApiWorkflowServiceImpl extends ServiceImpl<ZyWorkflowMapper, ZyWorkflow> implements ZyApiWorkflowService {

    @Resource
    private ZyWorkflowMapper zyWorkflowMapper;

    @Resource
    private ZyWorkflowStepMapper zyWorkflowStepMapper;

    @Resource
    private ZyApiDramaProjectService zyDramaProjectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createWorkflow(String userId, String projectId, String unitId) {
        requireProject(userId, projectId);
        // 幂等：同一章节已有工作流则直接返回
        ZyWorkflow existing = zyWorkflowMapper.selectOne(new QueryWrapper<ZyWorkflow>()
                .eq("project_id", projectId)
                .eq("unit_id", unitId)
                .last("limit 1"));
        if (ObjectUtil.isNotEmpty(existing)) {
            return toWorkflow(existing);
        }
        ZyWorkflow workflow = new ZyWorkflow();
        workflow.setId(IdUtil.fastSimpleUUID());
        workflow.setProjectId(projectId);
        workflow.setUnitId(unitId);
        workflow.setScope("unit");
        workflow.setStatus("pending");
        workflow.setRevision(1);
        workflow.setCreateUser(userId);
        workflow.setUpdateUser(userId);
        zyWorkflowMapper.insert(workflow);
        // 默认生产步骤：分镜图 / 动作预演 / 镜头视频
        String[][] defaultSteps = {{"storyboard", "分镜图"}, {"previz", "动作预演"}, {"video", "镜头视频"}};
        for (int i = 0; i < defaultSteps.length; i++) {
            ZyWorkflowStep step = new ZyWorkflowStep();
            step.setId(IdUtil.fastSimpleUUID());
            step.setWorkflowInstanceId(workflow.getId());
            step.setStepKey(defaultSteps[i][0]);
            step.setName(defaultSteps[i][1]);
            step.setPosition(i + 1);
            step.setStatus("pending");
            step.setCreateUser(userId);
            step.setUpdateUser(userId);
            zyWorkflowStepMapper.insert(step);
        }
        return toWorkflow(workflow);
    }

    @Override
    public List<Map<String, Object>> listWorkflowsByProject(String projectId) {
        List<ZyWorkflow> workflows = zyWorkflowMapper.selectList(new QueryWrapper<ZyWorkflow>()
                .eq("project_id", projectId)
                .orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ZyWorkflow workflow : workflows) {
            result.add(toWorkflow(workflow));
        }
        return result;
    }

    private Map<String, Object> toWorkflow(ZyWorkflow workflow) {
        Map<String, Object> instance = new LinkedHashMap<>();
        instance.put("id", workflow.getId());
        instance.put("projectId", workflow.getProjectId());
        instance.put("unitId", workflow.getUnitId());
        instance.put("scope", workflow.getScope());
        instance.put("status", workflow.getStatus());
        instance.put("revision", workflow.getRevision() != null ? workflow.getRevision() : 1);
        List<ZyWorkflowStep> steps = zyWorkflowStepMapper.selectList(new QueryWrapper<ZyWorkflowStep>()
                .eq("workflow_instance_id", workflow.getId())
                .orderByAsc("position"));
        List<Map<String, Object>> stepList = new ArrayList<>();
        for (ZyWorkflowStep step : steps) {
            stepList.add(toStep(step));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("instance", instance);
        result.put("steps", stepList);
        return result;
    }

    private Map<String, Object> toStep(ZyWorkflowStep step) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", step.getId());
        result.put("workflowInstanceId", step.getWorkflowInstanceId());
        result.put("stepKey", step.getStepKey());
        result.put("name", step.getName());
        result.put("position", step.getPosition());
        result.put("status", step.getStatus());
        if (ObjectUtil.isNotEmpty(step.getError())) {
            result.put("error", step.getError());
        }
        result.put("updatedAt", step.getUpdateTime() != null ? DateUtil.formatDateTime(step.getUpdateTime()) : step.getCreateTime() != null ? DateUtil.formatDateTime(step.getCreateTime()) : null);
        return result;
    }

    /**
     * 校验项目存在且归属当前用户
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @author hanbin
     * @date 2026/09/09 16:50
     */
    private void requireProject(String userId, String projectId) {
        ZyDramaProject project = zyDramaProjectService.getById(userId, projectId);
        if (ObjectUtil.isEmpty(project)) {
            throw new CommonException("短剧项目不存在");
        }
    }
}
