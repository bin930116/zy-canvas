package vip.xiaonuo.canvas.zyapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.canvas.zyapi.service.ZyApiModelService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.List;
import java.util.Map;

/**
 * 模型目录Controller
 * 提供与前端兼容的模型目录接口
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Tag(name = "模型目录")
@RestController
@RequestMapping("/api/model-catalog")
public class ZyApiModelCatalogController {

    @Resource
    private ZyApiModelService zyModelService;

    /**
     * 获取模型目录
     * 返回按能力分类的模型列表
     *
     * @return 模型目录
     */
    @Operation(summary = "获取模型目录")
    @GetMapping
    public CommonResult<Object> getCatalog() {
        return CommonResult.data(zyModelService.listModels(null));
    }

    /**
     * 获取可用模型目录
     * 根据意图过滤可用模型
     *
     * @param intent 请求意图
     * @return 可用模型列表
     */
    @Operation(summary = "获取可用模型目录")
    @PostMapping("/available")
    public CommonResult<List<Map<String, Object>>> getAvailableModels(@RequestBody Map<String, Object> intent) {
        String capability = (String) intent.get("capability");
        return CommonResult.data(zyModelService.getAvailableModels(capability));
    }
}