# PIRA Android Security Notes

PIRA separates model proposals from hardware execution. Model output is untrusted and must be converted into a typed, allowlisted proposal before policy evaluation.

The inherited TAAK prototype is research code. Before reuse, the exported MIAU receiver, hardcoded device identity, ADB/log bridge, unrestricted event trust, and ordinary SharedPreferences event storage must be removed or isolated behind authenticated, narrowly scoped interfaces.

The current policy engine binds an authorization to a capability and exact parameter map, checks expiry and one-use state, and supports an independent emergency-stop flag. These are application controls, not cryptographic grants or hardware-backed isolation.

No arbitrary ADB, shell, filesystem, BLE/GATT, background camera, or unattended actuation is part of the PIRA design.

API credentials must be stored with Android Keystore-backed protection and must never be logged. Android permissions are requested only when the corresponding user-initiated capability is used.
