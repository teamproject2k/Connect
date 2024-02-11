package com.example.connect.presentation.ui.chat.add_media

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.GetPlayerView
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.ChatNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@ChatNavGraph
@Destination
@Composable
fun AddMediaScreen(navigator: DestinationsNavigator, message: String, mediaData: MediaData) {
    val viewModel: AddMediaViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }, topBar = {
        AppTopAppBar(
            showNavigationIcon = true,
            title = stringResource(R.string.upload_media),
            onNavigationIconClick = {
                navigator.popBackStack()
            }
        )
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            MediaSection(viewModel = viewModel, context = context, mediaData)
        }
    }
    LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            coroutineScope.launch {
                snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
                viewModel.snackBarMessageState.value = ""
            }
        }
    }
}

@Composable
private fun MediaSection(viewModel: AddMediaViewModel, context: Context, selectedMedia: MediaData) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        if (selectedMedia.mediaType == ConstantsHelper.MEDIA_TYPE_IMAGE) {
            AsyncImage(
                model = selectedMedia.uri,
                contentDescription = stringResource(R.string.story_image),
                contentScale = ContentScale.Crop,
            )
        } else if (selectedMedia.mediaType == ConstantsHelper.MEDIA_TYPE_VIDEO) {
            GetPlayerView(
                context = context,
                uri = selectedMedia.uri.toString()
            ) { _, _ ->
            }
        }
    }
}