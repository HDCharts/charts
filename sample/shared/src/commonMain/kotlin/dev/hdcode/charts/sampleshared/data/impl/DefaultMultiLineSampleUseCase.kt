package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.MultiLineSampleData
import dev.hdcode.charts.sampleshared.data.MultiLineSampleUseCase
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf

internal class DefaultMultiLineSampleUseCase : MultiLineSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Weekly Revenue by Channel"
        private val REFRESH_RANGE = 100..1000
    }

    private val multiLineCategories =
        listOf("Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6")
    private val multiLineItems =
        listOf(
            "Web Store" to listOf(420f, 510f, 480f, 530f, 560f, 590f),
            "Mobile App" to listOf(360f, 420f, 410f, 460f, 500f, 540f),
            "Partner Sales" to listOf(280f, 320f, 340f, 360f, 390f, 420f),
        )

    override fun initialMultiLineSample(): MultiLineSampleData =
        MultiLineSampleData(
            dataSet =
                chartDataOf(
                    categories = multiLineCategories,
                    *multiLineItems
                        .map { (name, values) ->
                            ChartSeries(name = name, values = values.map { it.toDouble() })
                        }.toTypedArray(),
                ),
            seriesKeys = multiLineItems.map { it.first },
            title = DEFAULT_TITLE,
        )

    override fun multiLineRefreshRange(): IntRange = REFRESH_RANGE

    override fun multiLineSample(range: IntRange): MultiLineSampleData {
        val newItems =
            multiLineItems.map { (name, values) ->
                name to values.map { range.random().toFloat() }
            }
        val dataSet =
            chartDataOf(
                categories = multiLineCategories,
                *newItems
                    .map { (name, values) ->
                        ChartSeries(name = name, values = values.map { it.toDouble() })
                    }.toTypedArray(),
            )
        return MultiLineSampleData(
            dataSet = dataSet,
            seriesKeys = newItems.map { it.first },
            title = DEFAULT_TITLE,
        )
    }
}
