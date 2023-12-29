package com.example.connect.presentation.ui.home.add_story

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.VideoCameraFront
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.IconTextSection
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.TransparentTextField
import com.example.connect.presentation.ui.common.mediaPicker
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun AddStoryScreen(navigator: DestinationsNavigator) {
    val viewModel: AddStoryViewModel = hiltViewModel()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackBarHostState = SnackbarHostState()

    val imageResultLauncher = mediaPicker { uri: Uri ->
        val contentResolver = context.contentResolver
        val mediaType = contentResolver.getType(uri)?.substringBefore("/")
        if (mediaType != null) {
            viewModel.selectedMediaState.value =
                MediaData(uri, mediaType)
        }
    }

    Scaffold(topBar = {
        Surface(shadowElevation = 3.dp) {
            TopAppBar(title = {
                Text(
                    text = stringResource(id = R.string.add_story),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }, actions = {
                Button(
                    // enabled = viewModel.captionTextState.value.isNotBlank() || viewModel.selectedMediaState.value != null,
                    onClick = {
//                        handleButtonClick(
//                            viewModel,
//                            context,
//                            sharedViewModel.usersDetails.firebaseUserId
//                        )
                    }
                ) {
                    Text(text = stringResource(R.string.post))
                }
            })
        }
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            HandleAddStorySection(viewModel, context, navigator)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(viewModel.defaultStoryBackgroundColorState.value)
            ) {
                CaptionMediaSection(viewModel)
            }
            BottomButtons { mediaType ->
                imageResultLauncher.launch(PickVisualMediaRequest(mediaType))
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

@Composable
private fun HandleAddStorySection(
    viewModel: AddStoryViewModel,
    context: Context,
    navigator: DestinationsNavigator
) {
    var isResponseHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val addStoryState = viewModel.uploadStoryStateFlow.collectAsState().value
    when (addStoryState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(R.string.uploading_story))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                context.showToast(stringResource(R.string.story_uploaded_successfully))
                navigator.popBackStack()
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (addStoryState.message == FirebaseErrorCodes.NO_USER_FOUND) {
                    context.showToast(stringResource(id = R.string.some_error_occurred_please_login_again))
                    (LocalActivity.current as BaseActivity).logout()
                } else {
                    viewModel.snackBarMessageState.value =
                        addStoryState.message ?: stringResource(id = R.string.some_error_occurred)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "AddStoryScreen",
                    addStoryState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle it
        }
    }
}

@Composable
private fun CaptionMediaSection(viewModel: AddStoryViewModel) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        StoryCaptionField(viewModel)
        MediaSection(viewModel, context)
    }
}

@Composable
private fun StoryCaptionField(viewModel: AddStoryViewModel) {
    TransparentTextField(
        value = viewModel.captionTextState.value,
        placeholder = {
            Text(
                text = stringResource(R.string.type_something),
                fontSize = 14.sp,
                color = ColorsHelper.gray()
            )
        },
        onValueChange = { updatedValue ->
            viewModel.captionTextState.value = updatedValue
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun MediaSection(viewModel: AddStoryViewModel, context: Context) {
    val selectedMedia = viewModel.selectedMediaState.value
    if (selectedMedia != null) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (selectedMedia.mediaType == "image") {
                ShowSelectedImage(selectedMediaData = selectedMedia) {
                    viewModel.selectedMediaState.value = null
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.some_error_occurred)
                }
            } else {
                ShowSelectedVideo(selectedMediaData = selectedMedia, context = context)
            }
            Box(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(), contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = {
                        viewModel.selectedMediaState.value = null
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.background)
                ) {
                    Image(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.cancel)
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            IconButton(onClick = {
                val colorList = FunctionHelper.getStoryBackgroundColorList()
                val currentColorIndex =
                    colorList.indexOf(viewModel.defaultStoryBackgroundColorState.value)
                val nextColorIndex = (currentColorIndex + 1) % colorList.size
                viewModel.defaultStoryBackgroundColorState.value = colorList[nextColorIndex]
            }) {
                Image(
                    modifier = Modifier
                        .size(32.dp)
                        .background(viewModel.defaultStoryBackgroundColorState.value)
                        .border(1.dp, ColorsHelper.lightGray(), CircleShape),
                    imageVector = Icons.Outlined.Circle,
                    contentDescription = stringResource(R.string.change_story_background_color)
                )
            }
        }
    }
}

@Composable
private fun ShowSelectedImage(selectedMediaData: MediaData, onError: () -> Unit) {
    AsyncImage(
        model = selectedMediaData.uri,
        contentDescription = stringResource(R.string.story_image),
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.Crop,
        onError = {
            onError()
        }
    )
}

@SuppressLint("OpaqueUnitKey")
@Composable
private fun ShowSelectedVideo(selectedMediaData: MediaData, context: Context) {
    val exoPlayer = remember {
        FunctionHelper.getExoPlayer(context, selectedMediaData.uri.toString())
    }
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val height = FunctionHelper.convertDpToPixel(screenHeight * .50f, context)
    DisposableEffect(AndroidView(factory = {
        PlayerView(context).apply {
            player = exoPlayer
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height.toInt()
            )
        }
    }, update = {
        exoPlayer.setMediaItem(MediaItem.fromUri(selectedMediaData.uri))
    })) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
private fun BottomButtons(selectFileClick: (mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType) -> Unit) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconTextSection(
                icon = Icons.Rounded.Image,
                text = stringResource(R.string.add_image),
                modifier = Modifier.weight(1f)
            ) {
                selectFileClick(ActivityResultContracts.PickVisualMedia.ImageOnly)
            }
            IconTextSection(
                icon = Icons.Rounded.VideoCameraFront,
                text = stringResource(R.string.add_video),
                modifier = Modifier.weight(1f),
                contentArrangement = Arrangement.End
            ) {
                selectFileClick(ActivityResultContracts.PickVisualMedia.VideoOnly)
            }
        }
    }
}

private fun handleButtonClick(
    viewModel: AddStoryViewModel,
    context: Context,
    currentUserFirebaseId: String
) {
    if (viewModel.captionTextState.value.isBlank() && viewModel.selectedMediaState.value == null) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.please_either_attach_image_video_or_add_some_description)
    } else {
        //viewModel.uploadUserPost(currentUserFirebaseId)
    }
}
