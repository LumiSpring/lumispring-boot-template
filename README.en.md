# LumiSpring Boot Template

[中文](README.md) | [English](README.en.md)

A **Kotlin** + **Spring Boot** backend framework template. Shared capabilities are packaged as installable Maven Starters so application projects can depend on them, or you can keep developing the framework in this repository.

Current versions: framework `4.1.1.0` · Spring Boot `4.1.1` · Kotlin `2.4.20` · Java `21`

## Features

| Starter | What it provides |
| --- | --- |
| `framework-starter-base` | Unified response models, JSON/HTTP/JWT/crypto helpers, validation, cache, chain and strategy utilities |
| `framework-starter-web` | Spring MVC, response wrapping, global exception handling, trace IDs, SSE, validation, springdoc OpenAPI |
| `framework-starter-database-mysql` | MyBatis-Plus, pagination, auto-fill, JDBC/SQL extensions |
| `framework-starter-database-redis` | RedisTemplate, Redisson, distributed locks, Bloom filters, collections, and geo helpers |
| `framework-starter-security` | Auth annotations, `AuthPrincipal`, and `AuthenticationResolver` SPI |
| `framework-starter-security-rbac` | Built-in login/register, tokens, user/role/permission tables and admin APIs |

`framework-starter-web` currently brings MySQL transitively. `framework-starter-security` depends on web only. `framework-starter-security-rbac` brings the security kernel, MySQL, and Redis.

## Repository layout

```
lumispring-boot-template
├── framework-boot/                 # Framework sources and BOM
│   ├── framework-dependencies/     # Version and dependency management (BOM)
│   └── framework-starter/          # Starter modules
├── application/                    # Application placeholder (packaging is pom today)
├── dependencies/                   # Application-side dependency management
└── skills/                         # Agent scaffolder and capability notes
```

Framework Kotlin sources live under `src/main/java` (set by the parent POM), not `src/main/kotlin`.

## Requirements

- JDK 21 (do not compile with JDK 17)
- Maven 3.9+
- MySQL and Redis when Security is enabled

If the default `java` on the machine is still 17, point at JDK 21 first:

```bash
export JAVA_HOME="/path/to/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

## Install framework artifacts locally

A GitHub source repository is **not** a Maven repository. Consumers can resolve `com.lumispring.framework` only after a local install or a publish to a private registry.

```bash
mvn -DskipTests clean install
```

After a successful install, the coordinates are:

- Parent: `com.lumispring.framework:framework-starter-parent:4.1.1.0`
- BOM: `com.lumispring.framework:framework-dependencies:4.1.1.0`

## Use it from an application project

Prefer a new standalone Maven project that depends on the installed Starters. Copy this whole repository only when you intend to change framework source.

### Option A: inherit the framework parent (recommended)

```xml
<parent>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-parent</artifactId>
    <version>4.1.1.0</version>
    <relativePath/>
</parent>
```

Keep `relativePath` empty so Maven does not pick up an unrelated local parent.

### Option B: import the BOM only

Use this when another parent must stay in place:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.lumispring.framework</groupId>
            <artifactId>framework-dependencies</artifactId>
            <version>4.1.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Add Starters as needed:

```xml
<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-web</artifactId>
</dependency>
```

Disable security while you have no infrastructure:

```yaml
security:
  enabled: false
```

Before enabling the bundled RBAC, provide a default `DataSource` and Redis, then apply `framework-starter-security-rbac/src/main/resources/db/schema.sql`. The bundled administrator is for local development only. Replace or remove it before any reachable deployment, and do not publish the seed password. If you only need the annotations and will implement login yourself, depend on `framework-starter-security` and provide an `AuthenticationResolver`.

OpenAPI UI is available at `/swagger-ui.html` by default.

## Run an application inside this repository

`application` is currently packaged as `pom` and has no entry point. To start a service here:

1. Change `<packaging>` in `application/pom.xml` to `jar`
2. Add a Spring Boot application class and `application.yml`
3. Run `mvn -DskipTests package` or launch from the IDE

See [`skills/lumispring-backend-project`](skills/lumispring-backend-project/SKILL.md) for the full scaffolding notes.

## License

[MIT](LICENSE) © 2026 LumiSpring contributors
