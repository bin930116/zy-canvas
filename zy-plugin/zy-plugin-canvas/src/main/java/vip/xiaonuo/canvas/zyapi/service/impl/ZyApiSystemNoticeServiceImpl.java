package vip.xiaonuo.canvas.zyapi.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.biz.api.BizNoticeApi;
import vip.xiaonuo.canvas.zyapi.service.ZyApiSystemNoticeService;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.dev.api.DevMessageApi;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * C端系统公告/系统消息 Service实现
 * 公告来自 B 端 BizNotice；消息来自站内信 DevMessage。
 *
 * @author xuyuxiang
 * @date 2026/9/10
 **/
@Service
public class ZyApiSystemNoticeServiceImpl implements ZyApiSystemNoticeService {

    @Resource
    private BizNoticeApi bizNoticeApi;

    @Resource
    private DevMessageApi devMessageApi;

    @Override
    public Map<String, Object> announcementFeed(String userId) {
        List<Map<String, Object>> announcements = new ArrayList<>();
        for (JSONObject notice : bizNoticeApi.listEnabledNotices(50)) {
            announcements.add(toAnnouncement(notice));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("announcements", announcements);
        // 公告暂无个人已读表，unreadCount 置 0；消息走 /announcements/messages
        result.put("unreadCount", 0);
        return result;
    }

    @Override
    public Map<String, Object> getAnnouncement(String userId, String id) {
        if (StrUtil.isEmpty(id)) {
            throw new CommonException("公告ID不能为空");
        }
        JSONObject notice = bizNoticeApi.getNoticeById(id);
        if (notice == null) {
            throw new CommonException("公告不存在或已关闭");
        }
        return toAnnouncement(notice);
    }

    @Override
    public Map<String, Object> messageFeed(String userId, Integer limit) {
        int size = ObjectUtil.isNotEmpty(limit) && limit > 0 ? Math.min(limit, 50) : 20;
        List<Map<String, Object>> messages = new ArrayList<>();
        // 未读优先：站内信 list 默认返回未读；分页补全留给后续
        List<JSONObject> list = devMessageApi.list(List.of(userId), size);
        if (list != null) {
            for (JSONObject item : list) {
                Map<String, Object> message = new LinkedHashMap<>();
                message.put("id", item.getStr("id"));
                message.put("title", item.getStr("subject"));
                message.put("content", item.getStr("content"));
                String category = item.getStr("category");
                if (StrUtil.isEmpty(category)) {
                    category = "system";
                }
                message.put("category", category);
                message.put("createdAt", formatNoticeTime(item.get("createTime") != null ? item.get("createTime") : item.get("createTime")));
                message.put("read", isMessageRead(item));
                messages.add(message);
            }
        }
        // 已读列表：任务完成等后续走 category=task；先拉一份近期已读便于前端展示
        if (messages.size() < size) {
            try {
                Page<JSONObject> page = devMessageApi.page(List.of(userId), null);
                if (page != null && page.getRecords() != null) {
                    for (JSONObject item : page.getRecords()) {
                        String id = item.getStr("id");
                        if (id != null && messages.stream().anyMatch(m -> id.equals(m.get("id")))) {
                            continue;
                        }
                        Map<String, Object> message = new LinkedHashMap<>();
                        message.put("id", id);
                        message.put("title", item.getStr("subject"));
                        message.put("content", item.getStr("content"));
                        String category = item.getStr("category");
                        message.put("category", StrUtil.isEmpty(category) ? "system" : category);
                        message.put("createdAt", formatNoticeTime(item.get("createTime")));
                        message.put("read", isMessageRead(item));
                        messages.add(message);
                        if (messages.size() >= size) {
                            break;
                        }
                    }
                }
            } catch (Exception ignore) {
                // 仅未读列表也可用
            }
        }
        Long unread = devMessageApi.unreadCount(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("messages", messages);
        result.put("unreadCount", unread == null ? 0 : unread);
        return result;
    }

    private boolean isMessageRead(JSONObject item) {
        Object read = item.get("read");
        if (read instanceof Boolean bool) {
            return bool;
        }
        return "true".equalsIgnoreCase(String.valueOf(read));
    }

    /**
     * BizNotice → 影策 SystemAnnouncement 契约
     */
    private Map<String, Object> toAnnouncement(JSONObject notice) {
        Map<String, Object> item = new LinkedHashMap<>();
        String content = StrUtil.blankToDefault(notice.getStr("content"), notice.getStr("digest"));
        String publishedAt = formatNoticeTime(notice.get("createTime"));
        String updatedAt = formatNoticeTime(notice.get("updateTime"));
        item.put("id", notice.getStr("id"));
        item.put("title", StrUtil.blankToDefault(notice.getStr("title"), "系统通知"));
        item.put("content", content);
        item.put("imageResourceId", "");
        item.put("imageUrl", notice.getStr("image"));
        item.put("level", mapNoticeLevel(notice.getStr("type")));
        item.put("pinned", false);
        item.put("status", "active");
        item.put("createdBy", notice.getStr("createUser"));
        item.put("publishedAt", publishedAt);
        item.put("createdAt", publishedAt);
        item.put("updatedAt", StrUtil.isNotEmpty(updatedAt) ? updatedAt : publishedAt);
        return item;
    }

    private String formatNoticeTime(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Date date) {
            return DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
        }
        String text = String.valueOf(value);
        if (StrUtil.isBlank(text) || "null".equals(text)) {
            return "";
        }
        try {
            if (text.matches("\\d{10,}")) {
                return DateUtil.format(new Date(Long.parseLong(text)), "yyyy-MM-dd HH:mm:ss");
            }
            return DateUtil.format(DateUtil.parse(text), "yyyy-MM-dd HH:mm:ss");
        } catch (Exception ignore) {
            return text;
        }
    }

    private String mapNoticeLevel(String type) {
        // Snowy 公告 type 常见：通知/公告；统一映射为影策 level
        if (StrUtil.isEmpty(type)) {
            return "info";
        }
        String lower = type.toLowerCase();
        if (lower.contains("warn") || type.contains("警告") || type.contains("紧急")) {
            return "warning";
        }
        if (lower.contains("success") || type.contains("成功")) {
            return "success";
        }
        if (lower.contains("critical") || type.contains("严重")) {
            return "critical";
        }
        return "info";
    }
}
