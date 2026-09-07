# LumiSpring capability reference

Use this guide to select and demonstrate existing framework features. Confirm signatures against the consumed version when writing non-trivial code.

## Web and unified responses

Dependency:

```xml
<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-web</artifactId>
</dependency>
```

The Starter provides Spring MVC/WebFlux, AOP, validation, Knife4j, MySQL support, global exception handling, request trace IDs, SSE, and automatic response wrapping. Controller methods can return domain data directly:

```kotlin
@RestController
@RequestMapping("/api/todos")
class TodoController {
    @GetMapping
    fun list() = listOf(mapOf("id" to 1, "title" to "Learn LumiSpring"))
}
```

Use `com.lumispring.framework.web.model.IgnoreResponse` on a controller or method that must bypass wrapping. Do not manually wrap every result unless the endpoint needs explicit pagination or status semantics.

Knife4j is normally available at `/doc.html` after the web application starts.

## Base utilities

`framework-starter-base` includes:

- `Response`, `Code`, `Page`, and framework exception models;
- JSON/object conversion, HTTP client, JWT, encryption/encoding, validation, date, collection, number, string, file, image, HTML, logging, threading, and WebSocket extensions;
- cache, chain-of-responsibility, and strategy helpers;
- application-context bean access helpers.

Prefer focused imports from `com.lumispring.framework.base.*`. Avoid wildcard imports in generated production code. Check null/error behavior in source before using conversion or crypto helpers on untrusted input.

## MySQL and MyBatis-Plus

Dependency (already transitive from web/security in the current version):

```xml
<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-database-mysql</artifactId>
</dependency>
```

Configure the standard Spring Boot data source:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/app?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

The Starter configures MyBatis-Plus pagination and metadata filling and exposes extensions in `com.lumispring.framework.database.mysql.extension`, including:

- `IPage<T>.toResponse()` for paginated framework responses;
- `selectSqlMaps`, `selectSqlOneMap`, `selectSqlObjs<T>`, and `selectSqlOneObj<T>` for named-parameter JDBC queries;
- common update and JDBC helper functions.

Prefer Mapper/Service abstractions for business persistence. Use raw SQL helpers only for focused queries with parameters; never interpolate user input into SQL.

## Redis

Dependency:

```xml
<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-database-redis</artifactId>
</dependency>
```

Configuration:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

Use `com.lumispring.framework.database.redis.core.RedisClient` for strings, lists, hashes, sets, sorted sets, distributed locks, Bloom filters, Lua scripts, HyperLogLog, and geo operations:

```kotlin
RedisClient.set("todo:1", todo, timeout = 600)
val todo = RedisClient.get<Todo>("todo:1")
RedisClient.lock("todo:refresh") { refreshTodos() }
```

Use namespaced keys, bounded TTLs for caches, and `tryLock` where unbounded waiting is inappropriate.

## Security and RBAC

Dependency:

```xml
<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-security</artifactId>
</dependency>
```

Security is enabled by default (`matchIfMissing=true`). Keep it explicitly disabled while scaffolding without infrastructure:

```yaml
security:
  enabled: false
```

When enabling it, configure a default Spring `DataSource` and `StringRedisTemplate`, then initialize the schema shipped at `framework-starter-security/src/main/resources/db/schema.sql`. The current implementation supports optional independent connections at `spring.datasource.security.*` and `security.redis.*`, but still requires default application data source and Redis beans.

Shared infrastructure example:

```yaml
security:
  enabled: true
  timeout: 604800
  api-header: security-key
  # api-key: ${SECURITY_API_KEY} # omit unless a trusted bypass is explicitly required
```

Use these annotations:

```kotlin
import com.lumispring.framework.security.config.annotation.RequireAdmin
import com.lumispring.framework.security.config.annotation.RequireLogin
import com.lumispring.framework.security.config.annotation.RequireRole
import com.lumispring.framework.security.config.annotation.UnAuth
```

- `@RequireLogin`: authenticated user required.
- `@RequireAdmin`: administrator required.
- `@RequireRole("editor")`: any listed role by default.
- `@RequireRole("editor", "reviewer", mode = RequireRole.RoleCheckMode.ALL)`: all listed roles required.
- `@UnAuth`: bypass normal authentication for a public endpoint; use sparingly.

Current-user helpers are in `com.lumispring.framework.security.extension`: `currentUser`, `currentUserId`, `currentUsername`, `currentUserRoles`, `currentToken`, and `isAdmin`.

Built-in endpoints include login/register/logout, user/password operations, and admin APIs for users, roles, and permissions. Treat registration openness and the API-key bypass as policies to review before production.

The bundled schema contains a development seed administrator. Inspect it locally, replace or remove the seed before any externally reachable deployment, and do not repeat its password in generated public documentation.

## Dependency selection matrix

| Need | Direct Starter |
| --- | --- |
| utilities only | `framework-starter-base` |
| HTTP API, response wrapping, validation, docs, SSE | `framework-starter-web` |
| MyBatis-Plus without web | `framework-starter-database-mysql` |
| Redis/Redisson without security | `framework-starter-database-redis` |
| built-in auth and RBAC | `framework-starter-security` |

In version `3.4.0.0`, security brings web, MySQL, and Redis; web brings MySQL. Re-check transitive dependencies after framework upgrades.
