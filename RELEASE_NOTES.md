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

Release is blocked until the exact installed ATAK-CIV version/build is supplied and a matching public devkit, guarded APK build, signer fingerprint, APK inspection, and compatibility validation all pass.
