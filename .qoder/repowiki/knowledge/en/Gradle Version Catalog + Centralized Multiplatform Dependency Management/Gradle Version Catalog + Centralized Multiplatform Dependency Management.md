---
kind: dependency_management
name: Gradle Version Catalog + Centralized Multiplatform Dependency Management
category: dependency_management
scope:
    - '**'
source_files:
    - gradle/libs.versions.toml
    - settings.gradle.kts
    - build.gradle.kts
    - composeApp/build.gradle.kts
---

## Approach

The project is a Kotlin Multiplatform Compose application built with Gradle. Dependency management is centralized through the **Gradle Version Catalog** (`gradle/libs.versions.toml`) and configured via `settings.gradle.kts` for repository resolution.

## Key Files

- `gradle/libs.versions.toml` — single source of truth for all versions, library coordinates, and plugin aliases.
- `settings.gradle.kts` — declares repositories (`google()`, `mavenCentral()`, `gradlePluginPortal()` in `pluginManagement`; `google()`, `mavenCentral()` in `dependencyResolutionManagement`) and includes the `:composeApp` module.
- `build.gradle.kts` (root) — registers plugins with `apply false` so they are declared centrally but applied per-module.
- `composeApp/build.gradle.kts` — applies the KMP/Android/Compose plugins and consumes dependencies via `libs.<alias>` references from the catalog.
- `gradle.properties` and `gradle/wrapper/` — Gradle wrapper and JVM toolchain configuration.

## Architecture & Conventions

1. **Version Catalog as the single source of truth.** All third-party versions live under `[versions]` in `gradle/libs.versions.toml`: AGP `8.7.3`, Kotlin `2.1.20`, Compose Multiplatform `1.8.0`, AndroidX Activity `1.10.1`, Ktor `3.1.3`, Coroutines `1.10.2`. Library coordinates are defined under `[libraries]` using `version.ref = "..."` to pin versions by name rather than inline strings.

2. **Plugin aliases are also version-cataloged.** The root `build.gradle.kts` uses `alias(libs.plugins.androidApplication)`, `alias(libs.plugins.composeMultiplatform)`, `alias(libs.plugins.composeCompiler)`, `alias(libs.plugins.kotlinMultiplatform)` — all resolved from the `[plugins]` section of the catalog. This prevents version drift between modules.

3. **Per-target dependency scoping follows KMP conventions.** In `composeApp/build.gradle.kts`, dependencies are split across `commonMain`, `androidMain`, and `iosMain` source sets:
   - `commonMain` pulls shared UI (`compose.runtime`, `compose.foundation`, `compose.material3`, `compose.ui`) and Ktor client core.
   - `androidMain` adds `ktor-client-okhttp` and `androidx.activity.compose`.
   - `iosMain` adds `ktor-client-darwin`.
   - `commonTest` adds `kotlin-test`, `ktor-client-mock`, and `kotlinx-coroutines-test`.

4. **Repository policy is centralized in `settings.gradle.kts`.** Plugin resolution goes through `google()`, `mavenCentral()`, `gradlePluginPortal()`; runtime dependency resolution uses `google()` and `mavenCentral()` only. No private registry or `GOPRIVATE` is configured — this is a pure Kotlin/Gradle project, not a Go project.

5. **JVM toolchain is managed via the Foojay resolver convention.** `settings.gradle.kts` applies `org.gradle.toolchains.foojay-resolver-convention` version `1.0.0` and configures `compileSdk = 35`, `minSdk = 24`, `targetSdk = 35`, `JvmTarget.JVM_17`, and Java compatibility at `VERSION_17`.

6. **No vendoring or lockfile beyond Gradle's own cache.** There is no `go.mod`, `package.json`, `vendor/`, or equivalent. Dependencies are downloaded from Maven Central / Google Maven at build time.

## Conventions & Constraints

- Versions are never written inline in `*.gradle.kts` files; every dependency reference goes through `libs.<alias>` (enforced by the project's use of the version catalog throughout).
- Plugins are declared centrally in the root `build.gradle.kts` with `apply false` and re-applied per module via `alias(libs.plugins.<id>)`.
- Only public Maven registries (`google()`, `mavenCentral()`, `gradlePluginPortal()`) are allowed — no private Maven/GitHub Packages or GOPRIVATE-style configuration exists.
- Android targets compile against API level 35 with a minimum SDK of 24 and require JDK 17.