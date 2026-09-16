-- ========================================
-- Agent对话会话表初始化脚本（PostgreSQL）
-- ========================================

-- Agent对话会话表
CREATE TABLE IF NOT EXISTS zy_agent_chat (
    id                  varchar(64) not null primary key,
    user_id             varchar(64) not null,
    canvas_id           varchar(64) not null,
    title               varchar(256),
    messages_json       text,
    status              varchar(32) default 'active',
    delete_flag         varchar(32) default 'NOT_DELETE',
    create_user         varchar(64),
    create_time         timestamp default current_timestamp,
    update_user         varchar(64),
    update_time         timestamp
);

COMMENT ON TABLE zy_agent_chat IS 'Agent对话会话';
COMMENT ON COLUMN zy_agent_chat.id IS '主键';
COMMENT ON COLUMN zy_agent_chat.user_id IS '用户ID';
COMMENT ON COLUMN zy_agent_chat.canvas_id IS '画布ID';
COMMENT ON COLUMN zy_agent_chat.title IS '会话标题';
COMMENT ON COLUMN zy_agent_chat.messages_json IS '消息列表JSON';
COMMENT ON COLUMN zy_agent_chat.status IS '状态: active/archived';
COMMENT ON COLUMN zy_agent_chat.delete_flag IS '删除标志';
COMMENT ON COLUMN zy_agent_chat.create_user IS '创建人';
COMMENT ON COLUMN zy_agent_chat.create_time IS '创建时间';
COMMENT ON COLUMN zy_agent_chat.update_user IS '更新人';
COMMENT ON COLUMN zy_agent_chat.update_time IS '更新时间';

CREATE INDEX idx_zy_agent_chat_user ON zy_agent_chat(user_id);
CREATE INDEX idx_zy_agent_chat_canvas ON zy_agent_chat(canvas_id);
CREATE INDEX idx_zy_agent_chat_status ON zy_agent_chat(status);
