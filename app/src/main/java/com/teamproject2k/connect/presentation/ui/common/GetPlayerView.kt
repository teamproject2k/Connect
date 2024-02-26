package com.teamproject2k.connect.presentation.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.ColorRes
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.enums.MediaStateChangeEnum
import com.teamproject2k.connect.presentation.utils.FunctionHelper.getExoPlayer

@OptIn(UnstableApi::class)
@SuppressLint("OpaqueUnitKey")
@Composable
fun GetPlayerView(
    context: Context,
    uri: String,
    @ColorRes loadingColorRes: Int = R.color.light_app_theme,
    height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    playWhenReady: Boolean = false,
    onStateChange: ((changedState: MediaStateChangeEnum) -> Unit)? = null,
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
            exoPlayer.prepare()
            exoPlayer.playWhenReady = playWhenReady
            if (onStateChange != null) {
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        onStateChange(MediaStateChangeEnum.Error)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        when (playbackState) {
                            Player.STATE_READY -> {
                                onStateChange(MediaStateChangeEnum.Success)
                            }

                            Player.STATE_BUFFERING -> {
                                onStateChange(MediaStateChangeEnum.Loading)
                            }

                            else -> {
                                // no need to handle it
                            }
                        }
                    }


                })
            }
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            val progressBar = this.findViewById<ProgressBar>(androidx.media3.ui.R.id.exo_buffering)
            progressBar.indeterminateTintList =
                ContextCompat.getColorStateList(context, loadingColorRes)
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


