-- =========================================================
-- 画布平台 初始化 DDL (PostgreSQL)
-- 表名、字段统一小写
-- =========================================================

-- 画布项目
create table zy_project (
    id           varchar(64) not null primary key,
    user_id      varchar(64) not null,
    title        varchar(256),
    project_id   varchar(64),
    data_json    text,
    revision     bigint      default 1,
    status       varchar(32) default 'active',
    delete_flag  varchar(32) default 'NOT_DELETE',
    create_user  varchar(64),
    create_time  timestamp,
    update_user  varchar(64),
    update_time  timestamp
);

create index idx_zy_project_user    on zy_project (user_id);
create index idx_zy_project_project on zy_project (project_id);

comment on table  zy_project              is '画布项目';
comment on column zy_project.id           is '主键';
comment on column zy_project.user_id      is '用户id';
comment on column zy_project.title        is '标题';
comment on column zy_project.project_id   is '关联短剧项目id';
comment on column zy_project.data_json    is '画布完整数据(JSON)';
comment on column zy_project.revision     is '乐观锁版本号';
comment on column zy_project.status       is '状态';
comment on column zy_project.delete_flag  is '删除标志';
comment on column zy_project.create_user  is '创建人';
comment on column zy_project.create_time  is '创建时间';
comment on column zy_project.update_user  is '更新人';
comment on column zy_project.update_time  is '更新时间';

-- 画布项目分享
create table zy_share (
    id           varchar(64) not null primary key,
    project_id   varchar(64),
    token        varchar(128),
    enabled      boolean     default true,
    expires_at   timestamp,
    delete_flag  varchar(32) default 'NOT_DELETE',
    create_user  varchar(64),
    create_time  timestamp,
    update_user  varchar(64),
    update_time  timestamp
);

create index idx_zy_share_project on zy_share (project_id);
create index idx_zy_share_token    on zy_share (token);

comment on table  zy_share              is '画布项目分享';
comment on column zy_share.id           is '主键';
comment on column zy_share.project_id   is '项目id';
comment on column zy_share.token        is '分享token';
comment on column zy_share.enabled      is '是否启用';
comment on column zy_share.expires_at   is '过期时间';
comment on column zy_share.delete_flag  is '删除标志';
comment on column zy_share.create_user  is '创建人';
comment on column zy_share.create_time  is '创建时间';
comment on column zy_share.update_user  is '更新人';
comment on column zy_share.update_time  is '更新时间';

-- 短剧项目
create table zy_drama_project (
    id                   varchar(64) not null primary key,
    user_id              varchar(64) not null,
    team_id              varchar(64),
    name                 varchar(256),
    type                 varchar(64),
    aspect_ratio         varchar(32),
    source_type          varchar(64),
    description          text,
    cover_resource_id    varchar(64),
    style_preset_id      varchar(64),
    style_profile_json   text,
    default_image_model  varchar(128),
    default_video_model  varchar(128),
    status               varchar(32) default 'active',
    revision             bigint      default 1,
    delete_flag          varchar(32) default 'NOT_DELETE',
    create_user          varchar(64),
    create_time          timestamp,
    update_user          varchar(64),
    update_time          timestamp
);

create index idx_zy_drama_project_user on zy_drama_project (user_id);

create index idx_zy_drama_project_team on zy_drama_project (team_id);

comment on table  zy_drama_project                    is '短剧项目';
comment on column zy_drama_project.id                 is '主键';
comment on column zy_drama_project.user_id            is '用户id';
comment on column zy_drama_project.team_id            is '关联团队id(空为个人项目)';
comment on column zy_drama_project.name               is '名称';
comment on column zy_drama_project.type               is '类型';
comment on column zy_drama_project.aspect_ratio       is '画幅比';
comment on column zy_drama_project.source_type        is '来源类型';
comment on column zy_drama_project.description        is '描述';
comment on column zy_drama_project.cover_resource_id  is '封面资源id';
comment on column zy_drama_project.style_preset_id    is '风格预设id';
comment on column zy_drama_project.style_profile_json is '风格配置JSON';
comment on column zy_drama_project.default_image_model is '默认图片模型';
comment on column zy_drama_project.default_video_model is '默认视频模型';
comment on column zy_drama_project.status             is '状态';
comment on column zy_drama_project.revision           is '乐观锁版本号';
comment on column zy_drama_project.delete_flag        is '删除标志';
comment on column zy_drama_project.create_user        is '创建人';
comment on column zy_drama_project.create_time        is '创建时间';
comment on column zy_drama_project.update_user        is '更新人';
comment on column zy_drama_project.update_time        is '更新时间';

