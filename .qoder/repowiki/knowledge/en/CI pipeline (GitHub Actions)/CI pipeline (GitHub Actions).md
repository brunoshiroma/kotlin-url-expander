---
kind: external_dependency
name: CI pipeline (GitHub Actions)
slug: github-actions
category: external_dependency
category_hints:
    - client_constraint
scope:
    - '**'
source_files:
    - .github/workflows/build.yml
---

Two parallel jobs run on every push / workflow_dispatch:
- `android`: runs on `ubuntu-latest` with Temurin JDK 17, executes `testDebugUnitTest` and `assembleDebug`.
- `ios`: runs on `macos-latest` (required for Xcode toolchain), executes `iosSimulatorArm64Test` and `linkDebugFrameworkIosSimulatorArm64`.
The workflow pins `actions/checkout@v4`, `setup-java@v4`, and `gradle/actions/setup-gradle@v4`.