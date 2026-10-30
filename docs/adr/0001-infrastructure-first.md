# ADR 0001 — Infrastructure first

- Status: Accepted
- Date: 2026-10-30

## Context

PIRA must expose physical capabilities to agents without giving agents unrestricted authority. Android is the first client, not the whole product.

## Decision

Build a reusable runtime boundary first. Nebius is an initial provider, not an endorsement or partnership claim.

## Alternatives considered

Building a device-specific chatbot or restoring all TAAK functionality immediately.

## Consequences

Capabilities remain typed and replaceable; more integration work is required before broad device coverage.

## Security implications

Provider access and hardware authorization remain separate.

## Evidence and references

`docs/ARCHITECTURE.md`, `docs/DEVELOPMENT_LOG.md`.
