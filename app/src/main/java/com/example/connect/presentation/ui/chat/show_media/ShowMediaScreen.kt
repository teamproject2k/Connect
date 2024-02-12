package com.example.connect.presentation.ui.chat.show_media

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.enums.MediaStateChangeEnum
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.GetPlayerView
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.ChatNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@ChatNavGraph
@Destination
@Composable
fun ShowMediaScreen(navigator: DestinationsNavigator, mediaData: MediaData) {
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var currentMediaState by remember {
        mutableStateOf(MediaStateChangeEnum.Loading.name)
    }
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
                .padding(it)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (mediaData.mediaType == ConstantsHelper.MEDIA_TYPE_IMAGE) {
                if (currentMediaState == MediaStateChangeEnum.Error.name) {
                    Text(
                        text = stringResource(id = R.string.couldn_t_load_image),
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    AsyncImage(
                        model = mediaData.uri,
                        contentDescription = stringResource(id = R.string.uploaded_media),
                        contentScale = ContentScale.Crop,
                        onLoading = {
                            currentMediaState = MediaStateChangeEnum.Loading.name
                        },
                        onError = {
                            currentMediaState = MediaStateChangeEnum.Error.name
                        },
                        onSuccess = {
                            currentMediaState = MediaStateChangeEnum.Success.name
                        }
                    )
                }

            } else if (mediaData.mediaType == ConstantsHelper.MEDIA_TYPE_VIDEO) {
                if (currentMediaState == MediaStateChangeEnum.Error.name) {
                    Text(
                        text = stringResource(id = R.string.couldn_t_load_image),
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    GetPlayerView(
                        context = context,
                        uri = mediaData.uri.toString(),
                        onStateChange = {
                            if (it == MediaStateChangeEnum.Error) {
                                currentMediaState = MediaStateChangeEnum.Error.name
                            }
                        }
                    ) { _, _ ->
                    }
                }
            }
            if (currentMediaState == MediaStateChangeEnum.Loading.name) {
                CircularProgressIndicator()
            }
        }
    }
}