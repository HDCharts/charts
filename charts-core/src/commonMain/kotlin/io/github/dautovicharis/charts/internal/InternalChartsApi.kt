package io.github.dautovicharis.charts.internal

/**
 * Marks internal contract APIs shared across modular chart artifacts.
 *
 * These APIs are public for module interoperability but are not stable for direct consumer usage.
 */
@RequiresOptIn(
    message = "Internal Charts API. This may change without notice.",
    level = RequiresOptIn.Level.ERROR,
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.CONSTRUCTOR,
)
@Retention(AnnotationRetention.BINARY)
annotation class InternalChartsApi
