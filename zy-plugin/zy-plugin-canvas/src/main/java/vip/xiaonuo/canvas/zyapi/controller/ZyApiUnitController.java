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
package vip.xiaonuo.canvas.zyapi.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.shot.entity.ZyShot;
import vip.xiaonuo.canvas.modular.shotartifact.entity.ZyShotArtifact;
import vip.xiaonuo.canvas.modular.shotartifact.mapper.ZyShotArtifactMapper;
import vip.xiaonuo.canvas.modular.shotrevision.entity.ZyShotRevision;
import vip.xiaonuo.canvas.modular.unit.entity.ZyUnit;
import vip.xiaonuo.canvas.zyapi.param.ZyApiUnitImportParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiUnitParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiUnitReorderParam;
import vip.xiaonuo.canvas.zyapi.service.ZyApiAssetService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiShotService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiWorkflowService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiUnitService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 短剧项目章节接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 19:30
 **/
@Tag(name = "短剧项目章节")
@RestController
public class ZyApiUnitController {

    @Resource
    private ZyApiUnitService zyUnitService;

    @Resource
    private ZyApiShotService zyShotService;

    @Resource
    private ZyApiAssetService zyAssetService;

    @Resource
    private ZyApiWorkflowService zyWorkflowService;

    @Resource
    private ZyShotArtifactMapper zyShotArtifactMapper;

