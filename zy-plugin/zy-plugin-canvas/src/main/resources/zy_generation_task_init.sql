-- 生成任务
create table zy_generation_task (
    id                     varchar(64) not null primary key,
    user_id                varchar(64) not null,
    project_id             varchar(64),
    type                   varchar(64) not null,
    status                 varchar(32) default 'queued',
    progress               int         default 0,
    stage                  varchar(64),
    prompt                 text,
    operation              varchar(128),
    provider               varchar(128),
    model                  varchar(256),
    provider_request_id    varchar(256),
    provider_cancel_status varchar(32),
    provider_cancel_error  text,
    provider_cancel_attempts int         default 0,
    provider_cancel_requested_at timestamp,
    provider_cancelled_at  timestamp,
    error_code             varchar(128),
    official_status        varchar(32),
    preview_url            text,
    preview_kind           varchar(32),
    preview_poster_url     text,
    input_json             text,
    result_json            text,
    result_state           varchar(64),
    text_draft             text,
    error                  text,
    attempts               int         default 1,
    started_at             timestamp,
    completed_at           timestamp,
    client_context_json    text,
    delete_flag            varchar(32) default 'NOT_DELETE',
    create_user            varchar(64),
    create_time            timestamp,
    update_user            varchar(64),
    update_time            timestamp
);

create index idx_zy_generation_task_user    on zy_generation_task (user_id);
create index idx_zy_generation_task_project on zy_generation_task (project_id);
create index idx_zy_generation_task_status  on zy_generation_task (status);
create index idx_zy_generation_task_created on zy_generation_task (create_time desc);

comment on table  zy_generation_task              is '生成任务';
comment on column zy_generation_task.id               is '主键';
comment on column zy_generation_task.user_id          is '用户id';
comment on column zy_generation_task.project_id       is '项目id';
comment on column zy_generation_task.type             is '任务类型';
comment on column zy_generation_task.status           is '状态：queued-排队中，running-运行中，succeeded-成功，failed-失败，cancelled-已取消';
comment on column zy_generation_task.progress         is '进度';
comment on column zy_generation_task.stage            is '阶段';
comment on column zy_generation_task.prompt           is '提示词';
comment on column zy_generation_task.operation        is '操作';
comment on column zy_generation_task.provider         is '提供商';
comment on column zy_generation_task.model            is '模型';
comment on column zy_generation_task.provider_request_id is '提供商请求id';
comment on column zy_generation_task.provider_cancel_status is '提供商取消状态';
comment on column zy_generation_task.provider_cancel_error is '提供商取消错误';
comment on column zy_generation_task.provider_cancel_attempts is '提供商取消尝试次数';
comment on column zy_generation_task.provider_cancel_requested_at is '提供商取消请求时间';
comment on column zy_generation_task.provider_cancelled_at is '提供商取消时间';
comment on column zy_generation_task.error_code       is '错误码';
comment on column zy_generation_task.official_status  is '官方状态';
comment on column zy_generation_task.preview_url      is '预览URL';
comment on column zy_generation_task.preview_kind     is '预览类型';
comment on column zy_generation_task.preview_poster_url is '预览海报URL';
comment on column zy_generation_task.input_json       is '输入JSON';
comment on column zy_generation_task.result_json      is '结果JSON';
comment on column zy_generation_task.result_state     is '结果状态';
comment on column zy_generation_task.text_draft       is '文本草稿';
comment on column zy_generation_task.error            is '错误信息';
comment on column zy_generation_task.attempts         is '尝试次数';
comment on column zy_generation_task.started_at       is '开始时间';
comment on column zy_generation_task.completed_at     is '完成时间';
comment on column zy_generation_task.client_context_json is '客户端上下文JSON';
comment on column zy_generation_task.delete_flag      is '删除标志';
comment on column zy_generation_task.create_user      is '创建人';
comment on column zy_generation_task.create_time      is '创建时间';
comment on column zy_generation_task.update_user      is '更新人';
comment on column zy_generation_task.update_time      is '更新时间';