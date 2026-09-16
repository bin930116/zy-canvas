package vip.xiaonuo.canvas.zyapi.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.zyapi.service.ZyApiModelService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 画布网站 Agent 同步接口
 * <p>
 * 绕过任务队列，前端直接 POST → 后端同步调 LLM（SSE 流式）→ 返回 text/toolCalls。
 * 工具由前端浏览器执行后进入下一轮。
 *
 * @author xuyuxiang
 * @date 2026/9/14
 **/
@Tag(name = "画布Agent同步接口")
@RestController
@RequestMapping("/api/agent")
public class ZyApiAgentChatController {

    private static final Logger log = LoggerFactory.getLogger(ZyApiAgentChatController.class);

    @Resource
    private ZyApiModelService zyModelService;

    /**
     * 同步 Agent 聊天：POST body → SSE 流式返回
     * body: { model, messages[], tools[], tool_choice, systemPrompt }
     */
    @Operation(summary = "画布Agent同步聊天")
    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody Map<String, Object> body,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        // CORS
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", origin != null ? origin : "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(300000L);

        String userId = StpClientUtil.getLoginIdAsString();
        String modelKey = body.get("model") != null ? String.valueOf(body.get("model")).trim() : "";
        if (StrUtil.isEmpty(modelKey)) {
            emitter.completeWithError(new RuntimeException("缺少模型标识"));
            return emitter;
        }

        // 获取模型配置
        ZyModel model = zyModelService.getByModelKey(modelKey);
        if (model == null || StrUtil.isEmpty(model.getBaseUrl()) || StrUtil.isEmpty(model.getApiKey())) {
            emitter.completeWithError(new RuntimeException("模型配置不完整: " + modelKey));
            return emitter;
        }

