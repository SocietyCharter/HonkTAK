# Changelog

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

## 0.1.0

- Initial public source release with local goose sightings, expiry, randomized
  SITREPs, and FLOCKPOCALYPSE.