-- 短剧项目章节
create table zy_unit (
    id           varchar(64) not null primary key,
    project_id   varchar(64) not null,
    kind         varchar(32),
    title        varchar(256),
    source_text  text,
    word_count   int         default 0,
    status       varchar(32) default 'draft',
    position     int         default 0,
    delete_flag  varchar(32) default 'NOT_DELETE',
    create_user  varchar(64),
    create_time  timestamp,
    update_user  varchar(64),
    update_time  timestamp
);

create index idx_zy_unit_project on zy_unit (project_id);

comment on table  zy_unit             is '短剧项目章节';
comment on column zy_unit.id          is '主键';
comment on column zy_unit.project_id  is '项目id';
comment on column zy_unit.kind        is '类型：chapter|episode';
comment on column zy_unit.title       is '标题';
comment on column zy_unit.source_text is '正文';
comment on column zy_unit.word_count  is '字数';
comment on column zy_unit.status      is '状态：draft|ready|completed';
comment on column zy_unit.position    is '排序';
comment on column zy_unit.delete_flag is '删除标志';
comment on column zy_unit.create_user is '创建人';
comment on column zy_unit.create_time is '创建时间';
comment on column zy_unit.update_user is '更新人';
comment on column zy_unit.update_time is '更新时间';

-- 画布与章节关联
create table zy_canvas_unit_link (
    id          varchar(64) not null primary key,
    project_id  varchar(64) not null,
    canvas_id   varchar(64) not null,
    unit_id     varchar(64) not null,
    role        varchar(32) default 'storyboard',
    delete_flag varchar(32) default 'NOT_DELETE',
    create_user varchar(64),
    create_time timestamp,
    update_user varchar(64),
    update_time timestamp,
    constraint uk_canvas_unit unique (project_id, canvas_id, unit_id)
);

create index idx_zy_canvas_unit_link_project on zy_canvas_unit_link (project_id);
create index idx_zy_canvas_unit_link_canvas  on zy_canvas_unit_link (canvas_id);
create index idx_zy_canvas_unit_link_unit    on zy_canvas_unit_link (unit_id);

comment on table  zy_canvas_unit_link        is '画布与章节关联';
comment on column zy_canvas_unit_link.id     is '主键';
comment on column zy_canvas_unit_link.project_id is '项目id';
comment on column zy_canvas_unit_link.canvas_id  is '画布id';
comment on column zy_canvas_unit_link.unit_id    is '章节id';
comment on column zy_canvas_unit_link.role       is '角色：storyboard|reference';
comment on column zy_canvas_unit_link.delete_flag is '删除标志';
comment on column zy_canvas_unit_link.create_user is '创建人';
comment on column zy_canvas_unit_link.create_time is '创建时间';
comment on column zy_canvas_unit_link.update_user is '更新人';
comment on column zy_canvas_unit_link.update_time is '更新时间';

-- 短剧项目分镜
create table zy_shot (
    id                   varchar(64) not null primary key,
    project_id           varchar(64) not null,
    unit_id              varchar(64),
    current_revision_id  varchar(64),
    title                varchar(256),
    description          text,
    position             int         default 0,
    duration_ms          bigint      default 0,
    status               varchar(32) default 'draft',
    delete_flag          varchar(32) default 'NOT_DELETE',
    create_user          varchar(64),
    create_time          timestamp,
    update_user          varchar(64),
    update_time          timestamp
);

create index idx_zy_shot_project on zy_shot (project_id);
create index idx_zy_shot_unit on zy_shot (unit_id);

comment on table  zy_shot                  is '短剧项目分镜';
comment on column zy_shot.id               is '主键';
comment on column zy_shot.project_id       is '项目id';
comment on column zy_shot.unit_id         is '所属章节id';
comment on column zy_shot.current_revision_id is '当前版本id';
comment on column zy_shot.title            is '标题';
comment on column zy_shot.description      is '描述';
comment on column zy_shot.position         is '排序';
comment on column zy_shot.duration_ms      is '时长(毫秒)';
comment on column zy_shot.status           is '状态';
comment on column zy_shot.delete_flag      is '删除标志';
comment on column zy_shot.create_user      is '创建人';
comment on column zy_shot.create_time      is '创建时间';
comment on column zy_shot.update_user      is '更新人';
comment on column zy_shot.update_time      is '更新时间';

