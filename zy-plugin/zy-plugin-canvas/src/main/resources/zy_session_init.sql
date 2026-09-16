-- ========================================
-- 会话表初始化脚本（PostgreSQL）
-- ========================================

-- 会话表
CREATE TABLE IF NOT EXISTS zy_session (
    id                  varchar(64) not null primary key,
    user_id             varchar(64) not null,
    project_id          varchar(64),
    prompt              text,
    status              varchar(32) default 'active',
    canvas_snapshot_json text,
    canvas_ops_json     text,
    delete_flag         varchar(32) default 'NOT_DELETE',
    create_user         varchar(64),
    create_time         timestamp default current_timestamp,
    update_user         varchar(64),
    update_time         timestamp
);

COMMENT ON TABLE zy_session IS '会话';
COMMENT ON COLUMN zy_session.id IS '主键';
COMMENT ON COLUMN zy_session.user_id IS '用户ID';
COMMENT ON COLUMN zy_session.project_id IS '项目ID';
COMMENT ON COLUMN zy_session.prompt IS '提示词';
COMMENT ON COLUMN zy_session.status IS '状态';
COMMENT ON COLUMN zy_session.canvas_snapshot_json IS '画布快照JSON';
COMMENT ON COLUMN zy_session.canvas_ops_json IS '画布操作JSON';
COMMENT ON COLUMN zy_session.delete_flag IS '删除标志';
COMMENT ON COLUMN zy_session.create_user IS '创建人';
COMMENT ON COLUMN zy_session.create_time IS '创建时间';
COMMENT ON COLUMN zy_session.update_user IS '更新人';
COMMENT ON COLUMN zy_session.update_time IS '更新时间';

CREATE INDEX idx_zy_session_user ON zy_session(user_id);
CREATE INDEX idx_zy_session_project ON zy_session(project_id);
CREATE INDEX idx_zy_session_status ON zy_session(status);