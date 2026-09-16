/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议
 */
package vip.xiaonuo.canvas.zyapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.skill.entity.ZySkill;
import vip.xiaonuo.canvas.modular.skill.entity.ZyUserSkill;
import vip.xiaonuo.canvas.modular.skill.mapper.ZySkillMapper;
import vip.xiaonuo.canvas.modular.skill.mapper.ZyUserSkillMapper;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * C端技能库
 *
 * @author hanbin
 * @date  2026/09/11
 **/
@Tag(name = "C端技能库")
@RestController
@RequestMapping("/skills")
public class ZyApiSkillController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final List<Map<String, String>> CATEGORIES = List.of(
            Map.of("value", "drama", "label", "短剧影视"),
            Map.of("value", "ecommerce", "label", "电商营销"),
            Map.of("value", "creative", "label", "创意设计"),
            Map.of("value", "social", "label", "社媒内容"),
            Map.of("value", "others", "label", "其他")
    );

    @Resource
    private ZySkillMapper zySkillMapper;

    @Resource
    private ZyUserSkillMapper zyUserSkillMapper;

    // ==================== 列表 ====================

    @Operation(summary = "技能列表（支持 scope=public/mine/created/favorites）")
    @GetMapping
    public CommonResult<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int page_size,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag) {

        String userId = currentUserId();
        QueryWrapper<ZySkill> qw = new QueryWrapper<>();
        qw.eq("status", 1);

        // 按 scope 过滤
        if ("mine".equals(scope)) {
            // 我添加的：JOIN zy_user_skill
            List<String> addedSkillIds = userSkillIds(userId, true, false);
            if (addedSkillIds.isEmpty()) {
                return emptyPage(page, page_size);
            }
            qw.in("skill_id", addedSkillIds);
        } else if ("favorites".equals(scope)) {
            List<String> likedSkillIds = userSkillIds(userId, false, true);
            if (likedSkillIds.isEmpty()) {
                return emptyPage(page, page_size);
            }
            qw.in("skill_id", likedSkillIds);
        } else if ("created".equals(scope)) {
            // 我创建的
            qw.eq("owner_uid", userId);
        } else {
            // public / 默认：只看公开未私有
            qw.eq("is_private", 0);
        }

        if (tag != null && !tag.isEmpty() && !"all".equals(tag)) {
            qw.eq("tag", tag);
        }
        if (search != null && !search.isEmpty()) {
            qw.and(w -> w.like("skill_name", search).or().like("description", search));
        }
        qw.orderByDesc("sort_weight").orderByDesc("create_time");

        Page<ZySkill> p = new Page<>(page, page_size);
        Page<ZySkill> result = zySkillMapper.selectPage(p, qw);

        // 批量查用户关联
        Map<String, ZyUserSkill> relMap = loadUserRelations(userId, result.getRecords());

        List<Map<String, Object>> skills = new ArrayList<>();
        for (ZySkill s : result.getRecords()) {
            ZyUserSkill rel = relMap.get(s.getSkillId());
            boolean isAdded = rel != null && rel.getIsAdded() != null && rel.getIsAdded() == 1;
            boolean isLiked = rel != null && rel.getIsLiked() != null && rel.getIsLiked() == 1;
            boolean isOwner = userId != null && userId.equals(s.getOwnerUid());
            skills.add(toSkillMap(s, isAdded, isLiked, isOwner));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skills", skills);
        data.put("total_count", result.getTotal());
        data.put("has_more", result.getCurrent() * result.getSize() < result.getTotal());
        data.put("next_offset", (int) (result.getCurrent() * result.getSize()));
        data.put("page", result.getCurrent());
        data.put("page_size", result.getSize());
        data.put("categories", CATEGORIES);
        return CommonResult.data(data);
    }

    @Operation(summary = "我添加的技能")
    @GetMapping("/added")
    public CommonResult<Map<String, Object>> added() {
        String userId = currentUserId();
        List<String> skillIds = userSkillIds(userId, true, false);
        List<Map<String, Object>> skills = new ArrayList<>();
        if (!skillIds.isEmpty()) {
            QueryWrapper<ZySkill> qw = new QueryWrapper<>();
            qw.eq("status", 1).in("skill_id", skillIds).orderByDesc("sort_weight");
            List<ZySkill> list = zySkillMapper.selectList(qw);
            Map<String, ZyUserSkill> relMap = loadUserRelations(userId, list);
            for (ZySkill s : list) {
                ZyUserSkill rel = relMap.get(s.getSkillId());
                boolean isAdded = rel != null && rel.getIsAdded() != null && rel.getIsAdded() == 1;
                boolean isLiked = rel != null && rel.getIsLiked() != null && rel.getIsLiked() == 1;
                boolean isOwner = userId != null && userId.equals(s.getOwnerUid());
                skills.add(toSkillMap(s, isAdded, isLiked, isOwner));
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skills", skills);
        return CommonResult.data(data);
    }

    // ==================== 文件 ====================

    @Operation(summary = "技能文件列表")
    @GetMapping("/{id}/files")
    public CommonResult<Map<String, Object>> files(@PathVariable String id) {
        ZySkill s = zySkillMapper.selectById(id);
        List<Map<String, Object>> fileList = new ArrayList<>();
        if (s != null && s.getInstruction() != null && !s.getInstruction().isEmpty()) {
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("path", "SKILL.md");
            f.put("kind", "markdown");
            f.put("mime_type", "text/markdown");
            f.put("size", s.getInstruction().length());
            f.put("sha256", "");
            fileList.add(f);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("files", fileList);
        return CommonResult.data(data);
    }

    @Operation(summary = "技能文件内容")
    @GetMapping("/{id}/file")
    public CommonResult<Map<String, Object>> fileContent(
            @PathVariable String id,
            @RequestParam(defaultValue = "SKILL.md") String path) {
        ZySkill s = zySkillMapper.selectById(id);
        String content = s != null ? nz(s.getInstruction()) : "";

        Map<String, Object> fileInfo = new LinkedHashMap<>();
        fileInfo.put("path", "SKILL.md");
        fileInfo.put("kind", "markdown");
        fileInfo.put("mime_type", "text/markdown");
        fileInfo.put("size", content.length());
        fileInfo.put("sha256", "");

        Map<String, Object> fileContent = new LinkedHashMap<>();
        fileContent.put("file", fileInfo);
        fileContent.put("content", content);
        fileContent.put("binary", false);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("file", fileContent);
        return CommonResult.data(data);
    }

    @Operation(summary = "技能整包")
    @GetMapping("/{id}/bundle")
    public CommonResult<Map<String, Object>> bundle(@PathVariable String id) {
        ZySkill s = zySkillMapper.selectById(id);
        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("skill_id", s != null ? s.getSkillId() : id);
        bundle.put("name", s != null ? s.getSkillName() : "");
        bundle.put("description", s != null ? s.getDescription() : "");
        bundle.put("version_id", nz(s != null ? s.getVersionId() : ""));
        bundle.put("version", nz(s != null ? s.getVersion() : "1.0"));
        bundle.put("content_hash", nz(s != null ? s.getContentHash() : ""));

        List<Map<String, Object>> files = new ArrayList<>();
        if (s != null && s.getInstruction() != null && !s.getInstruction().isEmpty()) {
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("path", "SKILL.md");
            f.put("mime_type", "text/markdown");
            f.put("content_base64", "");
            files.add(f);
        }
        bundle.put("files", files);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bundle", bundle);
        return CommonResult.data(data);
    }

    @Operation(summary = "技能文件内搜索")
    @GetMapping("/{id}/search")
    public CommonResult<Map<String, Object>> searchFiles(
            @PathVariable String id,
            @RequestParam("q") String q) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("results", new ArrayList<>());
        return CommonResult.data(data);
    }

    // ==================== 详情 ====================

    @Operation(summary = "技能详情")
    @GetMapping("/{id}")
    public CommonResult<Map<String, Object>> detail(@PathVariable String id) {
        ZySkill s = zySkillMapper.selectById(id);
        if (s == null) {
            return CommonResult.data(null);
        }
        String userId = currentUserId();
        ZyUserSkill rel = findRelation(userId, s.getSkillId());
        boolean isAdded = rel != null && rel.getIsAdded() != null && rel.getIsAdded() == 1;
        boolean isLiked = rel != null && rel.getIsLiked() != null && rel.getIsLiked() == 1;
        boolean isOwner = userId != null && userId.equals(s.getOwnerUid());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skill", toSkillMap(s, isAdded, isLiked, isOwner));
        return CommonResult.data(data);
    }

    // ==================== 添加/移除 ====================

    @Operation(summary = "添加到我的工作台")
    @PostMapping("/{id}/add")
    public CommonResult<Map<String, Object>> add(@PathVariable String id) {
        String userId = currentUserId();
        upsertRelation(userId, id, true, null);
        bumpAddedCount(id, 1);

        ZySkill s = zySkillMapper.selectById(id);
        ZyUserSkill rel = findRelation(userId, id);
        boolean isLiked = rel != null && rel.getIsLiked() != null && rel.getIsLiked() == 1;
        boolean isOwner = userId != null && userId.equals(s.getOwnerUid());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skill", s != null ? toSkillMap(s, true, isLiked, isOwner) : null);
        return CommonResult.data(data);
    }

    @Operation(summary = "从我的工作台移除")
    @DeleteMapping("/{id}/add")
    public CommonResult<Map<String, Object>> remove(@PathVariable String id) {
        String userId = currentUserId();
        upsertRelation(userId, id, false, null);
        bumpAddedCount(id, -1);

        ZySkill s = zySkillMapper.selectById(id);
        ZyUserSkill rel = findRelation(userId, id);
        boolean isLiked = rel != null && rel.getIsLiked() != null && rel.getIsLiked() == 1;
        boolean isOwner = userId != null && userId.equals(s.getOwnerUid());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skill", s != null ? toSkillMap(s, false, isLiked, isOwner) : null);
        return CommonResult.data(data);
    }

    // ==================== 收藏 ====================

    @Operation(summary = "收藏")
    @PostMapping("/{id}/like")
    public CommonResult<Map<String, Object>> like(@PathVariable String id) {
        String userId = currentUserId();
        upsertRelation(userId, id, null, true);
        bumpLikeCount(id, 1);

        ZySkill s = zySkillMapper.selectById(id);
        ZyUserSkill rel = findRelation(userId, id);
        boolean isAdded = rel != null && rel.getIsAdded() != null && rel.getIsAdded() == 1;
        boolean isOwner = userId != null && userId.equals(s.getOwnerUid());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skill", s != null ? toSkillMap(s, isAdded, true, isOwner) : null);
        return CommonResult.data(data);
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{id}/like")
    public CommonResult<Map<String, Object>> unlike(@PathVariable String id) {
        String userId = currentUserId();
        upsertRelation(userId, id, null, false);
        bumpLikeCount(id, -1);

        ZySkill s = zySkillMapper.selectById(id);
        ZyUserSkill rel = findRelation(userId, id);
        boolean isAdded = rel != null && rel.getIsAdded() != null && rel.getIsAdded() == 1;
        boolean isOwner = userId != null && userId.equals(s.getOwnerUid());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skill", s != null ? toSkillMap(s, isAdded, false, isOwner) : null);
        return CommonResult.data(data);
    }

    // ==================== 私有方法 ====================

    private static String currentUserId() {
        try {
            return StpClientUtil.getLoginIdAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private ZyUserSkill findRelation(String userId, String skillId) {
        if (userId == null) return null;
        QueryWrapper<ZyUserSkill> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("skill_id", skillId).last("limit 1");
        return zyUserSkillMapper.selectOne(qw);
    }

    private List<String> userSkillIds(String userId, boolean needAdded, boolean needLiked) {
        if (userId == null) return Collections.emptyList();
        QueryWrapper<ZyUserSkill> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        if (needAdded) qw.eq("is_added", 1);
        if (needLiked) qw.eq("is_liked", 1);
        List<ZyUserSkill> list = zyUserSkillMapper.selectList(qw);
        return list.stream().map(ZyUserSkill::getSkillId).collect(Collectors.toList());
    }

    private Map<String, ZyUserSkill> loadUserRelations(String userId, List<ZySkill> skills) {
        if (userId == null || skills.isEmpty()) return Collections.emptyMap();
        List<String> skillIds = skills.stream().map(ZySkill::getSkillId).collect(Collectors.toList());
        QueryWrapper<ZyUserSkill> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).in("skill_id", skillIds);
        List<ZyUserSkill> rels = zyUserSkillMapper.selectList(qw);
        Map<String, ZyUserSkill> map = new HashMap<>();
        for (ZyUserSkill r : rels) {
            map.put(r.getSkillId(), r);
        }
        return map;
    }

    /**
     * 插入或更新用户-技能关联
     * isAdded/isLiked 传 null 表示不修改该字段
     */
    private void upsertRelation(String userId, String skillId, Boolean isAdded, Boolean isLiked) {
        if (userId == null) return;
        ZyUserSkill existing = findRelation(userId, skillId);
        if (existing == null) {
            ZyUserSkill rel = new ZyUserSkill();
            rel.setId(UUID.randomUUID().toString().replace("-", ""));
            rel.setUserId(userId);
            rel.setSkillId(skillId);
            rel.setIsAdded(isAdded != null && isAdded ? 1 : 0);
            rel.setIsLiked(isLiked != null && isLiked ? 1 : 0);
            zyUserSkillMapper.insert(rel);
        } else {
            UpdateWrapper<ZyUserSkill> uw = new UpdateWrapper<>();
            uw.eq("id", existing.getId());
            if (isAdded != null) uw.set("is_added", isAdded ? 1 : 0);
            if (isLiked != null) uw.set("is_liked", isLiked ? 1 : 0);
            zyUserSkillMapper.update(null, uw);
        }
    }

    private void bumpLikeCount(String skillId, int delta) {
        ZySkill s = zySkillMapper.selectById(skillId);
        if (s == null) return;
        int cur = s.getLikeCount() != null ? s.getLikeCount() : 0;
        int next = Math.max(0, cur + delta);
        UpdateWrapper<ZySkill> uw = new UpdateWrapper<>();
        uw.eq("id", skillId).set("like_count", next);
        zySkillMapper.update(null, uw);
    }

    private void bumpAddedCount(String skillId, int delta) {
        ZySkill s = zySkillMapper.selectById(skillId);
        if (s == null) return;
        int cur = s.getAddedCount() != null ? s.getAddedCount() : 0;
        int next = Math.max(0, cur + delta);
        UpdateWrapper<ZySkill> uw = new UpdateWrapper<>();
        uw.eq("id", skillId).set("added_count", next);
        zySkillMapper.update(null, uw);
    }

    private static CommonResult<Map<String, Object>> emptyPage(int page, int pageSize) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skills", new ArrayList<>());
        data.put("total_count", 0);
        data.put("has_more", false);
        data.put("next_offset", 0);
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("categories", CATEGORIES);
        return CommonResult.data(data);
    }

    private Map<String, Object> toSkillMap(ZySkill s, boolean isAdded, boolean isLiked, boolean isOwner) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("skill_id", s.getSkillId());
        m.put("skill_name", s.getSkillName());
        m.put("description", s.getDescription());
        m.put("instruction", s.getInstruction());
        m.put("version_id", nz(s.getVersionId()));
        m.put("version", nz(s.getVersion()));
        m.put("content_hash", nz(s.getContentHash()));
        m.put("file_count", s.getFileCount() != null ? s.getFileCount() : 0);
        m.put("total_bytes", s.getTotalBytes() != null ? s.getTotalBytes() : 0);
        m.put("source_type", nz(s.getSourceType()));
        m.put("source_url", nz(s.getSourceUrl()));
        m.put("source_ref", "");
        m.put("source_subdir", "");
        m.put("source_commit", "");
        m.put("sync_status", nz(s.getSyncStatus()));
        m.put("sync_error", "");
        m.put("auto_update", s.getAutoUpdate() != null && s.getAutoUpdate() == 1);
        m.put("last_checked_at", s.getLastCheckedAt() != null ? s.getLastCheckedAt() : 0);
        m.put("last_synced_at", s.getLastSyncedAt() != null ? s.getLastSyncedAt() : 0);
        m.put("status", s.getStatus());
        m.put("markdown_url", nz(s.getMarkdownUrl()));
        m.put("create_time", s.getCreateTime() != null ? s.getCreateTime().getTime() : 0);
        m.put("update_time", s.getUpdateTime() != null ? s.getUpdateTime().getTime() : 0);
        m.put("source", s.getSource() != null ? s.getSource() : 3);
        m.put("tag", nz(s.getTag()));
        m.put("sort_weight", s.getSortWeight() != null ? s.getSortWeight() : 0);
        m.put("is_private", s.getIsPrivate() != null && s.getIsPrivate() == 1);
        m.put("like_count", s.getLikeCount() != null ? s.getLikeCount() : 0);
        m.put("is_like", isLiked);
        m.put("owner_uid", nz(s.getOwnerUid()));
        m.put("effective_user", parseJson(s.getEffectiveUser()));
        m.put("original_skill_id", s.getOriginalSkillId());
        m.put("showcase_media", parseJson(s.getShowcaseMedia()));
        m.put("added_count", s.getAddedCount() != null ? s.getAddedCount() : 0);
        m.put("is_test", s.getIsTest() != null && s.getIsTest() == 1);
        m.put("extra_info", nz(s.getExtraInfo()));
        m.put("is_added", isAdded);
        m.put("is_owner", isOwner);
        return m;
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    private static Object parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(json, Object.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
