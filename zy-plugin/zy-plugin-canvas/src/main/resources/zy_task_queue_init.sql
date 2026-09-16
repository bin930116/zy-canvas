-- ========================================
-- 任务队列表结构调整脚本（PostgreSQL）
-- ========================================

-- 1. 新增字段
ALTER TABLE zy_generation_task ADD COLUMN IF NOT EXISTS worker_id varchar(64);
ALTER TABLE zy_generation_task ADD COLUMN IF NOT EXISTS lease_expires_at timestamp;
ALTER TABLE zy_generation_task ADD COLUMN IF NOT EXISTS retry_count int DEFAULT 0;
ALTER TABLE zy_generation_task ADD COLUMN IF NOT EXISTS max_retries int DEFAULT 3;

-- 2. 添加字段注释
COMMENT ON COLUMN zy_generation_task.worker_id IS '处理该任务的Worker ID';
COMMENT ON COLUMN zy_generation_task.lease_expires_at IS '任务租约过期时间';
COMMENT ON COLUMN zy_generation_task.retry_count IS '已重试次数';
COMMENT ON COLUMN zy_generation_task.max_retries IS '最大重试次数';

-- 3. 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_task_status_created ON zy_generation_task(status, create_time) 
WHERE status IN ('queued', 'running');

CREATE INDEX IF NOT EXISTS idx_task_worker_lease ON zy_generation_task(worker_id, lease_expires_at)
WHERE status = 'running';

-- 4. 更新默认值
UPDATE zy_generation_task SET max_retries = 3 WHERE max_retries IS NULL;
UPDATE zy_generation_task SET retry_count = 0 WHERE retry_count IS NULL;