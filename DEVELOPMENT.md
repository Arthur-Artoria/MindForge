# MindForge 开发环境

本文说明如何在本地启动、验证和排查 MindForge。当前推荐在 WSL 2 的 Linux 文件系统中开发，仓库路径应类似：

```text
/home/<user>/work-spaces/MindForge
```

尽量不要把长期使用的工作区放在 `/mnt/c`、`/mnt/d` 等 Windows 挂载目录中，以避免文件监听、权限、大小写和 I/O 性能差异。

## 1. 项目结构

```text
apps/backend   Spring Boot 后端
apps/web       Next.js 前端
docs           产品、设计和开发文档
docker-compose.yml
```

本地默认端口：

| 服务 | 地址 |
| --- | --- |
| Web | `http://localhost:3000` |
| Backend | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |

## 2. 环境要求

必需工具：

- WSL 2（Windows 开发环境推荐）
- Git
- Java 21
- Docker 和 Docker Compose
- Node.js 20 或更高版本
- pnpm

当前已验证的开发环境为 Java 21、Node.js 24、pnpm 11、Docker 29 和 Docker Compose 5。补丁版本不要求完全一致。

检查环境：

```bash
git --version
java -version
docker --version
docker compose version
node --version
pnpm --version
```

### SDKMAN 与非交互式 Shell

如果通过 SDKMAN 安装 Java，仅在 `.bashrc` 末尾加载 SDKMAN，非交互式登录 Shell 可能无法找到 Java。需要让 `JAVA_HOME` 和 `PATH` 在 `~/.profile` 中可用，例如：

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
export PATH="$JAVA_HOME/bin:$PATH"
```

修改后重新打开 WSL，或执行：

```bash
source ~/.profile
```

验证非交互式登录 Shell：

```bash
bash -lc 'printf "%s\n" "$JAVA_HOME"; command -v java; java -version'
```

## 3. 首次安装

在仓库根目录执行：

```bash
docker compose up -d
pnpm install --frozen-lockfile
```

确认数据库已启动：

```bash
docker compose ps
```

后端使用 Gradle Wrapper，无需单独安装 Gradle。Linux 下 wrapper 必须具有执行权限：

```bash
chmod +x apps/backend/gradlew
```

## 4. 启动开发服务

建议分别使用两个终端启动后端和前端。

后端：

```bash
cd apps/backend
./gradlew bootRun
```

前端：

```bash
pnpm --filter web dev
```

验证后端：

```bash
curl -i http://localhost:8080/hello
```

预期返回 HTTP 200。

## 5. 测试与静态检查

后端测试当前会连接本地 PostgreSQL，因此运行测试前需要先启动 Docker Compose：

```bash
docker compose up -d
cd apps/backend
./gradlew test
```

前端检查：

```bash
pnpm --filter web lint
pnpm --filter web build
```

提交代码前至少运行与改动范围对应的测试或检查。

## 6. 数据库与迁移

数据库由根目录的 `docker-compose.yml` 启动，数据库结构由 Flyway 管理：

```text
apps/backend/src/main/resources/db/migration/
```

开发规则：

- 保持 `spring.jpa.hibernate.ddl-auto=validate`。
- 不允许使用 Hibernate 自动修改数据库结构。
- 已提交且已使用的 Flyway migration 不应原地修改；数据库变化应新增 migration。
- 停止服务使用 `docker compose stop`。
- `docker compose down` 会移除容器和网络，但默认保留命名数据卷。
- 不要随意执行 `docker compose down -v`，该命令会删除本地数据库数据。

## 7. 本地配置与敏感信息

当前 `docker-compose.yml` 和 `application.properties` 中的配置仅用于本机开发。不要将真实环境凭据、访问令牌、Cookie、Session ID 或密码哈希写入代码、日志和文档。

Spring Boot 配置可以通过环境变量覆盖：

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/<database>'
export SPRING_DATASOURCE_USERNAME='<username>'
export SPRING_DATASOURCE_PASSWORD='<password>'
```

生产或共享环境必须使用外部注入的安全配置，不能复用仓库中的本地开发值。

## 8. 常见问题

### `JAVA_HOME is not set`

先检查：

```bash
printf '%s\n' "$JAVA_HOME"
command -v java
java -version
```

交互式终端可用、自动化命令不可用时，通常是 Java 只在 `.bashrc` 的交互式分支中初始化。参照上面的“SDKMAN 与非交互式 Shell”配置 `~/.profile`。

### Gradle Wrapper 不可执行

```bash
chmod +x apps/backend/gradlew
```

该执行权限应由 Git 保留，不建议通过关闭 `core.filemode` 隐藏差异。

### 后端无法连接数据库

```bash
docker compose ps
docker compose logs postgres
```

确认 PostgreSQL 状态正常，并检查端口是否被其他进程占用。输出诊断信息时不要复制敏感配置。

### Flyway 或 Hibernate 校验失败

检查 migration 是否完整执行，以及实体映射是否与数据库表名、列名一致。不要通过改成 `ddl-auto=update` 绕过问题。

### 端口被占用

```bash
ss -ltnp | grep -E ':(3000|5432|8080)\b'
```

停止冲突进程或通过本地环境配置调整端口。

## 9. 推荐的日常启动顺序

```bash
cd /path/to/MindForge
docker compose up -d
cd apps/backend && ./gradlew test
```

测试通过后，分别启动后端和前端。结束开发时可以停止应用进程，并按需执行：

```bash
docker compose stop
```

