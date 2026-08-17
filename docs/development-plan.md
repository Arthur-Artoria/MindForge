# MindForge 开发执行计划

> 更新日期：2026-08-17
>
> 当前方向：后端 Session/CSRF 与 Note CRUD 已完成；先补齐后端 API 契约和前端数据访问基础设施，再打通 Next.js 登录与 Note 纵向闭环。
>
> 产品范围以 `docs/developer-knowledge-hub-prd.md` 为准；`apps/web/app/prototype/dashboard/` 仅用于视觉方向探索，不是生产实现规范。

## 1. 当前状态

已实现：

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`（由 Spring Security `LogoutFilter` 处理）
- Session 中持久化和恢复 `SecurityContext`
- JSON 401 响应和错误凭据的统一错误响应
- `GET /api/auth/csrf` 以及登录、退出后的 CSRF token 刷新流程
- `POST /api/notes`
- `GET /api/notes`
- `GET /api/notes/{id}`
- `PUT /api/notes/{id}`
- `DELETE /api/notes/{id}`（软删除）
- Note 所有权约束：`userId` 只来自认证主体，跨用户读取、更新和删除统一返回 404

已有测试：

- [x] 正确登录后，使用同一个 Session 请求 `/api/auth/me` 成功
- [x] 未登录请求 `/api/auth/me` 返回 JSON 401
- [x] 错误凭据返回通用 JSON 401
- [x] logout 后，旧 Session 请求 `/api/auth/me` 返回 401

当前安全配置已经移除 login/logout 的 CSRF ignore 规则。浏览器客户端必须先通过
`GET /api/auth/csrf` 获取与 Session 绑定的 token，并在 login、logout 和 Note 写请求中携带相应 header。

## 2. 实施顺序

### 阶段 A：认证安全收尾

#### A1. 锁定完整 Session 生命周期

- [x] 增加一条完整集成测试：

  ```text
  login 200
    -> me 200
    -> logout 204
    -> 使用旧 Session 请求 me 401
  ```

- [x] 确认 logout 会使服务端 Session 失效，而不只是清空当前请求的认证信息。

#### A2. 恢复登录时的 Session 安全生命周期

当前 `AuthController` 手动执行：

```text
AuthenticationManager.authenticate(...)
  -> 创建 SecurityContext
  -> SecurityContextRepository.saveContext(...)
```

- [x] 在认证成功时执行适当的 `SessionAuthenticationStrategy`。
- [x] 保留默认的 session fixation protection，优先使用 Servlet 容器的 `changeSessionId` 行为。
- [x] 增加测试：如果登录前已有 Session，登录成功后 Session ID 必须发生变化，同时认证状态仍可由 `/me` 读取。
- [x] 在继续使用 controller 登录与改成 Spring Security filter 登录之间做一次明确选择；当前可以先用最小改动补齐生命周期，不必为了重构阻塞功能开发。

#### A3. 完成 SPA CSRF 流程

- [x] 增加允许匿名访问的 token 获取接口，例如 `GET /api/auth/csrf`。
- [x] 移除 login/logout 的 CSRF ignore 规则。
- [x] login 和 logout 必须携带有效 CSRF token。
- [x] 登录成功和退出成功后重新获取 token；旧 token 不得继续使用。
- [x] 为缺失或错误 token 的请求返回稳定的 JSON 403。
- [x] 补充端到端测试。

目标请求流程：

```text
GET  /api/auth/csrf
  -> 保存 Session Cookie，并取得 CSRF token

POST /api/auth/login
  -> 携带相同 Session Cookie 和 CSRF header
  -> 登录后刷新 CSRF token

POST /api/auth/logout
  -> 携带当前 CSRF token
  -> 退出后按需获取新的匿名 CSRF token
