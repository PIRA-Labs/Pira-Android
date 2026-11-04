# PIRA Android

**Physical Intelligence Runtime for Agents — Android companion**
**Status:** Experimental prototype · v0.1.0 · MIT License
**Repository:** [PIRA-Labs/Pira-Android](https://github.com/PIRA-Labs/Pira-Android)

---

## What this is

PIRA Android is the first product implementation of the PIRA runtime: a trusted, user-controlled boundary between AI agents and physical Android device capabilities.

Instead of giving agents unrestricted access to cameras, microphones, sensors, or Bluetooth, PIRA converts model output into typed, allowlisted proposals. A deterministic policy engine evaluates each proposal against short-lived, scoped authorizations. The user retains explicit consent, revocation, and auditability at every step.

The central guarantee:

> AI agents may propose actions. Only a trusted, user-controlled runtime may authorize and execute them.

---

## Current release

- **v0.1.0** — debug build verified on HONOR VNE-LX3
- Typed capability model and policy engine
- Android Keystore AES-GCM credential storage
- Emergency stop and audit logging
- Nebius Token Factory client shape verified
- Tavily web-search client implemented
- Unit tests for policy behavior

**This is experimental software. It is not production-ready.**

---

## What is in development

- **v0.2 beta** — native TTS execution behind explicit user authorization
- Expanded capability catalog: camera, GPS, sensors, notifications
- Durable grant storage and revocation UI
- Hardware-backed Keystore verification
- Live Nebius and Tavily behavior with user-provided keys

Do not represent planned capabilities as shipped. The v0.2 beta is under active development by the PIRA engineering team.

---

## Architecture

```
AI Agent / Model Provider
        │
        ▼  untrusted output
  Typed Action Proposal
        │
        ▼
  Policy & Consent
  deny-by-default · user-scoped · short-lived grants
        │
        ▼
  Trusted Capability Executor
  revalidates parameters · audits outcomes · emergency stop
        │
        ▼
  Android APIs / Sensors / Wearables
```

The dashed box is the trust boundary. Everything inside it is PIRA's responsibility. Everything outside it is untrusted.

---

## Security model

| Control | Mechanism | Status |
|---------|-----------|--------|
| Deny by default | `PolicyEngine.evaluate()` rejects unrecognized capabilities, missing auth, expired grants, parameter mismatch, emergency stop | Implemented |
| Typed proposals | `Capability` enum + `ActionProposal` data class | Implemented |
| One-use authorization | `Authorization.used` flag after first evaluation | Implemented |
| Parameter binding | Exact match between proposal and authorization parameters | Implemented |
| Parameter allowlist | Only accepted parameter keys reach execution | Implemented |
| Emergency stop | Independent flag blocks evaluation before capability checks | Implemented |
| Credential isolation | Nebius/Tavily keys in Android Keystore (AES-GCM), never logged | Implemented |
| Revocation UI | User-facing grant revocation after authorization | Incomplete |
| Durable grant storage | Persistence across app restart | Not implemented |
| Hardware-backed Keystore | StrongBox/TEE attestation on device | Not verified |
| Process isolation | PolicyEngine and UI in separate processes | Not implemented |

See [`docs/THREAT_MODEL.md`](docs/THREAT_MODEL.md) for the full analysis.

---

## Verified vs. unverified claims

### Verified
| Claim | Evidence |
|-------|----------|
| Debug compilation succeeds | Gradle build output |
| Unit tests pass | `:app:testDebugUnitTest` |
| APK installs on HONOR VNE-LX3 | ADB install + launch |
| Policy deny-by-default | `PolicyEngineTest` |
| Emergency stop blocks execution | Manual UI test |
| Nebius client shape | Code review + documented 401 response |
| Tavily client implemented | Code review |

### Blocked
| Claim | Blocker |
|-------|---------|
| Live Nebius/Nemotron inference | Requires user-entered API key |
| Live Tavily search | Requires user-entered API key |
| Camera-to-vision pipeline | Requires live model + device |
| Wearable message delivery | Requires Xiaomi/XMS setup |

### Not tested / deliberate
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
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

### Outputs
- Test report: `app/build/reports/tests/debugTest/index.html`
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Experiment registry

See [`docs/EXPERIMENTS.md`](docs/EXPERIMENTS.md) for the structured registry with hypotheses, results, and evidence links.

Summary:

| ID | Hypothesis | Result |
|----|-----------|--------|
| E-009 | Policy denies adversarial proposals | Passed |
| E-012 | Install on physical HONOR VNE-LX3 | Passed |
| E-013 | Native product UI on physical device | Passed |
| E-014 | Precision Play UI on physical HONOR | Passed |
| E-015 | Tavily runtime search | Blocked |
| E-016 | Physical object → Nemotron → Tavily | Blocked |
| E-010 | Unauthenticated Nebius model discovery | Blocked |
| E-011 | NVIDIA model inference | Blocked |

---

## Provenance

PIRA Android was established as an isolated workspace from the TaakCompanion research baseline. Research lineage:
- TAAK Intelligence (2026-08-30) → NODE-01 Android runtime experiments
- TaakCompanion → XMS wearable research, MIAU milestone
- PIRA Android (2026-10-30) → infrastructure-first runtime

Detailed experimental evidence, recovered artifacts, and engineering decisions are preserved in [`PIRA-Labs/Pira-Research`](https://github.com/PIRA-Labs/Pira-Research).
