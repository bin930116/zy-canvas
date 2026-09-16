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
package vip.xiaonuo.canvas.modular.team.controller;

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
import vip.xiaonuo.canvas.modular.team.entity.ZyTeam;
import vip.xiaonuo.canvas.modular.team.param.ZyTeamAddParam;
import vip.xiaonuo.canvas.modular.team.param.ZyTeamEditParam;
import vip.xiaonuo.canvas.modular.team.param.ZyTeamIdParam;
import vip.xiaonuo.canvas.modular.team.param.ZyTeamPageParam;
import vip.xiaonuo.canvas.modular.team.service.ZyTeamService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 团队信息表控制器
 *
 * @author hanbin
 * @date  2026/09/07 18:48
 */
@Tag(name = "团队信息表控制器")
@RestController
@Validated
public class ZyTeamController {

    @Resource
    private ZyTeamService zyTeamService;

    /**
     * 获取团队信息表分页
     *
     * @author hanbin
     * @date  2026/09/07 18:48
     */
    @Operation(summary = "获取团队信息表分页")
    @SaCheckPermission("/canvas/team/page")
    @GetMapping("/canvas/team/page")
    public CommonResult<Page<ZyTeam>> page(ZyTeamPageParam zyTeamPageParam) {
        return CommonResult.data(zyTeamService.page(zyTeamPageParam));
    }

    /**
     * 添加团队信息表
     *
     * @author hanbin
     * @date  2026/09/07 18:48
     */
    @Operation(summary = "添加团队信息表")
    @CommonLog("添加团队信息表")
    @SaCheckPermission("/canvas/team/add")
    @PostMapping("/canvas/team/add")
    public CommonResult<String> add(@RequestBody @Valid ZyTeamAddParam zyTeamAddParam) {
        zyTeamService.add(zyTeamAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑团队信息表
     *
     * @author hanbin
     * @date  2026/09/07 18:48
     */
    @Operation(summary = "编辑团队信息表")
    @CommonLog("编辑团队信息表")
    @SaCheckPermission("/canvas/team/edit")
    @PostMapping("/canvas/team/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZyTeamEditParam zyTeamEditParam) {
        zyTeamService.edit(zyTeamEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除团队信息表
     *
     * @author hanbin
     * @date  2026/09/07 18:48
     */
    @Operation(summary = "删除团队信息表")
    @CommonLog("删除团队信息表")
    @SaCheckPermission("/canvas/team/delete")
    @PostMapping("/canvas/team/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZyTeamIdParam> zyTeamIdParamList) {
        zyTeamService.delete(zyTeamIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取团队信息表详情
     *
     * @author hanbin
     * @date  2026/09/07 18:48
     */
    @Operation(summary = "获取团队信息表详情")
    @SaCheckPermission("/canvas/team/detail")
    @GetMapping("/canvas/team/detail")
    public CommonResult<ZyTeam> detail(@Valid ZyTeamIdParam zyTeamIdParam) {
        return CommonResult.data(zyTeamService.detail(zyTeamIdParam));
    }

    /**
     * 下载团队信息表导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:48
     */
    @Operation(summary = "下载团队信息表导入模板")
    @SaCheckPermission("/canvas/team/downloadImportTemplate")
    @GetMapping(value = "/canvas/team/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zyTeamService.downloadImportTemplate(response);
    }

    /**
     * 导入团队信息表
     *
     * @author hanbin
     * @date  2026/09/07 18:48
     */
    @Operation(summary = "导入团队信息表")
    @CommonLog("导入团队信息表")
    @SaCheckPermission("/canvas/team/importData")
    @PostMapping("/canvas/team/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zyTeamService.importData(file));
    }

    /**
     * 导出团队信息表
     *
     * @author hanbin
     * @date  2026/09/07 18:48
     */
    @Operation(summary = "导出团队信息表")
    @SaCheckPermission("/canvas/team/exportData")
    @PostMapping(value = "/canvas/team/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZyTeamIdParam> zyTeamIdParamList, HttpServletResponse response) throws IOException {
        zyTeamService.exportData(zyTeamIdParamList, response);
    }
}
