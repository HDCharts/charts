package io.github.hdcharts.core.internal.telemetry

import com.posthog.kmp.ErrorTrackingConfig
import com.posthog.kmp.PersonProfiles
import com.posthog.kmp.PostHog
import com.posthog.kmp.PostHogBeforeSend
import com.posthog.kmp.PostHogConfig
import com.posthog.kmp.PostHogContext
import com.posthog.kmp.PostHogEvent
import com.posthog.kmp.SessionRecordingConfig
import io.github.hdcharts.core.ChartsTelemetry

internal const val CHART_DISPLAYED = "hdcharts_chart_displayed"

internal class PostHogTelemetryClient(
    private val configOverride: ((TelemetryPlatform) -> PostHogConfig)? = null,
) : TelemetryClient {
    private var ownsInstance = false

    override fun setup(platform: TelemetryPlatform) {
        val config = telemetryConfig(platform)
        check(config.projectToken.isNotBlank())
        // The official KMP API exposes one instance. Preserve a host application's existing SDK.
        check(PostHog.getDistinctId().isNullOrBlank()) { "PostHog is already initialized by the application." }
        PostHog.setup(config, platform.context ?: PostHogContext())
        configurePlatformTelemetry()
        ownsInstance = true
    }

    override fun capture(
        chart: String,
        platform: TelemetryPlatform,
    ) {
        if (ownsInstance) PostHog.capture(CHART_DISPLAYED, mapOf("chart_type" to chart))
    }

    override fun setEnabled(enabled: Boolean) {
        if (!ownsInstance) return
        if (enabled) PostHog.optIn() else PostHog.optOut()
    }

    /**
     * One configuration path. The setup-time installation identity is captured by the filter at
     * event time, so a host `identify(...)` cannot change chart-event attribution.
     */
    internal fun telemetryConfig(platform: TelemetryPlatform): PostHogConfig =
        configOverride?.invoke(platform) ?: baseTelemetryConfig(platform)
}

internal fun baseTelemetryConfig(platform: TelemetryPlatform): PostHogConfig =
    PostHogConfig(
        projectToken = TelemetryBuildConfig.POSTHOG_PROJECT_TOKEN,
        host = TelemetryBuildConfig.POSTHOG_HOST,
        captureApplicationLifecycleEvents = false,
        captureScreenViews = false,
        captureDeepLinks = false,
        capturePushNotificationSubscriptions = false,
        capturePushNotificationOpened = false,
        sendFeatureFlagEvent = false,
        preloadFeatureFlags = false,
        personProfiles = PersonProfiles.NEVER,
        sessionRecording = SessionRecordingConfig(enabled = false),
        autocapture = false,
        errorTracking = ErrorTrackingConfig(autoCapture = false),
        beforeSend =
            listOf(
                PostHogBeforeSend {
                    // Consent and identity are re-read at event time: opt-out applies to queued
                    // events, and attribution follows the persistent installation identity rather
                    // than a host application's later `identify(...)`.
                    filterTelemetryEvent(it, platform, ChartsTelemetry.enabled, PostHog.getAnonymousId())
                },
            ),
    )

internal fun filterTelemetryEvent(
    event: PostHogEvent,
    platform: TelemetryPlatform,
    enabled: Boolean,
    installationId: String? = event.distinctId,
): PostHogEvent? {
    if (!enabled || event.event != CHART_DISPLAYED) return null
    return event.copy(
        // Attribute chart usage to the persistent installation identity, never a host user.
        distinctId = installationId ?: event.distinctId,
        properties =
            mapOf(
                "chart_type" to event.properties["chart_type"],
                // Keep installation attribution separate from a host application's identity.
                "installation_id" to installationId,
                "platform" to platform.name,
                "hdcharts_version" to TelemetryBuildConfig.CHARTS_VERSION,
                "token" to event.properties["token"],
                "\$lib" to event.properties["\$lib"],
                "\$lib_version" to event.properties["\$lib_version"],
                "\$process_person_profile" to false,
                "\$geoip_disable" to true,
            ),
    )
}

/** Extra native SDK privacy settings not exposed by the KMP configuration. */
internal expect fun configurePlatformTelemetry()
