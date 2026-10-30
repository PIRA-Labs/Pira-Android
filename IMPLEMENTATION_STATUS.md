# PIRA Android v0.1 — Implementation Status

This is a new isolated Android workspace. The original `taak-companion` repository is not modified.

## Verified working

- Debug APK builds successfully with Homebrew JDK 17.
- Policy unit tests pass, including expiry, replay, parameter binding, scope mismatch, and emergency-stop denial.
- Nebius client uses the verified OpenAI-compatible base URL `https://api.tokenfactory.nebius.com/v1/`, Bearer authentication, `GET /models`, and `POST /chat/completions`.
- Mission 4 live check: unauthenticated `GET /v1/models` returned HTTP 401 (`token is not present`); no NVIDIA model ID has been confirmed for this account.
- Physical install: `ai.pira.android` installed and launched on authorized HONOR VNE-LX3 / Android 14 / API 34; legacy package preserved.
- Native product UI: brand-aligned XML/AppCompat redesign verified by physical screenshot and crash-filtered logcat; live Nebius status remains honest until authenticated discovery succeeds.
- Precision Play: premium graphite instrument-panel treatment applied while preserving simple four-action home; mascot GLB intentionally deferred pending lightweight asset rendering.
- Physical refinement verification: `/tmp/pira-44.png` captured from HONOR VNE-LX3 after reinstall/launch; legacy package remains installed.
- Tavily: separate Keystore alias, fixed HTTPS `/search` client, basic depth/max 5, typed scoped proposal/authorization, emergency-stop gate, and result rendering implemented; live API response pending physical authorization and user-provided key.
- Tavily physical smoke test passed: APK launched on HONOR VNE-LX3; live search remains unverified until key entry.
- Nebius onboarding correction: saved-key state now restores on launch; model catalog is parsed into clickable selectable rows with NVIDIA/Nemotron prioritization instead of raw JSON.

### Inherited research

- Xiaomi XMS Wearable SDK dependency is present.
- Message API send/listener flow exists for the tested Redmi Watch 4 DID.
- Node API subscriptions exist for connection, charging, sleep, and wearing state.
- Mi Fitness service binding was demonstrated in the source research.

## Experimental / not production-ready

- MIAU event detection through an ADB/log bridge.
- Exported broadcast-based event delivery.
- Hardcoded watch DID and test payload.

## Not implemented yet

- Nebius vision request and model capability filtering.
- Capability registry and consent UI.
- Full revocation UI and device capability discovery UI.

## Planned first capability

User-initiated camera capture followed by a typed, policy-checked vision request. Wearable messaging remains a secondary adapter and will not be used as a general command channel.

## Nebius verification sources

- Official API Swagger: https://api.tokenfactory.nebius.com/docs
- Official service documentation: https://nebius.com/services/token-factory/inference-service
- The API supports OpenAI-compatible chat and model listing. Vision availability must be determined from `/v1/models` for the user's key.
