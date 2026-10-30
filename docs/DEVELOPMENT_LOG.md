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
