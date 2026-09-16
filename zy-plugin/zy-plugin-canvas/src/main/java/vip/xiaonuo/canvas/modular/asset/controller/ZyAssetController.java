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
package vip.xiaonuo.canvas.modular.asset.controller;

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
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.asset.param.ZyAssetAddParam;
import vip.xiaonuo.canvas.modular.asset.param.ZyAssetEditParam;
import vip.xiaonuo.canvas.modular.asset.param.ZyAssetIdParam;
import vip.xiaonuo.canvas.modular.asset.param.ZyAssetPageParam;
import vip.xiaonuo.canvas.modular.asset.service.ZyAssetService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 短剧项目资产控制器
 *
 * @author hanbin
 * @date  2026/09/08 14:46
 */
@Tag(name = "短剧项目资产控制器")
@RestController
@Validated
public class ZyAssetController {

    @Resource
    private ZyAssetService zyAssetService;

    /**
     * 获取短剧项目资产分页
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    @Operation(summary = "获取短剧项目资产分页")
    @SaCheckPermission("/canvas/asset/page")
    @GetMapping("/canvas/asset/page")
    public CommonResult<Page<ZyAsset>> page(ZyAssetPageParam zyAssetPageParam) {
        return CommonResult.data(zyAssetService.page(zyAssetPageParam));
    }

    /**
     * 添加短剧项目资产
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    @Operation(summary = "添加短剧项目资产")
    @CommonLog("添加短剧项目资产")
    @SaCheckPermission("/canvas/asset/add")
    @PostMapping("/canvas/asset/add")
    public CommonResult<String> add(@RequestBody @Valid ZyAssetAddParam zyAssetAddParam) {
        zyAssetService.add(zyAssetAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑短剧项目资产
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    @Operation(summary = "编辑短剧项目资产")
    @CommonLog("编辑短剧项目资产")
    @SaCheckPermission("/canvas/asset/edit")
    @PostMapping("/canvas/asset/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZyAssetEditParam zyAssetEditParam) {
        zyAssetService.edit(zyAssetEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除短剧项目资产
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    @Operation(summary = "删除短剧项目资产")
    @CommonLog("删除短剧项目资产")
    @SaCheckPermission("/canvas/asset/delete")
    @PostMapping("/canvas/asset/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZyAssetIdParam> zyAssetIdParamList) {
        zyAssetService.delete(zyAssetIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取短剧项目资产详情
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    @Operation(summary = "获取短剧项目资产详情")
    @SaCheckPermission("/canvas/asset/detail")
    @GetMapping("/canvas/asset/detail")
    public CommonResult<ZyAsset> detail(@Valid ZyAssetIdParam zyAssetIdParam) {
        return CommonResult.data(zyAssetService.detail(zyAssetIdParam));
    }

    /**
     * 下载短剧项目资产导入模板
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    @Operation(summary = "下载短剧项目资产导入模板")
    @SaCheckPermission("/canvas/asset/downloadImportTemplate")
    @GetMapping(value = "/canvas/asset/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zyAssetService.downloadImportTemplate(response);
    }

    /**
     * 导入短剧项目资产
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    @Operation(summary = "导入短剧项目资产")
    @CommonLog("导入短剧项目资产")
    @SaCheckPermission("/canvas/asset/importData")
    @PostMapping("/canvas/asset/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zyAssetService.importData(file));
    }

    /**
     * 导出短剧项目资产
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    @Operation(summary = "导出短剧项目资产")
    @SaCheckPermission("/canvas/asset/exportData")
    @PostMapping(value = "/canvas/asset/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZyAssetIdParam> zyAssetIdParamList, HttpServletResponse response) throws IOException {
        zyAssetService.exportData(zyAssetIdParamList, response);
    }
}
