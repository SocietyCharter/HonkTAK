# Device testing

HonkTAK supports an ATAK 5.6 CIV Espresso harness supplied separately from
this repository through the local `takEspressoSetup` Gradle property. ATAK SDK
artifacts, binaries, and signing material are not redistributed here.

The instrumented scenarios cover:

- plugin discovery, load, and tool UI opening;
- local save with no transmission and a local-marker assertion;
- one explicit **SHARE TO TEAM** action with dispatcher and CoT validation;
- inbound HonkTAK CoT rendering;
- invalid azimuth rejection;
- stale/expiry and three-sighting FLOCKPOCALYPSE behavior;
- disconnected sharing with no save, send, or silent retry;
- restart/reload persistence; and
- the plugin permission inventory.

`testCivDebugUnitTest`, `assembleCivDebug`, and
`assembleCivDebugAndroidTest` build and validate artifacts without touching a
device. `connectedCivDebugAndroidTest` uses ADB to install and start ATAK,
install the plugin and test APKs, modify test state, and collect results.
Run it only on an identified test device or emulator where those temporary
changes and cleanup are acceptable.

Local marker persistence across a real ATAK restart still requires device
validation; host-side mocks do not satisfy that check.
