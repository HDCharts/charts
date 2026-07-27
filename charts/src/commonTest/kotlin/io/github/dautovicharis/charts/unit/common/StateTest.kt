package io.github.dautovicharis.charts.unit.common

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.internal.common.composable.rememberAnimationState
import io.github.dautovicharis.charts.internal.common.composable.rememberShowState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StateTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rememberShowState_shouldReturnFalse() =
        runComposeUiTest {
            setContent {
                val result = rememberShowState()
                assertFalse {
                    result.value
                }
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rememberShowState_shouldReturnTrue() =
        runComposeUiTest {
            setContent {
                val result = rememberShowState(isPreviewMode = true)
                assertTrue {
                    result.value
                }
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rememberAnimationState_shouldReturnZero() =
        runComposeUiTest {
            setContent {
                val result = rememberAnimationState()
                assertTrue { result.value == 0f }
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rememberAnimationState_shouldReturnOne() =
        runComposeUiTest {
            setContent {
                val result = rememberAnimationState(isPreviewMode = true)
                assertTrue { result.value == 1f }
            }
        }
}
