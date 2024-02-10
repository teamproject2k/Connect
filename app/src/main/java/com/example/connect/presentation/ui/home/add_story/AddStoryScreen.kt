package com.example.connect.presentation.ui.home.add_story

import android.content.Context
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.GetPlayerView
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SpacerWidth16
import com.example.connect.presentation.ui.common.SpacerWidth8
import com.example.connect.presentation.ui.common.TransparentTextField
import com.example.connect.presentation.ui.common.mediaPicker
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@HomeNavGraph
@Destination
@Composable
fun AddStoryScreen(navigator: DestinationsNavigator) {
    val viewModel: AddStoryViewModel = hiltViewModel()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val mediaResultLauncher = mediaPicker { uri: Uri ->
        val contentResolver = context.contentResolver
        val mediaType = FunctionHelper.getMediaType(contentResolver, uri)
        if (mediaType != null) {
            if (mediaType == ConstantsHelper.MEDIA_TYPE_VIDEO) {
                viewModel.selectedMediaState.value =
                    MediaData(uri, mediaType, FunctionHelper.getVideoDuration(contentResolver, uri))
            } else {
                viewModel.selectedMediaState.value =
                    MediaData(uri, mediaType)
            }
        }
    }
    val textColor = MaterialTheme.colorScheme.onPrimary
    if (!viewModel.isDataInitialized) {
        viewModel.initData(textColor)
    }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            MainContentSection(
                viewModel, Modifier
                    .weight(1f)
                    .fillMaxSize(),
                navigator
            )
            BottomSection(homeSharedViewModel.usersDetails.firebaseUserId, viewModel) {
                mediaResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            }
        }
    }
    HandleAddStorySection(viewModel, context, navigator)
    LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            coroutineScope.launch {
                snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
                viewModel.snackBarMessageState.value = ""
            }
        }
    }
    LaunchedEffect(key1 = viewModel.selectedMediaState.value) {
        if (viewModel.selectedMediaState.value == null) {
            viewModel.colorOnMediaState.value = textColor
        } else {
            val fileBitmap = FunctionHelper.uriToBitmap(
                context.contentResolver,
                viewModel.selectedMediaState.value!!.uri
            )
            if (fileBitmap != null) {
                Palette.from(fileBitmap).generate { palette ->
                    val vibrant = palette?.vibrantSwatch
                    if (vibrant != null) {
                        viewModel.colorOnMediaState.value = Color(vibrant.rgb)
                    }
                }
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
                viewModel.snackBarMessageState.value =
                    addStoryState.message ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.AddStoryScreen.name,
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
private fun MainContentSection(
    viewModel: AddStoryViewModel,
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = Brush.linearGradient(viewModel.storyBackgroundColorState.value))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            MediaSection(viewModel, context)
            StoryCaptionField(viewModel)
        }
        IconButton(
            onClick = { navigator.popBackStack() }, colors = IconButtonDefaults.iconButtonColors(
                contentColor = if (viewModel.selectedMediaState.value != null) ColorsHelper.black() else MaterialTheme.colorScheme.onPrimary,
                containerColor = if (viewModel.selectedMediaState.value == null) Color.Transparent else MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(id = R.string.clear)
            )
        }

    }
}

@Composable
private fun StoryCaptionField(viewModel: AddStoryViewModel) {
    val context = LocalContext.current
    val screenWidth = context.resources.displayMetrics.widthPixels
    val screenHeight = context.resources.displayMetrics.heightPixels
    TransparentTextField(
        modifier = Modifier
            .offset {
                IntOffset(
                    viewModel.captionOffsetXState.floatValue.roundToInt(),
                    viewModel.captionOffsetYState.floatValue.roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    viewModel.captionOffsetXState.floatValue += dragAmount.x
                    viewModel.captionOffsetYState.floatValue += dragAmount.y
                }
            }
            .onGloballyPositioned {
                if (viewModel.isFirstTimePlaced) {
                    viewModel.captionOffsetXState.floatValue =
                        ((screenWidth - it.size.width) / 2).toFloat()
                    viewModel.captionOffsetYState.floatValue =
                        ((screenHeight - it.size.height) / 2).toFloat()
                    viewModel.isFirstTimePlaced = false
                }
            },
        value = viewModel.captionTextState.value,
        placeholder = {
            Text(
                text = stringResource(R.string.type_something),
                fontSize = 18.sp,
                color = viewModel.colorOnMediaState.value,
                textAlign = TextAlign.Center
            )
        },
        onValueChange = { updatedValue ->
            viewModel.captionTextState.value = updatedValue
        },
        textStyle = TextStyle.Default.copy(
            color = viewModel.colorOnMediaState.value,
            fontSize = 18.sp
        )
    )
}

