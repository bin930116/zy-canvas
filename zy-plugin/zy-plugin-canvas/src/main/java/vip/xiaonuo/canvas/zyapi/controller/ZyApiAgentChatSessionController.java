package vip.xiaonuo.canvas.zyapi.controller;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.agentchat.service.ZyAgentChatService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.List;
import java.util.Map;

/**
 * Agent对话会话接口
 *
 * @author xuyuxiang
 * @date 2026/9/14
 **/
@Tag(name = "Agent对话会话")
@RestController
@RequestMapping("/api/agent")
public class ZyApiAgentChatSessionController {

    @Resource
    private ZyAgentChatService zyAgentChatService;

    /**
     * 获取画布的Agent会话列表
     */
    @Operation(summary = "获取画布的Agent会话列表")
    @GetMapping("/sessions")
    public CommonResult<List<Map<String, Object>>> listSessions(@RequestParam("canvasId") String canvasId) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyAgentChatService.listByCanvas(userId, canvasId));
    }

    /**
     * 获取单个会话详情
     */
    @Operation(summary = "获取单个会话详情")
    @GetMapping("/sessions/{id}")
    public CommonResult<Map<String, Object>> getSession(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        Map<String, Object> session = zyAgentChatService.getDetail(userId, id);
        if (session == null) {
            return CommonResult.error("会话不存在");
        }
        return CommonResult.data(session);
    }

    /**
     * 保存或更新会话
     */
    @Operation(summary = "保存或更新会话")
    @PutMapping("/sessions/{id}")
    public CommonResult<Map<String, Object>> upsertSession(@PathVariable String id,
                                                           @RequestBody Map<String, Object> body) {
        String userId = StpClientUtil.getLoginIdAsString();
        String canvasId = body.get("canvasId") != null ? String.valueOf(body.get("canvasId")) : "";
        if (StrUtil.isBlank(canvasId)) {
            return CommonResult.error("缺少画布ID");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> session = body.get("session") instanceof Map ? (Map<String, Object>) body.get("session") : body;
        return CommonResult.data(zyAgentChatService.upsert(userId, canvasId, id, session));
    }

    /**
     * 删除会话
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{id}")
    public CommonResult<Map<String, Object>> deleteSession(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        zyAgentChatService.delete(userId, id);
        return CommonResult.data(Map.of("id", id));
    }
}
