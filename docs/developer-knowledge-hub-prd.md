# 开发者个人知识与项目中枢 PRD

## 1. 产品概述

### 1.1 产品名称

开发者个人知识与项目中枢

### 1.2 产品定位

一个面向个人开发者的知识管理、项目推进与学习复盘工具。它帮助用户收集技术资料、整理笔记、管理个人项目、追踪任务进度，并通过搜索、标签、关联和周期回顾，把零散的信息沉淀成可持续复用的个人知识系统。

本产品不是公开内容平台，也不是团队协作工具。第一阶段重点服务单个开发者的真实个人工作流。

### 1.3 目标用户

核心用户是有一定工程经验、长期学习和构建个人项目的开发者。

典型特征：

- 经常阅读技术文章、文档、源码和开源项目
- 有多个并行的学习主题或 side project
- 使用 Markdown 记录笔记
- 希望把链接、想法、任务、项目进度放在一个系统里
- 希望通过真实项目学习全栈开发、后端架构与工程化

### 1.4 产品目标

第一阶段目标：

- 支持用户创建、整理、搜索笔记和链接
- 支持围绕项目组织任务、资料和笔记
- 支持标签、状态、优先级等基础结构化管理
- 提供一个适合长期使用的个人开发工作台

学习目标：

- 通过项目覆盖 Next.js 与 Java 服务端开发的常见场景
- 练习用户系统、权限、API 设计、数据建模、搜索、文件上传、异步任务、测试、部署等后端能力
- 建立一个可以逐步演进的全栈项目骨架

## 2. 问题背景

开发者的知识和项目材料通常分散在多个地方：

- 浏览器书签
- 本地 Markdown 文件
- Notion / Obsidian
- GitHub issue
- Todo 应用
- 微信收藏、飞书文档、阅读列表
- 临时草稿和聊天记录

这些工具各自好用，但对个人开发者来说常见问题是：

- 资料收集后很少再被使用
- 笔记和任务分离，学习难以转化为行动
- 项目上下文散落，恢复成本高
- 缺少面向个人项目的轻量复盘机制
- 搜索和关联能力不足
- 很难从长期积累中看到学习路径和工程成果

本产品尝试把知识、项目、任务和回顾放在一个统一模型里。

## 3. 产品原则

### 3.1 个人优先

不做团队协作、组织权限、复杂评论和通知流。先服务一个人的深度工作流。

### 3.2 输入低摩擦

用户应该能快速保存一个想法、链接、代码片段或任务。输入入口要轻，整理动作可以稍后完成。

### 3.3 结构渐进

系统不强迫用户一开始就填写大量字段。标题、内容、标签、项目、状态等字段可以逐步补全。

### 3.4 搜索优先

长期知识系统的核心不是目录树，而是可搜索、可过滤、可关联。

### 3.5 项目驱动知识

笔记不是孤立收藏。它们应该能关联到项目、任务、学习主题和阶段回顾。

## 4. 版本范围

### 4.1 MVP 范围

MVP 目标是做出一个可日常使用的个人开发工作台。

包含：

- 用户登录
- 仪表盘
- 笔记管理
- 链接收藏
- 标签系统
- 项目管理
- 任务管理
- Markdown 编辑
- 基础搜索
- 文件附件
- 基础回顾

不包含：

- 多人协作
- 公开发布
- 支付系统
- 移动端原生 App
- 浏览器插件
- 复杂 AI agent
- 实时协同编辑

### 4.2 后续版本方向

V1.1:

- 高级全文搜索
- 笔记版本历史
- 项目时间线
- 周报/月报生成
- 数据导入导出

V1.2:

- AI 摘要、自动标签、语义搜索
- 链接内容自动抓取
- GitHub 仓库关联
- 日历视图

V2:

- 浏览器插件
- PWA 离线能力
- 插件化工作流
- 多设备同步优化

## 5. 核心用户场景

### 5.1 快速保存技术链接

用户在阅读文章时，希望保存链接并添加简单备注。

流程：

1. 用户点击新建链接
2. 输入 URL
3. 系统自动提取标题，允许用户手动修改
4. 用户添加标签和所属项目
5. 保存后可在链接库和项目详情中查看