```

注意：MockMvc 的 `.with(csrf())` 只能证明过滤器接受合法 token，不能证明浏览器客户端能够获取、保存和刷新 token。

#### A4. 阶段 A 完成标准

- [x] login、me、logout 完整生命周期测试通过。
- [x] session fixation protection 测试通过。
- [x] login 和 logout 不再绕过 CSRF。
- [x] 401 与 403 都返回约定的 JSON 错误结构。
- [x] 删除 `AuthController` 中的 `System.out.println`。

### 阶段 B：Note 第一个纵向切片

认证安全收尾后，不继续扩展 Auth，直接实现：

```text
POST /api/notes
GET  /api/notes/{id}
```

#### B1. Module 与所有权规则

- [x] 请求 DTO 不允许接收 `userId`。
- [x] `userId` 只能从后端 `AuthenticatedUser` / `SecurityContext` 推导。
- [x] Note 查询在 Repository seam 上同时约束 `noteId` 和 `userId`，例如 `findByIdAndUserId(...)`。
- [x] 其他用户访问不属于自己的 Note 时返回 404，避免暴露资源是否存在。

#### B2. 第一个切片的验收测试

- [x] 未登录创建 Note：401。
- [x] 已登录但缺少 CSRF token：403。
- [x] 已登录且 CSRF token 有效：创建成功，返回 201。
- [x] 当前用户读取自己的 Note：200。
- [x] 用户 B 读取用户 A 的 Note：404。
- [x] 数据库中的 `user_id` 来自登录用户，而不是请求体。

#### B3. CRUD 与软删除

以下接口已经完成：

- [x] `GET /api/notes`：只列出当前用户未删除的数据，按更新时间倒序。
- [x] `PUT /api/notes/{id}`：更新时继续带所有权约束。
- [x] `DELETE /api/notes/{id}`：采用软删除；删除后详情和列表均不可见。

软删除通过新增的 `V6__add_deleted_at_to_notes.sql` 和
`V7__change_notes_deleted_at_to_timestamptz.sql` 实现，没有修改已经使用过的 V2。
当前 Note 仍只包含 `title`、`content` 等核心字段；PRD 中的 `projectId`、`summary`、`favorite`
随对应业务切片再扩展。

#### B4. 阶段 B 完成标准

- [x] 创建、详情、列表、更新、删除接口均已实现。
- [x] 写请求均经过真实 Session + CSRF 生命周期验证。
- [x] 查询、更新和删除均在 Repository seam 上约束当前用户和未删除状态。
- [x] 软删除不会暴露或返回已删除 Note。
- [x] Note Controller 集成测试覆盖 14 个场景。

### 阶段 C：前后端基础设施与首个浏览器纵向闭环

当前后端已经具备可用的 Auth/Note 行为和集成测试，但还没有机器可消费的 API 契约；前端仍缺少同源通信、类型生成、统一请求层、Server State 管理和测试基线。继续直接写页面会把 URL、DTO、错误和缓存规则散落到组件中。

本阶段先建立支撑当前 Auth/Note 切片所必需的基础设施，再由真实页面消费它。基础设施必须被当前纵向闭环验证，不提前建设通用 SDK、复杂 BFF 或与现有需求无关的平台能力。

当前技术边界固定为：

- Spring Boot REST API 是服务端行为来源，OpenAPI 是 Java/TypeScript 之间的契约，不引入 tRPC BFF。
- 前端通过 Next.js 同源 rewrite 请求 `/api/*`；部署拓扑未变化前不新增 CORS。
- 使用 `openapi-typescript` 生成类型、`openapi-fetch` 作为轻量 HTTP client，不手写一套与 Java DTO 平行的 TypeScript DTO。
- 使用 TanStack Query 管理当前用户和 Note 等 Server State；CSRF token 由独立的内存协调器管理，不进入 Query Cache 或持久化存储。
- Spring Security 继续是认证与授权边界；前端路由保护只负责用户体验。

#### C1. 建立后端 API 契约

- [ ] 选定并锁定与 Spring Boot 4.1 兼容的 `springdoc-openapi` 3.x 版本；先验证应用启动、OpenAPI 输出和后端全量测试，不使用动态 `latest` 版本。
- [ ] 从 Spring Controller、Request/Response DTO 和 Validation 约束生成 OpenAPI 3 文档。
- [ ] 显式补充自动扫描无法完整推导的安全协议：由 `LogoutFilter` 处理的 `POST /api/auth/logout`、Session Cookie、CSRF header，以及统一的 JSON 401/403 响应。
- [ ] 确保 Auth 和 Note 当前全部端点、请求体、成功响应、错误响应与状态码都进入契约；不要把“能打开 Swagger UI”当作契约完成。
- [ ] 明确生成物边界：Java 实现是行为来源，版本控制中的 OpenAPI snapshot 是跨项目契约产物，生成的 TypeScript 文件只允许由脚本更新。
- [ ] 增加可重复执行的契约导出与漂移检查命令；契约变化但 snapshot/TS 类型未更新时，检查必须失败。
- [ ] 明确 API 文档端点的环境策略：本地/测试可用于生成和检查，生产是否暴露由配置决定。

#### C2. 建立前端数据访问基础设施

- [ ] 查阅仓库内 Next.js 16 文档，使用 `next.config.ts` rewrite 将浏览器 `/api/*` 请求转发到 Spring Boot。
- [ ] 后端 origin 使用仅服务端可见的环境变量配置；组件和浏览器 bundle 不得硬编码或暴露 `localhost:8080`。
- [ ] 增加 `api:generate` 和 `api:check` 等脚本，使用 `openapi-typescript` 从受版本控制的 OpenAPI snapshot 生成 `paths` 类型。
- [ ] 使用 `openapi-fetch` 建立唯一的底层 client，统一处理同源 Session Cookie、JSON/204、`no-store`、请求取消和后端约定错误；页面不得直接散写 `fetch`。
- [ ] 建立独立的 CSRF coordinator：token 只保存在内存中；并发获取使用 single-flight；login/logout 后旧 token 必须失效；只对明确的 `CSRF_TOKEN_INVALID` 刷新并最多重试一次。
- [ ] 建立 TanStack Query Provider、集中式 query key factory 和默认重试策略；401/403 与 mutation 不得使用通用自动重试。
- [ ] 明确缓存规则：`me`、Note list/detail 属于 Server State；logout 清除用户相关 Query Cache；CSRF token、表单输入和纯 UI 状态不放入 Query Cache。
- [ ] 在领域层暴露 `authApi`、`notesApi` 及对应 query/mutation hooks；组件不感知 CSRF header、后端 origin 或原始错误解析。

#### C3. 建立跨项目质量门禁

- [ ] 为前端增加独立的 `typecheck` 命令；不能只依赖 lint 或 Next.js build 间接发现类型错误。
- [ ] 建立前端单元测试基线，覆盖 OpenAPI client 适配、错误映射、CSRF single-flight/刷新和 Query Cache 清理等纯逻辑。
- [ ] 建立浏览器 E2E 基线，用真实 Next.js rewrite、Spring Boot、Session Cookie 和 CSRF 协议验证关键路径；不能用 mock 请求代替最终验收。
- [ ] 形成统一检查顺序：OpenAPI 漂移检查 -> 前端 typecheck/test/lint/build -> 后端全量测试 -> 关键浏览器 E2E。
- [ ] 将上述命令写入仓库文档；引入 CI 时复用同一组命令，避免本地与 CI 形成两套流程。

#### C4. 前端认证闭环

- [ ] 实现 `/login` 页面。
- [ ] 登录前调用 `GET /api/auth/csrf`，使用同一个 Session 提交登录请求。
- [ ] 登录成功后使旧 token 失效、重新获取 CSRF token，再调用 `/api/auth/me` 建立前端登录态。
- [ ] 使用 `me` query 在页面刷新后恢复登录态；workspace 路由能够识别匿名状态并进入登录页。
- [ ] logout mutation 携带当前 CSRF token；成功后清理用户数据和 Query Cache 并回到登录页。
- [ ] 401、`CSRF_TOKEN_INVALID` 和普通 `ACCESS_DENIED` 在前端有不同且可验证的处理行为。

#### C5. Note 页面闭环

- [ ] `/notes`：通过 Note list query 读取并展示当前用户未删除的 Note。
- [ ] `/notes/new`：通过 create mutation 创建 Note；成功后使相关 list query 失效。
- [ ] `/notes/{id}`：通过 detail query 读取，通过 update mutation 更新，并同步 detail/list cache。
- [ ] 删除前二次确认；delete mutation 成功后移除 detail cache、刷新列表且详情不可再访问。
- [ ] 第一版使用手动保存和简单 Markdown 输入；自动保存、幂等键、保存冲突和复杂编辑器后续单独实现。

#### C6. 阶段 C 完成标准

- [ ] OpenAPI snapshot 与 Spring 实现一致，生成 TS 类型无手工修改，契约漂移检查通过。
- [ ] 浏览器可以完成 login -> me -> Note CRUD -> logout 的真实流程。
- [ ] 刷新页面后仍能通过 Session 恢复登录态；多次并发请求不会重复获取 CSRF token。
- [ ] mutation 后 Query Cache 行为符合约定；logout 后旧用户数据和旧会话均不可继续使用。
- [ ] 前端 `typecheck`、单元测试、`lint`、`build`、关键 E2E 和后端全量测试全部通过。
- [ ] 记录本地启动、环境变量、契约生成和完整验证命令。

阶段 C 完成后，进入 PRD Milestone 2 的剩余内容：Tag CRUD、Note-Tag 绑定和笔记筛选。

## 3. 未排序的后续备忘

以下事项不属于当前阶段 C，不构成下一阶段承诺。完成当前计划后，再根据实际客户端和产品需求重新排序：

- 跨源部署时的 CORS、`credentials: "include"`、Cookie `HttpOnly`/`Secure`/`SameSite` 和允许来源的联合配置。
- Email 规范化与数据库唯一性规则统一。
- PasswordEncoder 格式迁移策略。
- Project、Task、Link 以及其他业务模块。

不要提前引入自定义 JWT filter。只有出现移动端、第三方客户端、无状态部署或跨服务 token 传播等真实需求时，才重新评估 Session 与 JWT 的选择。

## 4. 换电脑后的恢复步骤

```bash
git pull
docker compose up -d
cd apps/backend
./gradlew test
```

然后依次阅读：

1. `HANDOFF.md`
2. 本文件
3. `apps/backend/src/main/java/cn/artoria/mind_forge/auth/SecurityConfig.java`
4. `apps/backend/src/main/java/cn/artoria/mind_forge/auth/AuthController.java`
5. `apps/backend/src/test/java/cn/artoria/mind_forge/auth/AuthSessionIntegrationTests.java`

恢复开发时，从本文件第一个未勾选项开始。每完成一个验收项就更新复选框，并在行为或决策变化时同步更新相关说明。
