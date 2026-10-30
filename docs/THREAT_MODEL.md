# Threat model

## Assets

Nebius credentials, camera images, user consent, device identity, capability state, and audit history.

## Threats and controls

- Prompt injection / compromised agent: model output is untrusted; only typed allowlisted proposals reach policy.
- Confused deputy: model credentials are separate from hardware authorization.
- Replay / parameter tampering: short-lived authorization, one-use state, exact parameter-map binding.
- Unauthorized persistence: no background camera or recurring task facility exists.
- Credential leakage: Keystore-backed AES-GCM storage; keys and payloads are not logged.
- Exported components: inherited MIAU receiver is removed from the PIRA manifest.
- BLE impersonation: Xiaomi identity is not treated as verified by this app; adapter remains unvalidated.
- Emergency response: independent emergency-stop flag blocks policy evaluation.

## Unresolved

Keystore hardware backing is not verified; revocation UI and durable grant storage are incomplete; physical device identity and live Nebius/vision behavior remain untested.