验收标准：

- URL 为必填
- 标题可自动填充，也可手动输入
- 可绑定 0 个或 1 个项目
- 可添加多个标签
- 保存后能被搜索到

### 5.2 写一篇项目笔记

用户在开发个人项目时，希望记录设计思路、问题排查和代码片段。

流程：

1. 用户进入某个项目
2. 创建笔记
3. 使用 Markdown 编写内容
4. 添加标签和关联任务
5. 保存后显示在项目知识区

验收标准：

- 支持 Markdown 编辑与预览
- 支持代码块高亮
- 可关联项目
- 可关联任务
- 修改后更新最后编辑时间

### 5.3 管理个人项目

用户希望管理多个学习项目或 side project。

流程：

1. 创建项目
2. 设置项目目标、状态和技术栈
3. 在项目下创建任务、笔记和链接
4. 通过看板推进任务
5. 定期写项目回顾

验收标准：

- 项目有名称、描述、状态、开始时间
- 项目可以归档
- 项目详情页展示任务、笔记、链接、附件和回顾
- 支持按状态筛选项目

### 5.4 搜索已有知识

用户想找到过去记录过的 Spring Security 配置。

流程：

1. 用户在全局搜索框输入关键词
2. 系统返回笔记、链接、任务、项目
3. 用户通过类型、项目、标签进一步过滤
4. 点击结果进入详情

验收标准：

- 支持标题搜索
- 支持正文搜索
- 支持按类型过滤
- 支持按标签过滤
- 搜索结果显示命中内容摘要

### 5.5 每周回顾

用户希望复盘本周完成了什么、卡住了什么、下周要做什么。

流程：

1. 用户打开回顾页
2. 系统展示本周完成任务、创建笔记、保存链接
3. 用户填写总结、问题和下周计划
4. 保存后形成一篇周回顾

验收标准：

- 自动聚合指定日期范围内的活动
- 用户可编辑回顾正文
- 回顾可关联多个项目
- 回顾保存后可搜索

## 6. 功能需求

## 6.1 用户与认证

### 6.1.1 注册

MVP 可以只支持单用户初始化，也可以支持标准注册。

字段：

- 邮箱
- 用户名
- 密码

规则：

- 邮箱唯一
- 密码加密存储
- 用户名可修改

### 6.1.2 登录

支持邮箱和密码登录。

验收标准：

- 登录成功后返回访问凭证
- 登录失败显示明确错误
- 前端保存会话状态
- 后端接口校验用户身份

### 6.1.3 个人设置

支持修改：

- 用户名
- 头像
- 默认首页
- 主题偏好

MVP 可以只做用户名和主题偏好。

## 6.2 仪表盘

仪表盘是用户打开系统后的第一屏。

展示内容：

- 今日待办
- 最近编辑笔记
- 最近保存链接
- 活跃项目
- 本周完成任务数
- 快速创建入口

交互：

- 点击项目进入项目详情
- 点击任务可快速切换状态
- 点击笔记进入编辑页
- 支持快速新建笔记、任务、链接

## 6.3 笔记模块

### 6.3.1 笔记列表

展示字段：

- 标题
- 摘要
- 标签
- 所属项目
- 更新时间
- 收藏状态

筛选能力：

- 标签
- 项目
- 是否收藏
- 更新时间

排序能力：

- 最近更新
- 最近创建
- 标题

### 6.3.2 笔记详情与编辑

字段：

- 标题
- Markdown 正文
- 标签
- 所属项目
- 关联任务
- 收藏状态
- 创建时间
- 更新时间

验收标准：

- 支持自动保存或手动保存
- 支持 Markdown 预览
- 支持代码块
- 支持软删除
- 删除前二次确认

### 6.3.3 笔记类型

MVP 可以使用单一笔记模型。

后续可扩展类型：

- 普通笔记
- 代码片段
- 会议/思考记录
- 学习笔记
- 排障记录
- 决策记录

## 6.4 链接模块

### 6.4.1 链接创建

字段：

- URL
- 标题
- 描述
- 标签
- 所属项目
- 阅读状态
- 收藏状态

阅读状态：

- 未读
- 已读
- 稍后再读

验收标准：

