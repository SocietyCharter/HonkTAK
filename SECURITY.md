# Security Policy

Report vulnerabilities through a private GitHub security advisory. Do not include operational data, credentials, TAK.gov material, signing keys, proprietary packages, or real mission data in reports.

HonkTAK v0.2.0 permits only an explicit foreground **SHARE TO TEAM** action through ATAK's connected TAK network. Any automatic/background transmission, non-TAK transport, malformed inbound acceptance, persistence/mission-package write, UAS/device control, or mission-data mutation is considered critical.

Supported public version: `0.1.x`. The withdrawn v0.2.0 plugin failed ATAK Play Store signer trust. v0.2.1 remains a private release candidate until an official signing/registration path and separately authorized runtime test pass. No plugin build is trusted unless its source commit, target ATAK build, signer fingerprint, and SHA-256 are independently verified.
