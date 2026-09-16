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
package vip.xiaonuo.canvas.modular.shotrevision.controller;

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
import vip.xiaonuo.canvas.modular.shotrevision.entity.ZyShotRevision;
import vip.xiaonuo.canvas.modular.shotrevision.param.ZyShotRevisionAddParam;
import vip.xiaonuo.canvas.modular.shotrevision.param.ZyShotRevisionEditParam;
import vip.xiaonuo.canvas.modular.shotrevision.param.ZyShotRevisionIdParam;
import vip.xiaonuo.canvas.modular.shotrevision.param.ZyShotRevisionPageParam;
import vip.xiaonuo.canvas.modular.shotrevision.service.ZyShotRevisionService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 短剧项目分镜版本控制器
 *
 * @author hanbin
 * @date  2026/09/07 18:51
 */
@Tag(name = "短剧项目分镜版本控制器")
@RestController
@Validated
public class ZyShotRevisionController {

    @Resource
    private ZyShotRevisionService zyShotRevisionService;

    /**
     * 获取短剧项目分镜版本分页
     *
     * @author hanbin
     * @date  2026/09/07 18:51
     */
    @Operation(summary = "获取短剧项目分镜版本分页")
    @SaCheckPermission("/canvas/shotrevision/page")
    @GetMapping("/canvas/shotrevision/page")
    public CommonResult<Page<ZyShotRevision>> page(ZyShotRevisionPageParam zyShotRevisionPageParam) {
        return CommonResult.data(zyShotRevisionService.page(zyShotRevisionPageParam));
    }

    /**
     * 添加短剧项目分镜版本
     *
     * @author hanbin
     * @date  2026/09/07 18:51
     */
    @Operation(summary = "添加短剧项目分镜版本")
    @CommonLog("添加短剧项目分镜版本")
    @SaCheckPermission("/canvas/shotrevision/add")
    @PostMapping("/canvas/shotrevision/add")
    public CommonResult<String> add(@RequestBody @Valid ZyShotRevisionAddParam zyShotRevisionAddParam) {
        zyShotRevisionService.add(zyShotRevisionAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑短剧项目分镜版本
     *
     * @author hanbin
     * @date  2026/09/07 18:51
     */
    @Operation(summary = "编辑短剧项目分镜版本")
    @CommonLog("编辑短剧项目分镜版本")
    @SaCheckPermission("/canvas/shotrevision/edit")
    @PostMapping("/canvas/shotrevision/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZyShotRevisionEditParam zyShotRevisionEditParam) {
        zyShotRevisionService.edit(zyShotRevisionEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除短剧项目分镜版本
     *
     * @author hanbin
     * @date  2026/09/07 18:51
     */
    @Operation(summary = "删除短剧项目分镜版本")
    @CommonLog("删除短剧项目分镜版本")
    @SaCheckPermission("/canvas/shotrevision/delete")
    @PostMapping("/canvas/shotrevision/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZyShotRevisionIdParam> zyShotRevisionIdParamList) {
        zyShotRevisionService.delete(zyShotRevisionIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取短剧项目分镜版本详情
     *
     * @author hanbin
     * @date  2026/09/07 18:51
     */
    @Operation(summary = "获取短剧项目分镜版本详情")
    @SaCheckPermission("/canvas/shotrevision/detail")
    @GetMapping("/canvas/shotrevision/detail")
    public CommonResult<ZyShotRevision> detail(@Valid ZyShotRevisionIdParam zyShotRevisionIdParam) {
        return CommonResult.data(zyShotRevisionService.detail(zyShotRevisionIdParam));
    }

    /**
     * 下载短剧项目分镜版本导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:51
     */
    @Operation(summary = "下载短剧项目分镜版本导入模板")
    @SaCheckPermission("/canvas/shotrevision/downloadImportTemplate")
    @GetMapping(value = "/canvas/shotrevision/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zyShotRevisionService.downloadImportTemplate(response);
    }

    /**
     * 导入短剧项目分镜版本
     *
     * @author hanbin
     * @date  2026/09/07 18:51
     */
    @Operation(summary = "导入短剧项目分镜版本")
    @CommonLog("导入短剧项目分镜版本")
    @SaCheckPermission("/canvas/shotrevision/importData")
    @PostMapping("/canvas/shotrevision/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zyShotRevisionService.importData(file));
    }

    /**
     * 导出短剧项目分镜版本
     *
     * @author hanbin
     * @date  2026/09/07 18:51
     */
    @Operation(summary = "导出短剧项目分镜版本")
    @SaCheckPermission("/canvas/shotrevision/exportData")
    @PostMapping(value = "/canvas/shotrevision/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZyShotRevisionIdParam> zyShotRevisionIdParamList, HttpServletResponse response) throws IOException {
        zyShotRevisionService.exportData(zyShotRevisionIdParamList, response);
    }
}
