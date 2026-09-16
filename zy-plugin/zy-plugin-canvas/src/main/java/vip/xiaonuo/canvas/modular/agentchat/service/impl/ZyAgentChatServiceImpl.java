package vip.xiaonuo.canvas.modular.agentchat.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.agentchat.entity.ZyAgentChat;
import vip.xiaonuo.canvas.modular.agentchat.mapper.ZyAgentChatMapper;
import vip.xiaonuo.canvas.modular.agentchat.service.ZyAgentChatService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent对话会话Service实现
 *
 * @author xuyuxiang
 * @date 2026/9/14
 **/
@Service
public class ZyAgentChatServiceImpl extends ServiceImpl<ZyAgentChatMapper, ZyAgentChat> implements ZyAgentChatService {

    @Override
    public List<Map<String, Object>> listByCanvas(String userId, String canvasId) {
        List<ZyAgentChat> list = this.list(new QueryWrapper<ZyAgentChat>()
                .eq("user_id", userId)
                .eq("canvas_id", canvasId)
                .eq("status", "active")
                .orderByDesc("update_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ZyAgentChat chat : list) {
            result.add(toMap(chat));
        }
        return result;
    }

    @Override
    public Map<String, Object> upsert(String userId, String canvasId, String sessionId, Map<String, Object> session) {
        String title = session.get("title") != null ? String.valueOf(session.get("title")) : "";
        Object messages = session.get("messages");
        String messagesJson = messages != null ? JSONUtil.toJsonStr(messages) : "[]";

        ZyAgentChat exist = this.getOne(new QueryWrapper<ZyAgentChat>()
                .eq("user_id", userId)
                .eq("id", sessionId));

        if (ObjectUtil.isEmpty(exist)) {
            ZyAgentChat entity = new ZyAgentChat();
            entity.setId(sessionId);
            entity.setUserId(userId);
            entity.setCanvasId(canvasId);
            entity.setTitle(StrUtil.blankToDefault(title, "新对话"));
            entity.setMessagesJson(messagesJson);
            entity.setStatus("active");
            this.save(entity);
            return toMap(entity);
        }

        exist.setCanvasId(canvasId);
        if (StrUtil.isNotBlank(title)) {
            exist.setTitle(title);
        }
        exist.setMessagesJson(messagesJson);
        this.updateById(exist);
        return toMap(exist);
    }

    @Override
    public void delete(String userId, String sessionId) {
        this.remove(new QueryWrapper<ZyAgentChat>()
                .eq("user_id", userId)
                .eq("id", sessionId));
    }

    @Override
    public Map<String, Object> getDetail(String userId, String sessionId) {
        ZyAgentChat chat = this.getOne(new QueryWrapper<ZyAgentChat>()
                .eq("user_id", userId)
                .eq("id", sessionId));
        if (ObjectUtil.isEmpty(chat)) {
            return null;
        }
        return toMap(chat);
    }

    private Map<String, Object> toMap(ZyAgentChat chat) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", chat.getId());
        result.put("canvasId", chat.getCanvasId());
        result.put("title", chat.getTitle());
        result.put("messages", StrUtil.isNotBlank(chat.getMessagesJson()) ? JSONUtil.parseArray(chat.getMessagesJson()) : new JSONArray());
        result.put("status", chat.getStatus());
        result.put("createdAt", chat.getCreateTime() != null ? DateUtil.formatDateTime(chat.getCreateTime()) : null);
        result.put("updatedAt", chat.getUpdateTime() != null ? DateUtil.formatDateTime(chat.getUpdateTime()) : null);
        return result;
    }
}
