# HonkTAK validation

## v0.2.12 source snapshot

- Plugin API: `5.6.0.CIV`
- Compile SDK: 36
- Target SDK: 34
- Host-side JVM tests: 40/40 passed
- `testCivDebugUnitTest`, `lintCivDebug`, `assembleCivDebug`, and
  `assembleCivDebugAndroidTest`: passed with an ATAK 5.6 CIV SDK
- Packaged permission inventory: exactly `android.permission.INTERNET`, used
  for foreground, user-triggered Overpass reads
- Repository scan: no SDK, signing material, APK, local build configuration,
  or credential is tracked

## Limits of this evidence

The source and build checks do not establish retail ATAK compatibility.
On-device gesture behavior and retail signer/plugin registration remain to be
validated on a compatible test device. No v0.2.x APK is published.

See [`docs/DEVICE_TESTING.md`](docs/DEVICE_TESTING.md) for the device-test
scenarios and safety prerequisites.