- URL 格式校验
- 同一用户下 URL 可提示重复
- 可手动编辑标题和描述

### 6.4.2 链接列表

展示：

- 标题
- 域名
- 描述
- 标签
- 阅读状态
- 创建时间

筛选：

- 阅读状态
- 标签
- 项目
- 域名

## 6.5 项目模块

### 6.5.1 项目创建

字段：

- 名称
- 描述
- 目标
- 状态
- 技术栈
- 开始日期
- 结束日期

项目状态：

- 计划中
- 进行中
- 暂停
- 完成
- 归档

### 6.5.2 项目详情

项目详情页包含：

- 项目概览
- 任务看板
- 关联笔记
- 关联链接
- 附件
- 回顾
- 时间线

MVP 中时间线可以只展示最近活动。

### 6.5.3 项目归档

归档后：

- 默认不在活跃项目中展示
- 数据仍可搜索
- 可恢复

## 6.6 任务模块

### 6.6.1 任务字段

- 标题
- 描述
- 状态
- 优先级
- 所属项目
- 关联笔记
- 截止日期
- 创建时间
- 更新时间

任务状态：

- 待处理
- 进行中
- 已完成
- 已取消

优先级：

- 低
- 中
- 高

### 6.6.2 任务列表

支持：

- 按项目筛选
- 按状态筛选
- 按优先级筛选
- 按截止日期排序

### 6.6.3 项目看板

MVP 可做三列：

- 待处理
- 进行中
- 已完成

支持：

- 拖拽切换状态
- 快速新建任务
- 点击任务打开详情

## 6.7 标签模块

标签用于统一组织笔记、链接、项目和任务。

字段：

- 名称
- 颜色
- 使用次数
- 创建时间

规则：

- 同一用户下标签名称唯一
- 删除标签时不删除关联内容
- 支持重命名标签

## 6.8 搜索模块

### 6.8.1 MVP 搜索

搜索范围：

- 笔记标题与正文
- 链接标题与描述
- 项目名称与描述
- 任务标题与描述

筛选：

- 内容类型
- 标签
- 项目
- 时间范围

### 6.8.2 后续高级搜索

可扩展：

- PostgreSQL 全文搜索
- Elasticsearch / Meilisearch
- 语义搜索
- 搜索历史
- 保存搜索条件

## 6.9 文件附件

支持用户上传和管理附件。

MVP 支持：

- 图片
- PDF
- 文本文件

字段：

- 文件名
- 文件类型
- 文件大小
- 存储路径
- 所属资源类型
- 所属资源 ID

验收标准：

- 限制文件大小
- 校验文件类型
- 可从笔记或项目中上传附件
- 删除资源时附件关系被清理或标记失效

## 6.10 回顾模块

### 6.10.1 周回顾

字段：

- 标题
- 日期范围
- 正文
- 关联项目
- 自动聚合内容

自动聚合内容：

- 完成任务
- 新建笔记
- 保存链接
- 项目状态变化

### 6.10.2 项目回顾

字段：

- 项目
- 阶段
- 做了什么
- 遇到的问题
- 学到什么
- 下一步

## 7. 数据模型草案

### 7.1 User

- id
- email
- username
- passwordHash
- avatarUrl
- createdAt
- updatedAt

### 7.2 Project

- id
- userId
- name
- description
- goal
- status
- techStack
- startDate
- endDate
- archivedAt
- createdAt
- updatedAt

### 7.3 Note

- id
- userId
- projectId
- title
- content
- summary
- favorite
- deletedAt
- createdAt
- updatedAt

### 7.4 Link

- id
- userId
- projectId
- url
- title
- description
- domain
- readStatus
- favorite
- createdAt
- updatedAt

### 7.5 Task

- id
- userId
- projectId
- title
- description
- status
- priority
- dueDate
- completedAt
- createdAt
- updatedAt

### 7.6 Tag

- id
- userId
- name
- color
- createdAt
- updatedAt

### 7.7 TagBinding

- id
- userId
- tagId
- targetType
- targetId
- createdAt

### 7.8 Attachment

- id
- userId
- targetType
- targetId
- filename
- contentType
- size
- storageKey
- createdAt

