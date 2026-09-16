## 1. 当前焦点：NewAPI 媒体生成（含 wan2.7-r2v）

### 1.1 架构

```
影策前端 (web)
    │  POST /tasks  { type: image|video, model, prompt, metadata.shotId, ... }
    ▼
ZyApiGenerationTaskController
    ▼
TaskWorkerCoordinator → ImageGenerationTaskProcessor / VideoGenerationTaskProcessor
    ▼
NewApiClient（门面）
    ├─ NewApiMediaAdapter   ← 非百炼域名（用户 NewAPI 网关）
    └─ DashScopeMediaAdapter ← 百炼域名直连
    ▼
NewAPI (baseUrl + apiKey，存 zy_model)
```

### 1.2 NewAPI 官方协议（默认 protocol=`newapi`）

| 动作 | 协议 |
|------|------|
| 创建 | `POST {base}/v1/video/generations`，Body：`model/prompt/image/duration/width/height/metadata` |
| 查询 | `GET {base}/v1/video/generations/{task_id}` |
| 创建响应 | `{ task_id, status: "queued" }` |
| 查询响应 | `{ task_id, status: queued\|in_progress\|completed\|failed, url }` |

参考：https://docs.newapi.pro/zh/docs/api/ai-model/videos/createvideogeneration

### 1.3 wan2.7-r2v 参考生视频（仍走 NewAPI）

**出口 URL 始终是用户配置的 NewAPI**（`base_url` 非百炼域名时进 `NewApiMediaAdapter`）。  
模型 key 含 `wan` 且含 `r2v`/`i2v`/`t2v` 时，**只改 body 为 DashScope media 结构**，不是改打阿里：

```json
{
  "model": "wan2.7-r2v",
  "input": {
    "prompt": "...",
    "media": [
      { "type": "reference_image", "url": "https://...", "reference_voice": "https://....mp3" },
      { "type": "reference_video", "url": "https://....mp4" }
    ]
  },
  "parameters": { "resolution": "720P", "ratio": "16:9", "duration": 5 }
}
```

约束（阿里云 wan2.7-r2v）：
- 至少 1 个 reference_image 或 reference_video，二者合计 ≤ 5
- first_frame 最多 1 张
- duration：含参考视频时 2–10s；纯图 2–15s
- resolution：720P / 1080P
- `reference_voice` 可挂在 image/video 上（WAV/MP3，1–10s）

任务 input 字段：
- `referenceImages` / `referenceVideos`（或 `images`/`videos`）
- `referenceVoice`（可选，音色）
- `config.duration` / `config.resolution` / `config.aspectRatio`

### 1.4 分镜图 / 镜头视频产物

| 类型 | 处理器 | 回填 |
|------|--------|------|
| image | ImageGenerationTaskProcessor | `metadata.shotId` → `zy_shot_artifact`（默认 `storyboard`） |
| video | VideoGenerationTaskProcessor + TaskPollingService | 同上，默认 `video` |

### 1.5 模型配置（`zy_model`）

| 字段 | 用途 |
|------|------|
| `model_key` | 如 `wan2.7-r2v` |
| `protocol` | 建议 `newapi`（官方统一）；wan 系会自动切 media 协议 |
| `base_url` | **用户的 NewAPI 地址**（不要填 dashscope，除非直连百炼） |
| `api_key` | NewAPI 的 sk-... |

示例见 `zy_model_init.sql` 中 `model_wan27_r2v`。

### 1.6 相关接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/tasks` | 创建生成任务 |
| GET | `/tasks/{id}` | 任务详情 |
| POST | `/tasks/{id}/retry` | 重试 |
| POST | `/tasks/{id}/cancel` | 取消 |
| POST | `/tasks/{id}/query-provider` | 主动查上游 |
| GET | `/api/model-catalog` | 模型目录 |

---

## 2. 已完成（底座）

- C 端登录（Sa-Token，`/auth/c/*`）
- 画布项目 CRUD / 分享
- 短剧项目、章节、分镜（含版本）
- 角色卡、资产库 / 文件夹 / 素材候选
- 任务队列 Worker（timer + coordinator）
- 分镜脚本 LLM 任务 `agent_storyboard_rows`
- 默认工作流步骤：`storyboard` / `previz` / `video`
- **NewAPI 官方 video-generations 创建/查询协议对齐**
- **wan2.7-r2v media 协议（参考图/参考视频/reference_voice）**
- 状态解析兼容 `url` 顶层字段与 DashScope `output.video_url`
- 嵌套 error 对象解析

---

## 3. 进行中 / 待办

1. 用真实 NewAPI key 跑通：分镜图 → 镜头视频（wan2.7-r2v）→ 产物回填
2. 前端分镜区补 referenceVideos / referenceVoice 提交字段（若 UI 已有多参考则只对齐 input JSON）
3. SSE / relay 全面 token header
4. 资源上传 / Range 下载
5. 文本模型与 AI 中转完整接回

---

## 4. 关键约定（不要改坏）

- 模块：`snowy-plugin-canvas`；Controller 在 `vip.xiaonuo.canvas.zyapi.*`
- 鉴权：C 端 `StpClientUtil`
- 业务层只依赖 `NewApiClient`，协议差异在 `MediaModelAdapter`
- 模型 key 含 wan+r2v/i2v/t2v → 自动 DashScope media 体
- 分镜产物必须带 `shotId` 才能回填 `zy_shot_artifact`
- 参考素材 URL 须公网可达（NewAPI/百炼服务器要能下载）

---

## 5. 启动提示

```bash
# 后端
$env:JAVA_HOME='D:\jdk17'
mvn -pl snowy-web-app -am spring-boot:run

# 前端
# open-ai-canvas/web/.env.local: VITE_CANVAS_BACKEND_URL=http://localhost:82
```

`zy_model` 里把目标视频模型的 `base_url`/`api_key` 配成你的 NewAPI 网关；`model_key` 与 NewAPI 渠道上的模型名一致（如 `wan2.7-r2v`）。
