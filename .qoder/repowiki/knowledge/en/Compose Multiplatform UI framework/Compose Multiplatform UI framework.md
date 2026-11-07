---
kind: external_dependency
name: Compose Multiplatform UI framework
slug: compose-multiplatform
category: external_dependency
category_hints:
    - framework_behavior
scope:
    - '**'
source_files:
    - gradle/libs.versions.toml
    - composeApp/build.gradle.kts
---

UI framework for the shared cross-platform layer, applied via the `org.jetbrains.compose` and `org.jetbrains.kotlin.plugin.compose` Gradle plugins in `composeApp/build.gradle.kts`. Shared UI lives in `commonMain` and is consumed by both the Android app and the iOS host (`iosApp` SwiftUI project embeds `ComposeApp.framework`). Material3 + Foundation + UI modules are pulled from the Compose BOM.