### 7.9 Review

- id
- userId
- title
- type
- content
- startDate
- endDate
- createdAt
- updatedAt

### 7.10 ActivityLog

- id
- userId
- actorType
- action
- targetType
- targetId
- metadata
- createdAt

## 8. 页面结构

### 8.1 页面列表

- 登录页
- 仪表盘
- 笔记列表
- 笔记编辑页
- 链接列表
- 项目列表
- 项目详情页
- 任务列表
- 搜索结果页
- 标签管理页
- 回顾列表
- 回顾编辑页
- 设置页

### 8.2 导航结构

主导航：

- Dashboard
- Notes
- Links
- Projects
- Tasks
- Search
- Reviews
- Settings

全局操作：

- 新建笔记
- 新建链接
- 新建任务
- 全局搜索

## 9. API 草案

### 9.1 Auth

- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/logout
- GET /api/auth/me

### 9.2 Notes

- GET /api/notes
- POST /api/notes
- GET /api/notes/{id}
- PUT /api/notes/{id}
- DELETE /api/notes/{id}

### 9.3 Links

- GET /api/links
- POST /api/links
- GET /api/links/{id}
- PUT /api/links/{id}
- DELETE /api/links/{id}

### 9.4 Projects

- GET /api/projects
- POST /api/projects
- GET /api/projects/{id}
- PUT /api/projects/{id}
- DELETE /api/projects/{id}
- GET /api/projects/{id}/overview

### 9.5 Tasks

- GET /api/tasks
- POST /api/tasks
- GET /api/tasks/{id}
- PUT /api/tasks/{id}
- DELETE /api/tasks/{id}
- PATCH /api/tasks/{id}/status

### 9.6 Tags

- GET /api/tags
- POST /api/tags
- PUT /api/tags/{id}
- DELETE /api/tags/{id}

### 9.7 Search

- GET /api/search?q=&type=&tagId=&projectId=

### 9.8 Attachments

- POST /api/attachments
- GET /api/attachments/{id}
- DELETE /api/attachments/{id}

### 9.9 Reviews

- GET /api/reviews
- POST /api/reviews
- GET /api/reviews/{id}
- PUT /api/reviews/{id}
- DELETE /api/reviews/{id}
- GET /api/reviews/weekly-draft

## 10. 非功能需求

### 10.1 性能

- 常规列表接口响应时间低于 300ms
- 搜索接口 MVP 阶段低于 800ms
- 首页首屏加载应控制在合理范围内
- 大文本编辑不应明显卡顿

### 10.2 安全

- 密码必须加密存储
- 所有业务数据按 userId 隔离
- 后端不能信任前端传入的 userId
- 文件上传需要限制大小和类型
- 登录接口需要基础防暴力破解策略

### 10.3 可维护性

- 后端采用清晰分层
- Controller 不写复杂业务逻辑
- Service 负责业务规则
- Repository 负责数据访问
- DTO 与 Entity 分离
- 统一异常处理
- 统一 API 响应格式

### 10.4 可测试性

MVP 建议覆盖：

- 用户登录
- 笔记 CRUD
- 项目 CRUD
- 任务状态流转
- 标签绑定
- 搜索基础逻辑

测试类型：

- 后端单元测试
- 后端集成测试
- 前端组件测试
- 关键流程 E2E 测试

### 10.5 部署

本地开发：

- Next.js
- Java Spring Boot
- PostgreSQL
- Redis

建议使用 Docker Compose 启动依赖。

生产部署：

- 前端部署到 Vercel 或容器
- 后端部署到 VPS、云服务器或容器平台
- 数据库使用托管 PostgreSQL 或自建 PostgreSQL
- 文件存储后续迁移到 S3 兼容服务

## 11. 推荐技术方案

### 11.1 前端

- Next.js
- TypeScript
- Tailwind CSS
- shadcn/ui
- TanStack Query
- Zustand 或 React Context
- React Hook Form
- Zod
- TipTap 或 MDXEditor

### 11.2 后端

- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Redis
- JUnit
- Testcontainers
- OpenAPI / Swagger

### 11.3 工程化

- Monorepo 或双仓库均可
- Docker Compose
- GitHub Actions
- ESLint
- Prettier
- Checkstyle 或 Spotless
- Conventional Commits

