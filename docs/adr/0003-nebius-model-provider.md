# ADR 0003 — Nebius initial model provider

- Status: Accepted
- Date: 2026-10-30

## Context

PIRA needs a configurable model provider for the first client.

## Decision

Use Nebius Token Factory's OpenAI-compatible API with a configurable model ID. Credentials authenticate model access only.

## Alternatives considered

Hardcoded model IDs, fabricated mock responses, or coupling authorization to provider identity.

## Consequences

The app can use `/v1/models` and `/v1/chat/completions`, but live availability depends on the user's account.

## Security implications

The API key never grants Android hardware access.

## Evidence and references

https://api.tokenfactory.nebius.com/docs
