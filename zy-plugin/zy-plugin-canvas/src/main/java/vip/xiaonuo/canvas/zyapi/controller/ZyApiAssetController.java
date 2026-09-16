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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.zyapi.param.ZyApiAssetCandidateParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiAssetFolderParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiAssetParam;
import vip.xiaonuo.canvas.zyapi.service.ZyApiAssetService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.Map;

/**
 * 短剧项目资产接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 21:30
 **/
@Tag(name = "短剧项目资产")
@RestController
public class ZyApiAssetController {

    @Resource
    private ZyApiAssetService zyAssetService;

    /**
     * 资产列表
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     **/
    @Operation(summary = "资产列表")
    @GetMapping("/projects/{projectId}/assets")
    public CommonResult<Map<String, Object>> list(@PathVariable String projectId,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(name = "page_size", required = false) Integer pageSize,
                                                  @RequestParam(required = false) String category,
                                                  @RequestParam(name = "media_type", required = false) String mediaType,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(name = "folder_id", required = false) String folderId,
                                                  @RequestParam(required = false) String q) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyAssetService.listAssets(userId, projectId, page, pageSize, category, mediaType, status, folderId, q));
    }

    /**
     * 新建资产
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     **/
    @Operation(summary = "新建资产")
    @PostMapping("/projects/{projectId}/assets")
    public CommonResult<Map<String, Object>> create(@PathVariable String projectId,
                                                    @RequestBody @Valid ZyApiAssetParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyAssetService.createAsset(userId, projectId, param));
    }

    /**
     * 更新资产
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     **/
    @Operation(summary = "更新资产")
    @PatchMapping("/projects/{projectId}/assets/{assetId}")
    public CommonResult<Map<String, Object>> update(@PathVariable String projectId,
                                                    @PathVariable String assetId,
                                                    @RequestBody @Valid ZyApiAssetParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        try {
            return CommonResult.data(zyAssetService.updateAsset(userId, projectId, assetId, param));
        } catch (Exception e) {
            return CommonResult.error(e.getMessage());
        }
    }

    /**
     * 删除资产
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     **/
    @Operation(summary = "删除资产")
    @DeleteMapping("/projects/{projectId}/assets/{assetId}")
    public CommonResult<Map<String, Object>> delete(@PathVariable String projectId, @PathVariable String assetId) {
        String userId = StpClientUtil.getLoginIdAsString();
        try {
            return CommonResult.data(zyAssetService.deleteAsset(userId, projectId, assetId));
        } catch (Exception e) {
            return CommonResult.error(e.getMessage());
        }
    }

    /**
     * 资产加版本
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     **/
    @Operation(summary = "资产加版本")
    @PostMapping("/projects/{projectId}/assets/{assetId}/versions")
    public CommonResult<Map<String, Object>> createVersion(@PathVariable String projectId, @PathVariable String assetId) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyAssetService.createVersion(userId, projectId, assetId));
    }

    /**
     * 资产文件夹列表
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     **/
    @Operation(summary = "资产文件夹列表")
    @GetMapping("/projects/{projectId}/asset-folders")
    public CommonResult<Map<String, Object>> listFolders(@PathVariable String projectId) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyAssetService.listFolders(userId, projectId));
    }

    /**
     * 新建资产文件夹
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     **/
    @Operation(summary = "新建资产文件夹")
    @PostMapping("/projects/{projectId}/asset-folders")
    public CommonResult<Map<String, Object>> createFolder(@PathVariable String projectId,
                                                          @RequestBody @Valid ZyApiAssetFolderParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyAssetService.createFolder(userId, projectId, param));
    }

    /**
     * 更新资产文件夹
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     **/
    @Operation(summary = "更新资产文件夹")
    @PatchMapping("/projects/{projectId}/asset-folders/{folderId}")
    public CommonResult<Map<String, Object>> updateFolder(@PathVariable String projectId,
                                                          @PathVariable String folderId,
                                                          @RequestBody @Valid ZyApiAssetFolderParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyAssetService.updateFolder(userId, projectId, folderId, param));
    }

    /**
     * 删除资产文件夹
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     **/
    @Operation(summary = "删除资产文件夹")
    @DeleteMapping("/projects/{projectId}/asset-folders/{folderId}")
    public CommonResult<Map<String, Object>> deleteFolder(@PathVariable String projectId, @PathVariable String folderId) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyAssetService.deleteFolder(userId, projectId, folderId));
    }

    /**
     * 资产候选列表(带分页/过滤)
     *
     * @author xuyuxiang
     * @date 2026/9/5 22:00
     **/
    @Operation(summary = "资产候选列表")
    @GetMapping("/projects/{projectId}/asset-candidates")
    public CommonResult<Map<String, Object>> listAssetCandidates(@PathVariable String projectId,
                                                                  @RequestParam(required = false) Integer page,
                                                                  @RequestParam(name = "page_size", required = false) Integer pageSize,
                                                                  @RequestParam(name = "unit_id", required = false) String unitId,
                                                                  @RequestParam(required = false) String status,
                                                                  @RequestParam(required = false) String category,
                                                                  @RequestParam(required = false) String q) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyAssetService.listAssetCandidates(userId, projectId, page, pageSize, unitId, status, category, q));
    }

    /**
     * 创建资产候选
     *
     * @author xuyuxiang
     * @date 2026/9/5 22:00
     **/
    @Operation(summary = "创建资产候选")
    @PostMapping("/projects/{projectId}/asset-candidates")
    public CommonResult<Map<String, Object>> createAssetCandidates(@PathVariable String projectId,
                                                                    @RequestBody Map<String, Object> req) {
        String userId = StpClientUtil.getLoginIdAsString();
        // JSON 反序列化得到的是 List<LinkedHashMap>，直接强转会在遍历时 ClassCastException，需显式转换
        Object candidatesRaw = req.get("candidates");
        java.util.List<ZyApiAssetCandidateParam> candidates = candidatesRaw == null
                ? new java.util.ArrayList<>()
                : JSONUtil.toList(JSONUtil.parseArray(JSONUtil.toJsonStr(candidatesRaw)), ZyApiAssetCandidateParam.class);
        String source = (String) req.get("source");
        return CommonResult.data(zyAssetService.createAssetCandidates(userId, projectId, candidates, source));
    }

    /**
     * 确认资产候选
     *
     * @author xuyuxiang
     * @date 2026/9/5 22:00
     **/
    @Operation(summary = "确认资产候选")
    @PostMapping("/projects/{projectId}/asset-candidates/{candidateId}/confirm")
    public CommonResult<Map<String, Object>> confirmAssetCandidate(@PathVariable String projectId,
                                                                    @PathVariable String candidateId,
                                                                    @RequestBody Map<String, Object> req) {
        String userId = StpClientUtil.getLoginIdAsString();
        String assetId = (String) req.get("assetId");
        return CommonResult.data(zyAssetService.confirmAssetCandidate(userId, projectId, candidateId, assetId));
    }
}
