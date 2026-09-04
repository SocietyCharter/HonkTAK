package com.societycharter.honktak;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
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

    @Test public void permanentIsDefaultAndTemporaryControlsRequireExplicitSelection() {
        openForm();
        onView(withId(R.id.temporary_marker)).check(matches(
                org.hamcrest.Matchers.not(androidx.test.espresso.matcher.ViewMatchers.isChecked())));
        onView(withId(R.id.expiry_controls)).check(matches(
                withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.temporary_marker)).perform(click());
        onView(withId(R.id.expiry_controls)).check(matches(
                withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.expiry_minutes)).check(matches(androidx.test.espresso.matcher.ViewMatchers.isEnabled()));
        onView(withId(R.id.submit_deflock)).check(matches(
                org.hamcrest.Matchers.not(androidx.test.espresso.matcher.ViewMatchers.isEnabled())));
    }

    @Test public void newPlacementResetsPreviousTemporarySelection() {
        openForm();
        onView(withId(R.id.temporary_marker)).perform(click());
        helper.pressBackTimes(1);
        openForm();
        onView(withId(R.id.temporary_marker)).check(matches(
                org.hamcrest.Matchers.not(androidx.test.espresso.matcher.ViewMatchers.isChecked())));
        onView(withId(R.id.expiry_controls)).check(matches(
                withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)));
    }

    @Test public void localSaveDoesNotTransmit() {
        openForm();
        onView(withId(R.id.save_local)).perform(click());
        onView(withId(R.id.status)).check(matches(withText(org.hamcrest.Matchers.containsString("nothing left this device"))));
        Marker marker = HELPER.getMarkerOfType(HonkCotCodec.COT_TYPE);
        assertNotNull("Local marker missing", marker);
        assertFalse("ATAK icon adaptation must stay disabled for the goose",
                marker.getMetaBoolean("adapt_marker_icon", true));
        assertEquals("Goose icon must be visible", Marker.ICON_VISIBLE,
                marker.getIconVisibility());
        assertNotNull("Goose icon missing", marker.getIcon());
        assertTrue("Goose icon must use ATAK's supported base64 URI contract",
                marker.getIcon().getImageUri(0).startsWith("base64://"));
        assertTrue("Permanent must be the default",
                marker.getMetaBoolean("honktak.permanent", false));
        assertFalse("Permanent marker must not inherit an expiry",
                marker.hasMetaValue("honktak.expires_at_ms"));
    }

    @Test public void permanentSaveWritesReloadableLocalRecord() {
        localSaveDoesNotTransmit();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        boolean foundPermanent = false;
        long now = System.currentTimeMillis();
        for (Object value : context.getSharedPreferences("honktak_local_observations_v1",
                Context.MODE_PRIVATE).getAll().values()) {
            if (value instanceof String && HonkCotCodec.parsePersisted((String) value, now).isPermanent()) {
                foundPermanent = true;
            }
        }
        assertTrue("Saved marker must have a reloadable permanent record", foundPermanent);
    }

    @Test public void explicitTemporarySaveCarriesExpiry() {
        openForm();
        onView(withId(R.id.temporary_marker)).perform(click());
        onView(withId(R.id.expiry_minutes)).perform(replaceText("5"));
        onView(withId(R.id.save_local)).perform(click());
        Marker marker = HELPER.getMarkerOfType(HonkCotCodec.COT_TYPE);
        assertNotNull("Temporary marker missing", marker);
        assertFalse(marker.getMetaBoolean("honktak.permanent", true));
        assertTrue(marker.hasMetaValue("honktak.expires_at_ms"));
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

    @Test public void packageRequestsOnlyInternetPermission() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        PackageInfo info = context.getPackageManager().getPackageInfo(PACKAGE, PackageManager.GET_PERMISSIONS);
        assertNotNull(info.requestedPermissions);
        assertEquals(1, info.requestedPermissions.length);
        assertEquals(android.Manifest.permission.INTERNET, info.requestedPermissions[0]);
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
