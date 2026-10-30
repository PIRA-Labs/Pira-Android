# ADR 0002 — Agent/runtime trust boundary

- Status: Accepted
- Date: 2026-10-30

## Context

LLM output and tool responses are untrusted and can be manipulated.

## Decision

Agents submit typed proposals. The runtime evaluates policy, obtains consent, revalidates parameters, executes allowlisted operations, and audits outcomes.

## Alternatives considered

Natural-language command execution, arbitrary shell/ADB, unrestricted BLE/GATT, and system-prompt-only safety.

## Consequences

Some demos require explicit user interaction and cannot be unattended.

## Security implications

No arbitrary remote execution or surveillance is exposed.

## Evidence and references

`PiraRuntime.kt`, `PolicyEngineTest.kt`, `docs/THREAT_MODEL.md`.
