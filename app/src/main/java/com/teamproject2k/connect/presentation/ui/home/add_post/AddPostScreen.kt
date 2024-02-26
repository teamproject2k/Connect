package com.teamproject2k.connect.presentation.ui.home.add_post

import android.content.Context
import android.net.Uri
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.presentation.ui.common.AppTopAppBar
import com.teamproject2k.connect.presentation.ui.common.ColorsHelper
import com.teamproject2k.connect.presentation.ui.common.IconTextSection
import com.teamproject2k.connect.presentation.ui.common.LoaderDialog
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.common.ShowSelectedImage
import com.teamproject2k.connect.presentation.ui.common.ShowSelectedVideo
import com.teamproject2k.connect.presentation.ui.common.TransparentTextField
import com.teamproject2k.connect.presentation.ui.common.UserDetailsSection
import com.teamproject2k.connect.presentation.ui.common.VisibilityItem
import com.teamproject2k.connect.presentation.ui.common.VisibilityScopeBottomSheetItem
import com.teamproject2k.connect.presentation.ui.common.mediaPicker
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
import com.teamproject2k.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.teamproject2k.connect.presentation.ui.models.MediaData
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.teamproject2k.connect.presentation.utils.FunctionHelper.showToast
import com.teamproject2k.connect.presentation.utils.HomeNavGraph
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@HomeNavGraph
@Destination
@Composable
fun AddPostScreen(navigator: DestinationsNavigator) {
    val viewModel: AddPostViewModel = hiltViewModel()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    if (viewModel.isFirstTimeSetup) {
        viewModel.init(context)
    }

    var showPostVisibilityScopeBottomSheet by remember {
        mutableStateOf(false)
    }

    val snackBarHostState = remember { SnackbarHostState() }
    val mediaResultLauncher = mediaPicker { uri: Uri ->
        val contentResolver = context.contentResolver
        val mediaType = FunctionHelper.getMediaType(contentResolver, uri)
        if (mediaType != null) {
            viewModel.selectedMediaState.value =
                MediaData(uri, mediaType)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            AppTopAppBar(title = stringResource(R.string.create_post), actions = {
                Button(
                    enabled = viewModel.captionTextState.value.isNotBlank() || viewModel.selectedMediaState.value != null,
                    onClick = {
                        keyboardController?.hide()
                        handleButtonClick(
                            viewModel,
                            context,
                            homeSharedViewModel.usersDetails.firebaseUserId
                        )
                    }
                ) {
                    Text(text = stringResource(R.string.post))
                }
            })
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            HandleAddPostState(viewModel, context, navigator)
            TopDetailsSection(viewModel = viewModel, homeSharedViewModel) {
                coroutineScope.launch {
                    keyboardController?.hide()
                    showPostVisibilityScopeBottomSheet = true
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
                mediaResultLauncher.launch(PickVisualMediaRequest(mediaType))
            }
        }
        if (showPostVisibilityScopeBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPostVisibilityScopeBottomSheet = false },
                shape = RoundedCornerShape(
                    topEnd = ConstantsHelper.BottomSheetRoundness,
                    topStart = ConstantsHelper.BottomSheetRoundness
                )
            ) {
                PostVisibilityScopeBottomSheet(
                    modifier = Modifier.padding(bottom = ConstantsHelper.NavigationBarHeight),
                    viewModel = viewModel
                ) {
                    showPostVisibilityScopeBottomSheet = false
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
private fun HandleAddPostState(
    viewModel: AddPostViewModel,
    context: Context,
    navigator: DestinationsNavigator
) {
    var isResponseHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val addPostState = viewModel.uploadPostStateFlow.collectAsState().value
    when (addPostState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(R.string.uploading_post))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                context.showToast(stringResource(R.string.post_uploaded_successfully))
                navigator.popBackStack()
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    addPostState.message ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.AddPostScreen.name,
                    addPostState.message.toString()
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
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedMedia.mediaType == ConstantsHelper.MEDIA_TYPE_IMAGE) {
                ShowSelectedImage(selectedMediaData = selectedMedia) {
                    viewModel.selectedMediaState.value = null
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.something_went_wrong)
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
private fun TopDetailsSection(
    viewModel: AddPostViewModel,
    sharedViewModel: HomeSharedViewModel,
    onVisibilityScopeClick: () -> Unit
) {
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
            drawableId = viewModel.currentPostVisibilityState.value.drawableId,
            scopeName = viewModel.currentPostVisibilityState.value.scopeName
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
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun BottomButtons(onSelectMedia: (mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType) -> Unit) {
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
                onSelectMedia(ActivityResultContracts.PickVisualMedia.ImageOnly)
            }
            IconTextSection(
                icon = Icons.Rounded.VideoCameraFront,
                text = stringResource(R.string.add_video),
                modifier = Modifier.weight(1f),
                contentArrangement = Arrangement.End
            ) {
                onSelectMedia(ActivityResultContracts.PickVisualMedia.VideoOnly)
            }
        }
    }
}

/**
 * Handles the click event of the add post button.
 *
 * @param viewModel The view model for the add post screen.
 * @param context The context of the activity.
 * @param loggedInUserFirebaseId The Firebase ID of the current user.
 */
private fun handleButtonClick(
    viewModel: AddPostViewModel,
    context: Context,
    loggedInUserFirebaseId: String
) {
    if (viewModel.captionTextState.value.isBlank() && viewModel.selectedMediaState.value == null) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.please_either_attach_image_video_or_add_some_description)
    } else {
        if (context.isNetworkAvailable()) {
            viewModel.uploadUserPost(loggedInUserFirebaseId)
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
    }
}