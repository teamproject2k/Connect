package com.example.connect.presentation.ui.home.add_post

import android.content.Context
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.VideoCameraFront
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.presentation.ui.common.IconTextSection
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.ui.common.SpacerWidth6
import com.example.connect.presentation.ui.common.TransparentTextField
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.home.HomeSharedViewModel
import com.example.connect.presentation.ui.models.PostMediaData
import com.example.connect.presentation.ui.models.PostVisibilityScope
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@HomeNavGraph
@Destination
@Composable
fun AddPostScreen() {
    val viewModel: AddPostViewModel = hiltViewModel()
    val sharedViewModel: HomeSharedViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    if (viewModel.isFirstTimeSetup) {
        viewModel.setUpData(context)
    }
    val bottomSheetState =
        SheetState(skipPartiallyExpanded = true, initialValue = SheetValue.Hidden)

    val snackBarHostState = SnackbarHostState()
    val bottomSheetScaffoldState =
        rememberBottomSheetScaffoldState(bottomSheetState, snackBarHostState)
    val imageResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            //uri will be null in case user doesn't select any image
            if (uri != null) {
                val contentResolver = context.contentResolver
                val mediaType = contentResolver.getType(uri)?.substringBefore("/")
                if (mediaType != null) {
                    viewModel.selectedMediaState.value =
                        PostMediaData(uri, mediaType)
                }
            }
        }

    BottomSheetScaffold({
        PostVisibilityScopeBottomSheet(viewModel = viewModel) {
            coroutineScope.launch {
                bottomSheetState.hide()
            }
        }
    },
        sheetShape = RoundedCornerShape(
            topEnd = ConstantsHelper.BottomSheetRoundness,
            topStart = ConstantsHelper.BottomSheetRoundness
        ),
        scaffoldState = bottomSheetScaffoldState,
        topBar = {
            Surface(shadowElevation = 3.dp) {
                TopAppBar(title = {
                    Text(
                        text = stringResource(R.string.create_post),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }, actions = {
                    Button(onClick = {
                        handleButtonClick(viewModel, context)
                    }) {
                        Text(text = stringResource(R.string.post))
                    }
                })
            }
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            TopDetailsSection(viewModel = viewModel, sharedViewModel) {
                coroutineScope.launch {
                    keyboardController?.hide()
                    bottomSheetState.show()
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
fun PostVisibilityScopeBottomSheet(viewModel: AddPostViewModel, onDismissRequest: () -> Unit) {
    Column {
        viewModel.postVisibilityScopeList.forEach { postScope ->
            PostVisibilityScopeBottomSheetItem(postScope) {
                viewModel.currentPostVisibilityState.value = postScope
                onDismissRequest()
            }
        }
    }
}

@Composable
fun PostVisibilityScopeBottomSheetItem(
    postVisibilityScope: PostVisibilityScope,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = postVisibilityScope.drawableId),
                contentDescription = postVisibilityScope.scopeName
            )
            SpacerWidth12()
            Column {
                Text(
                    text = postVisibilityScope.scopeName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = postVisibilityScope.scopeDescription,
                    fontSize = 13.sp
                )
            }
        }
        Divider()
    }
}


@Composable
fun CaptionMediaSection(viewModel: AddPostViewModel) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        PostCaptionField(viewModel)
        MediaSection(viewModel, context)
    }
}

@Composable
fun MediaSection(viewModel: AddPostViewModel, context: Context) {
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
fun ShowSelectedImage(selectedMediaData: PostMediaData, onError: () -> Unit) {
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
fun ShowSelectedVideo(selectedMediaData: PostMediaData, context: Context) {
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
fun TopDetailsSection(
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
            imageUrl = sharedViewModel._userDetails.profilePhoto.toString(),
            userName = sharedViewModel._userDetails.name,
            userBio = sharedViewModel._userDetails.bio,
            modifier = Modifier.weight(1f)
        )
        PostVisibilityInTopSection(viewModel) {
            onVisibilityScopeClick()
        }
    }
}

@Composable
fun PostVisibilityInTopSection(
    viewModel: AddPostViewModel,
    onClick: () -> Unit
) {
    val currentSelectedPostVisibility = viewModel.currentPostVisibilityState.value
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp, Color(0x80000000),
                RoundedCornerShape(12.dp),
            )
            .clickable {
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Image(
            painterResource(id = currentSelectedPostVisibility.drawableId),
            contentDescription = currentSelectedPostVisibility.scopeName,
            modifier = Modifier.size(14.dp)
        )
        SpacerWidth6()
        Text(text = currentSelectedPostVisibility.scopeName, fontSize = 12.sp)
    }
}


@Composable
fun PostCaptionField(viewModel: AddPostViewModel) {
    TransparentTextField(
        value = viewModel.captionTextState.value,
        placeholder = {
            Text(
                text = stringResource(R.string.add_description),
                fontSize = 14.sp,
                color = Color.Gray
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
        viewModel.uploadUserPost("123")
    }
}

