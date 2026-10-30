# ADR 0004 — Keystore-backed credential storage

- Status: Accepted
- Date: 2026-10-30

## Context

The Companion needs to retain a provider credential without plaintext preferences.

## Decision

Generate an AES key in the Android Keystore and encrypt the API key with AES-GCM. Never log the key.

## Alternatives considered

Plaintext SharedPreferences, embedding a key, or claiming StrongBox without verification.

## Consequences

The app protects stored credentials within the Android app boundary; recovery and hardware backing are not guaranteed by this implementation.

## Security implications

This is not a cryptographically signed ActionGrant, attestation, or process-isolation mechanism.

## Evidence and references

`SecureKeyStore` in `PiraRuntime.kt`.
