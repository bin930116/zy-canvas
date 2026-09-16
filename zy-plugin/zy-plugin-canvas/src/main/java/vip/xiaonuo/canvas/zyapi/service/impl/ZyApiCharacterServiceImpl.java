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
package vip.xiaonuo.canvas.zyapi.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.asset.mapper.ZyAssetMapper;
import vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject;
import vip.xiaonuo.canvas.zyapi.service.ZyApiCharacterService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiDramaProjectService;
import vip.xiaonuo.common.exception.CommonException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 短剧项目角色Service实现(角色存储为 zy_asset, category=character, 卡片数据放 ext_json)
 *
 * @author xuyuxiang
 * @date 2026/9/5 21:00
 **/
@Service
public class ZyApiCharacterServiceImpl implements ZyApiCharacterService {

    private static final String CATEGORY_CHARACTER = "character";

    @Resource
    private ZyAssetMapper zyAssetMapper;

    @Resource
    private ZyApiDramaProjectService zyDramaProjectService;

    @Override
    public Map<String, Object> create(String userId, String projectId, String name, Map<String, Object> definition) {
        requireProject(userId, projectId);
        ZyAsset asset = new ZyAsset();
        asset.setId(IdUtil.fastSimpleUUID());
        asset.setProjectId(projectId);
        asset.setTitle(name);
        asset.setMediaType("image");
        asset.setCategory(CATEGORY_CHARACTER);
        asset.setStatus("active");
        asset.setSource("manual");
        asset.setVersionCount(1);
        asset.setExtJson(extJson(definition, null, null));
        zyAssetMapper.insert(asset);
        return characterDetail(asset);
    }

    @Override
    public Map<String, Object> get(String userId, String projectId, String assetId) {
        requireProject(userId, projectId);
        ZyAsset asset = findCharacter(userId, projectId, assetId);
        if (ObjectUtil.isEmpty(asset)) {
            throw new CommonException("角色不存在");
        }
        return characterDetail(asset);
    }

    @Override
    public Map<String, Object> update(String userId, String projectId, String assetId, String name, Map<String, Object> definition) {
        requireProject(userId, projectId);
        ZyAsset asset = findCharacter(userId, projectId, assetId);
        if (ObjectUtil.isEmpty(asset)) {
            throw new CommonException("角色不存在");
        }
        if (ObjectUtil.isNotEmpty(name)) {
            asset.setTitle(name);
        }
        if (definition != null && !definition.isEmpty()) {
            Map<String, Object> card = cardData(asset);
            @SuppressWarnings("unchecked")
            Map<String, Object> existingDefinition = (Map<String, Object>) card.get("definition");
            if (existingDefinition == null) {
                existingDefinition = new LinkedHashMap<>();
            }
            // 将新的 definition 值合并到现有的 definition 中，只更新非空字段
            for (Map.Entry<String, Object> entry : definition.entrySet()) {
                Object value = entry.getValue();
                // 只更新非空字符串和非空值
                if (value != null && !(value instanceof String && ((String) value).trim().isEmpty())) {
                    existingDefinition.put(entry.getKey(), value);
                }
            }
            card.put("definition", existingDefinition);
            asset.setExtJson(JSONUtil.toJsonStr(card));
        } else if (ObjectUtil.isNotEmpty(name)) {
            // 只更新名称时，也需要保存
            asset.setExtJson(JSONUtil.toJsonStr(cardData(asset)));
        }
        zyAssetMapper.updateById(asset);
        return characterDetail(asset);
    }

    @Override
    public Map<String, Object> putRepresentations(String userId, String projectId, String assetId, List<Map<String, Object>> representations) {
        requireProject(userId, projectId);
        ZyAsset asset = findCharacter(userId, projectId, assetId);
        if (ObjectUtil.isEmpty(asset)) {
            throw new CommonException("角色不存在");
        }
        Map<String, Object> card = cardData(asset);
        card.put("representations", representations == null ? new ArrayList<>() : representations);
        asset.setExtJson(JSONUtil.toJsonStr(card));
        zyAssetMapper.updateById(asset);
        return characterDetail(asset);
    }

    @Override
    public Map<String, Object> putVoice(String userId, String projectId, String assetId, Map<String, Object> voice) {
        requireProject(userId, projectId);
        ZyAsset asset = findCharacter(userId, projectId, assetId);
        if (ObjectUtil.isEmpty(asset)) {
            throw new CommonException("角色不存在");
        }
        Map<String, Object> card = cardData(asset);
        card.put("voice", voice == null ? new LinkedHashMap<>() : voice);
        asset.setExtJson(JSONUtil.toJsonStr(card));
        zyAssetMapper.updateById(asset);
        return characterDetail(asset);
    }

    @Override
    public Map<String, Object> deleteVoice(String userId, String projectId, String assetId) {
        requireProject(userId, projectId);
        ZyAsset asset = findCharacter(userId, projectId, assetId);
        if (ObjectUtil.isEmpty(asset)) {
            throw new CommonException("角色不存在");
        }
        Map<String, Object> card = cardData(asset);
        card.remove("voice");
        asset.setExtJson(JSONUtil.toJsonStr(card));
        zyAssetMapper.updateById(asset);
        return characterDetail(asset);
    }

