package io.github.hdcharts.sampleapi

/** A sample API could not be reached or sent an unreadable answer. */
class ApiUnavailableException(
    cause: Throwable,
) : Exception("Sample API data is unavailable", cause)
