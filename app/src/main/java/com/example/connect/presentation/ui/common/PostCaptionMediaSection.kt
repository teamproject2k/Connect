package com.example.connect.presentation.ui.common

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.PostBean
import com.example.connect.presentation.ui.enums.PostTypeEnum
import com.example.connect.presentation.utils.FunctionHelper

@SuppressLint("OpaqueUnitKey")
@Composable
fun PostCaptionMediaSection(postDetails: PostBean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        if (postDetails.postType == PostTypeEnum.Image.name || postDetails.postType == PostTypeEnum.TextImage.name) {
            var isImageLoadingFailed by remember {
                mutableStateOf(false)
            }
            var isPostLoading by remember {
                mutableStateOf(false)
            }
            if (!isImageLoadingFailed) {
                val modifier = Modifier.fillMaxSize()
                AsyncImage(
                    model = postDetails.mediaUrl,
                    contentDescription = postDetails.caption,
                    contentScale = ContentScale.Crop,
                    modifier = if (isPostLoading) modifier.shimmer() else modifier,
                    onError = {
                        isImageLoadingFailed = true
                        isPostLoading = false
                    },
                    onLoading = {
                        isPostLoading = true
                    },
                    onSuccess = {
                        isPostLoading = false
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ColorsHelper.lightGray()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.unable_to_load_media))
                }
            }
        } else if (postDetails.postType == PostTypeEnum.Video.name || postDetails.postType == PostTypeEnum.TextVideo.name) {
            val context = LocalContext.current
            val exoPlayer = remember {
                FunctionHelper.getExoPlayer(context, postDetails.mediaUrl)
            }
            DisposableEffect(AndroidView(factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            })) {
                onDispose {
                    exoPlayer.release()
                }
            }
        }
    }
}