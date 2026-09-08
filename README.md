# Nava

Production-ready, resilient Android SMS Gateway and forwarder built with Compose Multiplatform inside the `composeApp` module under package namespace `ir.cutte.nava`.

## Architecture
- Target: Android 5.0 (API 21) to Android 16 (API 36)
- UI: Jetpack Compose with Material 3 Expressive
- Networking: Ktor Client with JSON serialization and exponential backoff
- Persistence: Jetpack DataStore Preferences
- Background Processing: BroadcastReceiver with `goAsync()` and `dataSync` Foreground Service

## CI/CD
GitHub Actions workflow builds the universal APK on push and workflow_dispatch.
