# HonkTAK — Tactical Goose Awareness System

## Release status

Public `main` contains the latest v0.2.12 development source. The immutable
v0.1.0 tag remains the latest broadly installable release; no v0.2.x APK is
published because on-device validation and retail ATAK signer trust remain unresolved.

The validated developer path is **Developer ATAK 5.6.0.CIV Debug** on Android
API 36. Retail/Play Store ATAK rejects the SDK development signer, so retail
plugin trust remains unresolved. HonkTAK needs no separate server or
configuration for local markers. **SHARE TO TEAM** uses ATAK's existing TAK
connection; when ATAK is disconnected, HonkTAK reports failure and performs no
silent retry.

HonkTAK maps user-observed camera locations with separate **SAVE LOCALLY** and
**SHARE TO TEAM** actions. Sightings use a custom goose icon and **Unidentified
Waterfowl** label; they never use friendly, hostile, or other affiliation
symbology.

![HonkTAK local overlay mockup](docs/mockup.svg)

## Network behavior and safety boundary

**SHARE TO TEAM sends the completed observation off-device through ATAK's currently connected TAK network.** It uses the public ATAK external CoT dispatcher—no direct sockets or alternate transport. Transmission occurs only after the user presses the visibly labeled share action; opening the form and saving locally never transmit. Incoming HonkTAK CoT is bounded, validated, and rendered in the HonkTAK overlay.

HonkTAK does not access camera feeds, discover devices, scan Wi-Fi/Bluetooth, perform recognition, collect identifiers/contacts/device IDs, write mission packages, control UAS systems, or mutate real mission data. Local removal is local only and does not send a remote delete. The v0.2.12 source requests exactly `android.permission.INTERNET` for foreground, user-triggered Overpass reads; it performs no background polling or upstream OSM/DeFlock writes.

## Features

- Foreground viewport-bounded OSM ALPR import. Tap or long-press an imported goose or its FOV wedge to reopen the action panel and mark it **DEFEATED — LOCAL ONLY**; **MARK ACTIVE** is the undo.
- All goose markers—newly placed, locally restored, inbound, active imported, and defeated imported—render at approximately half their v0.2.10 on-map size. Defeated imported geese retain their smaller relative styling, stable OSM-node persistence, red X, desaturation, wedge suppression, local-only behavior, and MARK ACTIVE undo.

- Gesture placement: press **REPORT HONK**, long-press the camera location on
  the map, drag to aim the 45-degree coverage wedge, and release to return to
  the observation form.
- The drag sets true-bearing azimuth and a clamped 10–500 m range. Nothing is
  persisted or transmitted until **SAVE LOCALLY** or **SHARE TO TEAM** is
  pressed.
- Four randomized local SITREPs.
- `FLOCKPOCALYPSE` when three active sightings fall within 500 m of any active sighting.
- Camera class, optional azimuth, confidence, status, bounded notes, and observed time.
- Permanent, always-active local markers by default, restored when the plugin reloads.
- Explicit Temporary selection reveals a configurable expiry from 1 minute to 7 days; permanent markers never inherit that timer.
- A disabled DeFlock submission action reserves a clean UI boundary for the researched OpenStreetMap path without implementing submission.
- Optional audio setting is disabled by default. v0.1.0 intentionally bundles no audio asset, so the control remains disabled.

## Public source authority

This project was derived from the public `plugin-examples/plugintemplate` in [`deptofdefense/AndroidTacticalAssaultKit-CIV`](https://github.com/deptofdefense/AndroidTacticalAssaultKit-CIV) at commit `889eee292c43d3d2eafdd1f2fbf378ad5cd89ecc`, tag `4.6.0.5`, dated 2024-10-18. Compatibility validation additionally used an ATAK 5.6.0 CIV SDK supplied outside this repository; that SDK is not redistributable and is not included. No TAK.gov SDK, UAS Tool artifact, credential, or signing key is included in this repository.

## Use

1. Press **REPORT HONK** to enter exclusive map-placement mode.
2. Long-press the desired camera location, keep holding, and drag to aim the
   live coverage wedge.
3. Release to finalize the pending anchor, azimuth, range, and 45-degree FOV.
4. Complete the form, then choose **SAVE LOCALLY** or explicitly choose
   **SHARE TO TEAM**. **CANCEL PLACEMENT** restores normal map interaction.

Permanent local observations and their wedges restore when HonkTAK reloads.
Explicitly temporary observations and their wedges expire together. Incoming
validated HonkTAK CoT recreates the same wedge. Older HonkTAK events without
range/FOV or lifetime fields use bounded legacy defaults.

## Build status

The source declares Plugin API `5.6.0.CIV` and was compiled through the
Developer ATAK 5.6 SDK debug path. Host tests and build-time inspection pass.
Gesture execution on a device is not yet validated. Retail
ATAK signer trust is unresolved and is not claimed by this source snapshot.

Build prerequisites are Android SDK 36, Java 17-compatible bytecode tooling, and a licensed ATAK `5.6.0.CIV` SDK/devkit stored outside the repository. Create an untracked `local.properties` with local SDK paths and signing-key references. Never commit it. Do not substitute the public 4.6 devkit, reverse-engineer the Play Store APK, or redistribute SDK material.

Run the developer build with:

```text
./gradlew assembleCivDebug
```

## Install

Development builds are installed manually. After independently verifying the
APK hash and signer, install it using the normal ATAK-CIV plugin process. SDK
compile compatibility is validated for Plugin API `5.6.0.CIV`; device/runtime
compatibility must still be confirmed on a compatible test device.

Expected Android warnings are limited to the source-specific **Install unknown
apps** prompt above and the standard package-installer confirmation. A Play
Protect scan prompt may also appear depending on the phone's policy. HonkTAK
must not be described as runtime-tested or fully installable until ATAK 5.6
accepts its standalone signing certificate and plugin registration in a device
test.

## Tests

Host-side unit tests cover explicit one-shot share gating, no silent sends, CoT
serialization/receive parsing, malformed/oversized/stale/range rejection,
expiry, azimuth and placement range/FOV bounds, listener-session state,
backward-compatible wedge fields, local-only construction, and FLOCKPOCALYPSE.
The v0.2.12 validation scope is 40 passing JVM tests plus the complete
`testCivDebugUnitTest`, `lintCivDebug`, `assembleCivDebug`, and
`assembleCivDebugAndroidTest` Gradle gate, API/signer/permission inspection,
and packaged loader-descriptor comparison. The tests include deterministic
coverage of viewport-bounded import, selection/action availability,
defeated-versus-active style and scale, persistence/undo, wedge suppression,
and local-only/no-upstream-write policy.
On-device gesture behavior remains outside that evidence.

See [`VALIDATION.md`](VALIDATION.md) and
[`docs/DEVICE_TESTING.md`](docs/DEVICE_TESTING.md) for reproducible validation
scope and device-test prerequisites.

## Uninstall / rollback

In ATAK, remove or disable HonkTAK from **Tool Manager** if that option is available. Then open Android **Settings → Apps → HonkTAK → Uninstall** and restart ATAK. Disabling or unloading removes the rendered overlay while preserving permanent plugin-local records for reload; uninstalling clears the plugin's private records. To roll back, uninstall the current plugin and install a separately verified earlier artifact only if it is compatible with the target ATAK build.

## License

GPL-3.0-only, matching the upstream public template. See `LICENSE` and `NOTICE`.
