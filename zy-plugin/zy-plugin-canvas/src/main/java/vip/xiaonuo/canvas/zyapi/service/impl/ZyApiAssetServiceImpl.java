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
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.asset.mapper.ZyAssetMapper;
import vip.xiaonuo.canvas.modular.assetcandidate.entity.ZyAssetCandidate;
import vip.xiaonuo.canvas.modular.assetcandidate.mapper.ZyAssetCandidateMapper;
import vip.xiaonuo.canvas.modular.assetfolder.entity.ZyAssetFolder;
import vip.xiaonuo.canvas.modular.assetfolder.mapper.ZyAssetFolderMapper;
import vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject;
import vip.xiaonuo.canvas.zyapi.param.ZyApiAssetCandidateParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiAssetFolderParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiAssetParam;
import vip.xiaonuo.canvas.zyapi.service.ZyApiAssetService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiCharacterService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiDramaProjectService;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.dev.api.DevFileApi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 短剧项目资产Service实现
 *
 * @author xuyuxiang
 * @date 2026/9/5 21:30
 **/
@Service
public class ZyApiAssetServiceImpl implements ZyApiAssetService {

    private static final Logger log = LoggerFactory.getLogger(ZyApiAssetServiceImpl.class);

    private static final String CATEGORY_CHARACTER = "character";

    @Resource
    private ZyAssetMapper zyAssetMapper;

    @Resource
    private ZyAssetCandidateMapper zyAssetCandidateMapper;

    @Resource
    private ZyAssetFolderMapper zyAssetFolderMapper;

    @Resource
    private DevFileApi devFileApi;

    @Resource
    private ZyApiDramaProjectService zyDramaProjectService;

    @Resource
    private ZyApiCharacterService zyCharacterService;

