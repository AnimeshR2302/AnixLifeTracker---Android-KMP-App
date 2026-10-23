# AnixLifeTracker - SMS Privacy & Consent Specification

## 1. Principles

1. **Explicit Opt-In Consent**: SMS parsing is disabled by default. Enabling requires explicit consent via the `SmsConsentDialog`.
2. **Local-Only Processing**: All SMS parsing occurs on-device in KMP logic. Raw SMS texts are **NEVER** transmitted over the network or uploaded to cloud sync servers.
3. **Strict Ingestion Scope**: Processing is restricted to bank and financial institution messages. Personal SMS messages, OTPs, and promotional texts are filtered out in memory and ignored.
4. **Least-Privilege Provenance**: Saved records store only normalized accounting data (`amount`, `direction`, `merchant`, `accountRefMasked`, `transactionReference`) and a truncated snippet. Full message texts are discarded immediately after parsing.
5. **Instant Revocation**: Users can toggle off live monitoring or revoke feature consent in Settings at any time, halting all SMS background processing immediately.
