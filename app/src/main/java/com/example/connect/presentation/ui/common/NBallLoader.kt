package com.example.connect.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import java.util.Timer
import java.util.TimerTask

@Composable
fun NBallLoader(
    activatedColor: Color,
    deactivatedColor: Color,
    modifier: Modifier = Modifier,
    ballCount: Int = 3,
    animationTotalDurationInMillis: Long = 1000
) {
    var currentSelectedBallIndex by remember {
        mutableIntStateOf(0)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until ballCount) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f, true),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(if (currentSelectedBallIndex == i) 1f else 0.8f)
                        .background(
                            if (currentSelectedBallIndex == i) activatedColor else deactivatedColor,
                            CircleShape
                        )
                )
            }
        }
    }
    DisposableEffect(key1 = true) {
        val timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                if ((currentSelectedBallIndex + 1) == ballCount) {
                    currentSelectedBallIndex = 0
                } else {
                    currentSelectedBallIndex++
                }
            }
        }
        timer.schedule(timerTask, 0, animationTotalDurationInMillis / ballCount)
        onDispose {
            timer.cancel()
        }
    }

}

@Preview
@Composable
fun PreviewNBallLoader() {
    NBallLoader(activatedColor = Color.Black, deactivatedColor = Color.Blue)
}