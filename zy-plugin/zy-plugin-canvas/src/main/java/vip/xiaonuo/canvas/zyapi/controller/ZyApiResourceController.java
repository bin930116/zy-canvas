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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.zyapi.service.ZyApiResourceService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.Map;

/**
 * 资源接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 23:30
 **/
@Tag(name = "资源")
@RestController
public class ZyApiResourceController {

    @Resource
    private ZyApiResourceService zyResourceService;

    /**
     * 获取账号文件存储使用情况
     *
     * @author xuyuxiang
     * @date 2026/9/5 23:30
     **/
    @Operation(summary = "获取账号文件存储使用情况")
    @GetMapping("/resources/storage-usage")
    public CommonResult<Map<String, Object>> getStorageUsage() {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyResourceService.getStorageUsage(userId));
    }
}