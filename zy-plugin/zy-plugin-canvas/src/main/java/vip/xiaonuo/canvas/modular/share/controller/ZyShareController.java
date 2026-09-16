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
package vip.xiaonuo.canvas.modular.share.controller;

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
import vip.xiaonuo.canvas.modular.share.entity.ZyShare;
import vip.xiaonuo.canvas.modular.share.param.ZyShareAddParam;
import vip.xiaonuo.canvas.modular.share.param.ZyShareEditParam;
import vip.xiaonuo.canvas.modular.share.param.ZyShareIdParam;
import vip.xiaonuo.canvas.modular.share.param.ZySharePageParam;
import vip.xiaonuo.canvas.modular.share.service.ZyShareService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 画布项目分享控制器
 *
 * @author hanbin
 * @date  2026/09/07 18:56
 */
@Tag(name = "画布项目分享控制器")
@RestController
@Validated
public class ZyShareController {

    @Resource
    private ZyShareService zyShareService;

    /**
     * 获取画布项目分享分页
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    @Operation(summary = "获取画布项目分享分页")
    @SaCheckPermission("/canvas/share/page")
    @GetMapping("/canvas/share/page")
    public CommonResult<Page<ZyShare>> page(ZySharePageParam zySharePageParam) {
        return CommonResult.data(zyShareService.page(zySharePageParam));
    }

    /**
     * 添加画布项目分享
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    @Operation(summary = "添加画布项目分享")
    @CommonLog("添加画布项目分享")
    @SaCheckPermission("/canvas/share/add")
    @PostMapping("/canvas/share/add")
    public CommonResult<String> add(@RequestBody @Valid ZyShareAddParam zyShareAddParam) {
        zyShareService.add(zyShareAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑画布项目分享
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    @Operation(summary = "编辑画布项目分享")
    @CommonLog("编辑画布项目分享")
    @SaCheckPermission("/canvas/share/edit")
    @PostMapping("/canvas/share/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZyShareEditParam zyShareEditParam) {
        zyShareService.edit(zyShareEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除画布项目分享
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    @Operation(summary = "删除画布项目分享")
    @CommonLog("删除画布项目分享")
    @SaCheckPermission("/canvas/share/delete")
    @PostMapping("/canvas/share/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZyShareIdParam> zyShareIdParamList) {
        zyShareService.delete(zyShareIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取画布项目分享详情
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    @Operation(summary = "获取画布项目分享详情")
    @SaCheckPermission("/canvas/share/detail")
    @GetMapping("/canvas/share/detail")
    public CommonResult<ZyShare> detail(@Valid ZyShareIdParam zyShareIdParam) {
        return CommonResult.data(zyShareService.detail(zyShareIdParam));
    }

    /**
     * 下载画布项目分享导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    @Operation(summary = "下载画布项目分享导入模板")
    @SaCheckPermission("/canvas/share/downloadImportTemplate")
    @GetMapping(value = "/canvas/share/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zyShareService.downloadImportTemplate(response);
    }

    /**
     * 导入画布项目分享
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    @Operation(summary = "导入画布项目分享")
    @CommonLog("导入画布项目分享")
    @SaCheckPermission("/canvas/share/importData")
    @PostMapping("/canvas/share/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zyShareService.importData(file));
    }

    /**
     * 导出画布项目分享
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    @Operation(summary = "导出画布项目分享")
    @SaCheckPermission("/canvas/share/exportData")
    @PostMapping(value = "/canvas/share/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZyShareIdParam> zyShareIdParamList, HttpServletResponse response) throws IOException {
        zyShareService.exportData(zyShareIdParamList, response);
    }
}
