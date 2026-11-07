---
kind: build_system
name: Gradle Multiplatform Build & CI Pipeline
category: build_system
scope:
    - '**'
source_files:
    - build.gradle.kts
    - composeApp/build.gradle.kts
    - settings.gradle.kts
    - gradle/libs.versions.toml
    - gradle.properties
    - .github/workflows/build.yml
---

## What system/approach is used

The project uses **Gradle (Kotlin DSL)** as the sole build system, configured for a **Kotlin Multiplatform Compose** application targeting Android and iOS. Dependency versions are centrally managed via Gradle's version catalog (`gradle/libs.versions.toml`), and plugin versions are resolved through `pluginManagement` in `settings.gradle.kts`. Continuous integration runs on GitHub Actions with two parallel jobs — one on Ubuntu for Android builds and one on macOS for iOS framework linking.

## Key files and packages

- `build.gradle.kts` — root build script declaring plugins with `apply false`; no top-level tasks.
- `composeApp/build.gradle.kts` — only Gradle module; declares Kotlin multiplatform targets, source sets, dependencies, and Android configuration.
- `settings.gradle.kts` — defines root project name `url-expander`, includes the single `:composeApp` module, configures repositories (`google()`, `mavenCentral()`, `gradlePluginPortal()`), and applies the `org.gradle.toolchains.foojay-resolver-convention` plugin (v1.0.0) to auto-resolve JDK toolchains.
- `gradle/libs.versions.toml` — version catalog centralizing all dependency and plugin versions.
- `gradle.properties` — JVM args, caching, code style, AndroidX flags.
- `.github/workflows/build.yml` — CI pipeline with `android` and `ios` jobs.
- `gradle/gradle-daemon-jvm.properties` — Gradle daemon JVM settings.

## Architecture and conventions

### Multiplatform target layout

`composeApp/build.gradle.kts` configures:
- `androidTarget` with `JvmTarget.JVM_17`.
- Three iOS targets: `iosX64`, `iosArm64`, `iosSimulatorArm64`, each producing a **static** framework named `ComposeApp` (`isStatic = true`).

Source sets follow the standard KMP convention:
- `commonMain` — shared UI (Compose runtime, foundation, material3, ui) plus Ktor client core.
- `androidMain` — adds `androidx.activity.compose` and `ktor-client-okhttp`.
- `iosMain` — adds `ktor-client-darwin`.
- `commonTest` — uses `kotlin-test`, `ktor-client-mock`, and `kotlinx-coroutines-test`.

### Version management

All versions live in `gradle/libs.versions.toml` under `[versions]` and are referenced via `version.ref = ...` in `[libraries]` and `[plugins]`. The root `build.gradle.kts` references plugins exclusively through `alias(libs.plugins.*)` with `apply false`, deferring actual application to `composeApp/build.gradle.kts`. This keeps the root script minimal and avoids accidental plugin application at the wrong scope.

### Android configuration

Android-specific settings are in `composeApp/build.gradle.kts`:
- `namespace = "com.brunoshiroma.url_expander"`
- `compileSdk = 35`, `minSdk = 24`, `targetSdk = 35`
- `applicationId = "com.brunoshiroma.url_expander"`
- `versionCode = 1`, `versionName = "1.0.0"`
- Java/Kotlin compatibility set to `VERSION_17` / `JVM_17`.
- `release` build type reuses the debug signing config (`signingConfig = signingConfigs.getByName("debug")`) — release artifacts are signed with the debug key until a real keystore is configured.

### Dependency resolution

Repositories are declared twice: once in `pluginManagement` (for Gradle plugins from `gradlePluginPortal()`) and once in `dependencyResolutionManagement` (for app dependencies from `google()` and `mavenCentral()`). The `foojay-resolver-convention` plugin is applied in `settings.gradle.kts` to automatically provision JDK toolchains without manual SDK setup.

### CI pipeline

`.github/workflows/build.yml` defines two jobs triggered on push and `workflow_dispatch`:

| Job | Runner | Steps | Commands |
|-----|--------|-------|----------|
| `android` | `ubuntu-latest` | checkout → setup temurin JDK 17 → setup-gradle | `gradle :composeApp:testDebugUnitTest :composeApp:assembleDebug` |
| `ios` | `macos-latest` | checkout → setup temurin JDK 17 → setup-gradle | `gradle :composeApp:iosSimulatorArm64Test :composeApp:linkDebugFrameworkIosSimulatorArm64` |

Both jobs use `actions/setup-java@v4` with `distribution: temurin` and `java-version: 17`, and `gradle/actions/setup-gradle@v4` for Gradle wrapper caching.

## Conventions and constraints

- **Single-module Gradle project**: Only `:composeApp` is included; there are no additional library modules.
- **Version pinning via catalog**: All third-party versions go through `gradle/libs.versions.toml`; direct version strings in build scripts are not used.
- **Plugin aliasing**: Plugins are declared in the root script with `apply false` and applied in `composeApp/build.gradle.kts` using `alias(libs.plugins.*)`.
- **Kotlin/JVM 17 baseline**: Both `androidTarget.compilerOptions.jvmTarget` and `compileOptions.sourceCompatibility/targetCompatibility` are pinned to Java 17.
- **iOS static frameworks**: All three iOS targets produce static frameworks (`isStatic = true`) named `ComposeApp`.
- **Android release signing**: The `release` build type explicitly reuses the debug signing config — production releases are currently signed with the debug key.
- **CI coverage**: The workflow exercises both platforms but does not upload artifacts or publish binaries; it only verifies unit tests and debug builds/framework linking.
- **No Makefile/Dockerfile**: The repository contains no Makefiles, shell build scripts, or Dockerfiles — Gradle is the exclusive build entry point.