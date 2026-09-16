# 任务Worker配置指南

## 概述

任务Worker采用数据库轮询+租约机制，支持中型规模并发处理（50-100并发任务）。

## 核心配置

在 `application.properties` 中配置：

```properties
# Worker并发数（中型应用推荐50-100）
task.worker.concurrency=50

# 任务租约超时时间（秒）- 长任务需调大
task.worker.lease-timeout=120

# 任务轮询间隔（毫秒）
task.worker.poll-interval=2000

# 任务最大重试次数
task.worker.max-retries=3
```

## 配置说明

### 1. 并发数 (concurrency)

根据业务规模调整：

| 规模 | 并发数 | 任务量/小时 | 说明 |
|------|--------|------------|------|
| 小型 | 10-20 | < 50 | 个人项目、测试环境 |
| 中型 | 50-100 | 50-500 | 团队协作、生产环境 |
| 大型 | 100-300 | > 500 | 高并发、多租户 |

**注意事项**：
- 需确保数据库连接池 >= 并发数（当前配置80，足够）
- 需确保上游API支持相应并发
- Redis连接池也需相应调整

### 2. 租约超时 (lease-timeout)

根据任务类型调整：

| 任务类型 | 推荐值 | 说明 |
|---------|--------|------|
| 图片生成 | 60-90秒 | 短任务 |
| 视频生成 | 120-300秒 | 中长任务 |
| AI处理 | 180-600秒 | 长任务 |

租约机制确保任务不会因Worker崩溃而永久卡住。

### 3. 轮询间隔 (poll-interval)

| 场景 | 推荐值 | 说明 |
|------|--------|------|
| 高实时性 | 500-1000ms | 即时响应 |
| 标准 | 2000ms | 平衡性能和响应 |
| 低负载 | 3000-5000ms | 节省资源 |

## 监控接口

### 查看Worker状态

```bash
GET /api/tasks/worker/status
```

响应示例：
```json
{
  "workerId": "abc123",
  "maxConcurrency": 50,
  "currentConcurrency": 15,
  "activeTasks": 15,
  "queuedCount": 5,
  "runningCount": 15,
  "succeededCount": 1200,
  "failedCount": 3
}
```

## 架构特点

### 1. 数据库轮询
- 每2秒轮询一次任务队列
- 查询status=queued的任务，按创建时间排序

### 2. 租约机制
- 领取任务时设置租约过期时间
- 任务处理期间每30秒自动续约
- 租约过期后任务自动重新入队

### 3. 并发控制
- Redis原子计数器控制全局并发
- 分布式锁防止多Worker竞争同一任务

### 4. 重试机制
- 任务失败后自动重试
- 可配置最大重试次数
- 重试次数用完后标记失败

## 性能调优

### 数据库连接池

确保连接池足够：
```properties
spring.datasource.dynamic.druid.initial-size=10
spring.datasource.dynamic.druid.max-active=80
spring.datasource.dynamic.druid.min-idle=10
```

### Redis配置

确保Redis连接充足：
```properties
spring.data.redis.timeout=10s
```

### 索引优化

任务表已有以下索引：
- `idx_status` - 状态索引
- `idx_created_at` - 创建时间索引
- `idx_worker_id` - Worker ID索引
- `idx_lease_expires_at` - 租约过期时间索引

## 故障处理

### Worker崩溃
租约过期后，其他Worker自动接手任务

### 任务卡住
租约机制确保任务不会永久卡住

### 网络故障
Redis连接断开时，任务状态回滚，重新入队

## 扩展建议

### 多Worker部署
- 每个Worker有唯一ID
- 通过Redis共享并发计数器
- 支持水平扩展

### 任务优先级
可扩展支持任务优先级，高优先级任务优先处理

### 任务分类
可扩展支持任务分类，不同类别不同并发限制