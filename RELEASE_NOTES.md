# v0.1.0 — Initial public source release

- Adds the local-only HonkTAK Sightings overlay and REPORT HONK action.
- Adds custom non-military goose marker, local SITREPs, automatic expiry, and FLOCKPOCALYPSE threshold logic.
- Audio defaults off; no audio asset is bundled.
- Adds unit tests and explicit no-CoT/no-mission-package/no-UAS boundaries.

Artifact policy: attach an APK only after an allowlisted build succeeds from public inputs, its signing provenance is safe to disclose/distribute, manifest and compatibility validation pass, APK inspection passes, and SHA-256 is recorded. Otherwise this is a source-only release.

## v0.2.0 — pending, not published

- Adds a user-opened camera observation form and explicit location-source choice.
- Adds separate local-save and visibly warned TAK-network share actions.
- Adds bounded CoT serialization, inbound validation/rendering, and stale-time cleanup.
- Adds explicit-share/no-silent-send, codec, malformed-input, bounds, expiry, and existing flock tests.

Target is confirmed as ATAK `v5.6.0.12` / Plugin API `5.6.0.CIV`, Play Store build `1769863102`. Release remains blocked until Jesse supplies an authorized matching plugin SDK/devkit and the guarded APK build, signer fingerprint, APK inspection, and compatibility validation all pass. The public 4.6 devkit is not a valid substitute.
