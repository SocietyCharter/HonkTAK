# TAK Third Party Pipeline Test Evidence

HonkTAK uses the owner-supplied ATAK 5.6 SDK Espresso harness through a private
`takEspressoSetup` local property. `ATAKPluginTests-debug.aar`, SDK scripts, ATAK
binaries, and signing material remain outside this repository.

`HonkTakInstrumentedTest` derives from `ATAKTestClass` and packages the following
pipeline scenarios:

- plugin discovery/load and tool UI opening;
- local save with an explicit no-transmission status and local marker assertion;
- a single visible SHARE action, with dispatcher traffic counting and CoT
  validation assigned to the pipeline capture;
- inbound HonkTAK CoT injection and overlay rendering;
- azimuth field rejection at 360;
- stale/expiry and three-sighting FLOCKPOCALYPSE policy;
- disconnected sharing with no save/send and no retry;
- restart/reload persistence behavior;
- zero permissions on the plugin package.

Local JVM tests and `assembleCivDebug`/`assembleCivDebugAndroidTest` may run on a
build host. `connectedCivDebugAndroidTest` must not run without a separately
approved device action because the SDK harness uses ADB to install/start ATAK,
install the plugin and test APK, mutate ATAK test state, and collect results.

Approval must provide both a `safety_record_id` and `approval_id`, name the exact
test device or emulator, authorize temporary installation of the debug plugin
and instrumentation APKs, and authorize ATAK launch/restart plus cleanup. The
request must route through `device_action_request`; compilation alone is not
runtime evidence.

Known pre-execution risk: local marker persistence across ATAK restart is not yet
demonstrated. The pipeline scenario is intended to expose that gap rather than
replace it with a host-side mock.
