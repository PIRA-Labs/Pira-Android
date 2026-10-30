# Development log

## Mission 1 — Foundation (reconstructed, not a historical commit)

- Objective: create a separate PIRA Android workspace from TaakCompanion research.
- Implemented: PIRA UI baseline, typed capabilities/proposals/authorizations, deny-by-default policy, explicit camera action, Keystore/AES-GCM credential storage, initial adversarial tests, removal of exported MIAU receiver.
- Evidence: current source and unit tests.
- Limitations: no JDK initially; Nebius and vision were not live-verified; Xiaomi behavior was inherited research.

## Mission 2 — Build and provider integration (reconstructed, not a historical commit)

- Objective: unblock build and add a real Nebius-compatible client.
- Implemented: JDK 17 build configuration, debug signing fallback, `/v1/models` and `/v1/chat/completions` client, model field, scoped/expiring grants, parameter binding, emergency stop.
- Evidence: Gradle build and unit tests pass. API shape is documented by Nebius; no real key was available for a live call.
- Limitations: no verified live text, model catalog, vision, physical camera-to-model flow, or Xiaomi adapter.

## Mission 3 — Provenance and reproducibility

- Objective: make the repository auditable.
- Implemented: documentation structure, ADRs, experiment registry, threat model, changelog, contribution rules, secret/build hygiene, and local commits.

## Mission 4.2 — Physical first contact and package identity

- Changed only PIRA's `applicationId` to `ai.pira.android` and app label to `PIRA`; retained the Kotlin namespace to avoid unnecessary refactoring.
- Built with JDK 17, ran unit tests, installed the APK on authorized HONOR `VNE-LX3` hardware, and launched the activity successfully.
- Verified the legacy `com.taak.watch.signedtest` package remained installed and was not uninstalled or data-cleared.
- Nebius/NVIDIA inference remains pending until the user enters a key directly in the PIRA UI; no key was requested through shell or chat.

## Mission 4.3 — Native product design and first awakening

- Reworked the existing XML/AppCompat screen into a native PIRA product surface using the web brand's indigo dusk, lilac, blue, and peach palette.
- Added a clear intelligence onboarding hierarchy, real connection-state vocabulary, model discovery, first inference action, physical-world capability status, and always-visible emergency stop.
- Added API-key show/hide editing control while preserving Keystore storage and existing networking/policy code.
- Verified on the physical HONOR VNE-LX3: debug build/tests passed, APK installed, activity launched, screenshot captured, and filtered logcat showed no fatal exception.

## Mission 4.4 — Precision Play refinement

- Reworked the playful surface toward a premium industrial instrument: graphite/midnight surfaces, muted lavender/blue active accents, restrained peach, thin outlines, tighter geometry, and no emoji decoration.
- Preserved the four-action information architecture and emergency stop.
- Kept the Pirophone GLB as a future enhancement because the web repository does not provide an independent transparent PNG and Android has no 3D renderer dependency.
- Build and unit tests pass. After renewed USB authorization, the refined APK was installed and its home screen was captured on the HONOR VNE-LX3; the resulting composition shows restrained graphite surfaces, precise accent outlines, four primary actions, and a distinct emergency stop.

## Pirophone detail

- Added the supplied transparent Pirophone PNG as a small lower-corner brand detail.
- The original GLB remains untouched and reserved for a future renderer-backed treatment.
- APK recompilation passed; final physical reinstall was blocked when ADB authorization changed back to `unauthorized`.

## Mission 5 — Tavily physical-to-web bridge

- Added `TavilyClient` as a separate provider using `POST https://api.tavily.com/search`, `search_depth=basic`, and `max_results=5`.
- Added an independent Keystore alias and preference namespace for the Tavily key; the Nebius credential path is unchanged.
- Added typed `WebSearchProposal`/`WebSearchAuthorization`, exact parameter matching, expiry/replay checks, fixed endpoint, result limit, and emergency-stop enforcement.
- Added minimal on-device onboarding/search controls. No live Tavily result or bonus-award compliance is claimed until a real response is received.
- Physical smoke test: Tavily APK installed/launched on HONOR VNE-LX3; screenshot captured at `/tmp/pira-tavily.png`; no fatal logcat entries. Legacy package remained installed.

## Model onboarding correction

- Persisted Nebius credential state is now restored on app launch (`KEY SAVED`) instead of reverting to the empty initial presentation.
- Replaced raw model JSON display with a real clickable model picker. Returned IDs are parsed from the authenticated catalog, with NVIDIA/Nemotron models prioritized; selecting a row fills the model field and collapses the list.
- Build and unit tests pass. Physical reinstall is pending renewed ADB authorization.
- Physical verification completed after re-authorization: persisted `KEY SAVED` state and the Brain onboarding panel are visible on-device; no fatal logcat entries.

## Brain information architecture correction

- Separated everyday use from configuration: `ASK PIRA`, `SEARCH WEB`, and `BRAIN SETTINGS` are distinct modes.
- Nebius/Tavily credentials and model selection are no longer shown in the default ask surface.
- The ask surface now has one prompt and one primary action; search has its own query and source flow; settings contains provider setup.
- Build and unit tests pass. Physical install remains pending the next authorized ADB session.

## Product navigation refinement

- Moved provider configuration out of Brain into a bottom control dock alongside Devices and Safety.
- Brain now prioritizes asking; connection setup is reached through Connect, and device access remains separate.
- Model choices persist after selection and are presented with human-friendly Nemotron labels.
- Physical screenshot `/tmp/pira-dock.png` confirms the dock, persisted state, Pirophone detail, and clean home hierarchy on the HONOR device.
- Corrected dock routing after physical review: Connect opens only provider configuration, Devices opens physical capabilities, and Safety is no longer duplicated in the bottom dock.
- Final routing correction: Connect now hides Brain's Ask/Search tabs entirely and presents only provider setup; physical screenshot `/tmp/pira-connections-only.png` verifies the surface.
- Connection status refinement: saved Nebius credentials now render as `NEBIUS CONNECTED`, with key entry controls hidden; model selection shows human-readable labels and keeps the technical ID internal for inference.
- Connection lifecycle refinement: added explicit `USE THIS SETUP`, `CLEAR CONFIGURATION`, and close controls; active setup state is persisted and the Connect surface now has a complete open/activate/close/clear flow.
- Branding: replaced the generic Android launcher icon with the supplied PIRA isotipo and installed the updated APK on the HONOR device.
