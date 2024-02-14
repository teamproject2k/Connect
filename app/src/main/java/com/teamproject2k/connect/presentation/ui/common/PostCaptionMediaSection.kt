package com.teamproject2k.connect.presentation.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.presentation.ui.enums.MediaTypeEnum
import com.teamproject2k.connect.presentation.utils.ConstantsHelper

@SuppressLint("OpaqueUnitKey")
@Composable
fun PostCaptionMediaSection(postDetails: PostBean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(ConstantsHelper.POST_DISPLAY_MEDIA_HEIGHT)
    ) {
        if (postDetails.postContentType == MediaTypeEnum.Image.name || postDetails.postContentType == MediaTypeEnum.TextImage.name) {
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
        } else if (postDetails.postContentType == MediaTypeEnum.Video.name || postDetails.postContentType == MediaTypeEnum.TextVideo.name) {
            val context = LocalContext.current
            GetPlayerView(context = context, uri = postDetails.mediaUrl) { _, _ ->
                // no need to handle it
            }
        }
    }
}