    /**
     * 按 id+project 查角色(字符资产)
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param assetId   角色id
     * @return 角色资产
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    private ZyAsset findCharacter(String userId, String projectId, String assetId) {
        return zyAssetMapper.selectOne(new QueryWrapper<ZyAsset>()
                .eq("id", assetId)
                .eq("project_id", projectId)
                .eq("category", CATEGORY_CHARACTER));
    }

    /**
     * 读取角色卡数据
     *
     * @param asset 角色资产
     * @return { definition, representations, voice }
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> cardData(ZyAsset asset) {
        Map<String, Object> card = new LinkedHashMap<>();
        Map<String, Object> definition = new LinkedHashMap<>();
        List<Map<String, Object>> representations = new ArrayList<>();
        Map<String, Object> voice = null;
        if (ObjectUtil.isNotEmpty(asset.getExtJson())) {
            try {
                JSONObject ext = JSONUtil.parseObj(asset.getExtJson());
                if (ext.get("definition") instanceof Map) {
                    definition = new LinkedHashMap<>((Map<String, Object>) ext.get("definition"));
                }
                if (ext.get("representations") instanceof JSONArray) {
                    JSONArray reps = ext.getJSONArray("representations");
                    representations = new ArrayList<>();
                    for (int i = 0; i < reps.size(); i++) {
                        Object item = reps.get(i);
                        if (item instanceof JSONObject) {
                            representations.add((JSONObject) item);
                        }
                    }
                }
                if (ext.get("voice") instanceof JSONObject) {
                    voice = ext.getJSONObject("voice");
                }
            } catch (Exception ignored) {
            }
        }
        card.put("definition", definition);
        card.put("representations", representations);
        if (voice != null) {
            card.put("voice", voice);
        }
        return card;
    }

    /**
     * 构造 ext_json
     *
     * @param definition     角色定义
     * @param representations 形象
     * @param voice           音色
     * @return json字符串
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    private String extJson(Map<String, Object> definition, List<Map<String, Object>> representations, Map<String, Object> voice) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("definition", definition == null ? new LinkedHashMap<>() : definition);
        card.put("representations", representations == null ? new ArrayList<>() : representations);
        if (voice != null) {
            card.put("voice", voice);
        }
        return JSONUtil.toJsonStr(card);
    }

    /**
     * 组装角色详情 { asset, character }
     *
     * @param asset 角色资产
     * @return { asset, character }
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> characterDetail(ZyAsset asset) {
        Map<String, Object> definition = new LinkedHashMap<>();
        List<Map<String, Object>> representations = new ArrayList<>();
        Map<String, Object> voice = null;
        if (ObjectUtil.isNotEmpty(asset.getExtJson())) {
            try {
                JSONObject ext = JSONUtil.parseObj(asset.getExtJson());
                if (ext.get("definition") instanceof Map) {
                    definition = new LinkedHashMap<>((Map<String, Object>) ext.get("definition"));
                }
                if (ext.get("representations") instanceof JSONArray) {
                    JSONArray reps = ext.getJSONArray("representations");
                    representations = new ArrayList<>();
                    for (int i = 0; i < reps.size(); i++) {
                        Object item = reps.get(i);
                        if (item instanceof JSONObject) {
                            representations.add((JSONObject) item);
                        }
                    }
                }
                if (ext.get("voice") instanceof JSONObject) {
                    voice = ext.getJSONObject("voice");
                }
            } catch (Exception ignored) {
            }
        }
        Map<String, Object> character = new LinkedHashMap<>();
        character.put("versionId", asset.getId());
        character.put("version", asset.getVersionCount() != null ? asset.getVersionCount() : 1);
        character.put("definition", definition);
        character.put("representations", representations);
        if (voice != null) {
            character.put("voice", voice);
        }
        character.put("visualStatus", representations.isEmpty() ? "missing" : "ready");
        character.put("voiceStatus", voice == null ? "missing" : "ready");

        Map<String, Object> assetMap = new LinkedHashMap<>();
        assetMap.put("id", asset.getId());
        assetMap.put("projectId", asset.getProjectId());
        assetMap.put("title", asset.getTitle());
        assetMap.put("mediaType", asset.getMediaType());
        assetMap.put("category", asset.getCategory());
        assetMap.put("status", asset.getStatus());
        assetMap.put("primaryVersionId", asset.getPrimaryVersionId());
        assetMap.put("versionCount", asset.getVersionCount());
        assetMap.put("usages", new ArrayList<>());
        assetMap.put("folderId", asset.getFolderId());
        assetMap.put("position", asset.getPosition());
        assetMap.put("storageKey", asset.getStorageKey());
        assetMap.put("durationMs", asset.getDurationMs());
        assetMap.put("previewText", asset.getPreviewText());
        assetMap.put("character", character);
        assetMap.put("source", asset.getSource());
        assetMap.put("updatedAt", asset.getUpdateTime() != null ? DateUtil.formatDateTime(asset.getUpdateTime()) : asset.getCreateTime() != null ? DateUtil.formatDateTime(asset.getCreateTime()) : null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("asset", assetMap);
        result.put("character", character);
        return result;
    }

    /**
     * 校验项目存在且归属当前用户
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    private void requireProject(String userId, String projectId) {
        ZyDramaProject project = zyDramaProjectService.getById(userId, projectId);
        if (ObjectUtil.isEmpty(project)) {
            throw new CommonException("短剧项目不存在");
        }
    }
}
