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
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.shot.entity.ZyShot;
import vip.xiaonuo.canvas.zyapi.param.ZyApiShotParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiShotReplaceParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiShotRevisionInputParam;
import vip.xiaonuo.canvas.zyapi.service.ZyApiShotService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 短剧项目分镜接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 20:00
 **/
@Tag(name = "短剧项目分镜")
@RestController
public class ZyApiShotController {

    @Resource
    private ZyApiShotService zyShotService;

    /**
     * 保存(新增/更新)分镜
     *
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     **/
    @Operation(summary = "保存(新增/更新)分镜")
    @PostMapping("/projects/{projectId}/shots")
    public CommonResult<Map<String, Object>> save(@PathVariable String projectId,
                                                  @RequestBody @Valid ZyApiShotParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        try {
            ZyShot shot = zyShotService.save(userId, projectId, param);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("shot", toShot(shot));
            return CommonResult.data(result);
        } catch (Exception e) {
            return CommonResult.error(e.getMessage());
        }
    }

    /**
     * 删除分镜
     *
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     **/
    @Operation(summary = "删除分镜")
    @DeleteMapping("/projects/{projectId}/shots/{shotId}")
    public CommonResult<Map<String, Object>> delete(@PathVariable String projectId, @PathVariable String shotId) {
        String userId = StpClientUtil.getLoginIdAsString();
        try {
            zyShotService.delete(userId, projectId, shotId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("deleted", true);
            return CommonResult.data(result);
        } catch (Exception e) {
            return CommonResult.error(e.getMessage());
        }
    }

    /**
     * 按章节批量替换分镜
     *
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     **/
    @Operation(summary = "按章节批量替换分镜")
    @PutMapping("/projects/{projectId}/units/{unitId}/shots")
    public CommonResult<Map<String, Object>> replace(@PathVariable String projectId,
                                                     @PathVariable String unitId,
                                                     @RequestBody @Valid ZyApiShotReplaceParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        List<Map<String, Object>> shots = new ArrayList<>();
        for (ZyShot shot : zyShotService.replaceByUnit(userId, projectId, unitId, param.getShots())) {
            shots.add(toShot(shot));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shots", shots);
        return CommonResult.data(result);
    }

    /**
     * 创建分镜版本
     *
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     **/
    @Operation(summary = "创建分镜版本")
    @PostMapping("/projects/{projectId}/shots/{shotId}/revisions")
    public CommonResult<Map<String, Object>> createRevision(@PathVariable String projectId,
                                                            @PathVariable String shotId,
                                                            @RequestBody @Valid ZyApiShotRevisionInputParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        Map<String, Object> data = zyShotService.createRevision(userId, projectId, shotId, param);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shot", toShot((ZyShot) data.get("shot")));
        result.put("revision", data.get("revision"));
        return CommonResult.data(result);
    }

    /**
     * 实体转分镜对象
     *
     * @param shot 分镜实体
     * @return 分镜map
     * @author xuyuxiang
     * @date 2026/9/5 20:00
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
     * 绑定资产到镜头
     *
     * @author hanbin
     * @date 2026/09/09 16:20
     **/
    @Operation(summary = "绑定资产到镜头")
    @PostMapping("/projects/{projectId}/shots/{shotId}/assets")
    public CommonResult<Map<String, Object>> linkAsset(@PathVariable String projectId,
                                                       @PathVariable String shotId,
                                                       @RequestBody Map<String, Object> req) {
        String userId = StpClientUtil.getLoginIdAsString();
        String assetVersionId = req.get("assetVersionId") != null ? req.get("assetVersionId").toString() : null;
        String role = req.get("role") != null ? req.get("role").toString() : "reference";
        return CommonResult.data(zyShotService.linkAsset(userId, projectId, shotId, assetVersionId, role));
    }

    /**
     * 解绑镜头资产
     *
     * @author hanbin
     * @date 2026/09/09 16:20
     **/
    @Operation(summary = "解绑镜头资产")
    @DeleteMapping("/projects/{projectId}/shots/{shotId}/assets/{referenceId}")
    public CommonResult<Map<String, Object>> unlinkAsset(@PathVariable String projectId,
                                                         @PathVariable String shotId,
                                                         @PathVariable String referenceId) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyShotService.unlinkAsset(userId, projectId, shotId, referenceId));
    }

    /**
     * 获取镜头资产绑定列表
     *
     * @author hanbin
     * @date 2026/09/09 16:20
     **/
    @Operation(summary = "获取镜头资产绑定列表")
    @GetMapping("/projects/{projectId}/shots/{shotId}/assets")
    public CommonResult<List<Map<String, Object>>> listShotAssets(@PathVariable String projectId,
                                                                  @PathVariable String shotId) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyShotService.listAssetsByProject(projectId));
    }
}
