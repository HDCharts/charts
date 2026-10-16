package io.github.hdcharts.sampleapi.http

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Doubles from 2 seconds per failed attempt, capped at a minute. */
internal fun reconnectDelay(failedAttempts: Int): Duration =
    (2.seconds * (1 shl failedAttempts.coerceIn(0, 5))).coerceAtMost(60.seconds)
