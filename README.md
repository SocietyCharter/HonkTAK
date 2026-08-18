# HonkTAK — Tactical Goose Awareness System

HonkTAK is a clearly labeled joke plugin for ATAK-CIV. It adds an isolated, ephemeral `HonkTAK Sightings` overlay and a **REPORT HONK** button. Sightings use a custom goose icon and the label **Unidentified Waterfowl**; they never use friendly, hostile, or other military symbology.

![HonkTAK local overlay mockup](docs/mockup.svg)

## Safety boundary

HonkTAK does not send network CoT, write mission packages, control UAS systems, emit operational alerts, or modify real mission data. Every marker is tagged `nevercot`, is not archived, and is removed automatically. No Android/device or hardware action is part of the build or test workflow.

## Features

- Four randomized local SITREPs.
- `FLOCKPOCALYPSE` when three active sightings fall within 500 m of any active sighting.
- Configurable expiry from 1 to 1,440 minutes (30 minutes by default).
- Optional audio setting is disabled by default. v0.1.0 intentionally bundles no audio asset, so the control remains disabled.

## Public source authority

This project was derived from the public `plugin-examples/plugintemplate` in [`deptofdefense/AndroidTacticalAssaultKit-CIV`](https://github.com/deptofdefense/AndroidTacticalAssaultKit-CIV) at commit `889eee292c43d3d2eafdd1f2fbf378ad5cd89ecc`, tag `4.6.0.5`, dated 2024-10-18. No TAK.gov-authenticated SDK, UAS Tool artifact, private package, credential, or signing key is included.

## Build

Prerequisites: JDK 11, Android SDK/build-tools 30.0.2, and the public ATAK-CIV `atak-gradle-takdev.jar` built from the recorded upstream source. Create an untracked `local.properties` with `sdk.dir`, `takdev.plugin`, and your own debug signing-key paths/passwords. Never commit it. HonkTAK intentionally does not support authenticated TAK artifact repositories.

Run only the allowlisted wrapper task:

```text
./gradlew assembleDebug
```

The guarded Society build surface uses `plugin_build(project_path, task=assembleDebug)` and does not install the result.

## Install

Installation is deliberately outside this repository's automated workflow. After independently verifying the APK hash and signer, an authorized operator may install it using their normal ATAK-CIV plugin process. HonkTAK compatibility targets ATAK-CIV 4.6.0.

## Tests

Host-side unit tests cover expiry, proximity/count threshold, disabled audio default, and the local-only/no-CoT policy. The source can also be checked with plain `javac` for the pure policy classes when the Android toolchain is unavailable.

## Removal / rollback

Uninstall HonkTAK through the authorized ATAK/Android plugin-management flow, then restart ATAK. Removing the plugin clears its in-memory overlay on shutdown; its markers are never archived. To remove source, delete the local clone. To remove a GitHub release, delete the `v0.1.0` release and tag, then archive or delete the repository using the GitHub owner account.

## License

GPL-3.0-only, matching the upstream public template. See `LICENSE` and `NOTICE`.
