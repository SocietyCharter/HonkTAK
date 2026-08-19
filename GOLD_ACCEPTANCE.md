# Telemanus Gold acceptance — HonkTAK v0.2.0

Recorded: 2026-08-19 00:47 UTC

Job: `honktak-v020-20260818`

Founder directive envelope: `0a258981-e3cd-43ad-94e4-253a65f641b7`

## Grey waiver

Jesse explicitly waived all HonkTAK v0.2.0 Grey tasks and directed the release to proceed through formal Telemanus Gold acceptance. The Grey waiver is recorded as a release-process waiver only; it does not waive the Gold build, validation, artifact-integrity, or publication gates.

## Gold acceptance result

**FAIL — publication withheld.**

The formal gate command was:

```text
./gradlew testCivDebugUnitTest lintCivDebug assembleCivDebug --no-daemon
```

Gradle reached `:app:compileCivDebugJavaWithJavac` and failed with 80 compilation errors because the ATAK 5.6 API classes were unavailable to the compile classpath. Representative missing packages include `com.atakmap.android.maps`, `com.atakmap.android.dropdown`, `com.atakmap.coremap.cot.event`, `com.atak.plugins.impl`, and `gov.tak.api.plugin`.

The configured takdev Gradle plugin and private signing material were present, but the matching authorized ATAK 5.6 SDK/API dependency was not. No APK was produced by this gate. No device was installed, connected, or touched.

Because acceptance did not pass, v0.2.0 was not tagged, pushed, or published, and no `SHA256SUMS` file was fabricated. The public v0.1.0 release remains unchanged.

## Required unblock

Provide the lawful matching ATAK 5.6.0.CIV SDK/API artifact or repository access required by the takdev plugin, then rerun the complete Gold gate. Only after it passes may the signed APK be inspected, hashed, published with `SHA256SUMS`, and the public assets verified.
