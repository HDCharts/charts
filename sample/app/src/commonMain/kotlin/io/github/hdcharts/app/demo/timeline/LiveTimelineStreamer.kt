package io.github.hdcharts.app.demo.timeline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Repeating job that appends one live point per update interval.
 *
 * Every live demo owns the same loop, so the job, its cancellation, and the interval it reads live
 * here instead of in each view model.
 *
 * @param scope Scope the loop runs in, normally `viewModelScope`.
 * @param intervalMillis Read once per tick so an interval change applies to the next point.
 * @param onTick Appends the next point.
 */
internal class LiveTimelineStreamer(
    private val scope: CoroutineScope,
    private val intervalMillis: () -> Long,
    private val onTick: () -> Unit,
) {
    private var job: Job? = null

    val isRunning: Boolean
        get() = job?.isActive == true

    fun start() {
        job?.cancel()
        job =
            scope.launch {
                while (isActive) {
                    delay(intervalMillis())
                    onTick()
                }
            }
    }

    fun restartIfRunning() {
        if (isRunning) {
            start()
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
