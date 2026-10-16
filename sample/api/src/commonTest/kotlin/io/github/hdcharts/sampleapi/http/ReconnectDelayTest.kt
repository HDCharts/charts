package io.github.hdcharts.sampleapi.http

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ReconnectDelayTest {
    @Test
    fun reconnectDelay_doublesUntilOneMinute() {
        assertEquals(
            expected = listOf(2, 4, 8, 16, 32, 60, 60).map { it.seconds },
            actual = (0..6).map(::reconnectDelay),
        )
    }
}
