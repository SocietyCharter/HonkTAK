# HonkTAK — Tactical Goose Awareness System

HonkTAK is a clearly labeled joke plugin for ATAK-CIV. v0.2.0 portable source adds user-entered camera-location observations with separate **SAVE LOCALLY** and **SHARE TO TEAM** actions. Sightings use a custom goose icon and **Unidentified Waterfowl** label; they never use friendly, hostile, or other affiliation symbology.

![HonkTAK local overlay mockup](docs/mockup.svg)

## Network behavior and safety boundary

**SHARE TO TEAM sends the completed observation off-device through ATAK's currently connected TAK network.** It uses the public ATAK external CoT dispatcher—no direct sockets or alternate transport. Transmission occurs only after the user presses the visibly labeled share action; opening the form and saving locally never transmit. Incoming HonkTAK CoT is bounded, validated, and rendered in the HonkTAK overlay.

HonkTAK does not access camera feeds, discover devices, scan Wi-Fi/Bluetooth, perform recognition, collect identifiers/contacts/device IDs, write mission packages, control UAS systems, or mutate real mission data. Local removal is local only and does not send a remote delete. No new Android permission is requested.

## Features

- Four randomized local SITREPs.
- `FLOCKPOCALYPSE` when three active sightings fall within 500 m of any active sighting.
- Camera class, optional azimuth, confidence, status, bounded notes, observed time, and configurable expiry.
- Configurable expiry from 1 minute to 7 days (30 minutes by default); shared CoT stale time and local cleanup match.
- Optional audio setting is disabled by default. v0.1.0 intentionally bundles no audio asset, so the control remains disabled.

## Public source authority

This project was derived from the public `plugin-examples/plugintemplate` in [`deptofdefense/AndroidTacticalAssaultKit-CIV`](https://github.com/deptofdefense/AndroidTacticalAssaultKit-CIV) at commit `889eee292c43d3d2eafdd1f2fbf378ad5cd89ecc`, tag `4.6.0.5`, dated 2024-10-18. No TAK.gov-authenticated SDK, UAS Tool artifact, private package, credential, or signing key is included.

## Build status

The installed target is ATAK `v5.6.0.12`, source identifier `9c9a5897`, Play Store build `1769863102`, requiring Plugin API `5.6.0.CIV` on Android API 36. v0.2.0 is portable source and is **not yet claimed binary-compatible or released**. The official public `deptofdefense/AndroidTacticalAssaultKit-CIV` tags end at `4.6.0.5`; that public devkit must not be used to build an APK for this 5.6 target.

Prerequisites after Jesse provides the authorized SDK: the toolchain required by that SDK and an authorized ATAK `5.6.0.CIV` plugin SDK/devkit. Create an untracked `local.properties` with SDK paths and owner-only signing-key references. Never commit it. Do not substitute the public 4.6 devkit, reverse-engineer the Play Store APK, or obtain authenticated TAK.gov material indirectly.

Run only the allowlisted wrapper task:

```text
./gradlew assembleDebug
```

The guarded Society build surface uses `plugin_build(project_path, task=assembleDebug)` and does not install the result.

## Install

Installation is deliberately outside this repository's automated workflow. After independently verifying the APK hash and signer, an authorized operator may install it using their normal ATAK-CIV plugin process. The portable source is derived from the public ATAK-CIV 4.6.0.5 template, but no binary compatibility claim is made until it is built and tested with the authorized matching 5.6.0.CIV plugin SDK/devkit.

## Tests

Host-side unit tests cover explicit one-shot share gating, no silent sends, CoT serialization/receive parsing, malformed/oversized/stale/range rejection, expiry, azimuth bounds, local-only construction, and FLOCKPOCALYPSE.

## Removal / rollback

Uninstall HonkTAK through the authorized ATAK/Android plugin-management flow, then restart ATAK. Removing the plugin clears its in-memory overlay on shutdown; its markers are never archived. To remove source, delete the local clone. To remove a GitHub release, delete the `v0.1.0` release and tag, then archive or delete the repository using the GitHub owner account.

## License

GPL-3.0-only, matching the upstream public template. See `LICENSE` and `NOTICE`.
