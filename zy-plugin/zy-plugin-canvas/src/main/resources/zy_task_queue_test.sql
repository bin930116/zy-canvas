-- ========================================
-- 任务队列测试SQL
-- ========================================

-- 1. 检查是否有 queued 状态的任务
SELECT id, type, status, progress, create_time 
FROM zy_generation_task 
WHERE status = 'queued' 
ORDER BY create_time 
LIMIT 5;

-- 2. 插入测试任务
INSERT INTO zy_generation_task (
    id, user_id, type, status, progress, prompt, create_time, update_time, delete_flag
) VALUES (
    'test_task_' || to_char(now(), 'YYYYMMDDHHmmss'),
    'test_user',
    'test',
    'queued',
    0,
    '这是一个测试任务',
    now(),
    now(),
    'NOT_DELETE'
);

-- 3. 检查定时任务是否已注册
SELECT * FROM dev_job WHERE code = 'TASK_QUEUE_POLL';

-- 4. 注册定时任务
INSERT INTO dev_job (
    id, name, code, cron_expression, action_class, job_status, category, create_time, update_time, delete_flag
) VALUES (
    'task_queue_poll',
    '任务队列轮询',
    'TASK_QUEUE_POLL',
    '0/2 * * * * ?',
    'vip.xiaonuo.canvas.zyapi.task.timer.TaskQueueTimerTaskRunner',
    'RUNNING',
    'TASK',
    now(),
    now(),
    'NOT_DELETE'
) ON CONFLICT (code) DO UPDATE SET 
    job_status = 'RUNNING',
    update_time = now();
