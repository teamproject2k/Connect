package com.teamproject2k.connect.presentation.ui.chat.show_media

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.enums.MediaStateChangeEnum
import com.teamproject2k.connect.presentation.ui.common.AppTopAppBar
import com.teamproject2k.connect.presentation.ui.common.GetPlayerView
import com.teamproject2k.connect.presentation.ui.models.MediaData
import com.teamproject2k.connect.presentation.utils.ChatNavGraph
import com.teamproject2k.connect.presentation.utils.ConstantsHelper

@OptIn(ExperimentalMaterial3Api::class)
@ChatNavGraph
@Destination
@Composable
fun ShowMediaScreen(navigator: DestinationsNavigator, mediaData: MediaData) {
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            AppTopAppBar(
                title = stringResource(R.string.uploaded_media),
                showNavigationIcon = true,
                onNavigationIconClick = { navigator.popBackStack() })
        }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (mediaData.mediaType == ConstantsHelper.MEDIA_TYPE_IMAGE) {
                HandleImageSection(mediaUrl = mediaData.uri.toString())
            } else if (mediaData.mediaType == ConstantsHelper.MEDIA_TYPE_VIDEO) {
                HandleVideoSection(mediaUrl = mediaData.uri.toString())
            }
        }
    }
}

@Composable
fun HandleVideoSection(mediaUrl: String) {
    var currentVideoState by remember {
        mutableStateOf(MediaStateChangeEnum.Loading.name)
    }
    val context = LocalContext.current
    if (currentVideoState == MediaStateChangeEnum.Error.name) {
        Text(
            text = stringResource(id = R.string.couldn_t_load_video),
            modifier = Modifier.fillMaxSize(),
            textAlign = TextAlign.Center
        )
    } else {
        GetPlayerView(
            context = context,
            uri = mediaUrl,
            onStateChange = {
                currentVideoState = it.name
            }
        ) { _, _ ->
        }
    }
}

@Composable
fun HandleImageSection(mediaUrl: String) {
    var currentImageState by remember {
        mutableStateOf(MediaStateChangeEnum.Loading.name)
    }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale *= zoom
                offset += pan
            }
        }
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            translationX = offset.x,
            translationY = offset.y
        ), contentAlignment = Alignment.Center) {
        if (currentImageState == MediaStateChangeEnum.Error.name) {
            Text(
                text = stringResource(id = R.string.couldn_t_load_image),
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center
            )
        } else {
            AsyncImage(
                model = mediaUrl,
                contentDescription = stringResource(id = R.string.uploaded_media),
                contentScale = ContentScale.Crop,
                onLoading = {
                    currentImageState = MediaStateChangeEnum.Loading.name
                },
                onError = {
                    currentImageState = MediaStateChangeEnum.Error.name
                },
                onSuccess = {
                    currentImageState = MediaStateChangeEnum.Success.name
                }
            )
        }
        if (currentImageState == MediaStateChangeEnum.Loading.name) {
            CircularProgressIndicator()
        }
    }
}
