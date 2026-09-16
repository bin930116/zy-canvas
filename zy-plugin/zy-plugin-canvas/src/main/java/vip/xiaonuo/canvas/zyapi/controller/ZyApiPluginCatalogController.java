package vip.xiaonuo.canvas.zyapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.modular.model.mapper.ZyModelMapper;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.*;

@Tag(name = "插件供应商目录")
@RestController
@RequestMapping("/plugins")
public class ZyApiPluginCatalogController {

    @Resource
    private ZyModelMapper zyModelMapper;

    @Operation(summary = "获取插件供应商目录")
    @GetMapping("/catalog")
    public CommonResult<Map<String, Object>> catalog(
            @RequestParam(defaultValue = "user.custom-channel") String scope,
            @RequestParam(required = false) String capability) {

        // 从数据库查询可用模型
        QueryWrapper<ZyModel> queryWrapper = new QueryWrapper<ZyModel>()
                .eq("status", "active")
                .orderByAsc("sort_code");
        if (capability != null && !capability.isEmpty()) {
            queryWrapper.eq("capability", capability);
        }
        List<ZyModel> models = zyModelMapper.selectList(queryWrapper);

        // 按 providerName 分组
        Map<String, List<ZyModel>> groupedByProvider = new LinkedHashMap<>();
        for (ZyModel model : models) {
            String provider = model.getProviderName() != null ? model.getProviderName() : "unknown";
            groupedByProvider.computeIfAbsent(provider, k -> new ArrayList<>()).add(model);
        }

        // 构建 providers 列表
        List<Map<String, Object>> providers = new ArrayList<>();
        for (Map.Entry<String, List<ZyModel>> entry : groupedByProvider.entrySet()) {
            String providerName = entry.getKey();
            List<ZyModel> providerModels = entry.getValue();

            // 获取 provider 的公共信息（取第一个模型的 baseUrl 和 capability）
            ZyModel firstModel = providerModels.get(0);
            List<String> categories = new ArrayList<>();
            Set<String> baseUrlSet = new LinkedHashSet<>();
            for (ZyModel m : providerModels) {
                if (m.getCapability() != null && !categories.contains(m.getCapability())) {
                    categories.add(m.getCapability());
                }
                if (m.getBaseUrl() != null) {
                    baseUrlSet.add(m.getBaseUrl());
                }
            }

            // 构建具体模型列表
            List<Map<String, Object>> modelList = new ArrayList<>();
            for (ZyModel m : providerModels) {
                Map<String, Object> modelMap = new LinkedHashMap<>();
                modelMap.put("id", m.getModelKey());
                modelMap.put("name", m.getModelName());
                modelMap.put("capability", m.getCapability());
                modelList.add(modelMap);
            }

            // 构建 provider
            Map<String, Object> provider = new LinkedHashMap<>();
            provider.put("id", providerName.toLowerCase().replace(" ", "-"));
            provider.put("version", "1.0");
            provider.put("name", providerName);
            provider.put("vendor", providerName.toLowerCase().replace(" ", "-"));
            provider.put("categories", categories);
            provider.put("scopes", List.of("user.custom-channel"));
            provider.put("create", firstModel.getBaseUrl());
            provider.put("poll", null);
            provider.put("contentType", "application/json");
            provider.put("enabled", true);
            provider.put("unavailableReason", null);
            provider.put("baseUrl", firstModel.getBaseUrl());
            provider.put("workflows", new ArrayList<>());
            provider.put("models", modelList);

            providers.add(provider);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("providers", providers);
        return CommonResult.data(result);
    }
}
