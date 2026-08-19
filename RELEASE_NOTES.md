# v0.1.0 — Initial public source release

- Adds the local-only HonkTAK Sightings overlay and REPORT HONK action.
- Adds custom non-military goose marker, local SITREPs, automatic expiry, and FLOCKPOCALYPSE threshold logic.
- Audio defaults off; no audio asset is bundled.
- Adds unit tests and explicit no-CoT/no-mission-package/no-UAS boundaries.

Artifact policy: attach an APK only after an allowlisted build succeeds from public inputs, its signing provenance is safe to disclose/distribute, manifest and compatibility validation pass, APK inspection passes, and SHA-256 is recorded. Otherwise this is a source-only release.
