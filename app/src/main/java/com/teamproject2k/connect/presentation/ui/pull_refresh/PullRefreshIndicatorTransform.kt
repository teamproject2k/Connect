package com.teamproject2k.connect.presentation.ui.pull_refresh

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable

fun Modifier.pullRefreshIndicatorTransform(
    refreshState: PullRefreshState,
    scale: Boolean = false,
) = inspectable(
    inspectorInfo = debugInspectorInfo {
        name = "pullRefreshIndicatorTransform"
        properties["state"] = refreshState
        properties["scale"] = scale
    },
) {
    Modifier
        .drawWithContent {
            clipRect(
                top = 0f,
                left = -Float.MAX_VALUE,
                right = Float.MAX_VALUE,
                bottom = Float.MAX_VALUE,
            ) {
                this@drawWithContent.drawContent()
            }
        }
        .graphicsLayer {
            translationY = refreshState.position - size.height

            if (scale && !refreshState.refreshing) {
                val scaleFraction = LinearOutSlowInEasing
                    .transform(refreshState.position / refreshState.threshold)
                    .coerceIn(0f, 1f)
                scaleX = scaleFraction
                scaleY = scaleFraction
            }
        }
}