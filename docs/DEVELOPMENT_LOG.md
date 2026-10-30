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
