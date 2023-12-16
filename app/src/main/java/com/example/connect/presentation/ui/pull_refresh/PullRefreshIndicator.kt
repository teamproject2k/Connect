package com.example.connect.presentation.ui.pull_refresh

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.AlphaTween
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.ArcRadius
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.ArrowHeight
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.ArrowWidth
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.CrossFadeDurationMs
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.Elevation
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.IndicatorSize
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.MaxAlpha
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.MaxProgressArc
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.MinAlpha
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.SpinnerShape
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator.StrokeWidth
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@Composable
fun PullRefreshIndicator(
    refreshing: Boolean,
    refreshState: PullRefreshState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    scale: Boolean = false,
) {
    val showElevation by remember(refreshing, refreshState) {
        derivedStateOf { refreshing || refreshState.position > 0.5f }
    }

    Box(
        modifier = modifier
            .size(IndicatorSize)
            .pullRefreshIndicatorTransform(refreshState, scale)
            .shadow(if (showElevation) Elevation else 0.dp, SpinnerShape, clip = true)
            .background(
                color = surfaceColorAtElevation(
                    color = backgroundColor,
                    elevation = Elevation
                ), shape = SpinnerShape
            ),
    ) {
        Crossfade(
            targetState = refreshing,
            animationSpec = tween(durationMillis = CrossFadeDurationMs), label = "cross_fade",
        ) { refreshing ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val spinnerSize = (ArcRadius + StrokeWidth).times(2)

                if (refreshing) {
                    CircularProgressIndicator(
                        color = contentColor,
                        strokeWidth = StrokeWidth,
                        modifier = Modifier.size(spinnerSize),
                    )
                } else {
                    CircularArrowIndicator(refreshState, contentColor, Modifier.size(spinnerSize))
                }
            }
        }
    }
}

@Composable
private fun CircularArrowIndicator(
    state: PullRefreshState,
    color: Color,
    modifier: Modifier,
) {
    val path = remember { Path().apply { fillType = PathFillType.EvenOdd } }

    val targetAlpha by remember(state) {
        derivedStateOf {
            if (state.progress >= 1f) MaxAlpha else MinAlpha
        }
    }

    val alphaState = animateFloatAsState(
        targetValue = targetAlpha, animationSpec = AlphaTween,
        label = "alpha_state"
    )

    Canvas(modifier.semantics {}) {
        val values = ArrowValues(state.progress)
        val alpha = alphaState.value

        rotate(degrees = values.rotation) {
            val arcRadius = ArcRadius.toPx() + StrokeWidth.toPx() / 2f
            val arcBounds = Rect(
                size.center.x - arcRadius,
                size.center.y - arcRadius,
                size.center.x + arcRadius,
                size.center.y + arcRadius,
            )
            drawArc(
                color = color,
                alpha = alpha,
                startAngle = values.startAngle,
                sweepAngle = values.endAngle - values.startAngle,
                useCenter = false,
                topLeft = arcBounds.topLeft,
                size = arcBounds.size,
                style = Stroke(
                    width = StrokeWidth.toPx(),
                    cap = StrokeCap.Square,
                ),
            )
            drawArrow(path, arcBounds, color, alpha, values)
        }
    }
}

@Immutable
private class ArrowValues(
    val rotation: Float,
    val startAngle: Float,
    val endAngle: Float,
    val scale: Float,
)

private fun ArrowValues(progress: Float): ArrowValues {
    val adjustedPercent = max(min(1f, progress) - 0.4f, 0f) * 5 / 3
    val overshootPercent = abs(progress) - 1.0f
    val linearTension = overshootPercent.coerceIn(0f, 2f)
    val tensionPercent = linearTension - linearTension.pow(2) / 4
    val endTrim = adjustedPercent * MaxProgressArc
    val rotation = (-0.25f + 0.4f * adjustedPercent + tensionPercent) * 0.5f
    val startAngle = rotation * 360
    val endAngle = (rotation + endTrim) * 360
    val scale = min(1f, adjustedPercent)

    return ArrowValues(rotation, startAngle, endAngle, scale)
}

private fun DrawScope.drawArrow(
    arrow: Path,
    bounds: Rect,
    color: Color,
    alpha: Float,
    values: ArrowValues,
) {
    arrow.reset()
    arrow.moveTo(0f, 0f)
    arrow.lineTo(x = ArrowWidth.toPx() * values.scale, y = 0f)

    arrow.lineTo(
        x = ArrowWidth.toPx() * values.scale / 2,
        y = ArrowHeight.toPx() * values.scale,
    )

    val radius = min(bounds.width, bounds.height) / 2f
    val inset = ArrowWidth.toPx() * values.scale / 2f
    arrow.translate(
        Offset(
            x = radius + bounds.center.x - inset,
            y = bounds.center.y + StrokeWidth.toPx() / 2f,
        ),
    )
    arrow.close()
    rotate(degrees = values.endAngle) {
        drawPath(path = arrow, color = color, alpha = alpha)
    }
}

object PullRefreshIndicator {
    const val CrossFadeDurationMs = 100
    const val MaxProgressArc = 0.8f
    val IndicatorSize = 40.dp
    val SpinnerShape = CircleShape
    val ArcRadius = 7.5.dp
    val StrokeWidth = 2.5.dp
    val ArrowWidth = 10.dp
    val ArrowHeight = 5.dp
    val Elevation = 6.dp
    const val MinAlpha = 0.3f
    const val MaxAlpha = 1f
    val AlphaTween = tween<Float>(300, easing = LinearEasing)
}