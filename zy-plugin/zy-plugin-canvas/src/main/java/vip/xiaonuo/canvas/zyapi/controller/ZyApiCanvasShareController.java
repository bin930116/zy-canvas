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
package vip.xiaonuo.canvas.zyapi.controller;

import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.project.entity.ZyProject;
import vip.xiaonuo.canvas.zyapi.param.ZyApiShareParam;
import vip.xiaonuo.canvas.zyapi.result.ZyApiShareStatusResult;
import vip.xiaonuo.canvas.zyapi.service.ZyApiProjectService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiShareService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 画布项目分享接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 18:30
 **/
@Tag(name = "画布项目分享")
@RestController
public class ZyApiCanvasShareController {

    @Resource
    private ZyApiShareService zyShareService;

    @Resource
    private ZyApiProjectService zyProjectService;

    /**
     * 获取项目分享状态
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     **/
    @Operation(summary = "获取项目分享状态")
    @GetMapping("/canvas-projects/{projectId}/share")
    public CommonResult<Map<String, Object>> get(@PathVariable String projectId) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyProject project = zyProjectService.getById(userId, projectId);
        if (ObjectUtil.isEmpty(project)) {
            return CommonResult.error("画布项目不存在");
        }
        ZyApiShareStatusResult share = zyShareService.getStatus(userId, projectId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("share", share);
        return CommonResult.data(result);
    }

    /**
     * 创建(或轮换)项目分享
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     **/
    @Operation(summary = "创建(或轮换)项目分享")
    @PostMapping("/canvas-projects/{projectId}/share")
    public CommonResult<Map<String, Object>> create(@PathVariable String projectId,
                                                    @RequestBody @Valid ZyApiShareParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        ZyProject project = zyProjectService.getById(userId, projectId);
        if (ObjectUtil.isEmpty(project)) {
            return CommonResult.error("画布项目不存在");
        }
        ZyApiShareStatusResult share = zyShareService.create(userId, projectId, param.getExpiresDays(), param.getRotate());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("share", share);
        return CommonResult.data(result);
    }

    /**
     * 删除项目分享
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     **/
    @Operation(summary = "删除项目分享")
    @DeleteMapping("/canvas-projects/{projectId}/share")
    public CommonResult<Map<String, Object>> delete(@PathVariable String projectId) {
        String userId = StpClientUtil.getLoginIdAsString();
        zyShareService.delete(userId, projectId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", projectId);
        return CommonResult.data(result);
    }

    /**
     * 按token获取公开分享(免登录)
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     **/
    @Operation(summary = "按token获取公开分享")
    @GetMapping("/public/canvas-shares/{token}")
    public CommonResult<Map<String, Object>> publicShare(@PathVariable String token) {
        Map<String, Object> data = zyShareService.getPublicByToken(token);
        if (ObjectUtil.isEmpty(data)) {
            return CommonResult.error("分享链接无效或已过期");
        }
        return CommonResult.data(data);
    }
}
