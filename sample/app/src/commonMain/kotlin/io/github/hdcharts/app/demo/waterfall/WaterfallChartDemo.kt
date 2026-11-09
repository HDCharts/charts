package io.github.hdcharts.app.demo.waterfall

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.sampleshared.data.WaterfallSamples
import io.github.hdcharts.waterfall.WaterfallChart
import io.github.hdcharts.waterfall.WaterfallChartDefaults

@Composable
fun WaterfallChartDemo() {
    ChartDemo(onRefresh = {}, refreshVisible = false) {
        WaterfallChart(WaterfallSamples.crossingZero, modifier = Modifier.fillMaxSize(), title = "Cash balance")
    }
}

@Composable
fun WaterfallBusinessDemo() {
    ChartDemo(onRefresh = {}, refreshVisible = false) {
        BusinessWaterfall(modifier = Modifier.fillMaxSize())
    }
}

/** Revenue to profit, with computed subtotals and a currency readout on selection. */
@Composable
fun BusinessWaterfall(
    modifier: Modifier = Modifier,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
) {
    WaterfallChart(
        data = remember { WaterfallSamples.business },
        modifier = modifier,
        title = "Revenue to profit (USD thousands)",
        selection = selection,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        style =
            WaterfallChartDefaults.style(
                bars =
                    WaterfallChartDefaults.bars(
                        positiveColor = Color(0xFF27845A),
                        negativeColor = Color(0xFFCA4B4B),
                        totalColor = Color(0xFF426DB2),
                    ),
            ),
        valueFormatter = { value -> "$${value.toInt()}k" },
        axisValueFormatter = { value -> "$${value.toInt()}k" },
    )
}
