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
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.asset.mapper.ZyAssetMapper;
import vip.xiaonuo.canvas.modular.assetfolder.entity.ZyAssetFolder;
import vip.xiaonuo.canvas.modular.assetfolder.mapper.ZyAssetFolderMapper;
import vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject;
import vip.xiaonuo.canvas.modular.dramaproject.mapper.ZyDramaProjectMapper;
import vip.xiaonuo.canvas.zyapi.service.ZyApiUserDataService;
import vip.xiaonuo.common.exception.CommonException;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 平台用户数据Service实现
 *
 * @author xuyuxiang
 * @date 2026/9/5 18:40
 **/
@Service
public class ZyApiUserDataServiceImpl implements ZyApiUserDataService {

    @Resource
    private ZyAssetMapper zyAssetMapper;

    @Resource
    private ZyAssetFolderMapper zyAssetFolderMapper;

    @Resource
    private ZyDramaProjectMapper zyDramaProjectMapper;

    @Override
    public Map<String, Object> listAssets(String userId, Integer page, Integer pageSize,
                                        String kind, String category, String folderId,
                                        Boolean uncategorized, String status, String query) {
        // 获取用户所有项目的资产作为"全局资产"
        List<ZyDramaProject> projectList = zyDramaProjectMapper.selectList(
                new QueryWrapper<ZyDramaProject>().eq("user_id", userId)
        );

        List<String> projectIds = projectList.stream()
                .map(ZyDramaProject::getId)
                .collect(Collectors.toList());

        if (ObjectUtil.isEmpty(projectIds)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("assets", new ArrayList<>());
            if (ObjectUtil.isNotEmpty(page) && ObjectUtil.isNotEmpty(pageSize)) {
                result.put("kindCounts", new LinkedHashMap<>());
                result.put("categoryCounts", new LinkedHashMap<>());
                result.put("folderCounts", new LinkedHashMap<>());
                result.put("page", page);
                result.put("pageSize", pageSize);
                result.put("total", 0);
                result.put("hasMore", false);
            }
            return result;
        }

        // 查询所有项目的资产 + 用户个人全局资产（批量上传等素材，project_id 固定为 personal）
        List<ZyAsset> all = zyAssetMapper.selectList(
                new QueryWrapper<ZyAsset>()
                        .and(w -> w.in("project_id", projectIds)
                                .or(o -> o.eq("project_id", "personal").eq("create_user", userId)))
                        .orderByDesc("coalesce(update_time, create_time)")
        );

        // 统计
        Map<String, Integer> kindCounts = new LinkedHashMap<>();
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        Map<String, Integer> folderCounts = new LinkedHashMap<>();

        for (ZyAsset asset : all) {
            if (ObjectUtil.isNotEmpty(asset.getMediaType())) {
                kindCounts.merge(asset.getMediaType(), 1, Integer::sum);
            }
            if (ObjectUtil.isNotEmpty(asset.getCategory())) {
                categoryCounts.merge(asset.getCategory(), 1, Integer::sum);
            }
            if (ObjectUtil.isNotEmpty(asset.getFolderId())) {
                folderCounts.merge(asset.getFolderId(), 1, Integer::sum);
            }
        }

        // 过滤
        List<ZyAsset> filtered = all.stream().filter((asset) -> {
            if (ObjectUtil.isNotEmpty(kind) && !kind.equals(asset.getMediaType())) return false;
            if (ObjectUtil.isNotEmpty(category) && !category.equals(asset.getCategory())) return false;
            if (ObjectUtil.isNotEmpty(status) && !status.equals(asset.getStatus())) return false;
            if (ObjectUtil.isNotEmpty(folderId)) {
                if (!folderId.equals(asset.getFolderId())) return false;
            }
            if (Boolean.TRUE.equals(uncategorized) && ObjectUtil.isNotEmpty(asset.getFolderId())) return false;
            if (ObjectUtil.isNotEmpty(query) && (asset.getTitle() == null || !asset.getTitle().toLowerCase().contains(query.toLowerCase()))) return false;
            return true;
        }).collect(Collectors.toList());

        List<Map<String, Object>> assets = new ArrayList<>();

        // 分页
        if (ObjectUtil.isNotEmpty(page) && ObjectUtil.isNotEmpty(pageSize)) {
            int total = filtered.size();
            int from = Math.min(Math.max((page - 1) * pageSize, 0), total);
            int to = Math.min(from + pageSize, total);
            for (ZyAsset asset : filtered.subList(from, to)) {
                assets.add(toAsset(asset));
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("assets", assets);
            result.put("kindCounts", kindCounts);
            result.put("categoryCounts", categoryCounts);
            result.put("folderCounts", folderCounts);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("total", total);
            result.put("hasMore", (long) page * pageSize < total);
            return result;
        }

        // 不分页，返回摘要
        for (ZyAsset asset : filtered) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("id", asset.getId());
            summary.put("folderId", asset.getFolderId());
            summary.put("kind", asset.getMediaType());
            summary.put("title", asset.getTitle());
            summary.put("createdAt", asset.getCreateTime() != null ? DateUtil.formatDateTime(asset.getCreateTime()) : null);
            summary.put("updatedAt", asset.getUpdateTime() != null ? DateUtil.formatDateTime(asset.getUpdateTime()) : null);
            assets.add(summary);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assets", assets);
        return result;
    }

    @Override
    public Map<String, Object> listAssetsByIds(String userId, List<String> ids) {
        if (ObjectUtil.isEmpty(ids)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("assets", new ArrayList<>());
            return result;
        }

        List<ZyAsset> assets = zyAssetMapper.selectList(
                new QueryWrapper<ZyAsset>().in("id", ids)
        );

        List<Map<String, Object>> resultAssets = new ArrayList<>();
        for (ZyAsset asset : assets) {
            resultAssets.add(toAsset(asset));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assets", resultAssets);
        return result;
    }

    @Override
    public Map<String, Object> getAsset(String userId, String assetId) {
        ZyAsset asset = zyAssetMapper.selectOne(new QueryWrapper<ZyAsset>().eq("id", assetId));
        if (asset == null) {
            throw new CommonException("资产不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("asset", asset);
        return result;
    }

    @Override
    public Map<String, Object> upsertAsset(String userId, Object assetObj) {
        if (!(assetObj instanceof Map)) {
            throw new CommonException("资产数据格式错误");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> assetMap = (Map<String, Object>) assetObj;
        String id = (String) assetMap.get("id");
        if (id == null || id.isEmpty()) {
            throw new CommonException("资产 ID 不能为空");
        }
        // zy_asset.id varchar(64)：前端可能用 generation_<sha256> 等长 ID，超长时截断避免入库失败
        if (id.length() > 64) {
            id = id.substring(0, 64);
        }
        ZyAsset existing = zyAssetMapper.selectOne(new QueryWrapper<ZyAsset>().eq("id", id));
        if (existing != null) {
            if (assetMap.containsKey("title")) existing.setTitle((String) assetMap.get("title"));
            if (assetMap.containsKey("mediaType")) existing.setMediaType((String) assetMap.get("mediaType"));
            if (assetMap.containsKey("category")) existing.setCategory((String) assetMap.get("category"));
            if (assetMap.containsKey("folderId")) existing.setFolderId((String) assetMap.get("folderId"));
            if (assetMap.containsKey("tags")) existing.setTags(JSONUtil.toJsonStr(assetMap.get("tags")));
            if (assetMap.containsKey("status")) existing.setStatus((String) assetMap.get("status"));
            if (assetMap.containsKey("coverUrl")) existing.setCoverUrl((String) assetMap.get("coverUrl"));
            if (assetMap.containsKey("note")) existing.setNote((String) assetMap.get("note"));
            if (assetMap.containsKey("source")) existing.setSource((String) assetMap.get("source"));
            Object updateData = assetMap.get("data");
            if (updateData instanceof Map) {
                Object updateSk = ((Map<?, ?>) updateData).get("storageKey");
                if (updateSk instanceof String && StrUtil.isNotEmpty((String) updateSk)) {
                    existing.setStorageKey((String) updateSk);
                }
            }
            zyAssetMapper.updateById(existing);
        } else {
            ZyAsset newAsset = new ZyAsset();
            newAsset.setId(id);
            newAsset.setTitle((String) assetMap.getOrDefault("title", ""));
            newAsset.setMediaType((String) assetMap.getOrDefault("mediaType", "text"));
            newAsset.setCategory((String) assetMap.getOrDefault("category", "other"));
            newAsset.setFolderId((String) assetMap.get("folderId"));
            newAsset.setTags(JSONUtil.toJsonStr(assetMap.getOrDefault("tags", "[]")));
            newAsset.setStatus((String) assetMap.getOrDefault("status", "confirmed"));
            // project_id 列 NOT NULL：无项目归属的全局素材（批量上传等）固定使用 personal 占位
            newAsset.setProjectId("personal");
            newAsset.setCoverUrl((String) assetMap.get("coverUrl"));
            newAsset.setSource((String) assetMap.get("source"));
            // 显式设置创建人（C 端用户），供无项目全局资产在列表中按用户归属查询
            newAsset.setCreateUser(userId);
            newAsset.setVersionCount(1);
            // 从 data 中提取 storageKey，保证图片等素材缩略图可解析
            Object dataObj = assetMap.get("data");
            if (dataObj instanceof Map) {
                Object storageKeyObj = ((Map<?, ?>) dataObj).get("storageKey");
                if (storageKeyObj instanceof String && StrUtil.isNotEmpty((String) storageKeyObj)) {
                    newAsset.setStorageKey((String) storageKeyObj);
                }
            }
            // updateTime 置为当前时间，保证素材库列表（按更新时间倒序）能排到最前
            newAsset.setUpdateTime(new Date());
            zyAssetMapper.insert(newAsset);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        return result;
    }

    @Override
    public Map<String, Object> deleteAsset(String userId, String assetId) {
        ZyAsset asset = zyAssetMapper.selectOne(new QueryWrapper<ZyAsset>().eq("id", assetId));
        if (asset == null) {
            throw new CommonException("资产不存在");
        }
        zyAssetMapper.delete(new QueryWrapper<ZyAsset>().eq("id", assetId));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", assetId);
        return result;
    }

    @Override
    public Map<String, Object> listFolders(String userId) {
        // 获取用户所有项目的文件夹作为"全局文件夹"
        List<ZyDramaProject> projectList = zyDramaProjectMapper.selectList(
                new QueryWrapper<ZyDramaProject>().eq("user_id", userId)
        );

        List<String> projectIds = projectList.stream()
                .map(ZyDramaProject::getId)
                .collect(Collectors.toList());

        if (ObjectUtil.isEmpty(projectIds)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("folders", new ArrayList<>());
            return result;
        }

        List<Map<String, Object>> folders = new ArrayList<>();
        for (ZyAssetFolder folder : zyAssetFolderMapper.selectList(
                new QueryWrapper<ZyAssetFolder>().in("project_id", projectIds).orderByAsc("position").orderByAsc("create_time"))) {
            folders.add(toFolder(folder));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("folders", folders);
        return result;
    }

    @Override
    public Map<String, Object> createFolder(String userId, String name) {
        throw new CommonException("全局文件夹创建暂未实现，请使用项目文件夹");
    }

    @Override
    public Map<String, Object> updateFolder(String userId, String folderId, String name) {
        throw new CommonException("全局文件夹更新暂未实现，请使用项目文件夹");
    }

    @Override
    public Map<String, Object> deleteFolder(String userId, String folderId) {
        throw new CommonException("全局文件夹删除暂未实现，请使用项目文件夹");
    }

    @Override
    public Map<String, Object> moveAssetsToFolder(String userId, List<String> assetIds, String folderId) {
        throw new CommonException("资产移动暂未实现，请使用项目资产接口");
    }

    private String mediaTypeForAsset(ZyAsset asset, String kind) {
        if (StrUtil.isNotEmpty(asset.getMediaType()) && asset.getMediaType().contains("/")) {
            return asset.getMediaType();
        }
        if ("video".equals(kind)) {
            return "video/mp4";
        }
        if ("audio".equals(kind)) {
            return "audio/mpeg";
        }
        if ("image".equals(kind)) {
            return "image/png";
        }
        return asset.getMediaType() != null ? asset.getMediaType() : "application/octet-stream";
    }

    /**
     * 实体转资产对象
     *
     * @param asset 资产实体
     * @return 资产map
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    private Map<String, Object> toAsset(ZyAsset asset) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", asset.getId());
        result.put("title", asset.getTitle());

        // 根据 mediaType 或 category 确定 kind
        String kind = "image";
        if ("text".equals(asset.getMediaType())) {
            kind = "text";
        } else if ("video".equals(asset.getMediaType())) {
            kind = "video";
        } else if ("audio".equals(asset.getMediaType())) {
            kind = "audio";
        } else if ("model".equals(asset.getMediaType())) {
            kind = "model";
        } else if ("character".equals(asset.getCategory())) {
            kind = "entity";
        }
        result.put("kind", kind);

        result.put("coverUrl", asset.getCoverUrl() != null ? asset.getCoverUrl() : "");
        result.put("tags", parseJsonArray(asset.getTags()));
        result.put("mediaType", asset.getMediaType());
        result.put("category", asset.getCategory());
        result.put("status", asset.getStatus());
        result.put("primaryVersionId", asset.getPrimaryVersionId());
        result.put("versionCount", asset.getVersionCount());
        result.put("usages", parseJsonArray(asset.getUsages()));
        result.put("folderId", asset.getFolderId());
        result.put("position", asset.getPosition());
        result.put("storageKey", asset.getStorageKey());
        result.put("durationMs", asset.getDurationMs());
        result.put("previewText", asset.getPreviewText());
        result.put("source", asset.getSource());
        result.put("note", asset.getNote() != null ? asset.getNote() : "");
        result.put("createdAt", asset.getCreateTime() != null ? DateUtil.formatDateTime(asset.getCreateTime()) : null);
        result.put("updatedAt", asset.getUpdateTime() != null ? DateUtil.formatDateTime(asset.getUpdateTime()) : asset.getCreateTime() != null ? DateUtil.formatDateTime(asset.getCreateTime()) : null);

        // 添加 data 字段
        Map<String, Object> data = new LinkedHashMap<>();
        if ("text".equals(kind)) {
            data.put("content", asset.getPreviewText());
        } else if ("entity".equals(kind)) {
            // 对于角色类型，从 extJson 中解析 definition
            if (ObjectUtil.isNotEmpty(asset.getExtJson())) {
                try {
                    cn.hutool.json.JSONObject ext = JSONUtil.parseObj(asset.getExtJson());
                    if (ext.get("definition") instanceof Map) {
                        data.put("definition", ext.get("definition"));
                    }
                    // 解析角色形象图（representations），并回填 coverUrl 供前端缩略图展示
                    Object repsObj = ext.get("representations");
                    if (repsObj instanceof List) {
                        result.put("representations", repsObj);
                        if (ObjectUtil.isEmpty(asset.getCoverUrl())) {
                            for (Object repObj : (List<?>) repsObj) {
                                if (repObj instanceof Map) {
                                    Object url = ((Map<?, ?>) repObj).get("url");
                                    if (url instanceof String && ((String) url).startsWith("http")) {
                                        result.put("coverUrl", url);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } else {
            // 对于媒体类型，添加基本字段
            String storageKey = asset.getStorageKey();
            // 生成视频/音频：storageKey 存的是 MinIO/文件引擎可播直链；coverUrl 常为空
            String mediaUrl = StrUtil.isNotEmpty(storageKey) && storageKey.startsWith("http") ? storageKey : asset.getCoverUrl();
            data.put("storageKey", storageKey != null ? storageKey : "");
            data.put("width", 0);
            data.put("height", 0);
            data.put("bytes", 0L);
            data.put("mimeType", mediaTypeForAsset(asset, kind));
            if ("image".equals(kind)) {
                data.put("dataUrl", mediaUrl != null ? mediaUrl : "");
            }
            if ("audio".equals(kind) || "video".equals(kind)) {
                data.put("url", mediaUrl != null ? mediaUrl : "");
            }
            if (asset.getDurationMs() != null) {
                data.put("durationMs", asset.getDurationMs());
            }
            if ("model".equals(kind)) {
                data.put("fileName", asset.getTitle());
            }
        }
        result.put("data", data);

        // 添加 metadata 字段，包含项目关联信息
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (ObjectUtil.isNotEmpty(asset.getProjectId())) {
            metadata.put("source", "canvas");
            List<String> projectIds = new ArrayList<>();
            projectIds.add(asset.getProjectId());
            metadata.put("projectIds", projectIds);
            // 尝试获取项目名称
            try {
                vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject project = zyDramaProjectMapper.selectOne(
                    new QueryWrapper<vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject>().eq("id", asset.getProjectId())
                );
                if (project != null && ObjectUtil.isNotEmpty(project.getName())) {
                    metadata.put("projectName", project.getName());
                }
            } catch (Exception ignored) {
            }
        }
        result.put("metadata", metadata);

        return result;
    }

    /**
     * 解析 JSON 字符串为数组
     *
     * @param jsonStr JSON 字符串
     * @return 列表
     */
    private List<Object> parseJsonArray(String jsonStr) {
        if (ObjectUtil.isEmpty(jsonStr)) {
            return new ArrayList<>();
        }
        try {
            if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                return JSONUtil.toList(jsonStr, Object.class);
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }

    /**
     * 实体转文件夹对象
     *
     * @param folder 文件夹实体
     * @return 文件夹map
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    private Map<String, Object> toFolder(ZyAssetFolder folder) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", folder.getId());
        result.put("name", folder.getName());
        result.put("position", folder.getPosition());
        result.put("createdAt", folder.getCreateTime() != null ? DateUtil.formatDateTime(folder.getCreateTime()) : null);
        result.put("updatedAt", folder.getUpdateTime() != null ? DateUtil.formatDateTime(folder.getUpdateTime()) : folder.getCreateTime() != null ? DateUtil.formatDateTime(folder.getCreateTime()) : null);
        return result;
    }
}