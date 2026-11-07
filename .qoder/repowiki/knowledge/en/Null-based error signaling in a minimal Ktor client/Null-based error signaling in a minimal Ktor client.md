---
kind: error_handling
name: Null-based error signaling in a minimal Ktor client
category: error_handling
scope:
    - '**'
source_files:
    - composeApp/src/commonMain/kotlin/com/brunoshiroma/url_expander/GoUrlExpanderClient.kt
    - composeApp/src/commonTest/kotlin/com/brunoshiroma/url_expander/GoUrlExpanderClientTest.kt
---

## Overview

This Kotlin Multiplatform project is a small Compose client for the `go-url-expander` service. It has no dedicated error-handling framework, no custom exception types, and no middleware layer. Errors are handled at the HTTP boundary by inspecting the response status and returning `null` to signal failure.

## Approach

- **No exceptions thrown.** The only place an HTTP call occurs is `GoUrlExpanderClient.getUrlExpanded`, which uses Ktor's `HttpClient.get`. It does not wrap or catch exceptions; any transport-level failure would propagate as-is (Ktor throws). There is no `try/catch` or `Result<T>` usage in the codebase.
- **Failure is represented by `null`.** On success the function returns the expanded URL (`String?`). On non-success HTTP status it returns `null`:

  ```kotlin
  val response = client.get("$baseUrl/expand") { parameter("url", url) }
  return if (response.status.isSuccess()) response.body<String>() else null
  ```

- **A sentinel string represents "no input" rather than an error.** `NO_DATA = "NO DATA"` is checked before making the request and short-circuits to `null`:

  ```kotlin
  if (text == NO_DATA) return null
  ```

- **URL extraction also returns `null` on failure.** `extractUrl(text)` uses a regex and returns `String?`; callers treat `null` as "no URL found".

## Key files

- `composeApp/src/commonMain/kotlin/com/brunoshiroma/url_expander/GoUrlExpanderClient.kt` — the sole HTTP client; defines `getUrlExpanded`, `extractUrl`, and the `NO_DATA` sentinel.
- `composeApp/src/commonTest/kotlin/com/brunoshiroma/url_expander/GoUrlExpanderClientTest.kt` — verifies that non-success HTTP responses yield `null` and that `NO DATA` yields `null` without calling the server.

## Architecture and conventions observed

1. **Single-point HTTP boundary.** All network calls go through `GoUrlExpanderClient`, so there is one place where HTTP status codes are interpreted.
2. **Status-code filtering via Ktor's `isSuccess()`.** Success is defined as any 2xx status; everything else is treated as a failure and collapsed to `null`. No distinction is made between 4xx and 5xx errors.
3. **No error propagation to callers.** Callers of `getUrlExpanded` receive `String?` and must decide how to handle `null`. The UI layer (`PlatformWebView`, `MainActivity`, `MainViewController`) is responsible for interpreting the absence of a value.
4. **No structured error model.** There are no data classes, sealed hierarchies, or error codes representing different failure modes (e.g., network timeout vs. server error vs. invalid URL).
5. **Tests assert `null` outcomes.** The test suite confirms the contract: `nonSuccessReturnsNull` asserts that a `500 InternalServerError` results in `null`, and `noDataReturnsNull` asserts the sentinel path.

## Conventions and constraints

- **Descriptive convention:** Non-OK HTTP responses from the expander service are surfaced to callers as `null` rather than throwing or wrapping an exception.
- **Descriptive convention:** The literal string `"NO DATA"` is used as a sentinel to mean "nothing to expand" and is converted to `null` before any network call.
- **Constraint enforced by tests:** `GoUrlExpanderClientTest.nonSuccessReturnsNull` enforces that any non-success `HttpStatusCode` causes `getUrlExpanded` to return `null`.
- **Constraint enforced by tests:** `GoUrlExpanderClientTest.noDataReturnsNull` enforces that the `NO_DATA` sentinel bypasses the HTTP call and returns `null`.
- **Not enforced here:** Transport-layer exceptions (DNS failures, timeouts, etc.) are not caught or transformed; they would bubble up from Ktor unmodified.