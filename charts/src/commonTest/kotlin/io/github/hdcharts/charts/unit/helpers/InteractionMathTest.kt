package io.github.hdcharts.charts.unit.helpers

import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.interaction.nearestPointIndexForContentX
import io.github.hdcharts.core.internal.interaction.selectedIndexForBarFit
import io.github.hdcharts.core.internal.interaction.selectedIndexForContentX
import io.github.hdcharts.core.internal.interaction.selectedIndexForTouchX
import kotlin.test.Test
import kotlin.test.assertEquals

class InteractionMathTest {
    @Test
    fun selectedIndexForTouchX_returnsInvalidForInsufficientGeometry() {
        assertEquals(
            expected = NO_SELECTION,
            actual = selectedIndexForTouchX(touchX = 10f, widthPx = 0f, pointsCount = 5),
        )
        assertEquals(
            expected = NO_SELECTION,
            actual = selectedIndexForTouchX(touchX = 10f, widthPx = 100f, pointsCount = 1),
        )
    }

    @Test
    fun selectedIndexForTouchX_selectsNearestPointAndClamps() {
        // Four points span 1000 px: 0, 333, 667 and 1000.
        assertEquals(expected = 1, actual = selectedIndexForTouchX(touchX = 400f, widthPx = 1000f, pointsCount = 4))
        assertEquals(expected = 2, actual = selectedIndexForTouchX(touchX = 600f, widthPx = 1000f, pointsCount = 4))
        assertEquals(expected = 3, actual = selectedIndexForTouchX(touchX = 999f, widthPx = 1000f, pointsCount = 4))
        assertEquals(expected = 3, actual = selectedIndexForTouchX(touchX = 5000f, widthPx = 1000f, pointsCount = 4))
        assertEquals(expected = 0, actual = selectedIndexForTouchX(touchX = -50f, widthPx = 1000f, pointsCount = 4))
    }

    @Test
    fun selectedIndexForTouchX_touchOnOrBesideEveryPoint_selectsThatPoint() {
        for (pointsCount in 2..60) {
            for (widthPx in listOf(300f, 333f, 841f, 1000f, 1433f)) {
                val step = widthPx / (pointsCount - 1)
                for (index in 0 until pointsCount) {
                    val pointX = index * step
                    for (offset in listOf(0f, -step * 0.4f, step * 0.4f)) {
                        val touchX = (pointX + offset).coerceIn(0f, widthPx)
                        assertEquals(
                            expected = index,
                            actual =
                                selectedIndexForTouchX(
                                    touchX = touchX,
                                    widthPx = widthPx,
                                    pointsCount = pointsCount,
                                ),
                            message = "points = $pointsCount, width = $widthPx, touchX = $touchX",
                        )
                    }
                }
            }
        }
    }

    @Test
    fun nearestPointIndexForContentX_selectsNearestPointAndClamps() {
        assertEquals(expected = NO_SELECTION, actual = nearestPointIndexForContentX(10f, pointsCount = 0, stepPx = 12f))
        assertEquals(expected = NO_SELECTION, actual = nearestPointIndexForContentX(10f, pointsCount = 5, stepPx = 0f))
        assertEquals(expected = 2, actual = nearestPointIndexForContentX(23.9f, pointsCount = 100, stepPx = 12f))
        assertEquals(expected = 1, actual = nearestPointIndexForContentX(17.9f, pointsCount = 100, stepPx = 12f))
        assertEquals(expected = 99, actual = nearestPointIndexForContentX(1_188f, pointsCount = 100, stepPx = 12f))
        assertEquals(expected = 99, actual = nearestPointIndexForContentX(50_000f, pointsCount = 100, stepPx = 12f))
    }

    @Test
    fun selectedIndexForContentX_returnsNoSelectionForEmptyDataAndClampsRange() {
        assertEquals(
            expected = NO_SELECTION,
            actual = selectedIndexForContentX(contentX = 10f, dataSize = 0, unitWidthPx = 10f),
        )
        assertEquals(
            expected = 4,
            actual = selectedIndexForContentX(contentX = 10_000f, dataSize = 5, unitWidthPx = 10f),
        )
    }

    @Test
    fun selectedIndexForContentX_selectsBarSlotIncludingTheGapAfterIt() {
        // Slots are 20 px: a 10 px bar and a 10 px gap.
        assertEquals(expected = 0, actual = selectedIndexForContentX(contentX = 9.9f, dataSize = 10, unitWidthPx = 20f))
        assertEquals(
            expected = 0,
            actual = selectedIndexForContentX(contentX = 19.9f, dataSize = 10, unitWidthPx = 20f),
        )
        assertEquals(expected = 1, actual = selectedIndexForContentX(contentX = 20f, dataSize = 10, unitWidthPx = 20f))
    }

    @Test
    fun selectedIndexForBarFit_accountsForSpacingAndCanvasWidth() {
        assertEquals(
            expected = 2,
            actual =
                selectedIndexForBarFit(
                    positionX = 520f,
                    dataSize = 4,
                    canvasWidthPx = 1000f,
                    spacingPx = 20f,
                ),
        )
        assertEquals(
            expected = 3,
            actual =
                selectedIndexForBarFit(
                    positionX = 900f,
                    dataSize = 5,
                    canvasWidthPx = 1500f,
                    spacingPx = 0f,
                ),
        )
    }
}