        // 异步执行，避免阻塞 Servlet 线程
        new Thread(() -> {
            try {
                String url = buildChatCompletionsUrl(model.getBaseUrl());
                JSONObject requestBody = new JSONObject();
                requestBody.set("model", modelKey);

                // messages
                JSONArray messages = new JSONArray();
                String systemPrompt = body.get("systemPrompt") != null ? String.valueOf(body.get("systemPrompt")) : "";
                messages.add(new JSONObject().set("role", "system").set("content",
                        StrUtil.blankToDefault(systemPrompt, "You are a helpful assistant.")));
                Object rawMessages = body.get("messages");
                if (rawMessages instanceof JSONArray arr) {
                    messages.addAll(arr);
                } else if (rawMessages instanceof List<?> list) {
                    for (Object item : list) {
                        messages.add(JSONUtil.parseObj(JSONUtil.toJsonStr(item)));
                    }
                }
                requestBody.set("messages", messages);

                // tools
                Object tools = body.get("tools");
                if (tools != null) {
                    requestBody.set("tools", tools);
                }
                // tool_choice：对象格式转 required；required 失败时回退 auto
                Object toolChoice = body.get("tool_choice");
                String effectiveToolChoice = null;
                if (toolChoice != null) {
                    String choice = String.valueOf(toolChoice);
                    if (choice.startsWith("{")) {
                        effectiveToolChoice = "required";
                    } else {
                        effectiveToolChoice = choice;
                    }
                }
                if (effectiveToolChoice != null) {
                    requestBody.set("tool_choice", effectiveToolChoice);
                }
                requestBody.set("parallel_tool_calls", false);
                requestBody.set("temperature", 0.7);
                requestBody.set("max_tokens", 16000);
                requestBody.set("enable_thinking", false);
                requestBody.set("stream", true);

                log.info("Agent同步调用: model={}, url={}, toolChoice={}", modelKey, url, effectiveToolChoice);

                HttpResponse llmResponse = HttpRequest.post(url)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + model.getApiKey())
                        .header("Accept", "text/event-stream")
                        .body(requestBody.toString())
                        .timeout(300000)
                        .execute();

                // required 不被支持时回退 auto 重试
                if (!llmResponse.isOk() && "required".equals(effectiveToolChoice)) {
                    String errBody = llmResponse.body();
                    if (errBody != null && errBody.contains("tool_choice")) {
                        log.warn("tool_choice=required 不被支持，回退 auto 重试: {}", StrUtil.maxLength(errBody, 200));
                        requestBody.set("tool_choice", "auto");
                        llmResponse = HttpRequest.post(url)
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + model.getApiKey())
                                .header("Accept", "text/event-stream")
                                .body(requestBody.toString())
                                .timeout(300000)
                                .execute();
                    }
                }

                if (!llmResponse.isOk()) {
                    String errBody = llmResponse.body();
                    log.error("Agent LLM 调用失败: status={}, body={}", llmResponse.getStatus(), errBody);
                    emitter.send(SseEmitter.event().name("error").data(Map.of("message",
                            "LLM API 调用失败: " + llmResponse.getStatus() + " " + StrUtil.maxLength(errBody, 400))));
                    emitter.complete();
                    return;
                }

                // 解析 SSE 流
                StringBuilder contentBuilder = new StringBuilder();
                Map<Integer, Map<String, String>> toolCallParts = new LinkedHashMap<>();

                try (InputStream is = llmResponse.bodyStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (StrUtil.isEmpty(line) || !line.startsWith("data:")) continue;
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) break;
                        try {
                            JSONObject chunk = JSONUtil.parseObj(data);
                            JSONArray choices = chunk.getJSONArray("choices");
                            if (choices == null || choices.isEmpty()) continue;
                            JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                            if (delta == null) continue;

                            // content / reasoning_content / reasoning
                            String contentDelta = delta.getStr("content");
                            if (StrUtil.isEmpty(contentDelta)) contentDelta = delta.getStr("reasoning_content");
                            if (StrUtil.isEmpty(contentDelta)) contentDelta = delta.getStr("reasoning");
                            if (StrUtil.isNotEmpty(contentDelta)) {
                                contentBuilder.append(contentDelta);
                                emitter.send(SseEmitter.event()
                                        .name("delta")
                                        .data(Map.of("content", contentDelta)));
                            }

                            JSONArray toolCallsDelta = delta.getJSONArray("tool_calls");
                            if (toolCallsDelta != null) {
                                for (int i = 0; i < toolCallsDelta.size(); i++) {
                                    JSONObject tc = toolCallsDelta.getJSONObject(i);
                                    if (tc == null) continue;
                                    Integer index = tc.getInt("index");
                                    int idx = index != null ? index : i;
                                    Map<String, String> part = toolCallParts.computeIfAbsent(idx, k -> new LinkedHashMap<>());
                                    if (StrUtil.isNotEmpty(tc.getStr("id"))) part.put("id", tc.getStr("id"));
                                    JSONObject fn = tc.getJSONObject("function");
                                    if (fn != null) {
                                        if (StrUtil.isNotEmpty(fn.getStr("name"))) part.put("name", fn.getStr("name"));
                                        String argsDelta = fn.getStr("arguments");
                                        if (StrUtil.isNotEmpty(argsDelta)) {
                                            part.put("arguments", part.getOrDefault("arguments", "") + argsDelta);
                                        }
                                    }
                                }
                            }
                        } catch (Exception parseEx) {
                            log.debug("跳过无法解析的 SSE chunk: {}", StrUtil.maxLength(data, 120));
                        }
                    }
                }

                // 组装 toolCalls
                List<Map<String, Object>> toolCalls = new ArrayList<>();
                for (Map.Entry<Integer, Map<String, String>> entry : toolCallParts.entrySet()) {
                    Map<String, String> part = entry.getValue();
                    Map<String, Object> call = new LinkedHashMap<>();
                    call.put("id", part.getOrDefault("id", "call_" + entry.getKey()));
                    call.put("type", "function");
                    Map<String, Object> function = new LinkedHashMap<>();
                    function.put("name", part.get("name"));
                    function.put("arguments", part.getOrDefault("arguments", "{}"));
                    call.put("function", function);
                    toolCalls.add(call);
                }

                // 发送完成事件
                Map<String, Object> done = new LinkedHashMap<>();
                done.put("text", contentBuilder.toString());
                done.put("toolCalls", toolCalls);
                emitter.send(SseEmitter.event().name("done").data(JSONUtil.toJsonStr(done)));
                // 等待事件发送完成再关闭连接，避免 done 事件丢失
                Thread.sleep(100);
                emitter.complete();

                log.info("Agent同步完成: model={}, textLen={}, toolCalls={}", modelKey,
                        contentBuilder.length(), toolCalls.size());

            } catch (Exception e) {
                log.error("Agent同步调用异常: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(Map.of("message",
                            e.getMessage() != null ? e.getMessage() : "Agent调用失败")));
                    emitter.complete();
                } catch (Exception ignore) {
                    emitter.completeWithError(e);
                }
            }
        }, "agent-chat-" + userId).start();

        return emitter;
    }

    private String buildChatCompletionsUrl(String baseUrl) {
        String url = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        if (url.endsWith("/v1/")) {
            return url + "chat/completions";
        }
        return url + "v1/chat/completions";
    }
}
