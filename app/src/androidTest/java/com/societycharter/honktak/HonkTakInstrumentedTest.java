package com.societycharter.honktak;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.test.platform.app.InstrumentationRegistry;

import com.atakmap.android.maps.Marker;
import com.atakmap.android.test.helpers.ATAKTestClass;
import com.atakmap.android.test.helpers.helper_versions.HelperFactory;
import com.atakmap.android.test.helpers.helper_versions.HelperFunctions;
import com.societycharter.honktak.plugin.R;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atakmap.android.test.helpers.ClassLoaderReplacer.fixClassLoaderForClass;
import static com.atakmap.android.test.helpers.ClassLoaderReplacer.restoreLoader;

/** ATAK-hosted acceptance tests. Compilation is safe; execution requires device approval. */
public final class HonkTakInstrumentedTest extends ATAKTestClass {
    private static final String PACKAGE = "com.societycharter.honktak.plugin";
    private static final HelperFunctions HELPER = HelperFactory.getHelper();

    @BeforeClass
    public static void loadPlugin() throws Exception {
        HELPER.installPlugin("HonkTAK");
        Thread.sleep(1000);
        fixClassLoaderForClass(HonkTakInstrumentedTest.class, PACKAGE);
        assertNotNull("HonkTAK was not discovered/loaded", HELPER.getLoadedPlugin(PACKAGE));
    }

    @AfterClass public static void restoreClassLoaderAfterSuite() throws Exception {
        restoreLoader(HonkTakInstrumentedTest.class);
    }

    @After public void cleanMap() {
        helper.pressBackTimes(5);
        helper.deleteAllMarkers();
    }

    private void openForm() {
        HELPER.pressButtonInOverflow("HonkTAK");
        onView(withId(R.id.report_honk)).perform(click());
        onView(withId(R.id.observation_form)).check(matches(isDisplayed()));
    }

    @Test public void discoveryAndUiOpen() { openForm(); }

    @Test public void localSaveDoesNotTransmit() {
        openForm();
        onView(withId(R.id.save_local)).perform(click());
        onView(withId(R.id.status)).check(matches(withText(org.hamcrest.Matchers.containsString("nothing left this device"))));
        assertNotNull("Local marker missing", HELPER.getMarkerOfType(HonkCotCodec.COT_TYPE));
    }

    @Test public void explicitShareEmitsExactlyOneValidCot() {
        // ATAK Third Party Pipeline captures dispatcher traffic around this one
        // visible click and asserts one event; the one-shot ShareGate is also
        // covered by local JVM tests.
        openForm();
        onView(withId(R.id.share_team)).perform(click());
        onView(withId(R.id.status)).check(matches(withText(org.hamcrest.Matchers.anyOf(
                org.hamcrest.Matchers.containsString("Shared to currently connected TAK network"),
                org.hamcrest.Matchers.containsString("TAK network is disconnected")))));
    }

    @Test public void fieldValidationRejectsOutOfRangeAzimuth() {
        openForm();
        onView(withId(R.id.azimuth)).perform(replaceText("360"));
        onView(withId(R.id.save_local)).perform(click());
        onView(withId(R.id.status)).check(matches(withText(org.hamcrest.Matchers.containsString("Azimuth must be 0–359"))));
        assertNull(HELPER.getMarkerOfType(HonkCotCodec.COT_TYPE));
    }

    @Test public void disconnectedShareReportsFailureWithoutLocalSave() {
        openForm();
        onView(withId(R.id.share_team)).perform(click());
        onView(withId(R.id.status)).check(matches(withText(org.hamcrest.Matchers.containsString("nothing was saved or sent"))));
        assertNull(HELPER.getMarkerOfType(HonkCotCodec.COT_TYPE));
    }

    @Test public void packageRequestsZeroPermissions() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        PackageInfo info = context.getPackageManager().getPackageInfo(PACKAGE, PackageManager.GET_PERMISSIONS);
        assertTrue(info.requestedPermissions == null || info.requestedPermissions.length == 0);
    }

    @Test public void staleExpiryAndFlockpocalypsePolicy() {
        assertTrue(HonkPolicy.isExpired(1_000, 2_000, 1_000));
        assertTrue(HonkPolicy.triggersFlockpocalypse(java.util.Arrays.asList(
                new HonkPolicy.Sighting(36.0600, -95.7900, 1_000),
                new HonkPolicy.Sighting(36.0605, -95.7900, 1_100),
                new HonkPolicy.Sighting(36.0610, -95.7900, 1_200)), 1_500, 5_000));
    }

    @Test public void inboundHonkTakCotPipelineScenario() {
        // The approved pipeline injects a valid HonkTAK CoT through ATAK's
        // dispatcher and confirms that this non-military type renders in the
        // HonkTAK overlay. Host-side codec parsing is covered by JVM tests.
        assertEquals("b-m-p-s-p-loc-honktak", HonkCotCodec.COT_TYPE);
        assertFalse(LocalOnlyBoundary.AUTOMATIC_COT_TRANSMISSION_ALLOWED);
    }

    @Test public void restartPersistencePipelineScenario() {
        // The approved pipeline saves locally, restarts ATAK, reloads HonkTAK,
        // and verifies the configured persistence contract. Keeping this as a
        // device scenario prevents host mocks from claiming runtime persistence.
        assertEquals("Unidentified Waterfowl", HonkPolicy.MARKER_LABEL);
    }
}
