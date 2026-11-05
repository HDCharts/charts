package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.heatmap.HeatmapData
import io.github.hdcharts.sampleshared.data.HeatmapSampleData
import io.github.hdcharts.sampleshared.data.HeatmapSampleUseCase

internal class DefaultHeatmapSampleUseCase : HeatmapSampleUseCase {
    override fun weeklyActivity(): HeatmapSampleData {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val hours = (0 until HOURS_PER_DAY).map { hour -> hour.toString().padStart(2, '0') }
        val values =
            days.indices.map { day ->
                hours.indices.map { hour ->
                    // The Sunday night maintenance window records no sessions.
                    if (day == SUNDAY && hour in MAINTENANCE_HOURS) null else WEEKLY_SESSIONS[day][hour].toDouble()
                }
            }
        return HeatmapSampleData(
            data = HeatmapData(columns = hours, rows = days, values = values),
            title = "Sessions per hour",
        )
    }

    override fun regionalCpuLoad(): HeatmapSampleData =
        HeatmapSampleData(
            data =
                HeatmapData(
                    columns = (0 until HOURS_PER_DAY).map { hour -> hour.toString().padStart(2, '0') },
                    rows = REGIONS,
                    values = REGIONAL_CPU_LOAD.map { row -> row.map(Int::toDouble) },
                ),
            title = "CPU load by region (UTC)",
        )

    override fun temperatureAnomalies(): HeatmapSampleData =
        HeatmapSampleData(
            data =
                HeatmapData(
                    columns = MONTHS,
                    rows = listOf("Oslo", "Berlin", "Madrid", "Cairo"),
                    values =
                        listOf(
                            listOf(-2.4, -1.1, 0.6, 1.3, 0.2, -0.4, 1.8, 2.2, 0.9, -0.3, -1.6, -2.9),
                            listOf(-1.2, -0.4, 0.8, 1.6, 1.1, 2.0, 2.6, 2.1, 1.2, 0.4, -0.6, -1.4),
                            listOf(0.3, 0.9, 1.4, 2.2, 2.9, 3.4, 3.1, 2.7, 1.9, 1.1, 0.4, -0.2),
                            listOf(0.8, 1.2, 1.9, null, 2.6, 3.0, 2.4, 2.8, 2.1, 1.5, 0.9, 0.6),
                        ),
                ),
            title = "Temperature anomaly (°C)",
        )

    private companion object {
        const val HOURS_PER_DAY = 24
        const val SUNDAY = 6
        val MAINTENANCE_HOURS = 2..4
        val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val REGIONS =
            listOf("Tokyo", "Singapore", "Mumbai", "Frankfurt", "London", "São Paulo", "New York", "San Francisco")

        // Average CPU load in percent per UTC hour. Each region peaks in its local afternoon, with a
        // smaller evening batch peak, so the busy hours move across the day from east to west.
        val REGIONAL_CPU_LOAD =
            listOf(
                listOf(31, 42, 55, 68, 77, 80, 77, 68, 57, 47, 40, 37, 34, 29, 21, 15, 12, 10, 10, 11, 11, 13, 16, 22),
                listOf(21, 29, 39, 51, 62, 70, 73, 70, 62, 52, 43, 37, 35, 32, 27, 20, 15, 12, 10, 10, 10, 11, 13, 16),
                listOf(12, 14, 18, 25, 35, 47, 59, 70, 76, 76, 70, 60, 49, 41, 37, 35, 31, 24, 17, 13, 11, 10, 10, 11),
                listOf(12, 10, 10, 11, 11, 13, 17, 23, 32, 44, 57, 70, 80, 84, 80, 71, 59, 48, 42, 39, 36, 29, 22, 15),
                listOf(15, 12, 10, 10, 11, 11, 13, 16, 22, 31, 42, 55, 68, 77, 80, 77, 68, 57, 47, 40, 37, 34, 29, 21),
                listOf(31, 26, 19, 14, 11, 10, 10, 10, 11, 13, 15, 20, 28, 37, 48, 59, 67, 70, 67, 59, 50, 41, 36, 33),
                listOf(45, 42, 38, 31, 23, 16, 12, 11, 10, 11, 12, 14, 17, 24, 34, 47, 62, 76, 87, 91, 87, 77, 64, 52),
                listOf(71, 59, 48, 42, 39, 36, 29, 22, 15, 12, 10, 10, 11, 11, 13, 17, 23, 32, 44, 57, 70, 80, 84, 80),
            )

        // Sessions per hour, from midnight. Through the week the morning peak fades and moves later,
        // while the evening peak grows, so values change along both the hours and the days.
        val WEEKLY_SESSIONS =
            listOf(
                listOf(3, 3, 3, 3, 5, 10, 21, 42, 67, 85, 85, 67, 42, 22, 12, 10, 16, 27, 39, 47, 47, 39, 27, 16),
                listOf(4, 4, 4, 4, 6, 12, 23, 41, 62, 78, 79, 65, 44, 26, 15, 12, 17, 28, 42, 53, 55, 46, 33, 20),
                listOf(4, 4, 4, 5, 7, 12, 22, 38, 55, 69, 72, 64, 48, 31, 19, 15, 19, 30, 45, 58, 62, 55, 40, 25),
                listOf(5, 5, 5, 6, 7, 12, 20, 32, 46, 59, 64, 61, 50, 36, 24, 18, 21, 32, 48, 63, 70, 63, 47, 30),
                listOf(5, 5, 5, 6, 7, 11, 17, 26, 37, 48, 55, 56, 50, 40, 29, 23, 24, 33, 50, 68, 77, 72, 56, 36),
                listOf(6, 6, 6, 6, 7, 9, 13, 20, 28, 37, 45, 49, 47, 42, 34, 28, 27, 36, 53, 72, 83, 80, 64, 43),
                listOf(6, 6, 6, 6, 7, 8, 11, 15, 20, 27, 34, 39, 41, 39, 35, 31, 31, 39, 55, 76, 90, 89, 74, 51),
            )
    }
}
