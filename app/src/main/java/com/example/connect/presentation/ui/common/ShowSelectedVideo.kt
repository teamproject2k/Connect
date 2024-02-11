package com.example.connect.presentation.ui.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.media3.common.MediaItem
import com.example.connect.presentation.ui.models.MediaData

@Composable
fun ShowSelectedVideo(selectedMediaData: MediaData, context: Context) {
    GetPlayerView(context = context, uri = selectedMediaData.uri.toString()) { exoPlayer, _ ->
        exoPlayer.setMediaItem(MediaItem.fromUri(selectedMediaData.uri))
    }
}