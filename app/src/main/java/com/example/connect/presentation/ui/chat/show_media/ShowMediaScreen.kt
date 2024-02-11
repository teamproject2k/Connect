package com.example.connect.presentation.ui.chat.show_media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.ChatBean
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.ShowSelectedVideo
import com.example.connect.presentation.utils.ChatNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@ChatNavGraph
@Destination
@Composable
fun ShowMediaScreen(navigator: DestinationsNavigator, message: ChatBean) {
    val viewModel: ShowMediaViewModel = hiltViewModel()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = { AppTopAppBar(title = stringResource(R.string.search_friends)) }) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (message.mediaType == ConstantsHelper.MEDIA_TYPE_IMAGE) {
                AsyncImage(
                    model = message.mediaUrl,
                    contentDescription = stringResource(R.string.story_image),
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop,
                    onError = {

                    }
                )
            } else {
//                ShowSelectedVideo(
//                    selectedMediaData = message.mediaUrl,
//                    context = context
//                )
            }
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