package com.example.connect.presentation.ui.pull_refresh

import android.annotation.SuppressLint
import androidx.compose.animation.core.animate
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.connect.presentation.ui.pull_refresh.PullRefreshDefaults.DragMultiplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

@Composable
fun rememberPullRefreshState(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    refreshThreshold: Dp = PullRefreshDefaults.RefreshThreshold,
    refreshingOffset: Dp = PullRefreshDefaults.RefreshingOffset,
): PullRefreshState {
    require(refreshThreshold > 0.dp) { "The refresh trigger must be greater than zero!" }

    val scope = rememberCoroutineScope()
    val onRefreshState = rememberUpdatedState(onRefresh)
    val thresholdPx: Float
    val refreshingOffsetPx: Float

    with(LocalDensity.current) {
        thresholdPx = refreshThreshold.toPx()
        refreshingOffsetPx = refreshingOffset.toPx()
    }

    val refreshState = remember(scope) {
        PullRefreshState(scope, onRefreshState, refreshingOffsetPx, thresholdPx)
    }

    SideEffect {
        refreshState.setRefreshing(refreshing)
        refreshState.setThreshold(thresholdPx)
        refreshState.setRefreshingOffset(refreshingOffsetPx)
    }

    return refreshState
}

@SuppressLint("StateNameRule")
class PullRefreshState internal constructor(
    private val animationScope: CoroutineScope,
    private val onRefreshState: State<() -> Unit>,
    refreshingOffset: Float,
    threshold: Float
) {
    val progress get() = adjustedDistancePulledState / threshold

    internal val refreshing get() = _refreshingState
    internal val position get() = _positionState
    internal val threshold get() = _thresholdState

    private val adjustedDistancePulledState by derivedStateOf { distancePulledState * DragMultiplier }
    private var _refreshingState by mutableStateOf(false)
    private var _positionState by mutableFloatStateOf(0f)
    private var distancePulledState by mutableFloatStateOf(0f)
    private var _thresholdState by mutableFloatStateOf(threshold)
    private var _refreshingOffsetState by mutableFloatStateOf(refreshingOffset)

    internal fun onPull(pullDelta: Float): Float {
        if (_refreshingState) return 0f // Already refreshing, do nothing.

        val newOffset = (distancePulledState + pullDelta).coerceAtLeast(0f)
        val dragConsumed = newOffset - distancePulledState
        distancePulledState = newOffset
        _positionState = calculateIndicatorPosition()
        return dragConsumed
    }

    internal fun onRelease(velocity: Float): Float {
        if (refreshing) return 0f

        if (adjustedDistancePulledState > threshold) {
            onRefreshState.value()
        }
        animateIndicatorTo(0f)
        val consumed = when {
            distancePulledState == 0f -> 0f
            velocity < 0f -> 0f
            else -> velocity
        }
        distancePulledState = 0f
        return consumed
    }

    internal fun setRefreshing(refreshing: Boolean) {
        if (_refreshingState != refreshing) {
            _refreshingState = refreshing
            distancePulledState = 0f
            animateIndicatorTo(if (refreshing) _refreshingOffsetState else 0f)
        }
    }

    internal fun setThreshold(threshold: Float) {
        _thresholdState = threshold
    }

    internal fun setRefreshingOffset(refreshingOffset: Float) {
        if (_refreshingOffsetState != refreshingOffset) {
            _refreshingOffsetState = refreshingOffset
            if (refreshing) animateIndicatorTo(refreshingOffset)
        }
    }

    private val mutatorMutex = MutatorMutex()

    private fun animateIndicatorTo(offset: Float) = animationScope.launch {
        mutatorMutex.mutate {
            animate(initialValue = _positionState, targetValue = offset) { value, _ ->
                _positionState = value
            }
        }
    }

    private fun calculateIndicatorPosition(): Float = when {
        adjustedDistancePulledState <= threshold -> adjustedDistancePulledState
        else -> {
            val overshootPercent = abs(progress) - 1.0f
            val linearTension = overshootPercent.coerceIn(0f, 2f)
            val tensionPercent = linearTension - linearTension.pow(2) / 4
            val extraOffset = threshold * tensionPercent
            threshold + extraOffset
        }
    }
}

object PullRefreshDefaults {
    val RefreshThreshold = 80.dp
    val RefreshingOffset = 56.dp
    const val DragMultiplier = 0.5f
}

