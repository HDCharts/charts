package io.github.hdcharts.core.internal.telemetry

import com.posthog.kmp.PersonProfiles
import com.posthog.kmp.PostHogEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PostHogTelemetryClientTest {
    private val platform = TelemetryPlatform("android")

    @Test
    fun appleSetupFailsClosedUntilNativeSurveysCanBeDisabled() {
        val failure =
            assertFailsWith<IllegalStateException> {
                PostHogTelemetryClient().setup(TelemetryPlatform("ios"))
            }
        assertEquals("PostHog KMP cannot disable the native iOS survey integration.", failure.message)
    }

    @Test
    fun capturePreservesInstallationIdentityAndStripsUnrelatedProperties() {
        val incoming =
            PostHogEvent(
                CHART_DISPLAYED,
                "persistent-installation-id",
                mapOf(
                    "chart_type" to "line",
                    "application_id" to "dev.example.private",
                    "\$app_namespace" to "dev.example.private",
                    "\$app_name" to "Private app",
                    "\$host" to "private.example.com",
                    "\$referrer" to "https://private.example.com",
                    "\$device_id" to "device-id",
                    "\$session_id" to "session-id",
                    "\$current_url" to "https://example.com/private?token=secret",
                    "\$os_version" to "private-device-context",
                    "email" to "user@example.com",
                    "title" to "Private chart",
                    "values" to listOf(1, 2),
                    "\$lib" to "posthog-kmp",
                    "\$lib_version" to "0.10.0",
                ),
            )

        val filtered = assertNotNull(filterTelemetryEvent(incoming, platform, enabled = true))

        assertEquals("persistent-installation-id", filtered.distinctId)
        assertEquals("persistent-installation-id", filtered.properties["installation_id"])
        assertEquals(
            mapOf(
                "chart_type" to "line",
                "installation_id" to "persistent-installation-id",
                "platform" to "android",
                "hdcharts_version" to TelemetryBuildConfig.CHARTS_VERSION,
                "\$lib" to "posthog-kmp",
                "\$lib_version" to "0.10.0",
                "\$process_person_profile" to false,
                "\$geoip_disable" to true,
            ),
            filtered.properties,
        )
    }

    @Test
    fun optedOutAndUnrelatedEventsAreDropped() {
        assertNull(filterTelemetryEvent(PostHogEvent(CHART_DISPLAYED, "id", emptyMap()), platform, enabled = false))
        listOf("\$snapshot", "\$autocapture", "\$screen", "\$identify", "\$exception").forEach { event ->
            assertNull(filterTelemetryEvent(PostHogEvent(event, "id", emptyMap()), platform, enabled = true))
        }
    }

    @Test
    fun filteredEventPreservesExplicitInstallationIdWhenSdkIdentityChanges() {
        val filtered =
            assertNotNull(
                filterTelemetryEvent(
                    PostHogEvent(CHART_DISPLAYED, "host-user", emptyMap()),
                    platform,
                    enabled = true,
                    installationId = "persistent-installation-id",
                ),
            )

        assertEquals("persistent-installation-id", filtered.distinctId)
        assertEquals("persistent-installation-id", filtered.properties["installation_id"])
    }

    @Test
    fun sdkConfigurationDisablesUnrelatedFeatures() {
        val config = PostHogTelemetryClient().telemetryConfig(platform)

        assertFalse(config.captureApplicationLifecycleEvents)
        assertFalse(config.captureScreenViews)
        assertFalse(config.captureDeepLinks)
        assertFalse(config.capturePushNotificationSubscriptions)
        assertFalse(config.capturePushNotificationOpened)
        assertFalse(config.autocapture)
        assertFalse(config.sendFeatureFlagEvent)
        assertFalse(config.preloadFeatureFlags)
        assertEquals(PersonProfiles.NEVER, config.personProfiles)
        assertEquals(false, config.sessionRecording?.enabled)
        assertEquals(false, config.errorTracking?.autoCapture)
    }
}
