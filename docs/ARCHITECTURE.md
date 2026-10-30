# Architecture

PIRA is infrastructure first. The Android Companion is a human-facing client, not the authorization authority for an agent.

```text
Agent/model output (untrusted)
        -> typed ActionProposal
        -> PolicyEngine (deny by default)
        -> user consent / short-lived Authorization
        -> Capability executor (revalidates)
        -> AuditLogger
        -> Revocation / emergency stop
```

Current implementation: `ActionProposal`, `Authorization`, `PolicyEngine`, `AuditLogger`, `SecureKeyStore`, and `NebiusClient` live in `app/src/main/java/com/taak/watch/signedtest/PiraRuntime.kt`; the activity is the current user-facing coordinator. The camera operation is explicit and foreground-only. Xiaomi/XMS remains an optional research adapter, not an unrestricted command path.

The current boundary is application-level. It is not cryptographically signed grants, StrongBox attestation, process isolation, or a hardware security claim.
