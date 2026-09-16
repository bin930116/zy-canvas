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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.zyapi.param.ZyApiCharacterParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiRepresentationParam;
import vip.xiaonuo.canvas.zyapi.service.ZyApiCharacterService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短剧项目角色接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 21:00
 **/
@Tag(name = "短剧项目角色")
@RestController
public class ZyApiCharacterController {

    @Resource
    private ZyApiCharacterService zyCharacterService;

    /**
     * 创建角色
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     **/
    @Operation(summary = "创建角色")
    @PostMapping("/projects/{projectId}/characters")
    public CommonResult<Map<String, Object>> create(@PathVariable String projectId,
                                                    @RequestBody @Valid ZyApiCharacterParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyCharacterService.create(userId, projectId, param.getName(), param.getDefinition()));
    }

    /**
     * 获取角色
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     **/
    @Operation(summary = "获取角色")
    @GetMapping("/projects/{projectId}/characters/{assetId}")
    public CommonResult<Map<String, Object>> get(@PathVariable String projectId, @PathVariable String assetId) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyCharacterService.get(userId, projectId, assetId));
    }

    /**
     * 更新角色
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     **/
    @Operation(summary = "更新角色")
    @PatchMapping("/projects/{projectId}/characters/{assetId}")
    public CommonResult<Map<String, Object>> update(@PathVariable String projectId,
                                                    @PathVariable String assetId,
                                                    @RequestBody @Valid ZyApiCharacterParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyCharacterService.update(userId, projectId, assetId, param.getName(), param.getDefinition()));
    }

    /**
     * 更新角色形象
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     **/
    @Operation(summary = "更新角色形象")
    @PutMapping("/projects/{projectId}/characters/{assetId}/representations")
    public CommonResult<Map<String, Object>> representations(@PathVariable String projectId,
                                                             @PathVariable String assetId,
                                                             @RequestBody @Valid ZyApiRepresentationParam param) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyCharacterService.putRepresentations(userId, projectId, assetId, param.getRepresentations()));
    }

    /**
     * 更新角色音色
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     **/
    @Operation(summary = "更新角色音色")
    @PutMapping("/projects/{projectId}/characters/{assetId}/voice")
    public CommonResult<Map<String, Object>> voice(@PathVariable String projectId,
                                                   @PathVariable String assetId,
                                                   @RequestBody Map<String, Object> voice) {
        String userId = StpClientUtil.getLoginIdAsString();
        Map<String, Object> result = zyCharacterService.putVoice(userId, projectId, assetId, voice);
        return CommonResult.data(result);
    }

    /**
     * 删除角色音色
     *
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     **/
    @Operation(summary = "删除角色音色")
    @DeleteMapping("/projects/{projectId}/characters/{assetId}/voice")
    public CommonResult<Map<String, Object>> deleteVoice(@PathVariable String projectId, @PathVariable String assetId) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyCharacterService.deleteVoice(userId, projectId, assetId));
    }
}
