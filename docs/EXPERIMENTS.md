# Experiment registry

| ID | Hypothesis | Result | Evidence / follow-up |
|---|---|---|---|
| E-001 | PIRA installs on a physical Android phone | Not Tested | Requires device and ADB; produce install/log evidence. |
| E-002 | A real Nebius key authenticates | Blocked | No user key available. |
| E-003 | `/v1/models` lists usable models | Blocked | Execute after E-002. |
| E-004 | Live text inference works | Blocked | Execute after E-003. |
| E-005 | A selected model accepts image input | Not Tested | Must verify model capability from live catalog. |
| E-006 | Camera-to-model flow works without retaining image | Not Tested | Requires E-005 and device. |
| E-007 | Xiaomi/XMS adapter works in PIRA | Not Tested | Inherited research only; no production adapter restored. |
| E-008 | Revocation blocks device execution | Partially implemented | Emergency stop is tested; complete revocation UI/storage remains. |
| E-009 | Policy denies adversarial proposals | Passed | `PolicyEngineTest`: missing, expired, reused, mismatched, modified, and emergency-stop cases. |
| E-010 | Unauthenticated Nebius model discovery | Blocked | 2026-10-30: `GET https://api.tokenfactory.nebius.com/v1/models` returned HTTP 401 `token is not present`; no key was available in the environment. |
| E-011 | NVIDIA model inference through Nebius | Blocked | Requires E-010 with a real key; do not claim hackathon compliance until a live NVIDIA response is captured. |
| E-012 | Install PIRA on physical HONOR VNE-LX3 | Passed | 2026-10-30: ADB authorized serial `AKJDNU2919400629`, Android 14/API 34, arm64-v8a; installed `ai.pira.android` without uninstalling legacy `com.taak.watch.signedtest`; explicit activity launch returned `Status: ok`; no crash in filtered logcat. |
