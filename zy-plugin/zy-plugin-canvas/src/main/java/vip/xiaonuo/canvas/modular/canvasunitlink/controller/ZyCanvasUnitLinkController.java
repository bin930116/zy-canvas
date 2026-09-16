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
package vip.xiaonuo.canvas.modular.canvasunitlink.controller;

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
import vip.xiaonuo.canvas.modular.canvasunitlink.entity.ZyCanvasUnitLink;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkAddParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkEditParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkIdParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkPageParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.service.ZyCanvasUnitLinkService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 画布与章节关联控制器
 *
 * @author hanbin
 * @date  2026/09/08 14:31
 */
@Tag(name = "画布与章节关联控制器")
@RestController
@Validated
public class ZyCanvasUnitLinkController {

    @Resource
    private ZyCanvasUnitLinkService zyCanvasUnitLinkService;

    /**
     * 获取画布与章节关联分页
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    @Operation(summary = "获取画布与章节关联分页")
    @SaCheckPermission("/canvas/canvasunitlink/page")
    @GetMapping("/canvas/canvasunitlink/page")
    public CommonResult<Page<ZyCanvasUnitLink>> page(ZyCanvasUnitLinkPageParam zyCanvasUnitLinkPageParam) {
        return CommonResult.data(zyCanvasUnitLinkService.page(zyCanvasUnitLinkPageParam));
    }

    /**
     * 添加画布与章节关联
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    @Operation(summary = "添加画布与章节关联")
    @CommonLog("添加画布与章节关联")
    @SaCheckPermission("/canvas/canvasunitlink/add")
    @PostMapping("/canvas/canvasunitlink/add")
    public CommonResult<String> add(@RequestBody @Valid ZyCanvasUnitLinkAddParam zyCanvasUnitLinkAddParam) {
        zyCanvasUnitLinkService.add(zyCanvasUnitLinkAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑画布与章节关联
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    @Operation(summary = "编辑画布与章节关联")
    @CommonLog("编辑画布与章节关联")
    @SaCheckPermission("/canvas/canvasunitlink/edit")
    @PostMapping("/canvas/canvasunitlink/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZyCanvasUnitLinkEditParam zyCanvasUnitLinkEditParam) {
        zyCanvasUnitLinkService.edit(zyCanvasUnitLinkEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除画布与章节关联
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    @Operation(summary = "删除画布与章节关联")
    @CommonLog("删除画布与章节关联")
    @SaCheckPermission("/canvas/canvasunitlink/delete")
    @PostMapping("/canvas/canvasunitlink/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZyCanvasUnitLinkIdParam> zyCanvasUnitLinkIdParamList) {
        zyCanvasUnitLinkService.delete(zyCanvasUnitLinkIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取画布与章节关联详情
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    @Operation(summary = "获取画布与章节关联详情")
    @SaCheckPermission("/canvas/canvasunitlink/detail")
    @GetMapping("/canvas/canvasunitlink/detail")
    public CommonResult<ZyCanvasUnitLink> detail(@Valid ZyCanvasUnitLinkIdParam zyCanvasUnitLinkIdParam) {
        return CommonResult.data(zyCanvasUnitLinkService.detail(zyCanvasUnitLinkIdParam));
    }

    /**
     * 下载画布与章节关联导入模板
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    @Operation(summary = "下载画布与章节关联导入模板")
    @SaCheckPermission("/canvas/canvasunitlink/downloadImportTemplate")
    @GetMapping(value = "/canvas/canvasunitlink/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zyCanvasUnitLinkService.downloadImportTemplate(response);
    }

    /**
     * 导入画布与章节关联
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    @Operation(summary = "导入画布与章节关联")
    @CommonLog("导入画布与章节关联")
    @SaCheckPermission("/canvas/canvasunitlink/importData")
    @PostMapping("/canvas/canvasunitlink/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zyCanvasUnitLinkService.importData(file));
    }

    /**
     * 导出画布与章节关联
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    @Operation(summary = "导出画布与章节关联")
    @SaCheckPermission("/canvas/canvasunitlink/exportData")
    @PostMapping(value = "/canvas/canvasunitlink/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZyCanvasUnitLinkIdParam> zyCanvasUnitLinkIdParamList, HttpServletResponse response) throws IOException {
        zyCanvasUnitLinkService.exportData(zyCanvasUnitLinkIdParamList, response);
    }
}
