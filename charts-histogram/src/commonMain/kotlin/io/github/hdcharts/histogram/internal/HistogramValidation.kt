package io.github.hdcharts.histogram.internal

import io.github.hdcharts.bar.internal.validateBarData
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.format
import io.github.hdcharts.core.model.ChartData

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
