---
name: lumispring-backend-project
description: Scaffold, migrate, configure, or extend Kotlin + Spring Boot 3 backend services that use the LumiSpring Boot template and its framework-starter modules. Use this skill whenever a user asks to create a Kotlin backend from LumiSpring, copy the lumispring-boot-template application module, configure LumiSpring Maven parent/BOM coordinates, add LumiSpring web/MySQL/Redis/security capabilities, or implement code with LumiSpring response, database, Redis, SSE, authentication, or RBAC APIs—even if they do not explicitly call it a “LumiSpring skill.”
compatibility: Requires JDK 21 and Maven 3.9+. Python 3 is optional and is only needed for the bundled scaffolder.
---

# LumiSpring Kotlin backend projects

Create production-oriented Kotlin services on top of `lumispring-boot-template`. Prefer a new standalone Maven project that consumes the published or locally installed Starter artifacts. Copy the whole template only when the user explicitly needs to modify framework source and accepts maintaining a fork.

## Start with repository truth

Before editing a project, inspect its POM and the available LumiSpring source or artifact repository. Use the coordinates found there rather than copying stale snippets. For the current `3.4.0.0` source tree, the parent, BOM, and Starter group is `com.lumispring.framework`; treat `com.cmcoder.framework` as a legacy coordinate unless the user's artifact repository proves otherwise.

Read [references/project-setup.md](references/project-setup.md) when creating or migrating a project. Read [references/capabilities.md](references/capabilities.md) when selecting a Starter, adding configuration, or implementing a feature with framework APIs.

## Gather only decisions that affect the result

Infer sensible defaults from the request and existing workspace. Ask only when a missing choice would materially change the project:

- project directory, Maven `groupId`, `artifactId`, and Kotlin base package;
- framework version and how artifacts are resolved;
- required capabilities: web, MySQL, Redis, security/RBAC;
- whether security should be active now or merely available as a dependency;
- existing infrastructure, especially MySQL and Redis.

Default to Java 21, framework `3.4.0.0`, a standalone single-module JAR, `framework-starter-web`, and `security.enabled: false`. Never invent database passwords, Redis passwords, API keys, or production endpoints.

## Choose the creation mode

1. **Standalone consumer project (recommended):** create a new Maven project and consume LumiSpring artifacts. Use the framework parent when the service has no other required parent. Use the BOM form when another parent/build convention must remain.
2. **Full template copy (discouraged):** copy the repository, keep `framework-boot`, convert `application` from `pom` to `jar`, and add the application entry point. Preserve framework modules only because the user intends to develop them locally.

Explain that a public Git repository is not automatically a Maven repository. Before building a standalone consumer, ensure `com.lumispring.framework` artifacts are either installed locally with `mvn clean install` from the template or available from a configured Maven registry.

## Scaffold a standalone project

When Python 3 is available, prefer the deterministic generator:

```bash
python <skill-dir>/scripts/scaffold.py \
  --output <project-directory> \
  --group-id <group-id> \
  --artifact-id <artifact-id> \
  --package <base-package> \
  --starter web \
  --starter redis
```

Add `--starter security --enable-security` only when MySQL, Redis, schema initialization, and credential configuration are part of the request. Use `--pom-mode bom` when the project should import the framework BOM instead of inheriting the framework parent.

If Python is unavailable, render the files in `assets/standalone/` by replacing `{{...}}` placeholders. Do not overwrite a non-empty directory without explicit authorization.

## Implement requested capabilities

Select the smallest effective dependency set:

- `framework-starter-base`: core models, helpers, caches, HTTP/WebSocket/JWT/JSON and Kotlin extensions.
- `framework-starter-web`: base + MVC/WebFlux, response wrapping, exception handling, trace IDs, SSE, validation, Knife4j, and currently MySQL transitively.
- `framework-starter-database-mysql`: MyBatis-Plus, pagination, metadata fill, JDBC/select/update extensions.
- `framework-starter-database-redis`: Redisson, Redis templates, collections, locks, Bloom filters, scripts, geo, and HyperLogLog helpers.
- `framework-starter-security`: web + MySQL + Redis, authentication endpoints, user/role/permission models, annotations, and RBAC.

Avoid redundant direct dependencies when a selected Starter already brings them transitively, but keep an explicit dependency if the project deliberately wants that capability to remain even after upstream dependency changes.

Use framework APIs rather than recreating them. Typical imports and configuration are documented in `references/capabilities.md`. Inspect the exact installed version's source before relying on an unfamiliar helper signature.

## Security guardrails

The security Starter is enabled when `security.enabled` is absent. Keep it disabled until the application has a working `DataSource`, `StringRedisTemplate`, and initialized security schema. When enabling it:

- configure secrets through environment variables or a secret manager;
- initialize `framework-starter-security/src/main/resources/db/schema.sql` deliberately;
- inspect the local schema and replace or remove its development seed administrator before the first externally reachable startup; never repeat the seed password in generated public documentation;
- do not configure the API-key bypass unless the user explicitly needs it;
- note that the current implementation still expects the application's default data source and Redis beans even when independent security connections are configured.

## Verify the result

Run checks in increasing scope and fix failures caused by the generated project:

```bash
mvn -q -DskipTests compile
mvn test
```

If framework artifacts cannot be resolved, diagnose artifact availability first; do not paper over the problem by changing group IDs or versions. If infrastructure is unavailable, verify with security disabled and clearly report which integration checks remain.

Before handing off, confirm:

- the project uses JDK 21 and Kotlin sources compile from `src/main/kotlin`;
- the application class package matches its directory and configured main class;
- POM coordinates consistently use the verified LumiSpring group;
- no credentials or local-only endpoints were committed as production defaults;
- the selected Starter capabilities are demonstrated by a minimal, relevant example;
- generated instructions state how to resolve LumiSpring artifacts and run the service.

## Delivery summary

Report the chosen creation mode, generated/changed files, enabled Starters, security state, verification commands and results, plus any remaining MySQL/Redis or artifact-registry prerequisites.
