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


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.project.entity.ZyProject;
import vip.xiaonuo.canvas.zyapi.service.ZyApiProjectService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiUserDataService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台用户数据接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 18:40
 **/
@Tag(name = "平台用户数据")
@RestController
public class ZyApiUserDataController {

    @Resource
    private ZyApiProjectService zyProjectService;

    @Resource
    private ZyApiUserDataService zyUserDataService;

    /**
     * 获取登录用户数据聚合快照(画布项目 + 素材)
     * <p>
     * 平台登录时会拉取该快照作为"服务端实体真相"，素材暂返回空，由素材模块(P4)补充。
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "获取登录用户数据聚合快照")
    @GetMapping("/user-data/snapshot")
    public CommonResult<Map<String, Object>> snapshot() {
        String userId = StpClientUtil.getLoginIdAsString();
        List<Object> projects = new ArrayList<>();
        // 走分页插件，避免 easy-trans 在 PG 下因严格 GROUP BY 报错(与 Snowy 一致)
        List<ZyProject> projectList = zyProjectService.page(new Page<>(1, 10000, false), new QueryWrapper<ZyProject>().eq("user_id", userId)).getRecords();
        for (ZyProject project : projectList) {
            if (ObjectUtil.isNotEmpty(project.getDataJson())) {
                projects.add(JSONUtil.parseObj(project.getDataJson()));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assets", new ArrayList<>());
        result.put("projects", projects);
        return CommonResult.data(result);
    }

    /**
     * 资产列表(带分页/过滤；无分页参数时返回摘要)
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "资产列表")
    @GetMapping("/assets")
    public CommonResult<Map<String, Object>> listAssets(@RequestParam(required = false) Integer page,
                                                       @RequestParam(name = "page_size", required = false) Integer pageSize,
                                                       @RequestParam(required = false) String kind,
                                                       @RequestParam(required = false) String category,
                                                       @RequestParam(name = "folder_id", required = false) String folderId,
                                                       @RequestParam(name = "uncategorized", required = false) Boolean uncategorized,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String q) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyUserDataService.listAssets(userId, page, pageSize, kind, category, folderId, uncategorized, status, q));
    }

    /**
     * 批量获取资产详情
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "批量获取资产详情")
    @PostMapping("/assets/batch")
    public CommonResult<Map<String, Object>> listAssetsByIds(@RequestBody Map<String, List<String>> req) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyUserDataService.listAssetsByIds(userId, req.get("ids")));
    }

    /**
     * 获取单个资产详情
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "获取单个资产详情")
    @GetMapping("/assets/{id}")
    public CommonResult<Map<String, Object>> getAsset(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyUserDataService.getAsset(userId, id));
    }

    /**
     * 创建或更新资产
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "创建或更新资产")
    @PutMapping("/assets/{id}")
    public CommonResult<Map<String, Object>> upsertAsset(@PathVariable String id, @RequestBody Map<String, Object> req) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyUserDataService.upsertAsset(userId, req.get("asset")));
    }

    /**
     * 删除资产
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "删除资产")
    @DeleteMapping("/assets/{id}")
    public CommonResult<Map<String, Object>> deleteAsset(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyUserDataService.deleteAsset(userId, id));
    }

    /**
     * 资产文件夹列表
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "资产文件夹列表")
    @GetMapping("/asset-folders")
    public CommonResult<Map<String, Object>> listFolders() {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyUserDataService.listFolders(userId));
    }

    /**
     * 新建资产文件夹
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "新建资产文件夹")
    @PostMapping("/asset-folders")
    public CommonResult<Map<String, Object>> createFolder(@RequestBody Map<String, String> req) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyUserDataService.createFolder(userId, req.get("name")));
    }

    /**
     * 更新资产文件夹
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "更新资产文件夹")
    @PatchMapping("/asset-folders/{id}")
    public CommonResult<Map<String, Object>> updateFolder(@PathVariable String id, @RequestBody Map<String, String> req) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyUserDataService.updateFolder(userId, id, req.get("name")));
    }

    /**
     * 删除资产文件夹
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "删除资产文件夹")
    @DeleteMapping("/asset-folders/{id}")
    public CommonResult<Map<String, Object>> deleteFolder(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyUserDataService.deleteFolder(userId, id));
    }

    /**
     * 移动资产到文件夹（暂未实现）
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     **/
    @Operation(summary = "移动资产到文件夹")
    @PatchMapping("/assets/folder")
    public CommonResult<Map<String, Object>> moveAssetsToFolder(@RequestBody Map<String, Object> req) {
        String userId = StpClientUtil.getLoginIdAsString();
        @SuppressWarnings("unchecked")
        List<String> assetIds = (List<String>) req.get("assetIds");
        String folderId = (String) req.get("folderId");
        return CommonResult.data(zyUserDataService.moveAssetsToFolder(userId, assetIds, folderId));
    }
}
