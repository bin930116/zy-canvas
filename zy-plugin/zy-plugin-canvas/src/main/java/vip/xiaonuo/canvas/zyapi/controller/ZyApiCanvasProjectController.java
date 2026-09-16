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
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.project.entity.ZyProject;
import vip.xiaonuo.canvas.zyapi.param.ZyApiProjectUpsertParam;
import vip.xiaonuo.canvas.zyapi.result.ZyApiProjectSummaryResult;
import vip.xiaonuo.canvas.zyapi.service.ZyApiProjectService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 画布项目接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 18:30
 **/
@Tag(name = "画布项目")
@RestController
public class ZyApiCanvasProjectController {

    @Resource
    private ZyApiProjectService zyProjectService;

    /**
     * 获取画布项目列表
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     **/
    @Operation(summary = "获取画布项目列表")
    @GetMapping("/canvas-projects")
    public CommonResult<Map<String, Object>> list(@RequestParam(required = false) Integer page,
                                                  @RequestParam(name = "page_size", required = false) Integer pageSize,
                                                  @RequestParam(name = "project_id", required = false) String projectId,
                                                  @RequestParam(name = "q", required = false) String q,
                                                  @RequestParam(name = "sort", required = false) String sort) {
        String userId = StpClientUtil.getLoginIdAsString();
        QueryWrapper<ZyProject> wrapper = new QueryWrapper<ZyProject>().eq("user_id", userId);
        // project_id 为 "all" 或空时不过滤(前端画布列表默认 all)
        if (ObjectUtil.isNotEmpty(projectId) && !"all".equals(projectId)) {
            wrapper.eq("project_id", projectId);
        }
        if (ObjectUtil.isNotEmpty(q)) {
            wrapper.like("title", q);
        }
        if ("title".equals(sort)) {
            wrapper.orderByAsc("title");
        } else if ("createdAt".equals(sort)) {
            wrapper.orderByDesc("create_time");
        } else {
            wrapper.orderByDesc("update_time");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        if (ObjectUtil.isNotEmpty(page) && ObjectUtil.isNotEmpty(pageSize)) {
            // 走分页插件，避免 easy-trans 在 PG 下因严格 GROUP BY 报错(与 Snowy 一致)
            Page<ZyProject> pageResult = zyProjectService.page(new Page<>(page, pageSize), wrapper);
            List<Map<String, Object>> projects = new ArrayList<>();
            for (ZyProject project : pageResult.getRecords()) {
                projects.add(toCanvasLibrarySummary(project));
            }
            result.put("projects", projects);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("total", pageResult.getTotal());
            result.put("hasMore", (long) (page * pageSize) < pageResult.getTotal());
            return CommonResult.data(result);
        }
        List<ZyApiProjectSummaryResult> summaries = zyProjectService.listSummary(userId);
        result.put("projects", summaries);
        return CommonResult.data(result);
    }

    /**
     * 获取画布项目详情
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     **/
    @Operation(summary = "获取画布项目详情")
    @GetMapping("/canvas-projects/{id}")
    public CommonResult<Map<String, Object>> detail(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyProject project = zyProjectService.getById(userId, id);
        Map<String, Object> result = new LinkedHashMap<>();

        if (ObjectUtil.isEmpty(project)) {
            result.put("project", new LinkedHashMap<>());
            return CommonResult.data(result);
        }
        result.put("project", ObjectUtil.isNotEmpty(project.getDataJson()) ? JSONUtil.parseObj(project.getDataJson()) : new LinkedHashMap<>());
        return CommonResult.data(result);
    }

    /**
     * 保存画布项目(新增或更新)
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     **/
    @Operation(summary = "保存画布项目(新增或更新)")
    @PutMapping("/canvas-projects/{id}")
    public CommonResult<Map<String, Object>> upsert(@PathVariable String id,
                                                    @RequestBody @Valid ZyApiProjectUpsertParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyProject project = zyProjectService.upsert(userId, id, param.getProject());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", toSummaryMap(project));
        return CommonResult.data(result);
    }

    /**
     * 删除画布项目
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     **/
    @Operation(summary = "删除画布项目")
    @DeleteMapping("/canvas-projects/{id}")
    public CommonResult<Map<String, Object>> delete(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        zyProjectService.delete(userId, id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        return CommonResult.data(result);
    }

    /**
     * 实体转画布库摘要(含节点数与预览节点，用于分页列表)
     *
     * @param project 项目实体
     * @return 摘要map
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    private Map<String, Object> toCanvasLibrarySummary(ZyProject project) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", project.getId());
        result.put("projectId", project.getProjectId());
        result.put("title", project.getTitle());
        result.put("createdAt", project.getCreateTime() != null ? DateUtil.formatDateTime(project.getCreateTime()) : null);
        result.put("updatedAt", project.getUpdateTime() != null ? DateUtil.formatDateTime(project.getUpdateTime()) : null);
        Object nodesObj = null;
        if (ObjectUtil.isNotEmpty(project.getDataJson())) {
            nodesObj = JSONUtil.parseObj(project.getDataJson()).get("nodes");
        }
        JSONArray nodes = nodesObj instanceof JSONArray ? (JSONArray) nodesObj : new JSONArray();
        result.put("nodeCount", nodes.size());
        result.put("previewNodes", nodes);
        return result;
    }

    /**
     * 实体转摘要map
     *
     * @param project 项目实体
     * @return 摘要map
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    private Map<String, Object> toSummaryMap(ZyProject project) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", project.getId());
        result.put("projectId", project.getProjectId());
        result.put("title", project.getTitle());
        result.put("createdAt", project.getCreateTime() != null ? DateUtil.formatDateTime(project.getCreateTime()) : null);
        result.put("updatedAt", project.getUpdateTime() != null ? DateUtil.formatDateTime(project.getUpdateTime()) : null);
        return result;
    }
}