-- 短剧项目分镜版本
create table zy_shot_revision (
    id                 varchar(64) not null primary key,
    shot_id            varchar(64) not null,
    version            int         default 1,
    plot_description   text,
    action             text,
    dialogue           text,
    shot_size          varchar(32),
    camera_angle       varchar(32),
    camera_movement    varchar(64),
    duration_ms        bigint      default 0,
    image_prompt       text,
    video_prompt       text,
    negative_prompt    text,
    continuity_notes   text,
    action_beats_json  text,
    created_by         varchar(64),
    delete_flag        varchar(32) default 'NOT_DELETE',
    create_user        varchar(64),
    create_time        timestamp,
    update_user        varchar(64),
    update_time        timestamp
);

create index idx_zy_shot_revision_shot on zy_shot_revision (shot_id);

comment on table  zy_shot_revision                   is '短剧项目分镜版本';
comment on column zy_shot_revision.id                is '主键';
comment on column zy_shot_revision.shot_id           is '分镜id';
comment on column zy_shot_revision.version           is '版本号';
comment on column zy_shot_revision.plot_description  is '剧情描述';
comment on column zy_shot_revision.action            is '动作';
comment on column zy_shot_revision.dialogue          is '台词';
comment on column zy_shot_revision.shot_size         is '景别';
comment on column zy_shot_revision.camera_angle      is '机位角度';
comment on column zy_shot_revision.camera_movement   is '运镜';
comment on column zy_shot_revision.duration_ms       is '时长(毫秒)';
comment on column zy_shot_revision.image_prompt      is '图片提示词';
comment on column zy_shot_revision.video_prompt      is '视频提示词';
comment on column zy_shot_revision.negative_prompt   is '负面提示词';
comment on column zy_shot_revision.continuity_notes  is '连续性说明';
comment on column zy_shot_revision.action_beats_json is '动作节拍JSON';
comment on column zy_shot_revision.created_by        is '创建人';
comment on column zy_shot_revision.delete_flag       is '删除标志';
comment on column zy_shot_revision.create_user       is '创建人';
comment on column zy_shot_revision.create_time       is '创建时间';
comment on column zy_shot_revision.update_user       is '更新人';
comment on column zy_shot_revision.update_time       is '更新时间';

-- 短剧项目资产
create table zy_asset (
    id                   varchar(64) not null primary key,
    project_id           varchar(64) not null,
    title                varchar(256),
    media_type           varchar(32),
    category             varchar(64),
    status               varchar(32) default 'active',
    primary_version_id   varchar(64),
    version_count        int         default 0,
    usages               text,
    folder_id            varchar(64),
    position             int         default 0,
    storage_key          varchar(512),
    duration_ms          bigint      default 0,
    preview_text         text,
    character_id         varchar(64),
    source               varchar(64),
    ext_json             text,
    cover_url            varchar(512),
    tags                 text,
    note                 text,
    delete_flag          varchar(32) default 'NOT_DELETE',
    create_user          varchar(64),
    create_time          timestamp,
    update_user          varchar(64),
    update_time          timestamp
);

create index idx_zy_asset_project on zy_asset (project_id);
create index idx_zy_asset_folder on zy_asset (folder_id);

comment on table  zy_asset                 is '短剧项目资产';
comment on column zy_asset.id              is '主键';
comment on column zy_asset.project_id      is '项目id';
comment on column zy_asset.title           is '标题';
comment on column zy_asset.media_type      is '介质类型：image|video|audio|text|model';
comment on column zy_asset.category        is '分类：character|material|other';
comment on column zy_asset.status          is '状态：active|archived|deleted';
comment on column zy_asset.primary_version_id is '主版本id';
comment on column zy_asset.version_count   is '版本数';
comment on column zy_asset.usages          is '用途(JSON数组)';
comment on column zy_asset.folder_id       is '所属文件夹id';
comment on column zy_asset.position        is '排序';
comment on column zy_asset.storage_key     is '存储key';
comment on column zy_asset.duration_ms     is '时长(毫秒)';
comment on column zy_asset.preview_text    is '预览文本';
comment on column zy_asset.character_id    is '关联角色id';
comment on column zy_asset.source          is '来源';
comment on column zy_asset.ext_json        is '扩展JSON';
comment on column zy_asset.cover_url       is '封面图片URL';
comment on column zy_asset.tags            is '标签(JSON数组)';
comment on column zy_asset.note            is '备注信息';
comment on column zy_asset.delete_flag     is '删除标志';
comment on column zy_asset.create_user     is '创建人';
comment on column zy_asset.create_time     is '创建时间';
comment on column zy_asset.update_user     is '更新人';
comment on column zy_asset.update_time     is '更新时间';

