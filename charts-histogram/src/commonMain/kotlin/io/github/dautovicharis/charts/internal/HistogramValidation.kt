package io.github.dautovicharis.charts.internal

import io.github.dautovicharis.charts.model.ChartData

@InternalChartsApi
fun validateHistogramData(
    data: ChartData,
    colorsSize: Int = 0,
): List<String> {
    val validationErrors = validateBarData(data = data, colorsSize = colorsSize).toMutableList()

    data.series.singleOrNull()?.values?.forEachIndexed { index, value ->
        if (value < 0) {
            validationErrors.add(ValidationErrors.RULE_DATA_POINT_NEGATIVE.format(index))
        }
    }

    return validationErrors
}
