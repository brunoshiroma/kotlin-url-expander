# AGENTS.md

Guidance for AI agents (and humans) working on this Kotlin Multiplatform + Compose
Multiplatform project. Follow these conventions to keep the shared code portable,
testable, and consistent with the existing architecture.

## Project context

- **What it is:** a multiplatform (Android + iOS) client for the `go-url-expander`
  service. It extracts a URL from shared/pasted text, calls the API to expand it,
  and offers Copy / Share / Open external / Open internal (WebView) actions.
- **Stack:** Kotlin `2.1.20`, Compose Multiplatform `1.8.0`, Ktor `3.1.3`,
  Coroutines `1.10.2`, AGP `8.7.3`, JVM target 17, `minSdk 24`.
- **Package root:** `com.brunoshiroma.url_expander`.

## Module & source-set layout

- `composeApp/src/commonMain` — shared UI (Compose) and networking (Ktor). Default
  home for new business logic and composables.
- `composeApp/src/commonTest` — shared tests, run on the JVM. Put logic tests here.
- `composeApp/src/androidMain` — Android implementations (Activity, clipboard,
  share intents, `android.webkit.WebView`).
- `composeApp/src/iosMain` — iOS implementations (`MainViewController`,
  `UIPasteboard`, `UIActivityViewController`, `WKWebView`).
- `iosApp` — SwiftUI host that embeds `ComposeApp.framework`.

## Core KMP best practices

1. **Maximize shared code.** Implement features in `commonMain` first. Only drop to
   platform source sets when an OS API is genuinely required.
2. **Use `expect`/`actual` for platform boundaries.** Declare the contract once in
   `commonMain` (e.g. `expect fun PlatformWebView(...)`) and provide one `actual`
   per platform. Keep the surface minimal.
3. **Prefer interfaces + dependency injection for platform operations.** Follow the
   existing `PlatformActions` pattern: the shared UI depends on the interface; each
   platform supplies an implementation (Android object in `MainActivity`, `IosActions`
   object in `MainViewController`). This keeps shared code testable and decoupled.
4. **Never leak platform types into `commonMain`.** No `android.*`, `UIKit.*`, or
   `platform.*` imports in shared code.
5. **Use the correct Ktor engine per platform:** `okhttp` on Android, `darwin` on
   iOS, `mock` in tests. Never hardcode an engine in shared code — accept an
   `HttpClient` as a constructor parameter (see `GoUrlExpanderClient`).
6. **Coroutines everywhere.** Suspend functions for I/O; keep `commonMain` free of
   blocking/platform threading APIs.

## Dependency management

- **Always use the version catalog** (`gradle/libs.versions.toml`). Add versions
  under `[versions]`, libraries under `[libraries]`, and reference them with
  `libs.*` in `build.gradle.kts`. Do not inline version strings.
- Keep shared dependencies in `commonMain.dependencies`, platform-specific ones in
  `androidMain` / `iosMain`.
- Bump versions in the catalog only, so all modules stay consistent.

## Code style

- Kotlin **official** code style (`kotlin.code.style=official`); use `.editorconfig`
  / IDE defaults, 4-space indent.
- Keep files small and single-purpose, matching the existing structure
  (`GoUrlExpanderClient.kt`, `App.kt`, `PlatformActions.kt`).
- Compose UI state: hoist state with `remember`/`mutableStateOf`; drive side effects
  with `LaunchedEffect` (see `App.kt`). Avoid side effects in the composable body.
- Comment only non-obvious intent. Match the existing sparse-comment density.

## Error handling

- Follow the project's **null-based error signaling**: return `null` for
  "no data / not found / non-success" instead of throwing (see
  `GoUrlExpanderClient.getUrlExpanded` and the `NO_DATA` sentinel).
- Guard network calls at the call site with `runCatching { ... }.getOrNull()`
  (as done in `App.kt`) so failures degrade gracefully in the UI.
- Check `response.status.isSuccess()` before consuming the body.

## Testing

- Add unit tests for shared logic in `commonTest` using `kotlin.test`.
- Use Ktor's `MockEngine` to fake HTTP responses — assert on the requested URL,
  body, and null-on-error behavior (see `GoUrlExpanderClientTest`).
- Use `runTest` from `kotlinx-coroutines-test` for suspend functions.
- Tests run on the JVM: `gradle :composeApp:testDebugUnitTest`.

## Build & verification commands