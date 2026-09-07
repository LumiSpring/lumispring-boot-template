# LumiSpring Boot Template

一个基于 **Kotlin + Spring Boot** 的多模块后端开发模板。项目将常用基础能力封装成可组合的 Spring Boot Starter，涵盖统一响应、异常处理、MyBatis-Plus、Redis、用户认证与 RBAC 权限等功能，适合用作新服务的基础工程或内部 Starter 的开发起点。

> 当前仓库中的 `application` 是预留的业务聚合模块，尚未包含 `@SpringBootApplication` 启动类。因此仓库可以完整编译，但不是克隆后即可直接运行的成品服务。你可以在 `application` 中补充业务代码和启动入口，或将各 Starter 引入现有 Spring Boot 项目。

## 特性

- Kotlin 2.1 与 Java 21
- Spring Boot 3.4 多模块 Maven 工程
- Spring Boot 自动配置与可组合 Starter
- 统一 API 响应、全局异常处理和请求 Trace ID
- Servlet Web、WebFlux、SSE 与 HTTP/WebSocket 工具支持
- MyBatis-Plus、分页、字段自动填充与常用数据库扩展
- 基于 Redisson 的 Redis 客户端封装
- 用户、角色、权限与操作日志模型
- 注解式登录、管理员和角色权限校验
- 独立或共享的安全模块 MySQL / Redis 数据源
- Knife4j / OpenAPI 接口文档支持
- 常用 Kotlin 扩展、缓存、加解密、JWT、JSON、日期和集合工具

## 技术栈

| 技术 | 版本 |
| --- | --- |
| Java | 21 |
| Kotlin | 2.1.20 |
| Spring Boot | 3.4.0 |
| Maven | 3.9+ |
| MyBatis-Plus | 3.5.9 |
| Redisson | 3.27.2 |
| Knife4j | 4.5.0 |
| Hutool | 5.8.44 |

具体依赖版本以 [`framework-dependencies/pom.xml`](framework-boot/framework-dependencies/pom.xml) 为准。

## 项目结构

```text
lumispring-boot-template/
├── application/                       # 预留业务聚合模块（当前无启动类）
├── dependencies/                      # 应用侧依赖版本管理
├── framework-boot/
│   ├── framework-dependencies/        # 框架 BOM 与第三方依赖版本
│   └── framework-starter/
│       ├── framework-starter-parent/  # Starter 公共 Maven 配置
│       ├── framework-starter-base/    # 基础模型、扩展、缓存与工具
│       ├── framework-starter-web/     # Web、统一响应、异常与 SSE
│       ├── framework-starter-database/
│       │   ├── framework-starter-database-mysql/
│       │   └── framework-starter-database-redis/
│       └── framework-starter-security/ # 认证、用户、角色与权限
└── pom.xml                             # 根聚合工程
```

### 模块说明

| 模块 | 作用 |
| --- | --- |
| `framework-starter-base` | 响应与异常模型、设计模式封装，以及 HTTP、WebSocket、JWT、JSON、加密、日期、集合等 Kotlin 扩展。 |
| `framework-starter-web` | Web 自动配置、统一响应包装、全局异常处理、请求上下文、Trace ID、Controller AOP 与 SSE 服务。 |
| `framework-starter-database-mysql` | MyBatis-Plus 配置、分页、字段自动填充及常用查询/更新扩展。 |
| `framework-starter-database-redis` | 基于 Redisson 与 `RedisTemplate` 的 Redis 操作封装。 |
| `framework-starter-security` | Token 登录态、用户/角色/权限管理、权限注解、独立数据源与 Redis 配置。 |
| `dependencies` | 应用层依赖管理，统一导入框架与 Hutool 版本。 |
| `application` | 新业务的落点；当前仅声明基础依赖，需要自行添加启动类和配置。 |

## 环境要求

- JDK 21
- Maven 3.9 或更高版本
- MySQL 8.x（使用数据库或安全模块时）
- Redis（使用 Redis 或安全模块登录态时）

确认本地工具链：

```bash
java -version
mvn -version
```

`java -version` 和 Maven 输出中的 Java 运行时都应为 21。

## 构建项目

```bash
git clone <repository-url>
cd lumispring-boot-template
mvn clean test
```

安装所有 Starter 到本地 Maven 仓库：

```bash
mvn clean install
```

当前源码可以在 JDK 21 下通过完整 Reactor 构建。仓库暂未提供自动化测试用例，`mvn test` 主要验证各模块能够从源码完成编译和装配。

## 创建可运行应用

`application` 当前的 `packaging` 为 `pom`。若要直接在该模块中开发服务，请先将其改为 `jar`：

```xml
<artifactId>application</artifactId>
<packaging>jar</packaging>
```

然后在 `application/src/main/kotlin/com/lumispring/app/LumiSpringApplication.kt` 新增启动类，例如：

```kotlin
package com.lumispring.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LumiSpringApplication

fun main(args: Array<String>) {
    runApplication<LumiSpringApplication>(*args)
}
```

在 `application/src/main/resources/application.yml` 添加最小配置：

```yaml
spring:
  application:
    name: lumispring-application

security:
  enabled: false
```

安全模块默认启用，并依赖可用的数据源和 Redis。尚未准备这些基础设施时，可先设置 `security.enabled: false`。

启动应用：

```bash
mvn -pl application -am spring-boot:run
```

如需快速验证运行状态，可在 `application/src/main/kotlin/com/lumispring/app/HelloController.kt` 添加：

