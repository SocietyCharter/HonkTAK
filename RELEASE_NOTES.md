# HonkTAK v0.2.1 release candidate — not for installation

ATAK v5.6.0.12 Play Store rejected the v0.2.0 debug-signed plugin because its
certificate is not trusted by ATAK's release-plugin signer policy. APK signature
integrity passed, but ATAK plugin trust failed. v0.2.1 is a non-debuggable CIV
release candidate only; do not publish or install it until an official TAK
Product Center signing/registration path is completed and Jesse confirms that
ATAK loads it.

# Install HonkTAK v0.2.0 on a phone

1. Download `HonkTAK-v0.2.0-ATAK-5.6.0-CIV.apk` from the official GitHub Release.
2. Tap the APK in browser downloads or the Files app.
3. If prompted, open Android **Settings** and allow **Install unknown apps** for that browser/file manager, then return to the installer.
4. Tap **Install**, open ATAK, then select **HonkTAK** from ATAK's **Tools** menu; use **Tool Manager** to enable/add it if it is not visible.

Exact target: **ATAK v5.6.0.12 Play Store**, Plugin API **5.6.0.CIV**, Android API **36**. Local markers require no HonkTAK server or configuration. Team sharing uses ATAK's existing connection, reports disconnected state, and never retries silently. Android may show the standard unknown-app source warning, install confirmation, and a Play Protect scan prompt. ATAK signer/plugin trust acceptance still requires a separately authorized device test; no device was touched during release preparation.

# v0.1.0 — Initial public source release

- Adds the local-only HonkTAK Sightings overlay and REPORT HONK action.
- Adds custom non-military goose marker, local SITREPs, automatic expiry, and FLOCKPOCALYPSE threshold logic.
- Audio defaults off; no audio asset is bundled.
- Adds unit tests and explicit no-CoT/no-mission-package/no-UAS boundaries.

Artifact policy: attach an APK only after an allowlisted build succeeds from public inputs, its signing provenance is safe to disclose/distribute, manifest and compatibility validation pass, APK inspection passes, and SHA-256 is recorded. Otherwise this is a source-only release.

## v0.2.0

- Adds a user-opened camera observation form and explicit location-source choice.
- Adds separate local-save and visibly warned TAK-network share actions.
- Adds bounded CoT serialization, inbound validation/rendering, and stale-time cleanup.
- Adds explicit-share/no-silent-send, codec, malformed-input, bounds, expiry, and existing flock tests.

Target is ATAK `v5.6.0.12` / Plugin API `5.6.0.CIV`, Play Store build `1769863102`. The guarded offline SDK build, manifest compatibility validation, APK inspection, signature verification, CoT validation, and host tests pass. Runtime installation remains separately approval-routed and was not performed. The authorized SDK and signing material are excluded from the repository and release.

Release assets must contain exactly one user-facing APK named `HonkTAK-v0.2.0-ATAK-5.6.0-CIV.apk` plus `SHA256SUMS`. Ordinary installation requires no SDK, Android Studio, Gradle, ADB, source build, server, or command line.

Uninstall from ATAK **Tool Manager** when available, then Android **Settings → Apps → HonkTAK → Uninstall**, and restart ATAK. Local removal does not transmit a remote delete.
