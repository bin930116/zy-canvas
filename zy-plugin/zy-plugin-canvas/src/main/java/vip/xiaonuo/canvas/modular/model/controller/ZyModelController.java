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
package vip.xiaonuo.canvas.modular.model.controller;

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
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.modular.model.param.ZyModelAddParam;
import vip.xiaonuo.canvas.modular.model.param.ZyModelEditParam;
import vip.xiaonuo.canvas.modular.model.param.ZyModelIdParam;
import vip.xiaonuo.canvas.modular.model.param.ZyModelPageParam;
import vip.xiaonuo.canvas.modular.model.service.ZyModelService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 模型配置控制器
 *
 * @author hanbin
 * @date  2026/09/07 18:27
 */
@Tag(name = "模型配置控制器")
@RestController
@Validated
public class ZyModelController {

    @Resource
    private ZyModelService zyModelService;

    /**
     * 获取模型配置分页
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    @Operation(summary = "获取模型配置分页")
    @SaCheckPermission("/canvas/model/page")
    @GetMapping("/canvas/model/page")
    public CommonResult<Page<ZyModel>> page(ZyModelPageParam zyModelPageParam) {
        return CommonResult.data(zyModelService.page(zyModelPageParam));
    }

    /**
     * 添加模型配置
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    @Operation(summary = "添加模型配置")
    @CommonLog("添加模型配置")
    @SaCheckPermission("/canvas/model/add")
    @PostMapping("/canvas/model/add")
    public CommonResult<String> add(@RequestBody @Valid ZyModelAddParam zyModelAddParam) {
        zyModelService.add(zyModelAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑模型配置
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    @Operation(summary = "编辑模型配置")
    @CommonLog("编辑模型配置")
    @SaCheckPermission("/canvas/model/edit")
    @PostMapping("/canvas/model/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZyModelEditParam zyModelEditParam) {
        zyModelService.edit(zyModelEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除模型配置
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    @Operation(summary = "删除模型配置")
    @CommonLog("删除模型配置")
    @SaCheckPermission("/canvas/model/delete")
    @PostMapping("/canvas/model/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZyModelIdParam> zyModelIdParamList) {
        zyModelService.delete(zyModelIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取模型配置详情
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    @Operation(summary = "获取模型配置详情")
    @SaCheckPermission("/canvas/model/detail")
    @GetMapping("/canvas/model/detail")
    public CommonResult<ZyModel> detail(@Valid ZyModelIdParam zyModelIdParam) {
        return CommonResult.data(zyModelService.detail(zyModelIdParam));
    }

    /**
     * 下载模型配置导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    @Operation(summary = "下载模型配置导入模板")
    @SaCheckPermission("/canvas/model/downloadImportTemplate")
    @GetMapping(value = "/canvas/model/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zyModelService.downloadImportTemplate(response);
    }

    /**
     * 导入模型配置
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    @Operation(summary = "导入模型配置")
    @CommonLog("导入模型配置")
    @SaCheckPermission("/canvas/model/importData")
    @PostMapping("/canvas/model/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zyModelService.importData(file));
    }

    /**
     * 导出模型配置
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    @Operation(summary = "导出模型配置")
    @SaCheckPermission("/canvas/model/exportData")
    @PostMapping(value = "/canvas/model/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZyModelIdParam> zyModelIdParamList, HttpServletResponse response) throws IOException {
        zyModelService.exportData(zyModelIdParamList, response);
    }
}
