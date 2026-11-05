package io.github.hdcharts.sampleshared.data

import androidx.compose.ui.graphics.Color
import io.github.hdcharts.heatmap.HeatmapColorScale

/** Explicit color scales for the heatmap samples, shared by the demos, gallery and screenshots. */
object HeatmapSampleColorScales {
    /**
     * Sequential load scale over a fixed 0..100 percent domain: green, yellow, orange and red sit at
     * 0, 33, 67 and 100 percent, so a color always means the same load.
     */
    val cpuLoad =
        HeatmapColorScale(
            colors = listOf(Color(0xFF43A047), Color(0xFFFDD835), Color(0xFFFB8C00), Color(0xFFD32F2F)),
            min = 0.0,
            max = 100.0,
        )

    /** Diverging scale with symmetric bounds, so zero falls on the neutral middle color. */
    val temperatureAnomaly =
        HeatmapColorScale(
            colors = listOf(Color(0xFF2166AC), Color(0xFFF7F7F7), Color(0xFFB2182B)),
            min = -4.0,
            max = 4.0,
        )
}
