package vip.xiaonuo.canvas.modular.agentchat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.canvas.modular.agentchat.entity.ZyAgentChat;

import java.util.List;
import java.util.Map;

/**
 * Agent对话会话Service
 *
 * @author xuyuxiang
 * @date 2026/9/14
 **/
public interface ZyAgentChatService extends IService<ZyAgentChat> {

    /**
     * 获取用户在指定画布的会话列表
     */
    List<Map<String, Object>> listByCanvas(String userId, String canvasId);

    /**
     * 保存或更新会话
     */
    Map<String, Object> upsert(String userId, String canvasId, String sessionId, Map<String, Object> session);

    /**
     * 删除会话
     */
    void delete(String userId, String sessionId);

    /**
     * 获取单个会话详情
     */
    Map<String, Object> getDetail(String userId, String sessionId);
}
