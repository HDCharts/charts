package io.github.hdcharts.app.demo.earthquake

import io.github.hdcharts.sampleapi.earthquake.Continent
import io.github.hdcharts.sampleapi.earthquake.Earthquake
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** Quakes per UTC day for each continent, one count per day from [firstDay] on. */
internal data class DailyCounts(
    val firstDay: LocalDate,
    val days: Int,
    val counts: Map<Continent, List<Int>>,
)

/** Quakes of at least [minMagnitude] per UTC day and continent, for the [days] ending on [now]'s day. */
internal fun dailyCountsByContinent(
    quakes: Collection<Earthquake>,
    now: Instant,
    days: Int,
    minMagnitude: Double,
): DailyCounts {
    val firstDay = now.utcDate().plus(-(days - 1), DateTimeUnit.DAY)
    val counts = Continent.entries.associateWith { IntArray(days) }
    quakes
        .filter { it.magnitude >= minMagnitude }
        .forEach { quake ->
            val dayIndex = firstDay.daysUntil(quake.time.utcDate())
            if (dayIndex in 0 until days) counts.getValue(quake.continent)[dayIndex]++
        }
    return DailyCounts(firstDay = firstDay, days = days, counts = counts.mapValues { it.value.toList() })
}

/** Quakes reported at or after [since] per continent, oldest first. */
internal fun recentByContinent(
    quakes: Collection<Earthquake>,
    since: Instant,
): Map<Continent, List<Earthquake>> {
    val byContinent = quakes.filter { it.time >= since }.groupBy { it.continent }
    return Continent.entries.associateWith { continent ->
        byContinent[continent].orEmpty().sortedBy { it.time }
    }
}

/** Emits at the start of each slot, so playback moves on with the clock. */
internal fun slotTicks(
    clock: Clock,
    slotLength: Duration,
): Flow<Unit> =
    flow {
        while (true) {
            val now = clock.now()
            delay(now.startOfSlot(slotLength) + slotLength - now)
            emit(Unit)
        }
    }

private fun Instant.startOfSlot(slotLength: Duration): Instant {
    val slotMillis = slotLength.inWholeMilliseconds
    return Instant.fromEpochMilliseconds(toEpochMilliseconds().floorDiv(slotMillis) * slotMillis)
}

/** Quakes per whole-magnitude band, M0–1 up to M7+. */
internal fun quakesByMagnitude(quakes: Collection<Earthquake>): List<Int> {
    val counts = IntArray(MAGNITUDE_BINS)
    quakes.forEach { counts[it.magnitude.toInt().coerceIn(0, MAGNITUDE_BINS - 1)]++ }
    return counts.toList()
}

private const val MAGNITUDE_BINS = 8

private fun Instant.utcDate(): LocalDate = toLocalDateTime(TimeZone.UTC).date
