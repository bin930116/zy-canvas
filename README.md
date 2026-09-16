# 影策 AI 影视创作平台（zy）

面向短剧与 AI 影视创作的**一体化创作平台**：以 AI 画布为核心，串联创意、分镜、素材资产、技能库与智能体，对接大模型与文/图/视频生成能力，形成从创意到成片的创作流水线。

基于开源框架 **Snowy 3.0**（Spring Boot 3 插件化快速开发平台）与开源画布 **infinite-canvas** 二次开发，后端模块已统一品牌化为 `zy-*`。

---

## 技术栈

| 端 | 技术 |
|----|------|
| 后端 | Java 17 · Spring Boot 3.5.9 · MyBatis-Plus · Sa-Token · Knife4j · PostgreSQL · Redis/Redisson · dynamic-datasource + Druid · x-file-storage · 国密 SM2/SM3/SM4 |
| 管理端前端 | Vue 3 · Vite 5 · Ant Design Vue 4（`zy-web`） |
| 画布前端 | React · Vite · infinite-canvas 二次开发（`canvas-web`） |
| AI 网关 | 自研 `zyapi` 层对接 NewApi（OpenAI 兼容）/ ComfyUI，文/图/视频生成异步任务 |

## 核心能力

- **短剧创作**：项目 / 章节单元 / 分镜 / 分镜版本与产物管理
- **素材资产**：素材库、候选素材、素材文件夹（多级归类）
- **技能库**：创作技能（含提示词模板、智能体模板）与分享
- **AI 生成任务**：文本 / 图片 / 视频生成，异步任务调度 + 轮询 + SSE 实时推送
- **智能体对话**：会话与智能体聊天，支持画布工作流编排
- **团队协作**：团队与成员管理
- **平台底座**：用户 / 组织 / 角色 / 菜单 / 权限、开发工具（配置、字典、文件、定时任务、日志、消息）、代码生成

## 目录结构

```
zy/
├── pom.xml              聚合 POM（artifactId=zy，zy.version=3.0.0）
├── zy-common/           基础通用模块
├── zy-plugin/           业务插件聚合
│   ├── zy-plugin-auth/  登录鉴权
│   ├── zy-plugin-biz/   业务功能
│   ├── zy-plugin-client/ C 端功能
│   ├── zy-plugin-dev/   开发工具（配置/字典/文件/定时/日志/消息）
│   ├── zy-plugin-gen/   代码生成
│   ├── zy-plugin-mobile/ 移动端管理
│   ├── zy-plugin-sys/   系统功能（用户/组织/角色/菜单/按钮）
│   └── zy-plugin-canvas/ ★ 本项目定制：AI 画布 / 短剧业务插件
├── zy-plugin-api/       插件 API 聚合（zy-plugin-*-api，跨插件调用解耦）
├── zy-web-app/          主启动模块（jar，端口 82，应用名 zy）
├── zy-web/              管理端前端（Vue3，默认端口 83）
└── canvas-web/          画布前端（React，独立 git 仓库）
```

> Java 包名保持框架原生 `vip.xiaonuo.*`；业务实体/表前缀统一 `zy_`。

## 快速启动

### 后端

环境要求：JDK 17、Maven、PostgreSQL、Redis。

1. 创建数据库 `zy-new`（PostgreSQL），执行业务建表脚本（见各插件 `_sql` / 开发文档）
2. 修改 `zy-web-app/src/main/resources/application.properties` 中数据源、Redis 等配置
3. 启动主类 `vip.xiaonuo.Application`（默认端口 **82**）
4. 接口文档：`http://localhost:82/doc.html`（Knife4j，按插件分组）

### 管理端前端（zy-web）

```bash
cd zy-web
npm install
npm run dev   # 默认 83 端口
```

### 画布前端（canvas-web）

```bash
cd canvas-web
npm install
npm run dev
```

## 配置速查

| 项 | 值 |
|----|----|
| 服务端口 | 82 |
| 数据库 | PostgreSQL `zy-new`（`application.properties` 可切换 MySQL 等） |
| Redis | `127.0.0.1:6379`，database=1 |
| 鉴权 | Sa-Token 双体系（B 端 `StpUtil` / C 端 `StpClientUtil`） |
| AI 任务 | `task.worker.*` 配置（并发、轮询间隔、超时、重试） |

## 文档

- [后端架构与项目结构规范](./后端架构与项目结构规范.md)

## 致谢

- 后端框架：[Snowy / SnowyAdmin](https://gitee.com/xiaonuobase/snowy)（Apache-2.0）
- 画布前端：[infinite-canvas](https://github.com/ddiu8081/infinite-canvas) 及其生态

## License

本项目基于 **Apache License 2.0** 开源协议发布；沿用上游框架的版权与作者声明。
