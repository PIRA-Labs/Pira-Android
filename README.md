# PIRA Android

**Physical Intelligence Runtime for Agents — Android companion**
**Status:** Experimental prototype · v0.1.0 · MIT License
**Repository:** [PIRA-Labs/Pira-Android](https://github.com/PIRA-Labs/Pira-Android)

---

## Reviewer quick start (3-minute verification)

```bash
# 1. Clone
git clone https://github.com/PIRA-Labs/Pira-Android.git
cd Pira-Android

# 2. Build + unit tests (JDK 17, Gradle 9.7.1 wrapper included)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew :app:testDebugUnitTest :app:assembleDebug

# 3. Expected output
# - Test results: app/build/reports/tests/debugTest/index.html
# - APK: app/build/outputs/apk/debug/app-debug.apk
```

**What a reviewer can verify in 3 minutes:**
- Code compiles without errors
- Unit tests pass (`PolicyEngineTest`: deny-by-default, expiry, replay, parameter binding, scope mismatch, emergency stop)
- `PiraRuntime.kt` contains the trust-boundary implementation
- `THREAT_MODEL.md` lists implemented controls vs unresolved items
- `EXPERIMENTS.md` distinguishes tested from untested claims

---

## What PIRA Android is

PIRA Android is the **user-facing companion** for the PIRA runtime. It is not the authorization authority. It is the interface through which a human user connects an AI brain to physical capabilities on an Android device.

The central guarantee:

> **AI agents may propose actions. Only a trusted, user-controlled runtime can authorize and execute them.**

---

## What PIRA Android is NOT

- It is not a general-purpose AI chat app
- It does not give agents unrestricted access to device APIs
- It does not execute shell commands, ADB, arbitrary URLs, background camera, or unrestricted Bluetooth
- It is not production-ready
- It is not endorsed by Nebius, Xiaomi, or any third party

---

## Architecture

```
Agent / Model output (untrusted)
    → typed ActionProposal
    → PolicyEngine (deny by default)
    → user consent / short-lived Authorization
    → Capability executor (revalidates)
    → AuditLogger
    → Revocation / emergency stop
```

**Key components:**

| Component | File | Role |
|-----------|------|------|
| `Capability` enum | `PiraRuntime.kt` | Typed capability allowlist: CAMERA_LOOK, BATTERY_READ, WEARABLE_MESSAGE, WEB_SEARCH |
| `ActionProposal` | `PiraRuntime.kt` | Immutable proposal: capability, target, parameters |
| `Authorization` | `PiraRuntime.kt` | One-use, time-limited grant bound to exact parameters |
| `PolicyEngine` | `PiraRuntime.kt` | Deny-by-default evaluator with emergency-stop gate |
| `AuditLogger` | `PiraRuntime.kt` | Structured decision logging |
| `SecureKeyStore` | `PiraRuntime.kt` | Android Keystore AES-GCM credential storage |
| `NebiusClient` | `PiraRuntime.kt` | OpenAI-compatible Nebius Token Factory client |
| `TavilyClient` | `PiraRuntime.kt` | Scoped web search client |
| `MainActivity` | `MainActivity.kt` | User-facing coordinator |

---

## Security model

### Implemented controls

| Control | Mechanism | Evidence |
|---------|-----------|----------|
| **Deny by default** | `PolicyEngine.evaluate()` returns `false` for unrecognized capabilities, missing auth, expired grants, parameter mismatch, or emergency stop | `PiraRuntime.kt` lines 22–40 |
| **Typed proposals** | `Capability` enum + `ActionProposal` data class | `PiraRuntime.kt` lines 16–17 |
| **One-use authorization** | `Authorization.used` flag checked after first evaluation | `PiraRuntime.kt` line 30 |
| **Parameter binding** | Proposal parameters must exactly match authorization parameters | `PiraRuntime.kt` line 35 |
| **Parameter allowlist** | Only `"prompt"` and `"message"` keys accepted | `PiraRuntime.kt` line 33 |
| **Message length limit** | WEARABLE_MESSAGE capped at 120 characters | `PiraRuntime.kt` line 37 |
| **Emergency stop** | Independent flag blocks policy evaluation before any capability check | `PiraRuntime.kt` line 22 |
| **Credential isolation** | Nebius/Tavily keys in Android Keystore (AES-GCM), never logged | `PiraRuntime.kt` lines 48–78 |
| **Exported component removal** | MIAU broadcast receiver removed from PIRA manifest | `THREAT_MODEL.md` |

### Unresolved security items

| Item | Risk | Status |
|------|------|--------|
| Keystore hardware backing | Software-backed keys vulnerable to device compromise | Unverified |
| Revocation UI | User cannot revoke authorizations after granting | Incomplete |
| Durable grant storage | Authorizations lost on app restart | Not implemented |
| BLE identity validation | Xiaomi BLE identity could be spoofed | Not validated |
| Live Nebius/vision behavior | Actual model output with untrusted input untested | Pending |
| Prompt injection on proposals | Agent may craft semantically harmful proposals within allowlist | Open |
| Process isolation | PolicyEngine and UI share process | Not implemented |

See `docs/THREAT_MODEL.md` for the full threat model.

---

## Verified vs. unverified claims

### Verified (real evidence exists)

| Claim | Evidence | Date |
|-------|----------|------|
| Debug compilation succeeds | Gradle build output | 2026-10-30 |
| Unit tests pass | `:app:testDebugUnitTest` | 2026-10-30 |
| APK installs on HONOR VNE-LX3 | ADB install + launch | 2026-10-30 |
| Keystore credential paths | Code review + runtime check | 2026-10-30 |
| Policy deny-by-default | `PolicyEngineTest` | 2026-10-30 |
| Emergency stop blocks execution | Manual UI test | 2026-10-30 |
| Nebius client shape | Code review + 401 response documented | 2026-10-30 |
| Tavily client implemented | Code review | 2026-10-30 |

### Blocked (requires external input)

| Claim | Blocker |
|-------|---------|
| Live Nebius/Nemotron inference | Requires user-entered API key |
| Live Tavily search | Requires user-entered API key |
| Camera-to-vision pipeline | Requires live model + device |
| Wearable message delivery | Requires Xiaomi/XMS setup (research adapter) |

### Not tested

| Claim | Reason |
|-------|--------|
| Vision model capability filtering | Requires live `/v1/models` response |
| Background camera capture | Deliberately not implemented |
| Microphone capture | Deliberately not implemented |
| BLE/GATT operations | Deliberately not exposed to agent |

---

## Build and test

### Requirements

- JDK 17
- Android SDK API 34
- Gradle 9.7.1 (wrapper included)
- Physical Android 14 device recommended for hardware tests

### Commands

```bash
# Unit tests only
./gradlew :app:testDebugUnitTest

# Debug APK
./gradlew :app:assembleDebug

# Both
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

### Outputs

- Test report: `app/build/reports/tests/debugTest/index.html`
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

### Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Tested hardware

| Device | OS | API | Status |
|--------|----|-----|--------|
| HONOR VNE-LX3 | Android 14 | 34 | Install + launch verified |

---

## Experiment registry

See `docs/EXPERIMENTS.md` for the structured experiment registry with
hypotheses, results, and evidence links.

Summary:

| ID | Hypothesis | Result |
|----|-----------|--------|
| E-009 | Policy denies adversarial proposals | **Passed** |
| E-012 | Install on physical HONOR VNE-LX3 | **Passed** |
| E-013 | Native product UI on physical device | **Passed** |
| E-014 | Precision Play UI on physical HONOR | **Passed** |
| E-015 | Tavily runtime search | **Blocked** (key pending) |
| E-016 | Physical object → Nemotron → Tavily | **Blocked** |
| E-010 | Unauthenticated Nebius model discovery | **Blocked** (401) |
| E-011 | NVIDIA model inference | **Blocked** |

---

## Provenance

PIRA Android was established as an isolated workspace from the TaakCompanion
research baseline. The original `taak-companion` repository is preserved
locally and is not modified by this workspace.

Research lineage:
- TAAK Intelligence (2026-08-30) → NODE-01 Android runtime experiments
- TaakCompanion → XMS wearable research, MIAU milestone
- PIRA Android (2026-10-30) → infrastructure-first runtime

See `PIRA-Labs/Pira-Research` for the full research archive.

---

## Documentation

| Document | Purpose |
|----------|---------|
| `ARCHITECTURE.md` | Trust-boundary architecture |
| `THREAT_MODEL.md` | Assets, threats, controls, unresolved items |
| `DECISION_REGISTER.md` | Authoritative ADR summaries |
| `docs/adr/0001-infrastructure-first.md` | Infrastructure-first decision |
| `DEVELOPMENT_LOG.md` | Mission chronology with evidence |
| `EXPERIMENTS.md` | Structured experiment registry |
| `IMPLEMENTATION_STATUS.md` | What works, what's blocked, what's planned |
| `CHANGELOG.md` | Version history |
| `SECURITY.md` | Security policy and vulnerability reporting |
| `CONTRIBUTING.md` | Contribution rules |
| `THIRD_PARTY_LICENSES.md` | License audit for dependencies |

---

## License

PIRA-authored source is released under the MIT License. See `LICENSE`.

AndroidX and other build dependencies retain their upstream licenses (primarily
Apache-2.0). The inherited Xiaomi/XMS wearable AAR (`app/libs/xms-wearable-lib_1.4_release.aar`)
and Pirophone/brand image assets require confirmation of redistribution rights
before public release. See `docs/THIRD_PARTY_LICENSES.md`.

---

## What a reviewer should know

1. **This is a prototype.** The policy engine is 122 lines. `MainActivity` is
   290 lines. There are no ViewModels, no navigation component, no persistent
   authorization store, and no automated UI tests.

2. **Claims are bounded by evidence.** Every "verified" claim has a date, a
   command, a file, or a physical observation. "Blocked" means there is a
   known path but an external dependency (usually a credential or device).

3. **Security is taken seriously but is incomplete.** The deny-by-default
   engine is implemented. Keystore backing is unverified. Process isolation
   does not exist. Revocation UI is incomplete.

4. **No secrets are in this repository.** API keys, signing passwords, auth
   keys, and credentials are never committed. The `SecureKeyStore` stores
   user-entered keys in Android Keystore, not in SharedPreferences or Git.

5. **The research history is preserved.** The Vela/Android/MIAU milestone,
   the Mi Fitness feature gate discovery, the DID bypass, and the Gadgetbridge
   investigation are documented in `PIRA-Labs/Pira-Research` with full
   evidence chains, experiment logs, and decision records.

---

*README maintained by PIRA-Labs.
Last updated: 2026-10-30*
