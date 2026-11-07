---
kind: configuration_system
name: Gradle-based Build Configuration (No Runtime Config System)
category: configuration_system
scope:
    - '**'
source_files:
    - gradle.properties
    - settings.gradle.kts
    - build.gradle.kts
    - composeApp/build.gradle.kts
    - gradle/libs.versions.toml
---

This repository is a Kotlin Multiplatform Compose client for the go-url-expander service. It does not implement a runtime configuration system — there are no config files, environment variable loaders, feature flags, or secrets management in the application code.

**What exists instead:**

- **Build-time configuration via Gradle**: The project uses standard Gradle conventions:
  - `gradle.properties` sets JVM args, caching, Kotlin code style, and AndroidX flags.
  - `settings.gradle.kts` declares the single `:composeApp` module, plugin repositories (`google()`, `mavenCentral()`, `gradlePluginPortal()`), and the foojay toolchain resolver.
  - `build.gradle.kts` (root) only registers plugins with `apply false`.
  - `composeApp/build.gradle.kts` configures Kotlin Multiplatform targets (`androidTarget`, `iosX64`, `iosArm64`, `iosSimulatorArm64`), source set dependencies per platform (`commonMain`, `androidMain`, `iosMain`, `commonTest`), and Android-specific settings (`compileSdk = 35`, `minSdk = 24`, `targetSdk = 35`, `applicationId = "com.brunoshiroma.url_expander"`, signing configured to use debug key for release).
  - `gradle/libs.versions.toml` centralizes all dependency versions and plugin aliases (AGP 8.7.3, Kotlin 2.1.20, Compose Multiplatform 1.8.0, Ktor 3.1.3, Coroutines 1.10.2).

- **Android manifest**: `composeApp/src/androidMain/AndroidManifest.xml` is present but contains no app-level configuration beyond the default generated content.

- **Runtime behavior**: The app's HTTP transport is provided by Ktor clients selected per platform (`ktor-client-okhttp` on Android, `ktor-client-darwin` on iOS). There is no runtime URL/base-address configuration loader — the target server address is presumably embedded or supplied through the WebView/native bridge rather than a config file.

**Conventions observed:**
- Dependency versions are centralized exclusively in `gradle/libs.versions.toml` and referenced via `libs.xxx` version catalog aliases throughout build scripts.
- Platform-specific dependencies are declared inside `sourceSets { ... }` blocks in `composeApp/build.gradle.kts` rather than in separate files.
- Android build types use the debug signing config for release builds (commented as temporary until a real keystore is configured).

**Not present:** No `.env` files, no YAML/TOML/JSON config files, no `Config` class, no environment variable parsing, no feature flag system, and no secrets management. The only match for "config" in Kotlin sources is `isChangingConfigurations` in `MainActivity.kt`, which is an Android lifecycle check unrelated to application configuration.