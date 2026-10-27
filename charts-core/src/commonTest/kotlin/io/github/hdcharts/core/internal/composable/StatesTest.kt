package io.github.hdcharts.core.internal.composable

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatesTest {
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
}
