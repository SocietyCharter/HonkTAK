# Changelog

## 0.2.12

- Apply one global half-size policy to newly placed, restored, inbound, active imported, and defeated imported goose render paths.
- Preserve the defeated goose's grey/red-X treatment, smaller relative factor, wedge suppression, persistent state, and MARK ACTIVE undo.
- Keep explicit positive ATAK icon dimensions so the renderer does not fall back to source-bitmap sizing.

## 0.2.11

- Make manual, active imported, and defeated imported geese approximately half their v0.2.10 rendered size through one shared dimension policy.
- Preserve the grey/red-X style, wedge suppression, stable-ID persistence, undo, and local-only boundary.
- Preserve the defeated goose's 0.82 relative size factor and drive all smaller results through explicit positive ATAK `Icon.setSize` inputs rather than source-bitmap resizing.

## 0.2.10

- Make imported geese and their FOV wedges explicitly selectable by tap or long-press.
- Reopen/reveal the action panel with a visible `MARK DEFEATED — LOCAL ONLY` control and `MARK ACTIVE` undo.
- Render defeated red-X geese desaturated and 18% smaller while preserving stable-ID persistence and wedge suppression.
- Retain local-only mark actions with no CoT or upstream OSM/DeFlock write path.
- Verify 38 host-side JVM tests and the full unit-test, lint, main-APK, and Android-test-APK Gradle gate.
- Package exactly the `android.permission.INTERNET` permission for foreground Overpass reads.

## 0.2.6 — source candidate

- Added exclusive long-press/drag/release map placement.
- Added live, local-only 45-degree camera coverage wedges.
- Added bounded range and FOV fields to local observations and HonkTAK CoT.
- Added marker/wedge lifecycle coupling and inbound wedge reconstruction.
- Added placement math, cancellation/session, codec compatibility, and
  no-share-before-explicit-action tests.
- Retained zero Android permissions and explicit-only network sharing.

Compatibility: Developer ATAK 5.6.0.CIV Debug is the validated development
path. Retail ATAK plugin trust and on-device gesture validation remain pending.

## 0.2.9

- Add foreground viewport-bounded OSM ALPR import with bounded response handling.
- Add persistent local taken-down/active overrides, red-X goose rendering, and wedge suppression.
- Add the INTERNET permission required for user-triggered Overpass reads; no background polling or upstream writes.

## 0.2.8

- Make saved camera observations permanent and reloadable by default.
- Require explicit Temporary selection before exposing or applying an expiry.
- Reserve a disabled, explicit DeFlock submission action for the researched OSM path.

## 0.2.7

- Preserve the packaged goose graphic on saved markers by disabling ATAK's
  automatic CoT-type icon adaptation and explicitly keeping the icon visible.
- Extend the ATAK-hosted local-save regression test to verify the marker icon
  contract and encoded image URI.

## 0.1.0

- Initial public source release with local goose sightings, expiry, randomized
  SITREPs, and FLOCKPOCALYPSE.
