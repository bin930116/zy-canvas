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
package vip.xiaonuo.canvas.modular.unit.controller;

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
import vip.xiaonuo.canvas.modular.unit.entity.ZyUnit;
import vip.xiaonuo.canvas.modular.unit.param.ZyUnitAddParam;
import vip.xiaonuo.canvas.modular.unit.param.ZyUnitEditParam;
import vip.xiaonuo.canvas.modular.unit.param.ZyUnitIdParam;
import vip.xiaonuo.canvas.modular.unit.param.ZyUnitPageParam;
import vip.xiaonuo.canvas.modular.unit.service.ZyUnitService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 短剧项目章节控制器
 *
 * @author hanbin
 * @date  2026/09/07 18:37
 */
@Tag(name = "短剧项目章节控制器")
@RestController
@Validated
public class ZyUnitController {

    @Resource
    private ZyUnitService zyUnitService;

    /**
     * 获取短剧项目章节分页
     *
     * @author hanbin
     * @date  2026/09/07 18:37
     */
    @Operation(summary = "获取短剧项目章节分页")
    @SaCheckPermission("/canvas/unit/page")
    @GetMapping("/canvas/unit/page")
    public CommonResult<Page<ZyUnit>> page(ZyUnitPageParam zyUnitPageParam) {
        return CommonResult.data(zyUnitService.page(zyUnitPageParam));
    }

    /**
     * 添加短剧项目章节
     *
     * @author hanbin
     * @date  2026/09/07 18:37
     */
    @Operation(summary = "添加短剧项目章节")
    @CommonLog("添加短剧项目章节")
    @SaCheckPermission("/canvas/unit/add")
    @PostMapping("/canvas/unit/add")
    public CommonResult<String> add(@RequestBody @Valid ZyUnitAddParam zyUnitAddParam) {
        zyUnitService.add(zyUnitAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑短剧项目章节
     *
     * @author hanbin
     * @date  2026/09/07 18:37
     */
    @Operation(summary = "编辑短剧项目章节")
    @CommonLog("编辑短剧项目章节")
    @SaCheckPermission("/canvas/unit/edit")
    @PostMapping("/canvas/unit/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZyUnitEditParam zyUnitEditParam) {
        zyUnitService.edit(zyUnitEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除短剧项目章节
     *
     * @author hanbin
     * @date  2026/09/07 18:37
     */
    @Operation(summary = "删除短剧项目章节")
    @CommonLog("删除短剧项目章节")
    @SaCheckPermission("/canvas/unit/delete")
    @PostMapping("/canvas/unit/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZyUnitIdParam> zyUnitIdParamList) {
        zyUnitService.delete(zyUnitIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取短剧项目章节详情
     *
     * @author hanbin
     * @date  2026/09/07 18:37
     */
    @Operation(summary = "获取短剧项目章节详情")
    @SaCheckPermission("/canvas/unit/detail")
    @GetMapping("/canvas/unit/detail")
    public CommonResult<ZyUnit> detail(@Valid ZyUnitIdParam zyUnitIdParam) {
        return CommonResult.data(zyUnitService.detail(zyUnitIdParam));
    }

    /**
     * 下载短剧项目章节导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:37
     */
    @Operation(summary = "下载短剧项目章节导入模板")
    @SaCheckPermission("/canvas/unit/downloadImportTemplate")
    @GetMapping(value = "/canvas/unit/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zyUnitService.downloadImportTemplate(response);
    }

    /**
     * 导入短剧项目章节
     *
     * @author hanbin
     * @date  2026/09/07 18:37
     */
    @Operation(summary = "导入短剧项目章节")
    @CommonLog("导入短剧项目章节")
    @SaCheckPermission("/canvas/unit/importData")
    @PostMapping("/canvas/unit/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zyUnitService.importData(file));
    }

    /**
     * 导出短剧项目章节
     *
     * @author hanbin
     * @date  2026/09/07 18:37
     */
    @Operation(summary = "导出短剧项目章节")
    @SaCheckPermission("/canvas/unit/exportData")
    @PostMapping(value = "/canvas/unit/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZyUnitIdParam> zyUnitIdParamList, HttpServletResponse response) throws IOException {
        zyUnitService.exportData(zyUnitIdParamList, response);
    }
}
