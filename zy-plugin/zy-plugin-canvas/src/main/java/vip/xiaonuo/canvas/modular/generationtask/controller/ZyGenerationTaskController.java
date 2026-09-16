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
package vip.xiaonuo.canvas.modular.generationtask.controller;

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
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskAddParam;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskEditParam;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskIdParam;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskPageParam;
import vip.xiaonuo.canvas.modular.generationtask.service.ZyGenerationTaskService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 任务队列信息控制器
 *
 * @author hanbin
 * @date  2026/09/08 14:24
 */
@Tag(name = "任务队列信息控制器")
@RestController
@Validated
public class ZyGenerationTaskController {

    @Resource
    private ZyGenerationTaskService zyGenerationTaskService;

    /**
     * 获取任务队列信息分页
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    @Operation(summary = "获取任务队列信息分页")
    @SaCheckPermission("/canvas/generationtask/page")
    @GetMapping("/canvas/generationtask/page")
    public CommonResult<Page<ZyGenerationTask>> page(ZyGenerationTaskPageParam zyGenerationTaskPageParam) {
        return CommonResult.data(zyGenerationTaskService.page(zyGenerationTaskPageParam));
    }

    /**
     * 添加任务队列信息
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    @Operation(summary = "添加任务队列信息")
    @CommonLog("添加任务队列信息")
    @SaCheckPermission("/canvas/generationtask/add")
    @PostMapping("/canvas/generationtask/add")
    public CommonResult<String> add(@RequestBody @Valid ZyGenerationTaskAddParam zyGenerationTaskAddParam) {
        zyGenerationTaskService.add(zyGenerationTaskAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑任务队列信息
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    @Operation(summary = "编辑任务队列信息")
    @CommonLog("编辑任务队列信息")
    @SaCheckPermission("/canvas/generationtask/edit")
    @PostMapping("/canvas/generationtask/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZyGenerationTaskEditParam zyGenerationTaskEditParam) {
        zyGenerationTaskService.edit(zyGenerationTaskEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除任务队列信息
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    @Operation(summary = "删除任务队列信息")
    @CommonLog("删除任务队列信息")
    @SaCheckPermission("/canvas/generationtask/delete")
    @PostMapping("/canvas/generationtask/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZyGenerationTaskIdParam> zyGenerationTaskIdParamList) {
        zyGenerationTaskService.delete(zyGenerationTaskIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取任务队列信息详情
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    @Operation(summary = "获取任务队列信息详情")
    @SaCheckPermission("/canvas/generationtask/detail")
    @GetMapping("/canvas/generationtask/detail")
    public CommonResult<ZyGenerationTask> detail(@Valid ZyGenerationTaskIdParam zyGenerationTaskIdParam) {
        return CommonResult.data(zyGenerationTaskService.detail(zyGenerationTaskIdParam));
    }

    /**
     * 下载任务队列信息导入模板
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    @Operation(summary = "下载任务队列信息导入模板")
    @SaCheckPermission("/canvas/generationtask/downloadImportTemplate")
    @GetMapping(value = "/canvas/generationtask/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zyGenerationTaskService.downloadImportTemplate(response);
    }

    /**
     * 导入任务队列信息
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    @Operation(summary = "导入任务队列信息")
    @CommonLog("导入任务队列信息")
    @SaCheckPermission("/canvas/generationtask/importData")
    @PostMapping("/canvas/generationtask/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zyGenerationTaskService.importData(file));
    }

    /**
     * 导出任务队列信息
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    @Operation(summary = "导出任务队列信息")
    @SaCheckPermission("/canvas/generationtask/exportData")
    @PostMapping(value = "/canvas/generationtask/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZyGenerationTaskIdParam> zyGenerationTaskIdParamList, HttpServletResponse response) throws IOException {
        zyGenerationTaskService.exportData(zyGenerationTaskIdParamList, response);
    }
}
