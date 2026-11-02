package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: scatter-basic-usage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.scatter.ScatterChart
import io.github.hdcharts.scatter.ScatterData
import io.github.hdcharts.scatter.ScatterPoint
import io.github.hdcharts.scatter.ScatterSeries

@Composable
fun ScatterBasicExample(modifier: Modifier = Modifier) {
    val data =
        ScatterData(
            series =
                listOf(
                    ScatterSeries(
                        name = "North",
                        points =
                            listOf(
                                ScatterPoint(x = 1.0, y = 2.4, size = 12.0),
                                ScatterPoint(x = 2.5, y = 3.8, size = 24.0),
                                ScatterPoint(x = 4.0, y = 3.1, size = 18.0),
                            ),
                    ),
                    ScatterSeries(
                        name = "South",
                        points =
                            listOf(
                                ScatterPoint(x = 1.5, y = 1.8, size = 20.0),
                                ScatterPoint(x = 3.0, y = 2.7, size = 10.0),
                                ScatterPoint(x = 4.5, y = 4.2, size = 28.0),
                            ),
                    ),
                ),
        )
    ScatterChart(data = data, modifier = modifier, title = "Regional growth")
}
// END WIKI EXAMPLE: scatter-basic-usage
