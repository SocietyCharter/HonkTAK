# HonkTAK v0.2.12 development source

Adds an obvious imported-goose selection workflow: tap or long-press a goose or its
FOV wedge to reveal **MARK DEFEATED — LOCAL ONLY**, with **MARK ACTIVE** as undo.
Defeated state persists locally by stable OSM source ID and suppresses the wedge.
All goose markers—newly placed, restored, inbound, active imported, and defeated imported—use global render
dimensions approximately half their v0.2.10 size. Defeated geese retain their
desaturated red-X treatment and smaller relative factor.
These actions never emit CoT or write to OSM/DeFlock.

This is a source-only development snapshot. It is not an APK release and does
not claim retail ATAK compatibility. Device gesture validation and a trusted
production signing path remain outstanding.

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

## Changes through v0.2.12

- Adds bounded camera-observation fields and explicit local/share actions.
- Adds validated HonkTAK CoT send/receive with range and FOV metadata.
- Adds gesture placement and live `SensorFOV` coverage preview.
- Makes new observations permanent and reloadable by default.
- Requires explicit Temporary selection before showing or applying expiry.
- Expires explicitly temporary observation markers and wedges together.
- Preserves the v0.2.7 custom goose icon contract.
- Reserves a disabled DeFlock/OSM submission button without implementing submission.
- Adds foreground, viewport-bounded OSM ALPR import through bounded Overpass reads.
- Makes imported geese and FOV wedges selectable by tap or long-press.
- Adds persistent local **MARK DEFEATED — LOCAL ONLY** state with **MARK ACTIVE** undo.
- Halves the shared render dimensions used by newly placed, restored, inbound, active imported, and defeated imported geese relative to v0.2.10.
- Preserves the defeated goose's desaturated red-X treatment, smaller relative factor, and wedge suppression.
- Keeps import and defeated/active actions free of CoT dispatch and upstream OSM/DeFlock writes.
- Preserves backward compatibility for events without range/FOV metadata.
- Preserves randomized goose SITREPs and FLOCKPOCALYPSE behavior.

## Evidence scope

- 40 host-side JVM tests passed; they inventory every goose render path, verify the v0.2.12-to-v0.2.10 half-size ratio, and preserve defeated relative styling.
- The complete `testCivDebugUnitTest`, `lintCivDebug`, `assembleCivDebug`, and
  `assembleCivDebugAndroidTest` gate passed against the owner-authorized ATAK
  5.6 SDK.
- Plugin API `5.6.0.CIV`, the sole `android.permission.INTERNET` permission,
  SDK development signer, and packaged loader descriptor were inspected.
- No device/emulator gesture execution is included in this evidence.

## Compatibility limitation

The known-loading development configuration is Developer ATAK 5.6.0.CIV Debug
with its matching SDK development signer. Retail/Play Store ATAK trust is
unresolved and must not be claimed. Earlier v0.2.x APKs were rejected or
private test artifacts and are not release assets.