@Composable
private fun MediaSection(viewModel: AddStoryViewModel, context: Context) {
    val selectedMedia = viewModel.selectedMediaState.value
    if (selectedMedia != null) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (selectedMedia.mediaType == ConstantsHelper.MEDIA_TYPE_IMAGE) {
                ShowSelectedImage(selectedMediaData = selectedMedia) {
                    viewModel.selectedMediaState.value = null
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.something_went_wrong)
                }
            } else if (selectedMedia.mediaType == ConstantsHelper.MEDIA_TYPE_VIDEO) {
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

@Composable
private fun ShowSelectedVideo(selectedMediaData: MediaData, context: Context) {
    GetPlayerView(context = context, uri = selectedMediaData.uri.toString()) { exoPlayer, _ ->
        exoPlayer.setMediaItem(MediaItem.fromUri(selectedMediaData.uri))
    }
}

@Composable
private fun BottomSection(
    loggedInUserFirebaseId: String,
    viewModel: AddStoryViewModel,
    onMediaSelect: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorsHelper.black())
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(34.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(8.dp))
                .clickable { onMediaSelect() },
            painter = painterResource(id = R.drawable.ic_gallery),
            contentDescription = stringResource(R.string.add_media),
            contentScale = ContentScale.Crop
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.onPrimary,
                        RoundedCornerShape(percent = 50)
                    )
                    .clickable {
                        val currentColorIndex =
                            viewModel.textColorList.indexOf(viewModel.colorOnMediaState.value)
                        val nextColorIndex =
                            (currentColorIndex + 1) % viewModel.textColorList.size
                        viewModel.colorOnMediaState.value =
                            viewModel.textColorList[nextColorIndex]
                    }
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, ColorsHelper.gray(), CircleShape)
                        .background(viewModel.colorOnMediaState.value)
                )
                SpacerWidth8()
                Icon(
                    imageVector = Icons.Default.FormatColorText,
                    contentDescription = stringResource(R.string.text_color)
                )
            }

            SpacerWidth16()
            if (viewModel.selectedMediaState.value == null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                        .background(brush = Brush.linearGradient(viewModel.storyBackgroundColorState.value))
                        .clickable {
                            val currentColorIndex =
                                viewModel.gradientColorList.indexOf(viewModel.storyBackgroundColorState.value)
                            val nextColorIndex =
                                (currentColorIndex + 1) % viewModel.gradientColorList.size
                            viewModel.storyBackgroundColorState.value =
                                viewModel.gradientColorList[nextColorIndex]
                        }
                )
            }
            if (viewModel.captionTextState.value.isNotBlank() || viewModel.selectedMediaState.value != null) {
                SpacerWidth16()
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
                        .clickable {
                            handleButtonClick(viewModel, context, loggedInUserFirebaseId)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = stringResource(id = R.string.upload)
                    )
                }
            }
        }
    }
}

private fun handleButtonClick(
    viewModel: AddStoryViewModel,
    context: Context,
    loggedInUserFirebaseId: String
) {
    if (viewModel.captionTextState.value.isBlank() && viewModel.selectedMediaState.value == null) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.please_either_attach_image_video_or_add_some_description)
    } else {
        if (context.isNetworkAvailable()) {
            viewModel.uploadUserStory(loggedInUserFirebaseId)
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
    }
}