## 12. 里程碑计划

### Milestone 1: 项目骨架

目标：

- 建立前后端项目
- 完成本地开发环境
- 完成数据库连接
- 完成基础认证

交付：

- Next.js 应用
- Spring Boot 应用
- PostgreSQL
- Docker Compose
- 登录和当前用户接口

### Milestone 2: 笔记与标签

目标：

- 完成核心知识管理能力

交付：

- 笔记 CRUD
- Markdown 编辑
- 标签 CRUD
- 标签绑定
- 笔记列表筛选

### Milestone 3: 项目与任务

目标：

- 建立项目驱动的工作流

交付：

- 项目 CRUD
- 任务 CRUD
- 项目详情页
- 简单看板

### Milestone 4: 链接与搜索

目标：

- 补齐资料收集和知识查找能力

交付：

- 链接 CRUD
- URL 元数据解析
- 全局搜索
- 搜索筛选

### Milestone 5: 附件与回顾

目标：

- 增强长期使用价值

交付：

- 文件上传
- 周回顾
- 项目回顾
- 活动日志

### Milestone 6: 打磨与部署

目标：

- 让项目可展示、可部署、可长期维护

交付：

- API 文档
- 测试覆盖核心流程
- 部署文档
- 线上环境
- README

## 13. 学习路线映射

### 13.1 Next.js 学习点

- App Router
- Server Components
- Client Components
- Server Actions 或 API 调用封装
- 路由布局
- 表单处理
- 缓存与重新验证
- 文件上传
- 鉴权态处理
- 复杂列表与筛选

### 13.2 Java / Spring 学习点

- Spring Boot 项目结构
- Controller / Service / Repository 分层
- DTO 映射
- Bean Validation
- Spring Security
- JPA 实体关系
- 事务管理
- Flyway 数据库迁移
- 统一异常处理
- 测试与 Testcontainers

### 13.3 后端思想学习点

- 领域建模
- API 设计
- 权限边界
- 数据一致性
- 软删除
- 审计日志
- 异步任务
- 搜索架构
- 文件存储
- 可观测性
- 部署与环境隔离

## 14. 风险与取舍

### 14.1 范围过大

风险：

功能模块很多，容易半途而废。

应对：

严格以 MVP 为第一目标。每个模块先做最小可用版本。

### 14.2 编辑器复杂度过高

风险：

Markdown 编辑器、富文本编辑器和附件处理可能消耗大量时间。

应对：

MVP 使用成熟编辑器库，先不自研复杂编辑能力。

### 14.3 搜索实现过早复杂化

风险：

一开始引入 Elasticsearch 会增加维护成本。

应对：

MVP 使用数据库 LIKE 或 PostgreSQL 全文搜索，后续再替换为搜索引擎。

### 14.4 后端架构过度设计

风险：

为了学习架构而引入过多抽象。

应对：

先采用清晰单体架构。模块边界清楚即可，不急于微服务化。

## 15. MVP 验收清单

- 用户可以登录
- 用户可以创建、编辑、删除笔记
- 用户可以保存链接
- 用户可以创建标签并绑定到笔记和链接
- 用户可以创建项目
- 用户可以在项目下创建任务
- 用户可以在项目详情页看到关联笔记、链接和任务
- 用户可以搜索笔记、链接、项目和任务
- 用户可以上传附件
- 用户可以创建一篇周回顾
- 项目可以通过 Docker Compose 在本地启动
- 后端有基础测试
- README 能说明如何启动项目

## 16. 建议的第一周开发任务

第一周不要急着做复杂 UI，目标是打通全链路。

建议任务：

1. 初始化 Next.js 项目
2. 初始化 Spring Boot 项目
3. 配置 PostgreSQL 与 Flyway
4. 设计 User、Note、Tag 三个核心表
5. 实现登录接口
6. 实现笔记 CRUD
7. 前端完成登录页、笔记列表和笔记编辑页
8. 写第一组后端集成测试
9. 用 Docker Compose 启动数据库和后端

第一周完成后，项目就已经有真实骨架，后续所有功能都可以沿着这个骨架自然长出来。
