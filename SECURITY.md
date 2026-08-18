# Security Policy

Report vulnerabilities through a private GitHub security advisory. Do not include operational data, credentials, TAK.gov material, signing keys, proprietary packages, or real mission data in reports.

HonkTAK has an intentional hard boundary: local ephemeral map items only. A finding that enables CoT transmission, persistence/mission-package writes, UAS/device control, or mutation of mission data is considered critical.

Supported version: `0.1.x`. No plugin build is trusted unless its source commit, signer, and SHA-256 are independently verified.
