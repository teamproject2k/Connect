package com.example.connect.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.example.connect.R
import com.example.connect.domain.models.PostBean
import com.example.connect.presentation.ui.enums.PostTypeEnum

@Composable
fun UserProfilePostItem(
    postDetails: PostBean?,
    modifier: Modifier = Modifier,
    showShimmer: Boolean = false,
    onClick: () -> Unit = {}
) {
    val updatedModifier = if (postDetails != null) {
        modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            }
    } else {
        modifier
            .clip(RoundedCornerShape(8.dp))
    }
    Box(
        modifier = if (showShimmer) {
            updatedModifier.shimmer()
        } else {
            updatedModifier
        }
    ) {
        if (showShimmer || postDetails == null) return
        if (postDetails.postType == PostTypeEnum.Text.name) {
            UserProfilePostTextOnlyItem(caption = postDetails.caption)
        } else {
            var isImageLoadingFailed by remember {
                mutableStateOf(false)
            }
            val imageLoader =
                ImageLoader.Builder(LocalContext.current)
                    .components {
                        if (postDetails.postType == PostTypeEnum.Video.name || postDetails.postType == PostTypeEnum.TextVideo.name) {
                            add(VideoFrameDecoder.Factory())
                        }
                    }
                    .build()
            if (!isImageLoadingFailed) {
                AsyncImage(
                    model = postDetails.mediaUrl,
                    imageLoader = imageLoader,
                    contentDescription = postDetails.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = {
                        isImageLoadingFailed = true
                    }
                )
            } else {
                UserProfilePostTextOnlyItem(caption = postDetails.caption.ifBlank { stringResource(R.string.unable_to_load_post) })
            }
            if (postDetails.postType == PostTypeEnum.Video.name || postDetails.postType == PostTypeEnum.TextVideo.name) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ColorsHelper.onBlack()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = stringResource(R.string.play_video),
                    )
                }
            }
        }
    }
}
