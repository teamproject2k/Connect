package com.example.connect.presentation.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.connect.R
import com.example.connect.presentation.utils.FunctionHelper.getExoPlayer

@SuppressLint("OpaqueUnitKey")
@Composable
fun GetPlayerView(
    context: Context,
    uri: String,
    height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    onUpdate: (ExoPlayer, PlayerView) -> Unit
) {
    val exoPlayer = remember {
        getExoPlayer(context, uri)
    }
    DisposableEffect(AndroidView(factory = {
        PlayerView(context).apply {
            player = exoPlayer
            val controller = findViewById<View>(androidx.media3.ui.R.id.exo_controller)
            controller.findViewById<View>(androidx.media3.ui.R.id.exo_settings).visibility =
                View.GONE
            setShowPreviousButton(false)
            setShowNextButton(false)
            setShowFastForwardButton(false)
            setShowRewindButton(false)
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            val progressBar = this.findViewById<ProgressBar>(androidx.media3.ui.R.id.exo_buffering)
            progressBar.indeterminateTintList =
                ContextCompat.getColorStateList(context, R.color.light_app_theme)
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