    /**
     * 获取项目章节列表
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     **/
    @Operation(summary = "获取项目章节列表")
    @GetMapping("/projects/{projectId}/units")
    public CommonResult<Map<String, Object>> list(@PathVariable String projectId) {
        String userId = StpClientUtil.getLoginIdAsString();
        List<Map<String, Object>> units = new ArrayList<>();
        for (ZyUnit unit : zyUnitService.listByProject(userId, projectId)) {
            units.add(toUnit(unit));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("units", units);
        result.put("canvasCounts", new LinkedHashMap<>());
        return CommonResult.data(result);
    }

    /**
     * 获取章节详情
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     **/
    @Operation(summary = "获取章节详情")
    @GetMapping("/projects/{projectId}/units/{unitId}")
    public CommonResult<Map<String, Object>> detail(@PathVariable String projectId, @PathVariable String unitId) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyUnit unit = zyUnitService.get(userId, projectId, unitId);
        if (ObjectUtil.isEmpty(unit)) {
            return CommonResult.error("章节不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unit", toUnit(unit));
        return CommonResult.data(result);
    }

    /**
     * 新建章节
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     **/
    @Operation(summary = "新建章节")
    @PostMapping("/projects/{projectId}/units")
    public CommonResult<Map<String, Object>> create(@PathVariable String projectId,
                                                    @RequestBody @Valid ZyApiUnitParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyUnit unit = zyUnitService.create(userId, projectId, param);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unit", toUnit(unit));
        return CommonResult.data(result);
    }

    /**
     * 批量导入章节
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     **/
    @Operation(summary = "批量导入章节")
    @PostMapping("/projects/{projectId}/units/import")
    public CommonResult<Map<String, Object>> importUnits(@PathVariable String projectId,
                                                         @RequestBody @Valid ZyApiUnitImportParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        List<Map<String, Object>> units = new ArrayList<>();
        for (ZyUnit unit : zyUnitService.importUnits(userId, projectId, ObjectUtil.isNotEmpty(param.getUnits()) ? param.getUnits() : new ArrayList<>())) {
            units.add(toUnit(unit));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("units", units);
        return CommonResult.data(result);
    }

    /**
     * 更新章节
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     **/
    @Operation(summary = "更新章节")
    @PatchMapping("/projects/{projectId}/units/{unitId}")
    public CommonResult<Map<String, Object>> update(@PathVariable String projectId,
                                                    @PathVariable String unitId,
                                                    @RequestBody @Valid ZyApiUnitParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        try {
            ZyUnit unit = zyUnitService.update(userId, projectId, unitId, param);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("unit", toUnit(unit));
            return CommonResult.data(result);
        } catch (Exception e) {
            return CommonResult.error(e.getMessage());
        }
    }

    /**
     * 删除章节
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     **/
    @Operation(summary = "删除章节")
    @DeleteMapping("/projects/{projectId}/units/{unitId}")
    public CommonResult<Map<String, Object>> delete(@PathVariable String projectId, @PathVariable String unitId) {
        String userId = StpClientUtil.getLoginIdAsString();
        try {
            zyUnitService.delete(userId, projectId, unitId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", unitId);
            return CommonResult.data(result);
        } catch (Exception e) {
            return CommonResult.error(e.getMessage());
        }
    }

    /**
     * 章节排序
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     **/
    @Operation(summary = "章节排序")
    @PatchMapping("/projects/{projectId}/units/reorder")
    public CommonResult<Map<String, Object>> reorder(@PathVariable String projectId,
                                                     @RequestBody @Valid ZyApiUnitReorderParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        List<String> unitIds = zyUnitService.reorder(userId, projectId, param.getUnitIds());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unitIds", unitIds);
        return CommonResult.data(result);
    }

    /**
     * 获取章节工作区(章节 + 暂空的子资源，待角色/分镜/资产模块补齐)
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     **/
    @Operation(summary = "获取章节工作区")
    @GetMapping("/projects/{projectId}/units/{unitId}/workspace")
    public CommonResult<Map<String, Object>> workspace(@PathVariable String projectId, @PathVariable String unitId) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyUnit unit = zyUnitService.get(userId, projectId, unitId);
        if (ObjectUtil.isEmpty(unit)) {
            return CommonResult.error("章节不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unit", toUnit(unit));
        // 章节下真实分镜与其版本
        List<Map<String, Object>> shots = new ArrayList<>();
        List<Map<String, Object>> shotRevisions = new ArrayList<>();
        for (ZyShot shot : zyShotService.listByUnit(userId, projectId, unitId)) {
            shots.add(toShot(shot));
            for (ZyShotRevision revision : zyShotService.listRevisionsByShot(shot.getId())) {
                shotRevisions.add(toRevision(revision));
            }
        }
        result.put("shots", shots);
        result.put("shotRevisions", shotRevisions);
        result.put("workflows", zyWorkflowService.listWorkflowsByProject(projectId));
        List<Map<String, Object>> shotArtifacts = new ArrayList<>();
        List<String> shotIds = shots.stream().map(s -> (String) s.get("id")).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toList());
        QueryWrapper<ZyShotArtifact> artifactQuery = new QueryWrapper<ZyShotArtifact>();
        if (!shotIds.isEmpty()) {
            artifactQuery.and(w -> w.eq("project_id", projectId).or().in("shot_id", shotIds));
        } else {
            artifactQuery.eq("project_id", projectId);
        }
        for (ZyShotArtifact artifact : zyShotArtifactMapper.selectList(artifactQuery.orderByDesc("create_time"))) {
            shotArtifacts.add(toArtifact(artifact));
        }
        result.put("shotArtifacts", shotArtifacts);
        result.put("shotReferences", zyShotService.listAssetsByProject(projectId));
        result.put("assetCandidates", new ArrayList<>());
        Object workspaceAssets = zyAssetService.listAssets(userId, projectId, null, null, null, null, null, null, null).get("assets");
        result.put("assets", workspaceAssets != null ? workspaceAssets : new ArrayList<>());
        result.put("tasks", new ArrayList<>());
        return CommonResult.data(result);
    }

    /**
     * 创建(或复用)章节生产工作流
     *
     * @author hanbin
     * @date 2026/09/09 16:50
     **/
    @Operation(summary = "创建(或复用)章节生产工作流")
    @PostMapping("/projects/{projectId}/workflows")
    public CommonResult<Map<String, Object>> createWorkflow(@PathVariable String projectId,
                                                            @RequestBody(required = false) Map<String, Object> req) {
        String userId = StpClientUtil.getLoginIdAsString();
        String unitId = req != null && req.get("unitId") != null ? req.get("unitId").toString() : null;
        if (ObjectUtil.isEmpty(unitId)) {
            return CommonResult.error("章节id不能为空");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("workflow", zyWorkflowService.createWorkflow(userId, projectId, unitId));
        return CommonResult.data(result);
    }

    /**
     * 实体转章节对象
     *
     * @param unit 章节实体
     * @return 章节map
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    private Map<String, Object> toUnit(ZyUnit unit) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", unit.getId());
        result.put("projectId", unit.getProjectId());
        result.put("kind", unit.getKind());
        result.put("title", unit.getTitle());
        result.put("sourceText", unit.getSourceText());
        result.put("wordCount", unit.getWordCount());
        result.put("status", unit.getStatus());
        result.put("position", unit.getPosition());
        result.put("createdAt", unit.getCreateTime() != null ? DateUtil.formatDateTime(unit.getCreateTime()) : null);
        result.put("updatedAt", unit.getUpdateTime() != null ? DateUtil.formatDateTime(unit.getUpdateTime()) : unit.getCreateTime() != null ? DateUtil.formatDateTime(unit.getCreateTime()) : null);
        return result;
    }

    /**
     * 实体转分镜对象
     *
     * @param shot 分镜实体
     * @return 分镜map
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    private Map<String, Object> toShot(ZyShot shot) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", shot.getId());
        result.put("projectId", shot.getProjectId());
        result.put("unitId", shot.getUnitId());
        result.put("currentRevisionId", shot.getCurrentRevisionId());
        result.put("title", shot.getTitle());
        result.put("description", shot.getDescription());
        result.put("position", shot.getPosition());
        result.put("durationMs", shot.getDurationMs());
        result.put("status", shot.getStatus());
        result.put("createdAt", shot.getCreateTime() != null ? DateUtil.formatDateTime(shot.getCreateTime()) : null);
        result.put("updatedAt", shot.getUpdateTime() != null ? DateUtil.formatDateTime(shot.getUpdateTime()) : shot.getCreateTime() != null ? DateUtil.formatDateTime(shot.getCreateTime()) : null);
        return result;
    }

    /**
     * 实体转分镜版本对象
     *
     * @param revision 版本实体
     * @return 版本map
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    private Map<String, Object> toRevision(ZyShotRevision revision) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", revision.getId());
        result.put("shotId", revision.getShotId());
        result.put("version", revision.getVersion());
        result.put("plotDescription", revision.getPlotDescription());
        result.put("action", revision.getAction());
        result.put("dialogue", revision.getDialogue());
        result.put("shotSize", revision.getShotSize());
        result.put("cameraAngle", revision.getCameraAngle());
        result.put("cameraMovement", revision.getCameraMovement());
        result.put("durationMs", revision.getDurationMs());
        result.put("imagePrompt", revision.getImagePrompt());
        result.put("videoPrompt", revision.getVideoPrompt());
        result.put("negativePrompt", revision.getNegativePrompt());
        result.put("continuityNotes", revision.getContinuityNotes());
        result.put("actionBeatsJson", revision.getActionBeatsJson());
        result.put("createdBy", revision.getCreatedBy());
        result.put("createdAt", revision.getCreateTime() != null ? DateUtil.formatDateTime(revision.getCreateTime()) : null);
        return result;
    }

    /**
     * 实体转分镜产物对象
     *
     * @param artifact 产物实体
     * @return 产物map
     * @author hanbin
     * @date 2026/09/09 17:20
     */
    private Map<String, Object> toArtifact(ZyShotArtifact artifact) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", artifact.getId());
        result.put("projectId", artifact.getProjectId());
        result.put("unitId", artifact.getUnitId());
        result.put("shotId", artifact.getShotId());
        result.put("revisionId", artifact.getRevisionId());
        result.put("taskId", artifact.getTaskId());
        result.put("type", artifact.getType());
        result.put("version", artifact.getVersion() != null ? artifact.getVersion() : 1);
        result.put("resourceId", artifact.getResourceId());
        result.put("url", artifact.getUrl());
        result.put("mediaType", artifact.getMediaType());
        result.put("status", artifact.getStatus());
        result.put("selected", artifact.getSelected() != null && artifact.getSelected());
        result.put("metadataJson", artifact.getMetadataJson() != null ? artifact.getMetadataJson() : "");
        result.put("createdAt", artifact.getCreateTime() != null ? DateUtil.formatDateTime(artifact.getCreateTime()) : null);
        result.put("updatedAt", artifact.getUpdateTime() != null ? DateUtil.formatDateTime(artifact.getUpdateTime()) : artifact.getCreateTime() != null ? DateUtil.formatDateTime(artifact.getCreateTime()) : null);
        return result;
    }
}
