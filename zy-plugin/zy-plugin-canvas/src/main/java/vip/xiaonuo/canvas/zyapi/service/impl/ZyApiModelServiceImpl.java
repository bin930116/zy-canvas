package vip.xiaonuo.canvas.zyapi.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.modular.model.mapper.ZyModelMapper;
import vip.xiaonuo.canvas.zyapi.service.ZyApiModelService;

import java.io.Serializable;
import java.util.*;

/**
 * 模型配置Service实现
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Service
public class ZyApiModelServiceImpl extends ServiceImpl<ZyModelMapper, ZyModel> implements ZyApiModelService {

    /** 模型配置缓存：TTL 30 秒，覆盖任务处理与轮询的热点查询，减少数据库压力 */
    private static final long MODEL_CACHE_TTL_MS = 30_000L;

    /** 模型配置进程内缓存（按 id / modelKey 双键） */
    private final TimedCache<String, ZyModel> modelCache = CacheUtil.newTimedCache(MODEL_CACHE_TTL_MS);

    @Resource
    private ZyModelMapper zyModelMapper;

    @Override
    public ZyModel getById(Serializable id) {
        if (id == null) {
            return null;
        }
        String cacheKey = "id:" + id;
        ZyModel cached = modelCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        ZyModel model = super.getById(id);
        if (model != null) {
            modelCache.put(cacheKey, model);
        }
        return model;
    }

    @Override
    public Map<String, Object> listModels(String capability) {
        // 构建查询条件
        QueryWrapper<ZyModel> queryWrapper = new QueryWrapper<ZyModel>()
                .eq("status", "active")
                .orderByAsc("sort_code");
        
        if (ObjectUtil.isNotEmpty(capability)) {
            queryWrapper.eq("capability", capability);
        }
        
        // 查询模型列表
        List<ZyModel> models = zyModelMapper.selectList(queryWrapper);
        
        // 按提供商分组
        Map<String, List<Map<String, Object>>> modelsByProvider = new LinkedHashMap<>();
        for (ZyModel model : models) {
            String providerName = model.getProviderName();
            if (providerName == null) {
                providerName = "default";
            }
            if (!modelsByProvider.containsKey(providerName)) {
                modelsByProvider.put(providerName, new ArrayList<>());
            }
            
            Map<String, Object> modelMap = new LinkedHashMap<>();
            modelMap.put("id", model.getId());
            modelMap.put("modelKey", model.getModelKey());
            modelMap.put("modelName", model.getModelName());
            modelMap.put("providerName", model.getProviderName());
            modelMap.put("capability", model.getCapability());
            modelMap.put("protocol", model.getProtocol());
            modelMap.put("baseUrl", model.getBaseUrl());
            modelMap.put("capabilitySpecJson", model.getCapabilitySpecJson());
            modelMap.put("isDefault", model.getIsDefault());
            
            modelsByProvider.get(providerName).add(modelMap);
        }
        
        // 构建返回结果
        List<Map<String, Object>> providerList = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : modelsByProvider.entrySet()) {
            Map<String, Object> providerMap = new LinkedHashMap<>();
            providerMap.put("name", entry.getKey());
            providerMap.put("models", entry.getValue());
            providerList.add(providerMap);
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("providers", providerList);
        return result;
    }

    @Override
    public Map<String, Object> getDefaultModels() {
        // 查询默认模型
        List<ZyModel> defaultModels = zyModelMapper.selectList(
                new QueryWrapper<ZyModel>()
                        .eq("status", "active")
                        .eq("is_default", "Y")
                        .orderByAsc("sort_code")
        );
        
        // 按能力分类
        Map<String, Map<String, Object>> defaultsByCapability = new LinkedHashMap<>();
        for (ZyModel model : defaultModels) {
            Map<String, Object> modelMap = new LinkedHashMap<>();
            modelMap.put("id", model.getId());
            modelMap.put("modelKey", model.getModelKey());
            modelMap.put("modelName", model.getModelName());
            modelMap.put("providerName", model.getProviderName());
            modelMap.put("protocol", model.getProtocol());
            
            defaultsByCapability.put(model.getCapability(), modelMap);
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("defaults", defaultsByCapability);
        return result;
    }

    @Override
    public ZyModel getByModelKey(String modelKey) {
        if (StrUtil.isEmpty(modelKey)) {
            return null;
        }
        String cacheKey = "key:" + modelKey;
        ZyModel cached = modelCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        ZyModel model = zyModelMapper.selectOne(
                new QueryWrapper<ZyModel>()
                        .eq("model_key", modelKey)
                        .eq("status", "active")
        );
        if (model != null) {
            modelCache.put(cacheKey, model);
        }
        return model;
    }

    @Override
    public List<ZyModel> listByCapability(String capability) {
        return zyModelMapper.selectList(
                new QueryWrapper<ZyModel>()
                        .eq("capability", capability)
                        .eq("status", "active")
                        .orderByAsc("sort_code")
        );
    }

    @Override
    public ZyModel getDefaultModel(String capability) {
        return zyModelMapper.selectOne(
                new QueryWrapper<ZyModel>()
                        .eq("capability", capability)
                        .eq("status", "active")
                        .eq("is_default", "Y")
                        .last("LIMIT 1")
        );
    }

    @Override
    public List<Map<String, Object>> getAvailableModels(String capability) {
        QueryWrapper<ZyModel> queryWrapper = new QueryWrapper<ZyModel>()
                .eq("status", "active")
                .orderByAsc("sort_code");
        
        if (ObjectUtil.isNotEmpty(capability)) {
            queryWrapper.eq("capability", capability);
        }
        
        List<ZyModel> models = zyModelMapper.selectList(queryWrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (ZyModel model : models) {
            Map<String, Object> modelMap = new LinkedHashMap<>();
            modelMap.put("id", model.getId());
            modelMap.put("code", model.getModelKey());
            modelMap.put("name", model.getModelName());
            modelMap.put("capability", model.getCapability());
            modelMap.put("provider", model.getProviderName());
            modelMap.put("available", true);
            
            // 能力规格
            if (model.getCapabilitySpecJson() != null) {
                modelMap.put("capabilitySpec", JSONUtil.parseObj(model.getCapabilitySpecJson()));
            }
            
            result.add(modelMap);
        }
        
        return result;
    }
}