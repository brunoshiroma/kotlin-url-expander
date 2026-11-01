# url_expander

Client for go-url-expander, written with Kotlin Multiplatform + Compose Multiplatform.

- `composeApp/src/commonMain`: shared UI and Ktor client.
- `composeApp/src/androidMain`: Activity (receives `text/plain` shares), clipboard, share, WebView.
- `composeApp/src/iosMain`: `MainViewController` (clipboard, share, WKWebView).
- `iosApp`: SwiftUI host for the shared framework (create an Xcode iOS App project pointing at these sources, see below).

## Build

    gradle :composeApp:assembleDebug        # Android APK
    gradle :composeApp:testDebugUnitTest    # shared tests (JVM)
    gradle :composeApp:linkDebugFrameworkIosSimulatorArm64   # iOS framework (macOS)

For iOS, add a Run Script build phase to the Xcode project:
`cd "$SRCROOT/.." && gradle :composeApp:embedAndSignAppleFrameworkForXcode`, and link `ComposeApp.framework`.