```kotlin
package com.lumispring.app

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping("/api/hello")
    fun hello() = mapOf("message" to "Hello, LumiSpring!")
}
```

启动后访问 `http://localhost:8080/api/hello`，或打开 Knife4j 默认页面 `http://localhost:8080/doc.html`。

## 在其他项目中使用 Starter

先执行 `mvn clean install`，再按需添加依赖：

```xml
<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-web</artifactId>
    <version>3.4.0.0</version>
</dependency>
```

可选模块：

```xml
<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-base</artifactId>
    <version>3.4.0.0</version>
</dependency>

<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-database-mysql</artifactId>
    <version>3.4.0.0</version>
</dependency>

<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-database-redis</artifactId>
    <version>3.4.0.0</version>
</dependency>

<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-security</artifactId>
    <version>3.4.0.0</version>
</dependency>
```

这些组件通过 `AutoConfiguration.imports` 注册自动配置，通常不需要手动 `@Import`。

## 安全模块配置

安全模块支持复用应用默认的 MySQL / Redis，也可以优先使用独立连接。请通过环境变量或密钥管理服务注入真实凭据，不要将密码提交到仓库。

共享连接的最小示例：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/app
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  data:
    redis:
      host: localhost
      port: 6379

security:
  enabled: true
  # 单位为秒，默认 7 天。
  timeout: 604800
  # 可选：配置后，请求携带对应请求头可跳过用户验证。
  # 不需要旁路能力时不要配置；空值会禁用该能力。
  api-header: security-key
  # api-key: ${SECURITY_API_KEY}
```

若安全数据需要独立存储，可在保留上述默认连接的基础上追加：

```yaml
spring:
  datasource:
    security:
      jdbc-url: jdbc:mysql://localhost:3306/security
      username: ${SECURITY_DB_USERNAME}
      password: ${SECURITY_DB_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver

security:
  redis:
    host: localhost
    port: 6379
    database: 0
```

配置 `spring.datasource.security.jdbc-url` 后，安全模块优先使用独立 MySQL；配置 `security.redis.host` 后，优先使用独立 Redis。按当前实现，即使启用独立连接，应用默认的 `DataSource` 与 `StringRedisTemplate` Bean 仍需存在。

表结构位于：

```text
framework-boot/framework-starter/framework-starter-security/src/main/resources/db/schema.sql
```

执行脚本前请先创建数据库。例如在 Bash 中初始化 `app` 数据库：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS app CHARACTER SET utf8mb4;"
mysql -u root -p app < framework-boot/framework-starter/framework-starter-security/src/main/resources/db/schema.sql
```

脚本包含用于本地体验的默认管理员账号 `admin / admin123`。生产部署必须在首次对外启动前替换或移除该种子账号，不要留下可被利用的默认凭据。

### 权限注解

- `@RequireLogin`：要求用户已登录
- `@RequireAdmin`：要求管理员身份
- `@RequireRole`：要求指定角色
- `@UnAuth`：跳过认证检查

### 内置接口

启用安全模块后会注册认证及后台管理接口，包括：

- `/login`、`/register`、`/logout`
- `/user`、`/password`
- `/admin/api/user/**`
- `/admin/api/role/**`
- `/admin/api/permission/**`

登录成功后，从响应中获取访问 Token，并在需要登录态的请求中携带 `Authorization: Bearer <token>`。具体请求参数、公开接口范围和返回结构请以 Controller 源码及 Knife4j 页面生成的 OpenAPI 文档为准；部署前应再次审核注册、权限与 API Key 策略。

## 统一响应

`framework-starter-web` 会自动包装大多数 Controller 返回值，并附加 Trace ID。已经是统一响应、`ResponseEntity`、`SseEmitter`、`Flux` 或 `ModelAndView` 的结果不会被重复包装。可在方法或 Controller 上使用 `@IgnoreResponse` 跳过包装。

典型响应结构：

```json
{
  "code": "200",
  "message": "success",
  "data": {},
  "traceId": "...",
  "success": true
}
```

字段名称与成功码以 [`Response.kt`](framework-boot/framework-starter/framework-starter-base/src/main/java/com/cmcoder/framework/base/model/Response.kt) 和 [`Code.kt`](framework-boot/framework-starter/framework-starter-base/src/main/java/com/cmcoder/framework/base/model/Code.kt) 的当前实现为准。

## 开发注意事项

- 源文件目录仍保留历史路径 `com/cmcoder`，Kotlin 包名已经统一为 `com.lumispring`。IDE 和编译器可以正常处理，但建议后续逐步对齐目录与包名。
- `framework-starter-web` 会传递引入 MySQL Starter；如果希望更轻量，可在业务项目中按需排除相关依赖。
- 安全模块默认开启，且内置 API Key 旁路能力。生产环境应遵循最小权限原则，不需要时不要配置 `security.api-key`。
- SQL 初始化脚本不会由当前模板自动执行，请根据部署流程手动执行或接入 Flyway / Liquibase。
- 当前没有单元测试或集成测试，提交新功能时建议同步补充测试。
- 构建时存在少量弃用 API 与未检查类型转换警告，不影响当前编译，但升级 Java、Kotlin 或 Spring Boot 前应优先处理。

## 贡献

欢迎提交 Issue 和 Pull Request。建议在提交前运行：

```bash
mvn clean test
```

## 许可证

本项目基于 [MIT License](LICENSE) 开源。
