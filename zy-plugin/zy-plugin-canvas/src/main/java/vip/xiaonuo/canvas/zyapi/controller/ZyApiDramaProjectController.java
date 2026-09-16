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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.asset.mapper.ZyAssetMapper;
import vip.xiaonuo.canvas.modular.assetcandidate.entity.ZyAssetCandidate;
import vip.xiaonuo.canvas.modular.assetcandidate.mapper.ZyAssetCandidateMapper;
import vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject;
import vip.xiaonuo.canvas.modular.project.entity.ZyProject;
import vip.xiaonuo.canvas.modular.shot.entity.ZyShot;
import vip.xiaonuo.canvas.modular.shotartifact.entity.ZyShotArtifact;
import vip.xiaonuo.canvas.modular.shotartifact.mapper.ZyShotArtifactMapper;
import vip.xiaonuo.canvas.modular.unit.entity.ZyUnit;
import vip.xiaonuo.canvas.zyapi.param.ZyApiDramaProjectParam;
import vip.xiaonuo.canvas.modular.asset.mapper.ZyAssetMapper;
import vip.xiaonuo.canvas.modular.assetcandidate.mapper.ZyAssetCandidateMapper;
import vip.xiaonuo.canvas.zyapi.service.ZyApiDramaProjectService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiProjectCanvasService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiProjectService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiShotService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiUnitService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 短剧项目接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 19:00
 **/
@Tag(name = "短剧项目")
@RestController
public class ZyApiDramaProjectController {

    @Resource
    private ZyApiDramaProjectService zyDramaProjectService;

    @Resource
    private ZyApiUnitService zyUnitService;

    @Resource
    private ZyApiShotService zyShotService;

    @Resource
    private ZyApiProjectService zyProjectService;

    @Resource
    private ZyApiProjectCanvasService zyProjectCanvasService;

    @Resource
    private ZyAssetMapper zyAssetMapper;

    @Resource
    private ZyAssetCandidateMapper zyAssetCandidateMapper;

    @Resource
    private ZyShotArtifactMapper zyShotArtifactMapper;

