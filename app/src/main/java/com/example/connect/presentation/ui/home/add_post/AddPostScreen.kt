package com.example.connect.presentation.ui.home.add_post

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.VideoCameraFront
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.common.ErrorCodes
import com.example.connect.common.LoggingHelper
import com.example.connect.common.LoggingLevelEnum
import com.example.connect.common.RequestStatusEnum.EXCEPTION
import com.example.connect.common.RequestStatusEnum.LOADING
import com.example.connect.common.RequestStatusEnum.NONE
import com.example.connect.common.RequestStatusEnum.SUCCESS
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.IconTextSection
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.TransparentTextField
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.common.VisibilityItem
import com.example.connect.presentation.ui.common.VisibilityScopeBottomSheetItem
import com.example.connect.presentation.ui.common.mediaPicker
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.ui.models.PostMediaData
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@HomeNavGraph
@Destination
@Composable
fun AddPostScreen(navigator: DestinationsNavigator) {
    val viewModel: AddPostViewModel = hiltViewModel()
    val sharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    if (viewModel.isFirstTimeSetup) {
        viewModel.setUpData(context)
    }

    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    val snackBarHostState = SnackbarHostState()
    val imageResultLauncher = mediaPicker { uri: Uri ->
        val contentResolver = context.contentResolver
        val mediaType = contentResolver.getType(uri)?.substringBefore("/")
        if (mediaType != null) {
            viewModel.selectedMediaState.value =
                PostMediaData(uri, mediaType)
        }
    }

    Scaffold(topBar = {
        Surface(shadowElevation = 3.dp) {
            TopAppBar(title = {
                Text(
                    text = stringResource(R.string.create_post),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }, actions = {
                Button(
                    enabled = viewModel.captionTextState.value.isNotBlank() || viewModel.selectedMediaState.value != null,
                    onClick = {
                        handleButtonClick(viewModel, context)
                    }
                ) {
                    Text(text = stringResource(R.string.post))
                }
            })
        }
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            HandleAddPostSection(viewModel, context, navigator)
            TopDetailsSection(viewModel = viewModel, sharedViewModel) {
                coroutineScope.launch {
                    keyboardController?.hide()
                    showBottomSheet = true
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                CaptionMediaSection(viewModel)
            }
            BottomButtons { mediaType ->
                imageResultLauncher.launch(PickVisualMediaRequest(mediaType))
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                shape = RoundedCornerShape(
                    topEnd = ConstantsHelper.BottomSheetRoundness,
                    topStart = ConstantsHelper.BottomSheetRoundness
                )
            ) {
                PostVisibilityScopeBottomSheet(
                    modifier = Modifier.padding(bottom = ConstantsHelper.NavigationBarHeight),
                    viewModel = viewModel
                ) {
                    coroutineScope.launch {
                        showBottomSheet = false
                    }
                }
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
private fun HandleAddPostSection(
    viewModel: AddPostViewModel,
    context: Context,
    navigator: DestinationsNavigator
) {
    var isExceptionHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val addPostState = viewModel.uploadPostStateFlow.collectAsState().value
    when (addPostState.status) {
        LOADING -> {
            LoaderDialog(stringResource(R.string.uploading_post))
            isExceptionHandled = false
        }

        SUCCESS -> {
            context.showToast(stringResource(R.string.post_uploaded_successfully))
            navigator.popBackStack()
        }

        EXCEPTION -> {
            if (!isExceptionHandled) {
                if (addPostState.message == ErrorCodes.NoUserFound) {
                    context.showToast(stringResource(id = R.string.some_error_occurred_please_login_again))
                    (LocalActivity.current as BaseActivity).logout()
                } else {
                    viewModel.snackBarMessageState.value =
                        addPostState.message ?: stringResource(id = R.string.some_error_occurred)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "AddPostScreen",
                    addPostState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        NONE -> {
            // no need to handle it
        }
    }
}

@Composable
private fun PostVisibilityScopeBottomSheet(
    modifier: Modifier,
    viewModel: AddPostViewModel,
    onDismissRequest: () -> Unit
) {
    Column(modifier = modifier) {
        viewModel.postVisibilityScopeList.forEach { postScope ->
            VisibilityScopeBottomSheetItem(postScope) {
                viewModel.currentPostVisibilityState.value = postScope
                onDismissRequest()
            }
        }
    }
}

@Composable
private fun CaptionMediaSection(viewModel: AddPostViewModel) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        PostCaptionField(viewModel)
        MediaSection(viewModel, context)
    }
}

@Composable
private fun MediaSection(viewModel: AddPostViewModel, context: Context) {
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

    }
}

@Composable
private fun ShowSelectedImage(selectedMediaData: PostMediaData, onError: () -> Unit) {
    AsyncImage(
        model = selectedMediaData.uri,
        contentDescription = stringResource(R.string.post_image),
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.Crop,
        onError = {
            onError()
        }
    )
}

@Composable
private fun ShowSelectedVideo(selectedMediaData: PostMediaData, context: Context) {
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
    })) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
private fun TopDetailsSection(
    viewModel: AddPostViewModel,
    sharedViewModel: HomeSharedViewModel,
    onVisibilityScopeClick: () -> Unit
) {
    val currentSelectedPostVisibility = viewModel.currentPostVisibilityState.value
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserDetailsSection(
            user = sharedViewModel.usersDetails,
            modifier = Modifier.weight(1f)
        )
        VisibilityItem(
            drawableId = currentSelectedPostVisibility.drawableId,
            scopeName = currentSelectedPostVisibility.scopeName
        ) {
            onVisibilityScopeClick()
        }
    }
}

@Composable
private fun PostCaptionField(viewModel: AddPostViewModel) {
    TransparentTextField(
        value = viewModel.captionTextState.value,
        placeholder = {
            Text(
                text = stringResource(R.string.add_description),
                fontSize = 14.sp,
                color = ColorsHelper.gray()
            )
        },
        onValueChange = { updatedValue ->
            viewModel.captionTextState.value = updatedValue
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )
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

private fun handleButtonClick(viewModel: AddPostViewModel, context: Context) {
    if (viewModel.captionTextState.value.isBlank() && viewModel.selectedMediaState.value == null) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.please_either_attach_image_video_or_add_some_description)
    } else {
        viewModel.uploadUserPost()
    }
}