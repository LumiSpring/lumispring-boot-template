# Project setup reference

## Artifact coordinates

The current source tree declares:

- framework parent: `com.lumispring.framework:framework-starter-parent:3.4.0.0`
- framework BOM: `com.lumispring.framework:framework-dependencies:3.4.0.0`
- Starter group: `com.lumispring.framework`
- Spring Boot: `3.4.0`
- Kotlin: `2.1.20`
- Java: `21`

Older examples may use `com.cmcoder.framework`. Do not mix groups in one POM. Verify the coordinates exposed by the actual Maven registry or checked-out template version.

## Artifact resolution prerequisite

A GitHub source repository does not make Maven artifacts resolvable. Use one of these paths:

1. Clone the template and run `mvn clean install` with JDK 21 so artifacts exist in the local Maven repository.
2. Publish the framework modules to an internal Maven repository or package registry and configure that repository in Maven settings or the consumer POM.
3. In a CI build, install/deploy the framework artifacts before building consumer services.

Do not add arbitrary repository URLs or credentials. Ask for the organization's actual registry settings when remote resolution is required.

## Mode 1: full template copy (discouraged)

Use this only when framework source must be modified together with the application.

1. Copy or clone the full repository into the requested destination.
2. Rename the root application coordinates if requested, preserving framework coordinates unless a coordinated framework migration is intended.
3. Change `application/pom.xml` from `<packaging>pom</packaging>` to `jar`.
4. Add `src/main/kotlin/<package-path>/<ApplicationName>Application.kt`.
5. Override Maven source directories if the inherited parent still points to `src/main/java`.
6. Add `application.yml`, keep security disabled until infrastructure is ready, and build with `mvn clean test`.

This mode duplicates framework code and makes future upstream updates manual.

## Mode 2A: standalone project with framework parent (recommended)

Use this when LumiSpring can be the Maven parent:

```xml
<parent>
    <groupId>com.lumispring.framework</groupId>
    <artifactId>framework-starter-parent</artifactId>
    <version>3.4.0.0</version>
    <relativePath/>
</parent>
```

The empty `relativePath` prevents Maven from accidentally resolving an unrelated local parent POM. Add project coordinates, `packaging=jar`, standard Kotlin source directories, and the required Starter dependencies.

No additional framework BOM import is needed because the parent already inherits framework dependency management.

## Mode 2B: standalone project with BOM import

Use this when the project must retain another parent or use the Spring Boot parent directly:

```xml
<properties>
    <framework.version>3.4.0.0</framework.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.lumispring.framework</groupId>
            <artifactId>framework-dependencies</artifactId>
            <version>${framework.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

`type=pom` and `scope=import` are required for a real BOM import. In this mode, ensure Kotlin compilation, the Spring plugin, Java 21, source directories, and the Spring Boot Maven plugin are configured by the retained parent or by the project.

## Minimal application class

```kotlin
package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
```

Place this under `src/main/kotlin/com/example/demo/DemoApplication.kt` and keep the package, directory, and configured main class consistent.
