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
package vip.xiaonuo.canvas.modular.session.controller;

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
import vip.xiaonuo.canvas.modular.session.entity.ZySession;
import vip.xiaonuo.canvas.modular.session.param.ZySessionAddParam;
import vip.xiaonuo.canvas.modular.session.param.ZySessionEditParam;
import vip.xiaonuo.canvas.modular.session.param.ZySessionIdParam;
import vip.xiaonuo.canvas.modular.session.param.ZySessionPageParam;
import vip.xiaonuo.canvas.modular.session.service.ZySessionService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 会话控制器
 *
 * @author hanbin
 * @date  2026/09/07 18:57
 */
@Tag(name = "会话控制器")
@RestController
@Validated
public class ZySessionController {

    @Resource
    private ZySessionService zySessionService;

    /**
     * 获取会话分页
     *
     * @author hanbin
     * @date  2026/09/07 18:57
     */
    @Operation(summary = "获取会话分页")
    @SaCheckPermission("/canvas/session/page")
    @GetMapping("/canvas/session/page")
    public CommonResult<Page<ZySession>> page(ZySessionPageParam zySessionPageParam) {
        return CommonResult.data(zySessionService.page(zySessionPageParam));
    }

    /**
     * 添加会话
     *
     * @author hanbin
     * @date  2026/09/07 18:57
     */
    @Operation(summary = "添加会话")
    @CommonLog("添加会话")
    @SaCheckPermission("/canvas/session/add")
    @PostMapping("/canvas/session/add")
    public CommonResult<String> add(@RequestBody @Valid ZySessionAddParam zySessionAddParam) {
        zySessionService.add(zySessionAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑会话
     *
     * @author hanbin
     * @date  2026/09/07 18:57
     */
    @Operation(summary = "编辑会话")
    @CommonLog("编辑会话")
    @SaCheckPermission("/canvas/session/edit")
    @PostMapping("/canvas/session/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZySessionEditParam zySessionEditParam) {
        zySessionService.edit(zySessionEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除会话
     *
     * @author hanbin
     * @date  2026/09/07 18:57
     */
    @Operation(summary = "删除会话")
    @CommonLog("删除会话")
    @SaCheckPermission("/canvas/session/delete")
    @PostMapping("/canvas/session/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZySessionIdParam> zySessionIdParamList) {
        zySessionService.delete(zySessionIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取会话详情
     *
     * @author hanbin
     * @date  2026/09/07 18:57
     */
    @Operation(summary = "获取会话详情")
    @SaCheckPermission("/canvas/session/detail")
    @GetMapping("/canvas/session/detail")
    public CommonResult<ZySession> detail(@Valid ZySessionIdParam zySessionIdParam) {
        return CommonResult.data(zySessionService.detail(zySessionIdParam));
    }

    /**
     * 下载会话导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:57
     */
    @Operation(summary = "下载会话导入模板")
    @SaCheckPermission("/canvas/session/downloadImportTemplate")
    @GetMapping(value = "/canvas/session/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zySessionService.downloadImportTemplate(response);
    }

    /**
     * 导入会话
     *
     * @author hanbin
     * @date  2026/09/07 18:57
     */
    @Operation(summary = "导入会话")
    @CommonLog("导入会话")
    @SaCheckPermission("/canvas/session/importData")
    @PostMapping("/canvas/session/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zySessionService.importData(file));
    }

    /**
     * 导出会话
     *
     * @author hanbin
     * @date  2026/09/07 18:57
     */
    @Operation(summary = "导出会话")
    @SaCheckPermission("/canvas/session/exportData")
    @PostMapping(value = "/canvas/session/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZySessionIdParam> zySessionIdParamList, HttpServletResponse response) throws IOException {
        zySessionService.exportData(zySessionIdParamList, response);
    }
}
