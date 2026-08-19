# HonkTAK v0.2.6 source candidate

This is a source-only candidate branch. Do not publish an APK, create a tag or
release, or merge it to `main` until the gesture workflow passes Jesse's
on-device validation and the project acceptance gate is satisfied.

## Usage

1. Press **REPORT HONK**.
2. Long-press the desired camera location, drag to aim the live wedge, and
   release.
3. Complete the observation form.
4. Choose **SAVE LOCALLY** or explicitly choose **SHARE TO TEAM**.

Placement uses the gesture anchor, true-bearing azimuth, a clamped 10–500 m
range, and a 45-degree initial FOV. Cancel, dropdown close, release, and plugin
dispose restore ATAK map listeners/tool state. The preview is local-only and
never-CoT; persistence or transmission requires an explicit final action.

## Changes since v0.1.0

- Adds bounded camera-observation fields and explicit local/share actions.
- Adds validated HonkTAK CoT send/receive with range and FOV metadata.
- Adds gesture placement and live `SensorFOV` coverage preview.
- Expires observation markers and wedges together.
- Preserves backward compatibility for events without range/FOV metadata.
- Preserves randomized goose SITREPs and FLOCKPOCALYPSE behavior.

## Evidence scope

- 18 host-side JVM tests passed.
- Guarded `civDebug` build passed against the owner-authorized ATAK 5.6 SDK.
- Plugin API `5.6.0.CIV`, zero permissions, SDK development signer, and loader
  descriptor were inspected.
- No device/emulator gesture execution is included in this evidence.

## Compatibility limitation

The known-loading development configuration is Developer ATAK 5.6.0.CIV Debug
with its matching SDK development signer. Retail/Play Store ATAK trust is
unresolved and must not be claimed. Earlier v0.2.x APKs were rejected or
private test artifacts and are not release assets.
