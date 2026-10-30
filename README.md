# PIRA Android

PIRA (Physical Intelligence Runtime for Agents) is a security-first Android companion that lets a user connect an AI brain to physical capabilities. The app proposes actions through Nebius Token Factory and executes only user-authorized, scoped operations.

## Architecture and security

The app uses a native AppCompat UI, Keystore-backed credential storage, typed action proposals, and a deny-by-default policy engine. Model output is untrusted input: it cannot execute shell/ADB commands, arbitrary URLs, background camera or microphone capture, or unrestricted Bluetooth/GATT operations. Authorizations are short-lived, bound to exact parameters, rechecked by the executor, and blocked by the independent emergency stop. Audit events cover allowed and denied operations.

The real application ID is `ai.pira.android` (the inherited Kotlin namespace remains `com.taak.watch.signedtest` for compatibility).

## Build and hardware

Requirements: Android SDK API 34, JDK 17, and a Gradle-compatible environment. A physical Android 14 device is recommended for camera and device checks; no device is required for unit tests.

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Nebius / NVIDIA Nemotron

PIRA uses Nebius Token Factory's OpenAI-compatible endpoint `https://api.tokenfactory.nebius.com/v1/`. The user enters the API key in the app; it is stored in Android Keystore-backed encrypted preferences and is never committed or logged. PIRA discovers models with `GET /models` and sends requests to `POST /chat/completions`. NVIDIA Nemotron IDs are selected only when returned by the authenticated catalog; no entitlement or response data is fabricated.

## Tavily

Tavily is an independent provider at the fixed HTTPS endpoint `https://api.tavily.com/search`. A user-entered key is stored separately in the same protected credential mechanism. Searches use `search_depth=basic` and at most five results, require an exact user-approved proposal, and treat returned pages as untrusted evidence.

## Verified versus planned

Verified: debug compilation and unit tests, application identity and branding, Keystore credential paths, policy checks, emergency-stop boundary, and installation/launch on an HONOR VNE-LX3 (Android 14). Live Nebius/Nemotron, Tavily, camera-to-vision, wearable adapters, and end-to-end physical actions require valid credentials, network access, and hardware; they are claimed only when real evidence is recorded.

## Licensing and provenance

PIRA-authored source is released under the MIT License in this repository. AndroidX and other build dependencies retain their upstream licenses. The inherited Xiaomi/XMS wearable AAR (`app/libs/xms-wearable-lib_1.4_release.aar`) and Pirophone/brand image assets require confirmation of redistribution rights before public release; they are not relicensed by this MIT file. See `docs/THIRD_PARTY_LICENSES.md`.

The original research repository at `/Users/valedev/Documents/taak-companion` is intentionally left unchanged.
