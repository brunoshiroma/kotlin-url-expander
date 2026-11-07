---
kind: external_dependency
name: Ktor HTTP client (multiplatform)
slug: ktor
category: external_dependency
category_hints:
    - framework_behavior
scope:
    - '**'
source_files:
    - gradle/libs.versions.toml
    - composeApp/build.gradle.kts
---

HTTP client used by the shared `GoUrlExpanderClient` to call the upstream `go-url-expander` service.
- Android target wires `ktor-client-okhttp`; iOS target wires `ktor-client-darwin` — both are declared as platform-specific dependencies in `composeApp/build.gradle.kts`.
- Tests use `ktor-client-mock` (`MockEngine`) so network calls never leave the JVM test runner.
- The durable integration shape: a single Ktor client instance performs a GET against `{baseUrl}/expand?url=...`, returning null on non-success status codes; the base URL defaults to `https://go-url-expander.brunoshiroma.com`.
- Verify exact client/engine configuration against the Ktor 3.x docs.