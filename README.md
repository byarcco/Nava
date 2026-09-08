# نــوا (Nava)

Production-grade Android SMS Gateway & Verification Ingestion Engine built with Kotlin Multiplatform and Compose Multiplatform in the `composeApp` module (`ir.cutte.nava`).

## Core Features

- **High-Priority Ingestion**: High-priority `SMS_RECEIVED` broadcast receiver with multipart PDU concatenation, regex-free digit normalization, and case-insensitive sender verification.
- **Persistent Device Identification**:
  - Stable technical `device_id` derived from hardware (`Settings.Secure.ANDROID_ID` with persistent secure fallback) without requesting invasive phone-state permissions.
  - Human-readable default `device_name` derived from device manufacturer and model, with full in-app override capability in Settings.
- **Verification Payload Schema**:
  Dispatches structured verification and OTP payloads to the backend Cloudflare worker:
  ```json
  {
    "code": "123456",
    "timestamp": 1773056023,
    "device_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "device_name": "Samsung Galaxy S23"
  }
  ```
- **Dual-Endpoint Failover**: Instantaneous dispatch to Primary endpoint (`https://sms.cutte.ir`) with automatic failover to Secondary backup (`https://sms-pipeline.imartrioss.workers.dev`).
- **Offline Resilience**: Persistent `WorkManager` queue with network constraints and 15-minute TTL to prevent stale OTP replays when offline.
- **In-App Direct Diagnostic Simulator**: Built-in simulator card in the Dashboard allowing immediate synthetic message dispatch through the ingestion, failover, and telemetry pipeline without cellular SMS.
- **Material 3 Expressive UI & Persian Localization**:
  - Full RTL (Right-to-Left) layout localized in Persian.
  - Vazirmatn typography for body and controls; Yekan typography for the top bar.
  - Real-time Persian activity duration counter and Persian numeral formatting.
  - Smooth micro-interactions, expand/collapse animations, and pulsing status indicators.

## Architecture

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 16 (API 36)
- **UI Framework**: Compose Multiplatform with Material 3 Expressive
- **Networking**: Ktor Client with `kotlinx.serialization`
- **Storage**: Jetpack DataStore Preferences
- **Background Execution**: Lightweight `dataSync` Foreground Service and WorkManager CoroutineWorkers

## Build

Build debug APK:
```bash
./gradlew :composeApp:assembleDebug
```

Build release APK:
```bash
./gradlew :composeApp:assembleRelease
```
