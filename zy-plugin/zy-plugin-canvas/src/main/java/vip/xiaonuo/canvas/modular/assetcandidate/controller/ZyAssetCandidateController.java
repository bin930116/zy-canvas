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
package vip.xiaonuo.canvas.modular.assetcandidate.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.canvas.modular.assetcandidate.entity.ZyAssetCandidate;
import vip.xiaonuo.canvas.modular.assetcandidate.param.ZyAssetCandidateAddParam;
import vip.xiaonuo.canvas.modular.assetcandidate.param.ZyAssetCandidateEditParam;
import vip.xiaonuo.canvas.modular.assetcandidate.param.ZyAssetCandidateIdParam;
import vip.xiaonuo.canvas.modular.assetcandidate.param.ZyAssetCandidatePageParam;
import vip.xiaonuo.canvas.modular.assetcandidate.service.ZyAssetCandidateService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 短剧项目资产候选控制器
 *
 * @author hanbin
 * @date  2026/09/08 14:40
 */
@Tag(name = "短剧项目资产候选控制器")
@RestController
@Validated
public class ZyAssetCandidateController {

    @Resource
    private ZyAssetCandidateService zyAssetCandidateService;

    /**
     * 获取短剧项目资产候选分页
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    @Operation(summary = "获取短剧项目资产候选分页")
    @SaCheckPermission("/canvas/assetcandidate/page")
    @GetMapping("/canvas/assetcandidate/page")
    public CommonResult<Page<ZyAssetCandidate>> page(ZyAssetCandidatePageParam zyAssetCandidatePageParam) {
        return CommonResult.data(zyAssetCandidateService.page(zyAssetCandidatePageParam));
    }

    /**
     * 添加短剧项目资产候选
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    @Operation(summary = "添加短剧项目资产候选")
    @CommonLog("添加短剧项目资产候选")
    @SaCheckPermission("/canvas/assetcandidate/add")
    @PostMapping("/canvas/assetcandidate/add")
    public CommonResult<String> add(@RequestBody @Valid ZyAssetCandidateAddParam zyAssetCandidateAddParam) {
        zyAssetCandidateService.add(zyAssetCandidateAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑短剧项目资产候选
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    @Operation(summary = "编辑短剧项目资产候选")
    @CommonLog("编辑短剧项目资产候选")
    @SaCheckPermission("/canvas/assetcandidate/edit")
    @PostMapping("/canvas/assetcandidate/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZyAssetCandidateEditParam zyAssetCandidateEditParam) {
        zyAssetCandidateService.edit(zyAssetCandidateEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除短剧项目资产候选
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    @Operation(summary = "删除短剧项目资产候选")
    @CommonLog("删除短剧项目资产候选")
    @SaCheckPermission("/canvas/assetcandidate/delete")
    @PostMapping("/canvas/assetcandidate/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZyAssetCandidateIdParam> zyAssetCandidateIdParamList) {
        zyAssetCandidateService.delete(zyAssetCandidateIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取短剧项目资产候选详情
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    @Operation(summary = "获取短剧项目资产候选详情")
    @SaCheckPermission("/canvas/assetcandidate/detail")
    @GetMapping("/canvas/assetcandidate/detail")
    public CommonResult<ZyAssetCandidate> detail(@Valid ZyAssetCandidateIdParam zyAssetCandidateIdParam) {
        return CommonResult.data(zyAssetCandidateService.detail(zyAssetCandidateIdParam));
    }

    /**
     * 下载短剧项目资产候选导入模板
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    @Operation(summary = "下载短剧项目资产候选导入模板")
    @SaCheckPermission("/canvas/assetcandidate/downloadImportTemplate")
    @GetMapping(value = "/canvas/assetcandidate/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zyAssetCandidateService.downloadImportTemplate(response);
    }

    /**
     * 导入短剧项目资产候选
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    @Operation(summary = "导入短剧项目资产候选")
    @CommonLog("导入短剧项目资产候选")
    @SaCheckPermission("/canvas/assetcandidate/importData")
    @PostMapping("/canvas/assetcandidate/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zyAssetCandidateService.importData(file));
    }

    /**
     * 导出短剧项目资产候选
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    @Operation(summary = "导出短剧项目资产候选")
    @SaCheckPermission("/canvas/assetcandidate/exportData")
    @PostMapping(value = "/canvas/assetcandidate/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZyAssetCandidateIdParam> zyAssetCandidateIdParamList, HttpServletResponse response) throws IOException {
        zyAssetCandidateService.exportData(zyAssetCandidateIdParamList, response);
    }
}
