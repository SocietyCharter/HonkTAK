# Security Policy

Report vulnerabilities through a private GitHub security advisory. Do not include operational data, credentials, TAK.gov material, signing keys, proprietary packages, or real mission data in reports.

HonkTAK v0.2.0 permits only an explicit foreground **SHARE TO TEAM** action through ATAK's connected TAK network. Any automatic/background transmission, non-TAK transport, malformed inbound acceptance, persistence/mission-package write, UAS/device control, or mission-data mutation is considered critical.

Supported public versions: `0.1.x` and `0.2.0`. No plugin build is trusted unless its source commit, target ATAK build, signer fingerprint, and SHA-256 are independently verified. SDK compile compatibility does not replace a separately authorized runtime installation test.
