# Nava

Production-grade Android SMS Gateway built with Compose Multiplatform in the `composeApp` module (`ir.cutte.nava`).

## Core Features
- Ingestion: High-priority `SMS_RECEIVED` BroadcastReceiver with multipart PDU concatenation.
- Dual-Endpoint Failover: Dispatches to custom domain primary endpoint (`https://sms.cutte.ir`) with automatic instantaneous failover to workers.dev backup (`https://sms-pipeline.imartrioss.workers.dev`).
- Offline Resilience: Persistent `WorkManager` queue with network connectivity constraints and 15-minute TTL to prevent stale OTP replays.
- Keep-Alive Telemetry: 30-minute tiered periodic heartbeat probing primary, secondary, and captive portal reachability.
- Device Telemetry: Transmits battery level, charging state, device hardware identifiers, and SIM slot index.
- UI: Material 3 Expressive theme with reactive permission wizard, telemetry state chips, and delivery stream status.

## Architecture
- Minimum SDK: Android 5.0 (API 21)
- Compile / Target SDK: Android 16 (API 36)
- Networking: Ktor Client with JSON serialization (5s connect, 8s socket timeouts)
- Storage: Jetpack DataStore Preferences
- Background Execution: Lightweight `dataSync` Foreground Service and WorkManager CoroutineWorkers

## Build & CI/CD
Build release APK:
```bash
./gradlew assembleRelease --stacktrace
```
Artifacts are automatically built and staged by GitHub Actions in `.github/workflows/build.yml`.
