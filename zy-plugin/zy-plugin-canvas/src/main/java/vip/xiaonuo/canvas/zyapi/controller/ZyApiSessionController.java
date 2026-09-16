package vip.xiaonuo.canvas.zyapi.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.session.entity.ZySession;
import vip.xiaonuo.canvas.modular.session.mapper.ZySessionMapper;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会话Controller
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Tag(name = "会话")
@RestController
@RequestMapping("/api/sessions")
public class ZyApiSessionController {

    @Resource
    private ZySessionMapper zySessionMapper;

    /**
     * 创建会话
     */
    @Operation(summary = "创建会话")
    @PostMapping
    public CommonResult<Map<String, Object>> createSession(@RequestBody Map<String, Object> input) {
        String userId = StpClientUtil.getLoginIdAsString();

        ZySession session = new ZySession();
        session.setId(IdUtil.fastSimpleUUID());
        session.setUserId(userId);
        session.setProjectId((String) input.get("projectId"));
        session.setPrompt((String) input.get("prompt"));
        session.setStatus("active");

        if (input.get("canvasSnapshot") != null) {
            session.setCanvasSnapshotJson(JSONUtil.toJsonStr(input.get("canvasSnapshot")));
        }

        zySessionMapper.insert(session);

        return CommonResult.data(toSession(session));
    }

    /**
     * 获取会话详情
     */
    @Operation(summary = "获取会话详情")
    @GetMapping("/{id}")
    public CommonResult<Map<String, Object>> getSession(@PathVariable String id) {
        String userId = StpClientUtil.getLoginIdAsString();

        ZySession session = zySessionMapper.selectOne(
                new QueryWrapper<ZySession>()
                        .eq("id", id)
                        .eq("user_id", userId)
        );

        if (session == null) {
            return CommonResult.error("会话不存在");
        }

        return CommonResult.data(toSession(session));
    }

    /**
     * 列出会话
     */
    @Operation(summary = "列出会话")
    @GetMapping
    public CommonResult<List<Map<String, Object>>> listSessions() {
        String userId = StpClientUtil.getLoginIdAsString();

        List<ZySession> sessions = zySessionMapper.selectList(
                new QueryWrapper<ZySession>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time")
                        .last("LIMIT 100")
        );

        List<Map<String, Object>> result = sessions.stream()
                .map(this::toSession)
                .collect(Collectors.toList());

        return CommonResult.data(result);
    }

    private Map<String, Object> toSession(ZySession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", session.getId());
        result.put("projectId", session.getProjectId());
        result.put("prompt", session.getPrompt());
        result.put("status", session.getStatus());
        result.put("createdAt", session.getCreateTime());

        if (session.getCanvasSnapshotJson() != null) {
            result.put("canvasSnapshot", JSONUtil.parseObj(session.getCanvasSnapshotJson()));
        }

        return result;
    }
}
