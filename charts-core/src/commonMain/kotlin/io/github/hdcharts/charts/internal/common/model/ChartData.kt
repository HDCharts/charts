package io.github.hdcharts.charts.internal.common.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.jvm.JvmName
import kotlin.math.abs
import kotlin.math.roundToInt

// Avoid displaying "-0.0" for tiny values after rounding to two decimals.
private const val NEAR_ZERO_DISPLAY_EPSILON = 0.005

class ChartData(
    data: List<Pair<String, Double>>,
) {
    val data: ImmutableList<Pair<String, Double>> = data.toImmutableList()
    val labels: ImmutableList<String> = this.data.map { it.first }.toImmutableList()
    val points: ImmutableList<Double> = this.data.map { it.second }.toImmutableList()
}

private fun parseStringToDouble(value: String): Double = value.toDoubleOrNull() ?: Double.NaN

private fun formatNumericLabel(value: Double): String {
    val rounded = ((value * 100.0).roundToInt()) / 100.0
    val normalized = if (abs(rounded) < NEAR_ZERO_DISPLAY_EPSILON) 0.0 else rounded
    return normalized.toString()
}

@JvmName("toDoubleChartData")
fun List<Double>.toChartData(
    prefix: String = "",
    postfix: String = "",
    labels: List<String>? = null,
): ChartData =
    ChartData(
        this.mapIndexed { index, it ->
            (if (!labels.isNullOrEmpty()) labels[index] else "${prefix}${formatNumericLabel(it)}$postfix") to it
        },
    )

@JvmName("toFloatChartData")
fun List<Float>.toChartData(
    prefix: String = "",
    postfix: String = "",
    labels: List<String>? = null,
): ChartData =
    ChartData(
        this.mapIndexed { index, it ->
            (
                if (!labels.isNullOrEmpty()) labels[index] else "${prefix}${formatNumericLabel(it.toDouble())}$postfix"
            ) to it.toDouble()
        },
    )

@JvmName("toStringChartData")
fun List<String>.toChartData(
    prefix: String = "",
    postfix: String = "",
    labels: List<String>? = null,
): ChartData =
    ChartData(
        this.mapIndexed { index, value ->
            val label = if (!labels.isNullOrEmpty()) labels[index] else "${prefix}${value}$postfix"
            label to parseStringToDouble(value)
        },
    )

@JvmName("toIntChartData")
fun List<Int>.toChartData(
    prefix: String = "",
    postfix: String = "",
    labels: List<String>? = null,
): ChartData =
    ChartData(
        this.mapIndexed { index, it ->
            (if (!labels.isNullOrEmpty()) labels[index] else "${prefix}${it}$postfix") to it.toDouble()
        },
    )
