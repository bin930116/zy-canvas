-- ============================================================
-- 团队表 zy_team
-- 用于团队管理，记录团队基本信息
-- ============================================================
create table zy_team (
    id          varchar(64) not null primary key,
    user_id     varchar(64) not null,
    name        varchar(256) not null,
    description text,
    avatar_url  varchar(512),
    status      varchar(32) default 'active',
    delete_flag varchar(32) default 'NOT_DELETE',
    create_user varchar(64),
    create_time timestamp,
    update_user varchar(64),
    update_time timestamp
);

comment on table  zy_team is '团队表';
comment on column zy_team.id is '主键ID';
comment on column zy_team.user_id is '创建者用户ID';
comment on column zy_team.name is '团队名称';
comment on column zy_team.description is '团队描述';
comment on column zy_team.avatar_url is '团队头像URL';
comment on column zy_team.status is '状态：active=正常 / deleted=已删除';
comment on column zy_team.create_time is '创建时间';
comment on column zy_team.update_time is '更新时间';

-- ============================================================
-- 团队成员表 zy_team_member
-- 用于记录团队与成员的关系及角色
-- ============================================================
create table zy_team_member (
    id          varchar(64) not null primary key,
    team_id     varchar(64) not null,
    user_id     varchar(64) not null,
    role        varchar(32) not null default 'member',
    status      varchar(32) not null default 'active',
    invited_by  varchar(64),
    delete_flag varchar(32) default 'NOT_DELETE',
    create_user varchar(64),
    create_time timestamp,
    update_user varchar(64),
    update_time timestamp,
    constraint uk_zy_team_member unique (team_id, user_id)
);

comment on table  zy_team_member is '团队成员关系表';
comment on column zy_team_member.id is '主键ID';
comment on column zy_team_member.team_id is '关联团队ID，对应 zy_team.id';
comment on column zy_team_member.user_id is '成员用户ID';
comment on column zy_team_member.role is '成员角色：owner=所有者 / admin=管理员 / member=普通成员';
comment on column zy_team_member.status is '状态：active=正常';
comment on column zy_team_member.invited_by is '邀请人用户ID';
comment on column zy_team_member.create_time is '创建时间';
comment on column zy_team_member.update_time is '更新时间';

-- 按团队ID查询成员
create index idx_zy_team_member_team on zy_team_member (team_id);
-- 按用户ID查询"我参与的团队"
create index idx_zy_team_member_user on zy_team_member (user_id);
