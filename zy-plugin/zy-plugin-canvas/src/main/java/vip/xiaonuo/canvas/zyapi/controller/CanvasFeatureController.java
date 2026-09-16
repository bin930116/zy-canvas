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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 平台-系统功能控制台接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 18:11
 **/
@Tag(name = "平台-系统")
@RestController
public class CanvasFeatureController {

    /**
     * 获取功能开关
     * <p>
     * 平台前端通过 <code>refreshFeatureAvailability()</code> 调用本接口，
     * <code>RequireFeature</code> 组件依赖它决定 /projects、/tasks 等核心页是否开放。
     * 当前返回默认开关（与前端 getAuthSession() 内置默认一致），后续由管理端配置覆盖。
     *
     * @author xuyuxiang
     * @date 2026/9/5 18:11
     **/
    @Operation(summary = "获取功能开关")
    @GetMapping("/features")
    public CommonResult<Map<String, Object>> features() {
        Map<String, Object> featureMap = new LinkedHashMap<>();
        featureMap.put("shortDramaEnabled", true);
        featureMap.put("taskCenterEnabled", true);
        featureMap.put("creditsEnabled", false);
        featureMap.put("customChannelsEnabled", true);
        featureMap.put("frontendModelsEnabled", true);
        featureMap.put("pluginCenterEnabled", false);
        featureMap.put("systemPluginsVisibleToUsers", false);
        featureMap.put("desktopLocalChannelsEnabled", false);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("features", featureMap);
        return CommonResult.data(result);
    }
}
