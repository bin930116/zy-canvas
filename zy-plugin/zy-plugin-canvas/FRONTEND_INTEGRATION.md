# 前端对接Java后端指南

## 一、Java后端API接口清单

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/model-catalog` | GET | 获取模型目录 |
| `/api/model-catalog/available` | POST | 获取可用模型 |
| `/api/tasks` | POST | 创建任务 |
| `/api/tasks` | GET | 任务列表 |
| `/api/tasks/{id}` | GET | 任务详情 |
| `/api/tasks/{id}/cancel` | POST | 取消任务 |
| `/api/tasks/{id}/retry` | POST | 重试任务 |
| `/api/tasks/worker/status` | GET | Worker状态 |
| `/api/sessions` | POST | 创建会话 |
| `/api/sessions` | GET | 会话列表 |
| `/api/sessions/{id}` | GET | 会话详情 |
| `/files` | POST | 上传图片，返回可访问URL（仅允许 jpg/jpeg/png/gif/bmp/webp） |
| `/api/models` | GET | 模型列表（旧接口） |
| `/api/models/defaults` | GET | 默认模型（旧接口） |

## 二、前端需要修改的文件

### 1. 请求配置 (`web/src/services/api/request.ts`)

修改 `baseURL` 指向Java后端：

```typescript
// 修改前
const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

// 修改后
const baseURL = 'http://localhost:82/api';  // Java后端地址
```

认证Token配置：

```typescript
// 前端使用 Sa-Token，header名称是 "token"
apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem("snowy_token");
    if (token) {
        config.headers["token"] = token;  // Java后端使用相同header
    }
    return config;
});
```

### 2. 模型API (`web/src/services/api/logical-models.ts`)

修改模型API路径：

```typescript
// 获取模型目录
export async function getModelCatalog() {
    const response = await apiClient.get('/model-catalog');
    return response.data.data;  // Java返回格式 {code, data, msg}
}

// 获取可用模型
export async function getAvailableModels(intent: ModelRequestIntent) {
    const response = await apiClient.post('/model-catalog/available', intent);
    return response.data.data;
}
```

### 3. 任务API (`web/src/services/api/task-center.ts`)

确保任务API路径一致：

```typescript
// 创建任务
export async function createGenerationTask(input: CreateTaskInput) {
    const response = await apiClient.post('/tasks', input);
    return response.data.data;  // 直接返回任务对象
}

// 查询任务
export async function queryGenerationTask(id: string) {
    const response = await apiClient.get(`/tasks/${id}`);
    return response.data.data;
}

// 取消任务
export async function cancelGenerationTask(id: string) {
    const response = await apiClient.post(`/tasks/${id}/cancel`);
    return response.data.data;
}
```

## 三、响应格式说明

Java后端返回格式：

```json
{
    "code": 200,
    "data": { ... },
    "msg": "success"
}
```

前端需要解析 `response.data.data` 获取实际数据。

## 四、任务结果格式

视频结果：
```json
{
    "mode": "video",
    "video": {
        "dataUrl": "/api/assets/{assetId}/file",
        "resourceId": "{assetId}",
        "mimeType": "video/mp4"
    }
}
```

图片结果：
```json
{
    "mode": "image",
    "images": [{
        "dataUrl": "/api/assets/{assetId}/file",
        "resourceId": "{assetId}",
        "mimeType": "image/png"
    }]
}
```

## 五、数据库初始化

在数据库中执行以下SQL脚本：

1. [zy_model_init.sql](snowy-plugin/snowy-plugin-canvas/src/main/resources/zy_model_init.sql) - 模型配置表
2. [zy_session_init.sql](snowy-plugin/snowy-plugin-canvas/src/main/resources/zy_session_init.sql) - 会话表

## 六、启动顺序

1. 启动 Redis
2. 启动 PostgreSQL 数据库
3. 执行数据库初始化脚本
4. 启动 Java 后端（端口 82）
5. 修改前端配置
6. 启动前端开发服务器

## 七、测试验证

```bash
# 测试模型目录接口
curl http://localhost:82/api/model-catalog

# 测试任务创建
curl -X POST http://localhost:82/api/tasks \
  -H "Content-Type: application/json" \
  -H "token: {your_token}" \
  -d '{"type":"video","prompt":"测试","model":"kling-v1"}'
```

## 八、注意事项

1. **认证Token**：前端使用 Sa-Token，需要确保登录后获取 token 存储到 localStorage
2. **跨域配置**：Java后端已配置CORS，允许前端跨域访问
3. **文件上传**：`/files` 接口走 C 端鉴权（`/files/**` 已加入放行路径），内部调用 `DevFileApi.uploadDynamicReturnUrl`（动态上传文件返回url，使用系统配置的默认文件引擎），返回 `{ id, filename, storageKey, mimeType, bytes, url, width?, height? }`；`url` 为带签名的公开下载地址（`/dev/file/download?id=&sign=`，无需登录，可直接用于 `<img>`）
4. **Worker并发**：默认50并发，可在 `application.properties` 中调整

### 上传图片接口示例

```bash
curl -X POST http://localhost:82/files \
  -H "token: {c_token}" \
  -F "file=@/path/to/image.png" \
  -F "width=1024" \
  -F "height=768"
```

返回：

```json
{
  "code": 200,
  "data": {
    "id": "1377109572375810050",
    "filename": "image.png",
    "storageKey": "devfile:1377109572375810050",
    "mimeType": "image/png",
    "bytes": 12345,
    "url": "http://localhost:83/api/dev/file/download?id=1377109572375810050&sign=xxx"
  },
  "msg": "success"
}
```

> 注意：`url` 的域名来自 `snowy.config.common.backend-url` 配置（当前为 `http://localhost:83/api`），请确保该地址可被前端访问，否则图片无法加载。