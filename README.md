# LumiSpring Boot Template

[中文](README.md) | [English](README.en.md)

基于 **Kotlin** 与 **Spring Boot** 的后端框架模板。把通用能力拆成可安装的 Maven Starter，业务项目可以直接依赖，也可以在本仓库里继续改框架源码。

当前版本：框架 `4.1.1.0` · Spring Boot `4.1.1` · Kotlin `2.4.20` · Java `21`

## 功能概览

| Starter | 说明 |
| --- | --- |
| `framework-starter-base` | 统一响应模型、JSON/HTTP/JWT/加解密、校验、缓存、责任链与策略等扩展 |
| `framework-starter-web` | Spring MVC、统一响应包装、全局异常、链路 Trace、SSE、校验、springdoc OpenAPI |
| `framework-starter-database-mysql` | MyBatis-Plus、分页、字段自动填充、JDBC/SQL 扩展 |
| `framework-starter-database-redis` | RedisTemplate、Redisson、分布式锁、布隆过滤器、集合与 Geo 等操作 |
| `framework-starter-security` | 登录注册、Token、用户/角色/权限与 RBAC 注解 |

`framework-starter-web` 当前会传递依赖 MySQL；`framework-starter-security` 会带上 Web、MySQL 和 Redis。

## 仓库结构

```
lumispring-boot-template
├── framework-boot/                 # 框架源码与 BOM
│   ├── framework-dependencies/     # 版本与依赖管理（BOM）
│   └── framework-starter/          # 各 Starter 模块
├── application/                    # 业务应用占位（当前 packaging 为 pom）
├── dependencies/                   # 应用侧依赖管理
└── skills/                         # Agent 脚手架与能力说明
```

框架 Kotlin 源码在 `src/main/java`（由父 POM 指定），不是 `src/main/kotlin`。

## 环境要求

- JDK 21（不要用 JDK 17 编译）
- Maven 3.9+
- 使用 Security 时需要 MySQL 与 Redis

本机默认 `java` 若仍是 17，请先指定 JDK 21，例如：

```bash
export JAVA_HOME="/path/to/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

## 本地安装框架构件

GitHub 源码仓库**不是** Maven 仓库。消费方要能解析 `com.lumispring.framework`，需要先在本机安装，或发布到私有仓库。

```bash
mvn -DskipTests clean install
```

成功后，构件会出现在本地 Maven 仓库，坐标为：

- Parent：`com.lumispring.framework:framework-starter-parent:4.1.1.0`
- BOM：`com.lumispring.framework:framework-dependencies:4.1.1.0`

## 业务项目怎么用

推荐新建独立 Maven 工程，只依赖已安装的 Starter，不要整仓复制（除非你要改框架源码）。

### 方式 A：继承框架 Parent（推荐）

```xml
<parent>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-parent</artifactId>
    <version>4.1.1.0</version>
    <relativePath/>
</parent>
```

`relativePath` 留空，避免误用本地无关的 parent。

### 方式 B：只导入 BOM

已有其他 Parent 时使用：

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

按需添加 Starter：

```xml
<dependency>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-web</artifactId>
</dependency>
```

没有基础设施时，先关闭安全模块：

```yaml
security:
  enabled: false
```

启用安全前请准备好默认数据源和 Redis，并执行 `framework-starter-security/src/main/resources/db/schema.sql`。内置管理员账号仅供本地开发，上线前必须替换或删除，不要把种子密码写进公开文档。

OpenAPI 文档默认在 `/swagger-ui.html`。

## 在本仓库里跑业务应用

`application` 目前是 `pom` 打包、没有启动类。若要在本仓库启动服务：

1. 把 `application/pom.xml` 的 `<packaging>` 改为 `jar`
2. 增加启动类与 `application.yml`
3. 再执行 `mvn -DskipTests package` 或在 IDE 中运行

更完整的脚手架说明见 [`skills/lumispring-backend-project`](skills/lumispring-backend-project/SKILL.md)。

## 许可证

[MIT](LICENSE) © 2026 LumiSpring contributors
