package io.github.hdcharts.charts.unit.helpers

import io.github.hdcharts.charts.internal.common.interaction.selectedIndexForBarFit
import io.github.hdcharts.charts.internal.common.interaction.selectedIndexForContentX
import io.github.hdcharts.charts.internal.common.interaction.selectedIndexForTouchX
import kotlin.test.Test
import kotlin.test.assertEquals

class InteractionMathTest {
    @Test
    fun selectedIndexForTouchX_returnsInvalidForInsufficientGeometry() {
        assertEquals(
            expected = -1,
            actual = selectedIndexForTouchX(touchX = 10f, widthPx = 0f, pointsCount = 5, invalidIndex = -1),
        )
        assertEquals(
            expected = -1,
            actual = selectedIndexForTouchX(touchX = 10f, widthPx = 100f, pointsCount = 1, invalidIndex = -1),
        )
    }

    @Test
    fun selectedIndexForTouchX_mapsAndClampsSelection() {
        assertEquals(
            expected = 1,
            actual = selectedIndexForTouchX(touchX = 500f, widthPx = 1000f, pointsCount = 4, invalidIndex = -1),
        )
        assertEquals(
            expected = 3,
            actual = selectedIndexForTouchX(touchX = 5000f, widthPx = 1000f, pointsCount = 4, invalidIndex = -1),
        )
    }

    @Test
    fun selectedIndexForContentX_respectsInvalidIndexAndClampsRange() {
        assertEquals(
            expected = 0,
            actual = selectedIndexForContentX(contentX = 10f, dataSize = 0, unitWidthPx = 10f, invalidIndex = 0),
        )
        assertEquals(
            expected = 4,
            actual = selectedIndexForContentX(contentX = 10_000f, dataSize = 5, unitWidthPx = 10f, invalidIndex = -1),
        )
    }

    @Test
    fun selectedIndexForBarFit_accountsForSpacingAndCanvasWidth() {
        assertEquals(
            expected = 2,
            actual = selectedIndexForBarFit(positionX = 520f, dataSize = 4, canvasWidthPx = 1000f, spacingPx = 20f),
        )
        assertEquals(
            expected = 3,
            actual = selectedIndexForBarFit(positionX = 900f, dataSize = 5, canvasWidthPx = 1500f, spacingPx = 0f),
        )
    }
}
