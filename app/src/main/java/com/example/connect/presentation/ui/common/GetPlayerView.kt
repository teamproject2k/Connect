package com.example.connect.presentation.ui.common

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.FunctionHelper.getExoPlayer

@Composable
fun GetPlayerView(
    context: Context,
    selectedMediaData: MediaData,
    height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    onUpdate: (ExoPlayer, PlayerView) -> Unit
) {
    val exoPlayer = remember {
        getExoPlayer(context, selectedMediaData.uri.toString())
    }
    DisposableEffect(AndroidView(factory = {
        PlayerView(context).apply {
            player = exoPlayer
            setShowPreviousButton(false)
            setShowNextButton(false)
            setShowFastForwardButton(false)
            setShowRewindButton(false)
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            layoutParams = ViewGroup.LayoutParams(
                width,
                height
            )
        }
    }, update = { playerView ->
        onUpdate(exoPlayer, playerView)
    })) {
        onDispose {
            exoPlayer.release()
        }
    }
}