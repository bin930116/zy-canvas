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
package vip.xiaonuo.canvas.modular.teammember.controller;

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
import vip.xiaonuo.canvas.modular.teammember.entity.ZyTeamMember;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberAddParam;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberEditParam;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberIdParam;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberPageParam;
import vip.xiaonuo.canvas.modular.teammember.service.ZyTeamMemberService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 团队成员关系表控制器
 *
 * @author hanbin
 * @date  2026/09/07 18:44
 */
@Tag(name = "团队成员关系表控制器")
@RestController
@Validated
public class ZyTeamMemberController {

    @Resource
    private ZyTeamMemberService zyTeamMemberService;

    /**
     * 获取团队成员关系表分页
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    @Operation(summary = "获取团队成员关系表分页")
    @SaCheckPermission("/canvas/teammember/page")
    @GetMapping("/canvas/teammember/page")
    public CommonResult<Page<ZyTeamMember>> page(ZyTeamMemberPageParam zyTeamMemberPageParam) {
        return CommonResult.data(zyTeamMemberService.page(zyTeamMemberPageParam));
    }

    /**
     * 添加团队成员关系表
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    @Operation(summary = "添加团队成员关系表")
    @CommonLog("添加团队成员关系表")
    @SaCheckPermission("/canvas/teammember/add")
    @PostMapping("/canvas/teammember/add")
    public CommonResult<String> add(@RequestBody @Valid ZyTeamMemberAddParam zyTeamMemberAddParam) {
        zyTeamMemberService.add(zyTeamMemberAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑团队成员关系表
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    @Operation(summary = "编辑团队成员关系表")
    @CommonLog("编辑团队成员关系表")
    @SaCheckPermission("/canvas/teammember/edit")
    @PostMapping("/canvas/teammember/edit")
    public CommonResult<String> edit(@RequestBody @Valid ZyTeamMemberEditParam zyTeamMemberEditParam) {
        zyTeamMemberService.edit(zyTeamMemberEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除团队成员关系表
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    @Operation(summary = "删除团队成员关系表")
    @CommonLog("删除团队成员关系表")
    @SaCheckPermission("/canvas/teammember/delete")
    @PostMapping("/canvas/teammember/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<ZyTeamMemberIdParam> zyTeamMemberIdParamList) {
        zyTeamMemberService.delete(zyTeamMemberIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取团队成员关系表详情
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    @Operation(summary = "获取团队成员关系表详情")
    @SaCheckPermission("/canvas/teammember/detail")
    @GetMapping("/canvas/teammember/detail")
    public CommonResult<ZyTeamMember> detail(@Valid ZyTeamMemberIdParam zyTeamMemberIdParam) {
        return CommonResult.data(zyTeamMemberService.detail(zyTeamMemberIdParam));
    }

    /**
     * 下载团队成员关系表导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    @Operation(summary = "下载团队成员关系表导入模板")
    @SaCheckPermission("/canvas/teammember/downloadImportTemplate")
    @GetMapping(value = "/canvas/teammember/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        zyTeamMemberService.downloadImportTemplate(response);
    }

    /**
     * 导入团队成员关系表
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    @Operation(summary = "导入团队成员关系表")
    @CommonLog("导入团队成员关系表")
    @SaCheckPermission("/canvas/teammember/importData")
    @PostMapping("/canvas/teammember/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(zyTeamMemberService.importData(file));
    }

    /**
     * 导出团队成员关系表
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    @Operation(summary = "导出团队成员关系表")
    @SaCheckPermission("/canvas/teammember/exportData")
    @PostMapping(value = "/canvas/teammember/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<ZyTeamMemberIdParam> zyTeamMemberIdParamList, HttpServletResponse response) throws IOException {
        zyTeamMemberService.exportData(zyTeamMemberIdParamList, response);
    }
}
