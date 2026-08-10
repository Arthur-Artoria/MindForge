# MindForge 后端开发计划

> 更新日期：2026-08-10
>
> 当前方向：先收尾 Session 认证安全生命周期，再实现 Note CRUD。
>
> 产品范围以 `docs/developer-knowledge-hub-prd.md` 和 `docs/design.md` 为准。

## 1. 当前状态

已实现：

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`（由 Spring Security `LogoutFilter` 处理）
- Session 中持久化和恢复 `SecurityContext`
- JSON 401 响应和错误凭据的统一错误响应

已有测试：

- [x] 正确登录后，使用同一个 Session 请求 `/api/auth/me` 成功
- [x] 未登录请求 `/api/auth/me` 返回 JSON 401
- [x] 错误凭据返回通用 JSON 401
- [ ] logout 后，旧 Session 请求 `/api/auth/me` 返回 401

当前临时安全配置：

```java
.csrf(csrf -> csrf
    .ignoringRequestMatchers("/api/auth/login", "/api/auth/logout"))
```

这个规则只用于当前开发阶段，不是最终的浏览器安全方案。

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

这条自定义认证路径尚未调用 `SessionAuthenticationStrategy`。

- [ ] 在认证成功时执行适当的 `SessionAuthenticationStrategy`。
- [ ] 保留默认的 session fixation protection，优先使用 Servlet 容器的 `changeSessionId` 行为。
- [ ] 增加测试：如果登录前已有 Session，登录成功后 Session ID 必须发生变化，同时认证状态仍可由 `/me` 读取。
- [ ] 在继续使用 controller 登录与改成 Spring Security filter 登录之间做一次明确选择；当前可以先用最小改动补齐生命周期，不必为了重构阻塞功能开发。

#### A3. 完成 SPA CSRF 流程

- [ ] 增加允许匿名访问的 token 获取接口，例如 `GET /api/auth/csrf`。
- [ ] 移除 login/logout 的 CSRF ignore 规则。
- [ ] login 和 logout 必须携带有效 CSRF token。
- [ ] 登录成功和退出成功后重新获取 token；旧 token 不得继续使用。
- [ ] 为缺失或错误 token 的请求返回稳定的 JSON 403。

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

- [ ] login、me、logout 完整生命周期测试通过。
- [ ] session fixation protection 测试通过。
- [ ] login、logout 和至少一个业务写接口不再绕过 CSRF。
- [ ] 401 与 403 都返回约定的 JSON 错误结构。
- [ ] 删除 `AuthController` 中的 `System.out.println`。

### 阶段 B：Note 第一个纵向切片

认证安全收尾后，不继续扩展 Auth，直接实现：

```text
POST /api/notes
GET  /api/notes/{id}
```

#### B1. Module 与所有权规则

- [ ] 请求 DTO 不允许接收 `userId`。
- [ ] `userId` 只能从后端 `AuthenticatedUser` / `SecurityContext` 推导。
- [ ] Note 查询在 Repository seam 上同时约束 `noteId` 和 `userId`，例如 `findByIdAndUserId(...)`。
- [ ] 其他用户访问不属于自己的 Note 时返回 404，避免暴露资源是否存在。

#### B2. 第一个切片的验收测试

- [ ] 未登录创建 Note：401。
- [ ] 已登录但缺少 CSRF token：403。
- [ ] 已登录且 CSRF token 有效：创建成功，返回 201。
- [ ] 当前用户读取自己的 Note：200。
- [ ] 用户 B 读取用户 A 的 Note：404。
- [ ] 数据库中的 `user_id` 来自登录用户，而不是请求体。

#### B3. 后续 CRUD 顺序

第一个切片稳定后依次实现：

1. `GET /api/notes`：只列出当前用户的数据。
2. `PUT /api/notes/{id}`：更新时继续带所有权约束。
3. `DELETE /api/notes/{id}`：先确定硬删除或软删除语义。

当前 `V2__create_notes_table.sql` 只有 `title`、`content` 等核心字段，而 PRD 还包含 `projectId`、`summary`、`favorite`、`deletedAt`。第一阶段按现有核心字段实现；在实现 DELETE 前必须明确删除语义。如果选择软删除，应新增 Flyway migration，不能修改已经使用过的 V2。

## 3. 未排序的后续备忘

以下事项不属于当前已经排期的 Auth 与 Note CRUD 计划，不构成下一阶段承诺。完成当前计划后，再根据实际客户端和产品需求重新排序：

- Next.js 登录态接入。
- CORS、`credentials: "include"`、Cookie `HttpOnly`/`Secure`/`SameSite` 和允许来源的联合配置。
- Email 规范化与数据库唯一性规则统一。
- PasswordEncoder 格式迁移策略。
- Tag、NoteTag 以及其他业务模块。

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