    /**
     * 获取短剧项目列表
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     **/
    @Operation(summary = "获取短剧项目列表")
    @GetMapping("/projects")
    public CommonResult<Map<String, Object>> list(@RequestParam(required = false) Integer page,
                                                  @RequestParam(name = "page_size", required = false) Integer pageSize) {
        String userId = StpClientUtil.getLoginIdAsString();
        Page<ZyDramaProject> pageResult = zyDramaProjectService.listPage(userId, page, pageSize);
        List<Map<String, Object>> projects = new ArrayList<>();
        for (ZyDramaProject project : pageResult.getRecords()) {
            projects.add(toSummary(project));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projects", projects);
        if (ObjectUtil.isNotEmpty(page) && ObjectUtil.isNotEmpty(pageSize)) {
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("total", pageResult.getTotal());
            result.put("hasMore", (long) (page * pageSize) < pageResult.getTotal());
        }
        return CommonResult.data(result);
    }

    /**
     * 获取短剧项目详情(子资源暂返回空数组，待子模块补齐)
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     **/
    @Operation(summary = "获取短剧项目详情")
    @GetMapping("/projects/{id}")
    public CommonResult<Map<String, Object>> detail(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyDramaProject project = zyDramaProjectService.getById(userId, id);
        if (ObjectUtil.isEmpty(project)) {
            return CommonResult.error("短剧项目不存在");
        }
        // 查询项目章节
        List<ZyUnit> unitList = zyUnitService.list(new QueryWrapper<ZyUnit>()
                .eq("project_id", id)
                .orderByAsc("position"));
        List<Map<String, Object>> units = new ArrayList<>();
        for (ZyUnit unit : unitList) {
            units.add(toUnit(unit));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", toProject(project));
        result.put("units", units);
        result.put("canvases", new ArrayList<>());
        result.put("canvasUnitLinks", new ArrayList<>());
        result.put("unitCanvasCounts", new LinkedHashMap<>());
        result.put("assets", new ArrayList<>());
        result.put("assetFolders", new ArrayList<>());
        result.put("workflows", new ArrayList<>());
        result.put("shots", new ArrayList<>());
        result.put("shotRevisions", new ArrayList<>());
        result.put("shotArtifacts", new ArrayList<>());
        result.put("shotReferences", new ArrayList<>());
        result.put("assetCandidates", new ArrayList<>());
        result.put("tasks", new ArrayList<>());
        return CommonResult.data(result);
    }

    /**
     * 获取短剧项目核心(仅项目)
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     **/
    @Operation(summary = "获取短剧项目核心(仅项目)")
    @GetMapping("/projects/{id}/core")
    public CommonResult<Map<String, Object>> core(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyDramaProject project = zyDramaProjectService.getById(userId, id);
        if (ObjectUtil.isEmpty(project)) {
            return CommonResult.error("短剧项目不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", toProject(project));
        return CommonResult.data(result);
    }

    /**
     * 获取短剧项目概览(暂无子资源时计数为0)
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     **/
    @Operation(summary = "获取短剧项目概览")
    @GetMapping("/projects/{id}/overview")
    public CommonResult<Map<String, Object>> overview(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyDramaProject project = zyDramaProjectService.getById(userId, id);
        if (ObjectUtil.isEmpty(project)) {
            return CommonResult.error("短剧项目不存在");
        }
        List<ZyUnit> units = zyUnitService.list(new QueryWrapper<ZyUnit>().eq("project_id", id));
        int unitCount = units.size();
        int completedUnitCount = (int) units.stream().filter((u) -> "completed".equals(u.getStatus())).count();
        long totalWordCount = units.stream().mapToLong((u) -> u.getWordCount() != null ? u.getWordCount() : 0).sum();
        long unitsWithoutText = units.stream().filter((u) -> ObjectUtil.isEmpty(u.getSourceText())).count();
        long unitsWithoutShots = units.stream().filter((u) -> zyShotService.count(new QueryWrapper<ZyShot>().eq("unit_id", u.getId())) == 0).count();
        int shotCount = (int) zyShotService.count(new QueryWrapper<ZyShot>().eq("project_id", id));
        int canvasCount = (int) zyProjectService.count(new QueryWrapper<ZyProject>().eq("project_id", id));
        
        // 查询角色数量（category=character）
        Long characterCountLong = zyAssetMapper.selectCount(
            new QueryWrapper<ZyAsset>().eq("project_id", id).eq("category", "character")
        );
        int characterCount = characterCountLong == null ? 0 : characterCountLong.intValue();
        
        // 查询资产数量
        Long assetCountLong = zyAssetMapper.selectCount(
            new QueryWrapper<ZyAsset>().eq("project_id", id)
        );
        int assetCount = assetCountLong == null ? 0 : assetCountLong.intValue();
        
        // 查询待确认候选数量
        Long pendingCandidateCountLong = zyAssetCandidateMapper.selectCount(
            new QueryWrapper<ZyAssetCandidate>().eq("project_id", id).eq("status", "pending_confirmation")
        );
        int pendingCandidateCount = pendingCandidateCountLong == null ? 0 : pendingCandidateCountLong.intValue();
        
        // 产物以 zy_shot_artifact 为准（分镜图/预演/镜头视频），不要按 zy_asset.category 统计
        // storeMedia 写入的资产是 category=other/status=active，不会命中旧的 previz/video 过滤
        long readyStoryboardCount = zyShotArtifactMapper.selectCount(
            new QueryWrapper<ZyShotArtifact>().eq("project_id", id).eq("type", "storyboard").eq("status", "ready").eq("selected", true)
        );

        long readyPrevizCount = zyShotArtifactMapper.selectCount(
            new QueryWrapper<ZyShotArtifact>().eq("project_id", id).eq("type", "previz").eq("status", "ready").eq("selected", true)
        );

        long readyVideoCount = zyShotArtifactMapper.selectCount(
            new QueryWrapper<ZyShotArtifact>().eq("project_id", id).eq("type", "video").eq("status", "ready").eq("selected", true)
        );
        
        // 查询渲染成功数量（generation_task status='succeeded'）
        // 这个统计暂时为0，因为需要 generation_task 的 projectId 关联
        int renderSucceededCount = 0;
        
        // 查询过期工件数量（asset status='stale'）
        Long staleArtifactCountLong = zyAssetMapper.selectCount(
            new QueryWrapper<ZyAsset>().eq("project_id", id).eq("status", "stale")
        );
        int staleArtifactCount = staleArtifactCountLong == null ? 0 : staleArtifactCountLong.intValue();

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("unitCount", unitCount);
        metrics.put("completedUnitCount", completedUnitCount);
        metrics.put("totalWordCount", totalWordCount);
        metrics.put("unitsWithoutText", unitsWithoutText);
        metrics.put("unitsWithoutShots", unitsWithoutShots);
        metrics.put("canvasCount", canvasCount);
        metrics.put("characterCount", characterCount);
        metrics.put("assetCount", assetCount);
        metrics.put("shotCount", shotCount);
        metrics.put("pendingCandidateCount", pendingCandidateCount);
        metrics.put("readyStoryboardCount", (int) readyStoryboardCount);
        metrics.put("readyPrevizCount", (int) readyPrevizCount);
        metrics.put("readyVideoCount", (int) readyVideoCount);
        metrics.put("renderSucceededCount", renderSucceededCount);
        metrics.put("staleArtifactCount", staleArtifactCount);
        List<Map<String, Object>> overviewUnits = new ArrayList<>();
        for (ZyUnit unit : units) {
            Map<String, Object> unitMap = new LinkedHashMap<>();
            unitMap.put("unit", toUnit(unit));
            unitMap.put("shotCount", (int) zyShotService.count(new QueryWrapper<ZyShot>().eq("unit_id", unit.getId())));
            unitMap.put("candidateCount", 0);
            unitMap.put("canvasCount", (int) zyProjectService.count(new QueryWrapper<ZyProject>().eq("project_id", id)));
            overviewUnits.add(unitMap);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metrics", metrics);
        result.put("units", overviewUnits);
        return CommonResult.data(result);
    }

    /**
     * 新建短剧项目
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     **/
    @Operation(summary = "新建短剧项目")
    @PostMapping("/projects")
    public CommonResult<Map<String, Object>> create(@RequestBody @Valid ZyApiDramaProjectParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyDramaProject project = zyDramaProjectService.create(userId, param);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", toProject(project));
        return CommonResult.data(result);
    }

    /**
     * 更新短剧项目
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     **/
    @Operation(summary = "更新短剧项目")
    @PatchMapping("/projects/{id}")
    public CommonResult<Map<String, Object>> update(@PathVariable String id,
                                                    @RequestBody @Valid ZyApiDramaProjectParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyDramaProject project = zyDramaProjectService.update(userId, id, param);
        if (ObjectUtil.isEmpty(project)) {
            return CommonResult.error("权限修改项目设置，只有项目创建者可以设置");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", toProject(project));
        return CommonResult.data(result);
    }

    /**
     * 删除短剧项目
     *
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     **/
    @Operation(summary = "删除短剧项目")
    @DeleteMapping("/projects/{id}")
    public CommonResult<Map<String, Object>> delete(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        try {
            zyDramaProjectService.delete(userId, id);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", id);
            return CommonResult.data(result);
        } catch (IllegalArgumentException e) {
            return CommonResult.error(e.getMessage());
        }
    }

    /**
     * 关联/更换/取消关联团队(请求体传teamId，传空取消关联)
     *
     * @author xuyuxiang
     * @date 2026/9/6 12:00
     **/
    @Operation(summary = "关联/取消关联团队")
    @PutMapping("/projects/{id}/team")
    public CommonResult<Map<String, Object>> updateTeam(@PathVariable String id,
                                                        @RequestBody Map<String, Object> input) {
        String userId = StpClientUtil.getLoginIdAsString();
        String teamId = input.get("teamId") != null ? String.valueOf(input.get("teamId")).trim() : "";
        ZyDramaProject project;
        try {
            project = zyDramaProjectService.updateTeam(userId, id, teamId);
        } catch (IllegalArgumentException e) {
            return CommonResult.error(e.getMessage());
        }
        if (ObjectUtil.isEmpty(project)) {
            return CommonResult.error("短剧项目不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", toProject(project));
        return CommonResult.data(result);
    }

    /**
     * 实体转项目对象
     *
     * @return 项目map
     * @author xuyuxiang
     * @date 2026/9/5 19:00
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

    private Map<String, Object> toProject(ZyDramaProject project) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", project.getId());
        result.put("userId", project.getUserId());
        result.put("teamId", project.getTeamId());
        result.put("name", project.getName());
        result.put("type", project.getType());
        result.put("aspectRatio", project.getAspectRatio());
        result.put("sourceType", project.getSourceType());
        result.put("description", project.getDescription());
        result.put("coverResourceId", project.getCoverResourceId());
        result.put("stylePresetId", project.getStylePresetId());
        result.put("styleProfileJson", project.getStyleProfileJson());
        result.put("defaultImageModel", project.getDefaultImageModel());
        result.put("defaultVideoModel", project.getDefaultVideoModel());
        result.put("status", project.getStatus());
        result.put("revision", project.getRevision());
        result.put("createdAt", project.getCreateTime() != null ? DateUtil.formatDateTime(project.getCreateTime()) : null);
        result.put("updatedAt", project.getUpdateTime() != null ? DateUtil.formatDateTime(project.getUpdateTime()) : project.getCreateTime() != null ? DateUtil.formatDateTime(project.getCreateTime()) : null);
        return result;
    }

    /**
     * 实体转摘要(项目+计数)
     *
     * @param project 项目实体
     * @return 摘要map
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     */
    private Map<String, Object> toSummary(ZyDramaProject project) {
        String projectId = project.getId();
        
        // 查询章节数量
        int unitCount = (int) zyUnitService.count(
            new QueryWrapper<ZyUnit>().eq("project_id", projectId)
        );
        
        // 查询已完成章节数量
        int completedUnitCount = (int) zyUnitService.count(
            new QueryWrapper<ZyUnit>().eq("project_id", projectId).eq("status", "completed")
        );
        
        // 查询画布数量
        int canvasCount = (int) zyProjectService.count(
            new QueryWrapper<ZyProject>().eq("project_id", projectId)
        );
        
        // 查询资产数量
        Long assetCountLong = zyAssetMapper.selectCount(
            new QueryWrapper<ZyAsset>().eq("project_id", projectId)
        );
        int assetCount = assetCountLong == null ? 0 : assetCountLong.intValue();
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", toProject(project));
        result.put("canvasCount", canvasCount);
        result.put("assetCount", assetCount);
        result.put("unitCount", unitCount);
        result.put("completedUnitCount", completedUnitCount);
        return result;
    }

    /**
     * 获取项目画布列表
     *
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     **/
    @Operation(summary = "获取项目画布列表")
    @GetMapping("/projects/{id}/canvases")
    public CommonResult<Map<String, Object>> listCanvases(@PathVariable String id,
                                                          @RequestParam(required = false) Integer page,
                                                          @RequestParam(name = "page_size", required = false) Integer pageSize) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyProjectCanvasService.listCanvases(userId, id, page, pageSize));
    }

    /**
     * 关联画布与章节
     *
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     **/
    @Operation(summary = "关联画布与章节")
    @PostMapping("/projects/{id}/canvas-links")
    public CommonResult<Map<String, Object>> linkCanvasUnit(@PathVariable String id,
                                                             @RequestBody Map<String, String> input) {
        String userId = StpClientUtil.getLoginIdAsString();
        String canvasId = input.get("canvasId");
        String unitId = input.get("unitId");
        String role = input.get("role");
        return CommonResult.data(zyProjectCanvasService.linkCanvasUnit(userId, id, canvasId, unitId, role));
    }

    /**
     * 解除画布与章节的关联
     *
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     **/
    @Operation(summary = "解除画布与章节的关联")
    @DeleteMapping("/projects/{id}/canvas-links/{canvasId}/units/{unitId}")
    public CommonResult<Map<String, Object>> unlinkCanvasUnit(@PathVariable String id,
                                                              @PathVariable String canvasId,
                                                              @PathVariable String unitId) {
        String userId = StpClientUtil.getLoginIdAsString();
        zyProjectCanvasService.unlinkCanvasUnit(userId, id, canvasId, unitId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("canvasId", canvasId);
        result.put("unitId", unitId);
        return CommonResult.data(result);
    }

    /**
     * 解除画布与项目的关联
     *
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     **/
    @Operation(summary = "解除画布与项目的关联")
    @DeleteMapping("/projects/{id}/canvases/{canvasId}")
    public CommonResult<Map<String, Object>> unlinkCanvasProject(@PathVariable String id,
                                                                 @PathVariable String canvasId) {
        String userId = StpClientUtil.getLoginIdAsString();
        zyProjectCanvasService.unlinkCanvasProject(userId, id, canvasId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("canvasId", canvasId);
        return CommonResult.data(result);
    }
}
