package io.github.hdcharts.sampleshared.data

import io.github.hdcharts.waterfall.WaterfallData
import io.github.hdcharts.waterfall.WaterfallEntry.Change
import io.github.hdcharts.waterfall.WaterfallEntry.Total

/** Deterministic business and edge-case data shared by demos and screenshot previews. */
object WaterfallSamples {
    val business =
        WaterfallData(
            listOf(
                Change("Revenue", 240.0),
                Change("Returns", -15.0),
                Total("Net sales"),
                Change("Materials", -85.0),
                Change("Payroll", -60.0),
                Total("Operating profit"),
                Change("Tax", -20.0),
                Total("Net profit"),
            ),
        )

    val crossingZero =
        WaterfallData(
            listOf(
                Change("Opening", 40.0),
                Change("Costs", -90.0),
                Total("Deficit"),
                Change("Funding", 70.0),
                Total("Balance"),
            ),
        )

    val zeroChanges =
        WaterfallData(
            listOf(
                Total("Opening"),
                Change("Sales", 100.0),
                Change("Adjustment", 0.0),
                Total("Subtotal"),
                Total("Confirmed"),
            ),
        )
}