    @Override
    public Map<String, Object> listAssets(String userId, String projectId, Integer page, Integer pageSize,
                                          String category, String mediaType, String status, String folderId, String q) {
        requireProject(userId, projectId);
        List<ZyAsset> all = zyAssetMapper.selectPage(new Page<>(1, 10000, false),
                new QueryWrapper<ZyAsset>().eq("project_id", projectId)).getRecords();

        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        Map<String, Integer> folderCounts = new LinkedHashMap<>();
        for (ZyAsset asset : all) {
            if (ObjectUtil.isNotEmpty(asset.getCategory())) {
                categoryCounts.merge(asset.getCategory(), 1, Integer::sum);
            }
            if (ObjectUtil.isNotEmpty(asset.getFolderId())) {
                folderCounts.merge(asset.getFolderId(), 1, Integer::sum);
            }
        }
        List<ZyAsset> filtered = all.stream().filter((asset) -> {
            if (ObjectUtil.isNotEmpty(category) && !category.equals(asset.getCategory())) return false;
            if (ObjectUtil.isNotEmpty(mediaType) && !mediaType.equals(asset.getMediaType())) return false;
            if (ObjectUtil.isNotEmpty(status) && !status.equals(asset.getStatus())) return false;
            if (ObjectUtil.isNotEmpty(folderId)) {
                if (!folderId.equals(asset.getFolderId())) return false;
            }
            if (ObjectUtil.isNotEmpty(q) && (asset.getTitle() == null || !asset.getTitle().toLowerCase().contains(q.toLowerCase()))) return false;
            return true;
        }).collect(Collectors.toList());

        List<Map<String, Object>> assets = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(page) && ObjectUtil.isNotEmpty(pageSize)) {
            int total = filtered.size();
            int from = Math.min(Math.max((page - 1) * pageSize, 0), total);
            int to = Math.min(from + pageSize, total);
            for (ZyAsset asset : filtered.subList(from, to)) {
                assets.add(toAsset(asset));
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("assets", assets);
            result.put("categoryCounts", categoryCounts);
            result.put("folderCounts", folderCounts);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("total", total);
            result.put("hasMore", (long) page * pageSize < total);
            return result;
        }
        for (ZyAsset asset : filtered) {
            assets.add(toAsset(asset));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assets", assets);
        return result;
    }

    @Override
    public Map<String, Object> createAsset(String userId, String projectId, ZyApiAssetParam param) {
        requireProject(userId, projectId);
        ZyAsset asset = new ZyAsset();
        asset.setId(IdUtil.fastSimpleUUID());
        asset.setProjectId(projectId);
        asset.setTitle(param.getTitle());
        asset.setMediaType(param.getMediaType());
        asset.setCategory(param.getCategory());
        asset.setStatus(ObjectUtil.isNotEmpty(param.getStatus()) ? param.getStatus() : "active");
        asset.setFolderId(param.getFolderId());
        asset.setStorageKey(param.getStorageKey());
        asset.setDurationMs(param.getDurationMs() != null ? param.getDurationMs().intValue() : null);
        asset.setPreviewText(param.getPreviewText());
        asset.setSource(param.getSource());
        asset.setPosition(param.getPosition() != null ? param.getPosition() : 0);
        asset.setVersionCount(0);
        asset.setUsages("[]");
        zyAssetMapper.insert(asset);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("asset", toAsset(asset));
        return result;
    }

    @Override
    public Map<String, Object> updateAsset(String userId, String projectId, String assetId, ZyApiAssetParam param) {
        requireProject(userId, projectId);
        ZyAsset asset = zyAssetMapper.selectOne(new QueryWrapper<ZyAsset>().eq("id", assetId).eq("project_id", projectId));
        if (ObjectUtil.isEmpty(asset)) {
            throw new CommonException("资产不存在");
        }
        // 检查编辑权限：只能编辑自己创建的资产
        if (!ZyApiDramaProjectServiceImpl.canEditResource(asset.getCreateUser(), userId)) {
            throw new CommonException("无权限编辑该资产，只能编辑自己创建的内容");
        }
        if (param.getTitle() != null) asset.setTitle(param.getTitle());
        if (param.getMediaType() != null) asset.setMediaType(param.getMediaType());
        if (param.getCategory() != null) asset.setCategory(param.getCategory());
        if (param.getFolderId() != null) asset.setFolderId(param.getFolderId());
        if (param.getStatus() != null) asset.setStatus(param.getStatus());
        if (param.getStorageKey() != null) asset.setStorageKey(param.getStorageKey());
        if (param.getDurationMs() != null) asset.setDurationMs(param.getDurationMs().intValue());
        if (param.getPreviewText() != null) asset.setPreviewText(param.getPreviewText());
        zyAssetMapper.updateById(asset);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("asset", toAsset(asset));
        return result;
    }

    @Override
    public Map<String, Object> deleteAsset(String userId, String projectId, String assetId) {
        requireProject(userId, projectId);
        ZyAsset asset = zyAssetMapper.selectOne(new QueryWrapper<ZyAsset>().eq("id", assetId).eq("project_id", projectId));
        if (ObjectUtil.isEmpty(asset)) {
            throw new CommonException("资产不存在");
        }
        // 检查删除权限：只能删除自己创建的资产
        if (!ZyApiDramaProjectServiceImpl.canDeleteResource(asset.getCreateUser(), userId)) {
            throw new CommonException("无权限删除该资产，只能删除自己创建的内容");
        }
        zyAssetMapper.deleteById(assetId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", assetId);
        return result;
    }

    @Override
    public Map<String, Object> listFolders(String userId, String projectId) {
        requireProject(userId, projectId);
        List<Map<String, Object>> folders = new ArrayList<>();
        for (ZyAssetFolder folder : zyAssetFolderMapper.selectList(new QueryWrapper<ZyAssetFolder>()
                .eq("project_id", projectId).orderByAsc("position").orderByAsc("create_time"))) {
            folders.add(toFolder(folder));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("folders", folders);
        return result;
    }

    @Override
    public Map<String, Object> createFolder(String userId, String projectId, ZyApiAssetFolderParam param) {
        requireProject(userId, projectId);
        ZyAssetFolder folder = new ZyAssetFolder();
        folder.setId(IdUtil.fastSimpleUUID());
        folder.setProjectId(projectId);
        folder.setName(param.getName());
        folder.setParentId(param.getParentId());
        folder.setStyle(param.getStyle());
        folder.setTheme(param.getTheme());
        folder.setPosition(param.getPosition() != null ? param.getPosition() : 0);
        zyAssetFolderMapper.insert(folder);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("folder", toFolder(folder));
        return result;
    }

    @Override
    public Map<String, Object> updateFolder(String userId, String projectId, String folderId, ZyApiAssetFolderParam param) {
        requireProject(userId, projectId);
        ZyAssetFolder folder = zyAssetFolderMapper.selectOne(new QueryWrapper<ZyAssetFolder>().eq("id", folderId).eq("project_id", projectId));
        if (ObjectUtil.isEmpty(folder)) {
            throw new CommonException("文件夹不存在");
        }
        if (param.getName() != null) folder.setName(param.getName());
        if (param.getParentId() != null) folder.setParentId(param.getParentId());
        if (param.getStyle() != null) folder.setStyle(param.getStyle());
        if (param.getTheme() != null) folder.setTheme(param.getTheme());
        if (param.getPosition() != null) folder.setPosition(param.getPosition());
        zyAssetFolderMapper.updateById(folder);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("folder", toFolder(folder));
        return result;
    }

    @Override
    public Map<String, Object> deleteFolder(String userId, String projectId, String folderId) {
        requireProject(userId, projectId);
        zyAssetFolderMapper.delete(new QueryWrapper<ZyAssetFolder>().eq("id", folderId).eq("project_id", projectId));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", folderId);
        return result;
    }

    @Override
    public Map<String, Object> createVersion(String userId, String projectId, String assetId) {
        requireProject(userId, projectId);
        ZyAsset asset = zyAssetMapper.selectOne(new QueryWrapper<ZyAsset>().eq("id", assetId).eq("project_id", projectId));
        if (ObjectUtil.isEmpty(asset)) {
            throw new CommonException("资产不存在");
        }
        int nextVersion = (asset.getVersionCount() == null ? 0 : asset.getVersionCount()) + 1;
        asset.setVersionCount(nextVersion);
        if (ObjectUtil.isEmpty(asset.getPrimaryVersionId())) {
            asset.setPrimaryVersionId(asset.getId());
        }
        zyAssetMapper.updateById(asset);
        Map<String, Object> version = new LinkedHashMap<>();
        version.put("id", IdUtil.fastSimpleUUID());
        version.put("assetId", assetId);
        version.put("version", nextVersion);
        version.put("status", "ready");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version", version);
        return result;
    }

    /**
     * 实体转资产对象
     *
     * @param asset 资产实体
     * @return 资产map
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    private Map<String, Object> toAsset(ZyAsset asset) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", asset.getId());
        result.put("title", asset.getTitle());
        result.put("mediaType", asset.getMediaType());
        result.put("category", asset.getCategory());
        result.put("status", asset.getStatus());
        result.put("primaryVersionId", asset.getPrimaryVersionId());
        result.put("versionCount", asset.getVersionCount());
        result.put("usages", new ArrayList<>());
        result.put("folderId", asset.getFolderId());
        result.put("position", asset.getPosition());
        result.put("storageKey", asset.getStorageKey());
        result.put("durationMs", asset.getDurationMs());
        result.put("previewText", asset.getPreviewText());
        result.put("source", asset.getSource());
        result.put("updatedAt", asset.getUpdateTime() != null ? DateUtil.formatDateTime(asset.getUpdateTime()) : asset.getCreateTime() != null ? DateUtil.formatDateTime(asset.getCreateTime()) : null);

        // 如果是角色类型，添加 character 字段
        if (CATEGORY_CHARACTER.equals(asset.getCategory())) {
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
            result.put("character", character);
        }

        return result;
    }

    /**
     * 实体转文件夹对象
     *
     * @param folder 文件夹实体
     * @return 文件夹map
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    private Map<String, Object> toFolder(ZyAssetFolder folder) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", folder.getId());
        result.put("projectId", folder.getProjectId());
        result.put("parentId", folder.getParentId());
        result.put("name", folder.getName());
        result.put("style", folder.getStyle());
        result.put("theme", folder.getTheme());
        result.put("position", folder.getPosition());
        result.put("createdAt", folder.getCreateTime() != null ? DateUtil.formatDateTime(folder.getCreateTime()) : null);
        result.put("updatedAt", folder.getUpdateTime() != null ? DateUtil.formatDateTime(folder.getUpdateTime()) : folder.getCreateTime() != null ? DateUtil.formatDateTime(folder.getCreateTime()) : null);
        return result;
    }

    /**
     * 校验项目存在且归属当前用户
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    private void requireProject(String userId, String projectId) {
        ZyDramaProject project = zyDramaProjectService.getById(userId, projectId);
        if (ObjectUtil.isEmpty(project)) {
            throw new CommonException("短剧项目不存在");
        }
    }

    @Override
    public Map<String, Object> listAssetCandidates(String userId, String projectId, Integer page, Integer pageSize,
                                                   String unitId, String status, String category, String query) {
        requireProject(userId, projectId);

        QueryWrapper<ZyAssetCandidate> queryWrapper = new QueryWrapper<ZyAssetCandidate>()
                .eq("project_id", projectId)
                .orderByDesc("create_time");

        if (ObjectUtil.isNotEmpty(unitId)) {
            queryWrapper.eq("unit_id", unitId);
        }
        if (ObjectUtil.isNotEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        if (ObjectUtil.isNotEmpty(category)) {
            queryWrapper.eq("category", category);
        }
        if (ObjectUtil.isNotEmpty(query)) {
            queryWrapper.like("name_key", query.toLowerCase());
        }

        Page<ZyAssetCandidate> pageResult = zyAssetCandidateMapper.selectPage(
                new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 100),
                queryWrapper
        );

        List<Map<String, Object>> candidates = new ArrayList<>();
        for (ZyAssetCandidate candidate : pageResult.getRecords()) {
            candidates.add(toAssetCandidate(candidate));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("candidates", candidates);
        result.put("page", pageResult.getCurrent());
        result.put("pageSize", pageResult.getSize());
        result.put("total", pageResult.getTotal());
        result.put("hasMore", pageResult.getCurrent() * pageResult.getSize() < pageResult.getTotal());
        return result;
    }

    @Override
    public Map<String, Object> createAssetCandidates(String userId, String projectId,
                                                     java.util.List<ZyApiAssetCandidateParam> params, String source) {
        requireProject(userId, projectId);

        if (ObjectUtil.isEmpty(params)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("candidates", new ArrayList<>());
            return result;
        }

        List<Map<String, Object>> candidates = new ArrayList<>();
        for (ZyApiAssetCandidateParam param : params) {
            String nameKey = param.getName() != null ? param.getName().toLowerCase() : "";
            // 幂等：同项目下同名同类候选已存在（未删除，逻辑删除由 @TableLogic 自动过滤）时跳过，避免前端并发/恢复重复插入
            Long existing = zyAssetCandidateMapper.selectCount(new QueryWrapper<ZyAssetCandidate>()
                    .eq("project_id", projectId)
                    .eq("name_key", nameKey)
                    .eq("category", param.getCategory()));
            if (existing != null && existing > 0) {
                continue;
            }
            ZyAssetCandidate candidate = new ZyAssetCandidate();
            candidate.setId(IdUtil.fastSimpleUUID());
            candidate.setProjectId(projectId);
            candidate.setUnitId(param.getUnitId());
            candidate.setShotId(param.getShotId());
            candidate.setName(param.getName());
            candidate.setNameKey(param.getName() != null ? param.getName().toLowerCase() : "");
            candidate.setCategory(param.getCategory());
            candidate.setStatus("pending_confirmation");
            candidate.setSource(source);
            if (param.getDetails() != null) {
                candidate.setDetailsJson(cn.hutool.json.JSONUtil.toJsonStr(param.getDetails()));
            }
            zyAssetCandidateMapper.insert(candidate);
            candidates.add(toAssetCandidate(candidate));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("candidates", candidates);
        return result;
    }

    @Override
    public Map<String, Object> confirmAssetCandidate(String userId, String projectId, String candidateId, String assetId) {
        requireProject(userId, projectId);

        ZyAssetCandidate candidate = zyAssetCandidateMapper.selectOne(
                new QueryWrapper<ZyAssetCandidate>()
                        .eq("id", candidateId)
                        .eq("project_id", projectId)
        );
        if (ObjectUtil.isEmpty(candidate)) {
            throw new CommonException("资产候选不存在");
        }

        // 更新候选状态
        candidate.setStatus("confirmed");

        // 如果提供了资产id，返回该资产
        if (ObjectUtil.isNotEmpty(assetId)) {
            candidate.setResolvedAssetId(assetId);
            zyAssetCandidateMapper.updateById(candidate);
            ZyAsset asset = zyAssetMapper.selectOne(
                    new QueryWrapper<ZyAsset>()
                            .eq("id", assetId)
                            .eq("project_id", projectId)
            );
            if (ObjectUtil.isNotEmpty(asset)) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("asset", toAsset(asset));
                return result;
            }
        } else {
            // 未指定资产id：从候选详情创建角色卡并返回，匹配前端"确认新角色"契约（前端同步个人角色投影需要 asset）
            Map<String, Object> definition = new LinkedHashMap<>();
            if (StrUtil.isNotEmpty(candidate.getDetailsJson())) {
                JSONObject details = JSONUtil.parseObj(candidate.getDetailsJson());
                for (Map.Entry<String, Object> entry : details.entrySet()) {
                    Object value = entry.getValue();
                    if (value != null && !(value instanceof String && ((String) value).trim().isEmpty())) {
                        definition.put(entry.getKey(), value);
                    }
                }
            }
            Map<String, Object> created = zyCharacterService.create(userId, projectId, candidate.getName(), definition);
            Map<String, Object> createdAsset = created != null ? (Map<String, Object>) created.get("asset") : null;
            if (createdAsset != null) {
                candidate.setResolvedAssetId((String) createdAsset.get("id"));
                zyAssetCandidateMapper.updateById(candidate);
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("asset", createdAsset);
                return result;
            }
        }

        // 否则返回空（前端会处理）
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("asset", null);
        return result;
    }

    /**
     * 实体转资产候选对象
     *
     * @param candidate 候选实体
     * @return 候选map
     * @author xuyuxiang
     * @date 2026/9/5 22:00
     */
    private Map<String, Object> toAssetCandidate(ZyAssetCandidate candidate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", candidate.getId());
        result.put("projectId", candidate.getProjectId());
        result.put("unitId", candidate.getUnitId());
        result.put("shotId", candidate.getShotId());
        result.put("name", candidate.getName());
        result.put("category", candidate.getCategory());
        result.put("status", candidate.getStatus());
        result.put("detailsJson", candidate.getDetailsJson());
        result.put("resolvedAssetId", candidate.getResolvedAssetId());
        result.put("createdAt", candidate.getCreateTime() != null ? DateUtil.formatDateTime(candidate.getCreateTime()) : null);
        result.put("updatedAt", candidate.getUpdateTime() != null ? DateUtil.formatDateTime(candidate.getUpdateTime()) : null);
        return result;
    }

    @Override
    public ZyAsset storeMedia(String userId, String projectId, byte[] data, String mediaType, String mimeType) {
        return doStore(userId, projectId, mediaType, mimeType, new MultipartFileSupplier() {
            @Override
            public MultipartFile create(String filename, String contentType) {
                return toMultipartFile(data, filename, contentType);
            }

            @Override
            public byte[] peekBytes() {
                return data;
            }
        });
    }

    @Override
    public ZyAsset storeMediaFile(String userId, String projectId, File file, String mediaType, String mimeType) {
        return doStore(userId, projectId, mediaType, mimeType, new MultipartFileSupplier() {
            @Override
            public MultipartFile create(String filename, String contentType) {
                return toFileMultipartFile(file, filename, contentType);
            }

            @Override
            public byte[] peekBytes() {
                // 只读文件头 12 字节用于魔数嗅探，避免全量读取
                try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "r")) {
                    byte[] head = new byte[12];
                    int n = raf.read(head);
                    return n > 0 ? java.util.Arrays.copyOf(head, n) : null;
                } catch (java.io.IOException e) {
                    return null;
                }
            }
        });
    }

    /**
     * 通用媒体入库：创建资产实体 → 上传默认文件引擎 → 入库
     */
    private ZyAsset doStore(String userId, String projectId, String mediaType, String mimeType,
                            MultipartFileSupplier multipartSupplier) {
        // 图片类型做魔数嗅探兜底：上游/调用方硬编码的 image/png 可能与真实格式不符（如 JPEG），
        // 嗅探到明确格式时以真实格式落库，保证扩展名与 Content-Type 一致
        String resolvedMime = mimeType;
        if ("image".equals(mediaType)) {
            String sniffed = sniffImageMimeType(multipartSupplier.peekBytes());
            if (sniffed != null && (StrUtil.isEmpty(resolvedMime) || "image/png".equalsIgnoreCase(resolvedMime))) {
                resolvedMime = sniffed;
            }
        }
        if (StrUtil.isEmpty(resolvedMime)) {
            resolvedMime = "video".equals(mediaType) ? "video/mp4" : "audio".equals(mediaType) ? "audio/mpeg" : "image/png";
        }

        // 创建资产实体
        ZyAsset asset = new ZyAsset();
        asset.setId(IdUtil.fastSimpleUUID());
        asset.setTitle("Generated " + mediaType + " " + System.currentTimeMillis());
        asset.setMediaType(mediaType);
        asset.setStatus("active");
        asset.setSource("generated");
        // zy_asset.project_id 非空：无项目归属的生成结果落到个人素材库
        asset.setProjectId(StrUtil.isNotEmpty(projectId) ? projectId : "personal");
        // 生成任务在 worker 线程执行，无登录上下文，MyBatis-Plus 自动填充 createUser 会兜底为 -1；
        // 这里显式使用任务携带的用户ID。
        if (StrUtil.isNotEmpty(userId)) {
            asset.setCreateUser(userId);
        }

        // 上传到系统默认文件引擎（当前为 MINIO），storageKey 存外网 URL，前端可直接加载
        String storageKey = uploadToDefaultEngine(multipartSupplier, mediaType, resolvedMime);
        asset.setStorageKey(storageKey);

        // 入库
        zyAssetMapper.insert(asset);

        return asset;
    }

    /**
     * 图片魔数嗅探：识别 PNG / JPEG / GIF / WebP，无法识别返回 null
     */
    public static String sniffImageMimeType(byte[] data) {
        if (data == null || data.length < 12) {
            return null;
        }
        // PNG: 89 50 4E 47
        if ((data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return "image/png";
        }
        // JPEG: FF D8 FF
        if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        // GIF: 'GIF'
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
            return "image/gif";
        }
        // WebP: RIFF....WEBP
        if (data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    /** 按 MIME 推导扩展名（与真实字节格式保持一致，避免 JPEG 存成 .png） */
    private String extensionOf(String mimeType, String mediaType) {
        if (StrUtil.isNotEmpty(mimeType)) {
            switch (mimeType.toLowerCase()) {
                case "video/mp4": return "mp4";
                case "video/webm": return "webm";
                case "video/quicktime": return "mov";
                case "image/png": return "png";
                case "image/jpeg": return "jpg";
                case "image/webp": return "webp";
                case "image/gif": return "gif";
                case "audio/mpeg": return "mp3";
                case "audio/wav": return "wav";
                case "audio/mp4":
                case "audio/m4a": return "m4a";
                default: break;
            }
        }
        if ("video".equals(mediaType)) return "mp4";
        if ("audio".equals(mediaType)) return "mp3";
        return "png";
    }

    /**
     * 上传到系统默认文件引擎（SNOWY_SYS_DEFAULT_FILE_ENGINE，当前为 MINIO），
     * 直接返回 MinIO 公网直链（storage_path），前端浏览器直连 MinIO，避免走 82 代理中转大文件（视频）。
     * 若拿不到直链则回退到 dev 带签名下载地址。
     */
    private String uploadToDefaultEngine(MultipartFileSupplier multipartSupplier, String mediaType, String mimeType) {
        try {
            String extension = extensionOf(mimeType, mediaType);
            String filename = IdUtil.fastSimpleUUID() + "." + extension;
            String contentType = StrUtil.isNotEmpty(mimeType) ? mimeType : ("video".equals(mediaType) ? "video/mp4" : "audio".equals(mediaType) ? "audio/mpeg" : "image/png");
            MultipartFile multipartFile = multipartSupplier.create(filename, contentType);
            return uploadAndResolveStorageKey(multipartFile);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            throw new CommonException("文件上传失败: " + e.getMessage());
        }
    }

    private String uploadAndResolveStorageKey(MultipartFile multipartFile) {
        try {
            // 上传一次，拿到 dev 带签名 url
            String devUrl = devFileApi.uploadDynamicReturnUrl(multipartFile);
            if (StrUtil.isEmpty(devUrl)) {
                throw new CommonException("文件引擎未返回可用地址");
            }
            // 从 devUrl 里解析文件 id，查 MinIO 公网直链
            String fileId = null;
            int idIdx = devUrl.indexOf("id=");
            if (idIdx >= 0) {
                int end = devUrl.indexOf("&", idIdx);
                fileId = devUrl.substring(idIdx + 3, end > idIdx ? end : devUrl.length());
            }
            if (StrUtil.isNotEmpty(fileId)) {
                try {
                    JSONObject info = devFileApi.getFileInfoById(fileId);
                    if (info != null) {
                        // 兼容 camelCase / snake_case；MinIO 公网直链必须优先，否则 NewAPI 拉不到参考图
                        String storagePath = info.getStr("storagePath");
                        if (StrUtil.isEmpty(storagePath)) {
                            storagePath = info.getStr("storage_path");
                        }
                        if (StrUtil.isEmpty(storagePath)) {
                            storagePath = info.getStr("url");
                        }
                        if (StrUtil.isNotEmpty(storagePath) && storagePath.startsWith("http")
                                && !storagePath.contains("/dev/file/download")) {
                            return storagePath;
                        }
                        log.warn("MinIO 直链不可用，回退 dev 下载链 fileId={}, storagePath={}", fileId, storagePath);
                    }
                } catch (Exception e) {
                    log.warn("查询文件详情失败，回退 dev 下载链 fileId={}: {}", fileId, e.getMessage());
                }
            }
            return devUrl;
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            throw new CommonException("文件上传失败: " + e.getMessage());
        }
    }

    private MultipartFile toMultipartFile(byte[] data, String filename, String contentType) {
        return new MultipartFile() {
            @Override
            public String getName() { return "file"; }
            @Override
            public String getOriginalFilename() { return filename; }
            @Override
            public String getContentType() { return contentType; }
            @Override
            public boolean isEmpty() { return data == null || data.length == 0; }
            @Override
            public long getSize() { return data == null ? 0 : data.length; }
            @Override
            public byte[] getBytes() { return data; }
            @Override
            public InputStream getInputStream() { return new ByteArrayInputStream(data); }
            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                FileUtil.writeBytes(data, dest);
            }
        };
    }

    private MultipartFile toFileMultipartFile(File file, String filename, String contentType) {
        return new MultipartFile() {
            @Override
            public String getName() { return "file"; }
            @Override
            public String getOriginalFilename() { return filename; }
            @Override
            public String getContentType() { return contentType; }
            @Override
            public boolean isEmpty() { return file == null || file.length() == 0; }
            @Override
            public long getSize() { return file == null ? 0 : file.length(); }
            @Override
            public byte[] getBytes() throws IOException {
                return FileUtil.readBytes(file);
            }
            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }
            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                FileUtil.copy(file, dest, true);
            }
        };
    }

    /** 按需提供 MultipartFile（允许实现先探测字节再构造） */
    private interface MultipartFileSupplier {
        MultipartFile create(String filename, String contentType);

        /** 仅用于魔数嗅探的字节窥视（stream 场景返回 null 表示不可用） */
        default byte[] peekBytes() {
            return null;
        }
    }

    @Override
    public ZyAsset storeBase64(String userId, String projectId, String dataUrl, String mediaType) {
        // 解析Base64数据
        if (dataUrl == null || !dataUrl.contains(",")) {
            throw new CommonException("无效的Base64数据");
        }
        
        String base64Data = dataUrl.split(",")[1];
        byte[] data = java.util.Base64.getDecoder().decode(base64Data);
        
        // 提取MIME类型
        String mimeType = "application/octet-stream";
        if (dataUrl.contains(":") && dataUrl.contains(";")) {
            mimeType = dataUrl.substring(dataUrl.indexOf(":") + 1, dataUrl.indexOf(";"));
        }
        
        return storeMedia(userId, projectId, data, mediaType, mimeType);
    }
}
