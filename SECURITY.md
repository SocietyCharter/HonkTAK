# Security Policy

Report vulnerabilities through a private GitHub security advisory. Do not include operational data, credentials, TAK.gov material, signing keys, proprietary packages, or real mission data in reports.

HonkTAK permits only an explicit foreground **SHARE TO TEAM** action through ATAK's connected TAK network. Its separate foreground Overpass import performs bounded reads only after visible user action. Any automatic/background transmission, unauthorized non-TAK transport, malformed inbound acceptance, mission-package write, UAS/device control, upstream OSM/DeFlock mutation, or real mission-data mutation is considered critical.

Supported public release: `0.1.x`. Public `main` also carries v0.2.12 development source, but no v0.2.x APK is a supported public release. Earlier v0.2.x APKs failed retail ATAK signer trust or were private development artifacts. No plugin build is trusted unless its source commit, target ATAK build, signer fingerprint, and SHA-256 are independently verified.