-- 短剧项目资产文件夹
create table zy_asset_folder (
    id           varchar(64) not null primary key,
    project_id   varchar(64) not null,
    parent_id    varchar(64),
    name         varchar(256) not null,
    style        varchar(32),
    theme        varchar(32),
    position     int         default 0,
    delete_flag  varchar(32) default 'NOT_DELETE',
    create_user  varchar(64),
    create_time  timestamp,
    update_user  varchar(64),
    update_time  timestamp
);

create index idx_zy_asset_folder_project on zy_asset_folder (project_id);

comment on table  zy_asset_folder                  is '短剧项目资产文件夹';
comment on column zy_asset_folder.id               is '主键';
comment on column zy_asset_folder.project_id       is '项目id';
comment on column zy_asset_folder.parent_id        is '上级文件夹id';
comment on column zy_asset_folder.name             is '名称';
comment on column zy_asset_folder.style            is '风格';
comment on column zy_asset_folder.theme            is '主题';
comment on column zy_asset_folder.position         is '排序';
comment on column zy_asset_folder.delete_flag      is '删除标志';
comment on column zy_asset_folder.create_user      is '创建人';
comment on column zy_asset_folder.create_time      is '创建时间';
comment on column zy_asset_folder.update_user      is '更新人';
comment on column zy_asset_folder.update_time      is '更新时间';

-- 短剧项目资产候选
create table zy_asset_candidate (
    id                   varchar(64) not null primary key,
    project_id           varchar(64) not null,
    unit_id              varchar(64),
    shot_id              varchar(64),
    name                 varchar(240),
    name_key             varchar(240),
    category             varchar(32),
    status               varchar(32) default 'pending_confirmation',
    source               varchar(48),
    details_json         text,
    resolved_asset_id    varchar(80),
    delete_flag          varchar(32) default 'NOT_DELETE',
    create_user          varchar(64),
    create_time          timestamp,
    update_user          varchar(64),
    update_time          timestamp
);

create index idx_zy_asset_candidate_project on zy_asset_candidate (project_id);
create index idx_zy_asset_candidate_project_unit_status on zy_asset_candidate (project_id, unit_id, status);
create index idx_zy_asset_candidate_project_status_category on zy_asset_candidate (project_id, status, category);

comment on table  zy_asset_candidate                 is '短剧项目资产候选';
comment on column zy_asset_candidate.id              is '主键';
comment on column zy_asset_candidate.project_id      is '项目id';
comment on column zy_asset_candidate.unit_id         is '章节/剧集id';
comment on column zy_asset_candidate.shot_id         is '镜头id';
comment on column zy_asset_candidate.name            is '名称';
comment on column zy_asset_candidate.name_key        is '名称key（用于搜索）';
comment on column zy_asset_candidate.category        is '分类';
comment on column zy_asset_candidate.status          is '状态：pending_confirmation-待确认，confirmed-已确认，ignored-已忽略';
comment on column zy_asset_candidate.source          is '来源：chapter_character_extract-章节角色提取，agent-AI Agent建议';
comment on column zy_asset_candidate.details_json    is '附加详情JSON';
comment on column zy_asset_candidate.resolved_asset_id is '已解析的资产id';
comment on column zy_asset_candidate.delete_flag     is '删除标志';
comment on column zy_asset_candidate.create_user     is '创建人';
comment on column zy_asset_candidate.create_time     is '创建时间';
comment on column zy_asset_candidate.update_user     is '更新人';
comment on column zy_asset_candidate.update_time     is '更新时间';

-- ============================================================
-- 增量升级：项目关联团队（存量库手动执行；新建库已包含在上面的建表 DDL 中）
-- ============================================================
alter table zy_drama_project add column if not exists team_id varchar(64);
create index if not exists idx_zy_drama_project_team on zy_drama_project (team_id);
comment on column zy_drama_project.team_id is '关联团队id(空为个人项目)';
