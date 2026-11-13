import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Serializes remote Maven uploads.
 *
 * Maven Central throttles bursts of snapshot uploads with HTTP 429, and Gradle's
 * built-in retry (3 attempts, 1s initial backoff) gives up inside the throttle
 * window. Publishing runs with the full worker count; this service caps how many
 * `PublishToMavenRepository` tasks may run at once, so at most one publication
 * uploads while compilation, signing, and packaging stay parallel.
 *
 * `PublishToMavenLocal` is a different task type and is not throttled.
 */
abstract class MavenUploadThrottle :
    BuildService<BuildServiceParameters.None>,
    AutoCloseable {
    override fun close() = Unit
}
