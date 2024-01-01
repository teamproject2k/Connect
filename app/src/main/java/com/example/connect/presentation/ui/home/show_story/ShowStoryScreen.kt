package com.example.connect.presentation.ui.home.show_story

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.SpacerWidth32
import com.example.connect.presentation.ui.common.StoryUserItem
import com.example.connect.presentation.ui.common.TextBold14
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.getColorFromHexString
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HomeNavGraph
@Destination
@Composable
fun ShowStoryScreen(
    navigator: DestinationsNavigator,
    currentStoryPoster: UsersBean,
    allStoriesString: String,
    allStoryPosters: ArrayList<UsersBean>,
    loggedInUserFirebaseId: String
) {
    val allStories: MutableMap<String, ArrayList<StoryBean>> = Gson().fromJson(
        allStoriesString,
        object : TypeToken<MutableMap<String, ArrayList<StoryBean>>>() {}.type
    )
    val viewModel: ShowStoryViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = SnackbarHostState()
    val context = LocalContext.current

    if (!viewModel.isCurrentStoryPosterInitialized) {
        viewModel.currentStoryPosterState = remember {
            mutableStateOf(currentStoryPoster)
        }
        viewModel.isCurrentStoryPosterInitialized = true
    }

    val currentUserStories = allStories[viewModel.currentStoryPosterState.value.firebaseUserId]

    if (currentUserStories != null) {

        val currentStory = currentUserStories[viewModel.currentStoryState.intValue]
        val storyGradientColors = currentStory.backgroundGradientColor.split(",")
        val colorList = arrayListOf<Color>()

        storyGradientColors.forEach { colorString ->
            colorList.add(getColorFromHexString(colorString))
        }

        Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(colorList)
                    )
            ) {
                // StoryProgressBar(stories.size)
                StoryUserItem(
                    user = viewModel.currentStoryPosterState.value,
                    story = currentStory,
                    context = context,
                    navigator = navigator
                )
                StoryContentSection(
                    currentStory,
                    viewModel,
                    allStoryPosters,
                    currentUserStories.size,
                    navigator,
                    loggedInUserFirebaseId,
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                )
                HandleGetSeenListState(viewModel, context)
                HandleDeleteStoryState(
                    currentUserStories,
                    currentStory,
                    viewModel,
                    navigator,
                    context
                )
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
fun HandleGetSeenListState(viewModel: ShowStoryViewModel, context: Context) {
    var isExceptionHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val getSeenListState = viewModel.getSeenListStateFlow.collectAsState().value
    when (getSeenListState.status) {
        RequestStatusEnum.Loading -> {
            isExceptionHandled = false
        }

        RequestStatusEnum.Success -> {
            if (getSeenListState.data != null) {
                LoadSeenListBottomSheet(viewModel, getSeenListState.data, context)
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    getSeenListState.message
                        ?: stringResource(id = R.string.some_error_occurred)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "ShowStoryScreen",
                    getSeenListState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
fun HandleDeleteStoryState(
    currentUserStories: ArrayList<StoryBean>,
    currentStory: StoryBean,
    viewModel: ShowStoryViewModel,
    navigator: DestinationsNavigator,
    context: Context
) {
    var isResponseHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val deleteStoryState = viewModel.deleteStoryStateFlow.collectAsState().value
    when (deleteStoryState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(R.string.deleting_story))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                context.showToast(stringResource(R.string.story_deleted_successfully))
                currentUserStories.remove(currentStory)
                if (currentUserStories.isEmpty()) {
                    navigator.popBackStack()
                } else if (viewModel.currentStoryState.intValue > 0) {
                    viewModel.currentStoryState.intValue--
                }
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    deleteStoryState.message ?: stringResource(id = R.string.some_error_occurred)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "ShowStoryScreen",
                    deleteStoryState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadSeenListBottomSheet(
    viewModel: ShowStoryViewModel,
    seenList: List<Pair<String, Long>>,
    context: Context
) {
    var showBottomSheet by viewModel.showSeenListBottomSheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            shape = RoundedCornerShape(
                topEnd = ConstantsHelper.BottomSheetRoundness,
                topStart = ConstantsHelper.BottomSheetRoundness
            )
        ) {
            SeenListBottomSheet(
                modifier = Modifier.padding(bottom = ConstantsHelper.NavigationBarHeight),
                seenList,
                context
            )
        }
    }
}

@Composable
private fun SeenListBottomSheet(
    modifier: Modifier,
    seenList: List<Pair<String, Long>>,
    context: Context
) {
    Column(modifier = modifier) {
        seenList.forEach { listItem ->
            // SeenListUserItem(user =, postedAt = listItem.second, context)
        }
    }
}

@Composable
private fun StoryProgressBar(numberOfStories: Int) {
    var progress by remember { mutableStateOf(0f) }

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val progressBarWidth = (screenWidth / numberOfStories).dp

    LaunchedEffect(Unit) {
        repeat(numberOfStories) { part ->
            launch {
                for (i in 0..100 step 25) {
                    progress = i.toFloat()
                    delay(5000)
                }
            }
        }
    }

    LazyRow(modifier = Modifier.padding(top = 4.dp)) {
        items(numberOfStories) {
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier.width(progressBarWidth),
                color = Color.White,
                trackColor = Color.Gray
            )
        }
    }
}

@Composable
private fun StoryContentSection(
    story: StoryBean,
    viewModel: ShowStoryViewModel,
    allStoryPosters: ArrayList<UsersBean>,
    numberOfStories: Int,
    navigator: DestinationsNavigator,
    loggedInUserFirebaseId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isWithinFirstHalf by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var horizontalDrag = remember { 0f }
    val isLoggedInUser = loggedInUserFirebaseId == story.fireBaseUserId

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            horizontalDrag > 0 -> {  // Right Swipe
                                val currentUserIndex =
                                    allStoryPosters.indexOf(viewModel.currentStoryPosterState.value)
                                val previousUser =
                                    allStoryPosters.getOrNull(currentUserIndex - 1)
                                if (previousUser == null) {
                                    navigator.popBackStack()
                                } else {
                                    viewModel.currentStoryPosterState.value = previousUser
                                    viewModel.currentStoryState.intValue = 0
                                }
                            }

                            horizontalDrag < 0 -> {  // Left Swipe
                                val currentUserIndex =
                                    allStoryPosters.indexOf(viewModel.currentStoryPosterState.value)
                                val nextUser =
                                    allStoryPosters.getOrNull(currentUserIndex + 1)
                                if (nextUser == null) {
                                    navigator.popBackStack()
                                } else {
                                    viewModel.currentStoryPosterState.value = nextUser
                                    viewModel.currentStoryState.intValue = 0
                                }
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        horizontalDrag = dragAmount
                    },
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    isWithinFirstHalf = offset.x.dp < (screenWidth / 2)
                    if (isWithinFirstHalf) {
                        if (viewModel.currentStoryState.intValue != 0) {
                            viewModel.currentStoryState.intValue--
                        }
                    } else {
                        if (viewModel.currentStoryState.intValue != numberOfStories - 1) {
                            viewModel.currentStoryState.intValue++
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            MediaSection(story, viewModel, context)
            StoryCaptionField(story)
            if (isLoggedInUser) {
                StoryActionButtons(story.id, viewModel)
            }
        }
    }
    if (!isLoggedInUser) {
        LaunchedEffect(key1 = Unit) {
            viewModel.addUserToSeenList(story.id, loggedInUserFirebaseId)
        }
    }
}

@Composable
private fun MediaSection(story: StoryBean, viewModel: ShowStoryViewModel, context: Context) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (story.mediaType == MediaTypeEnum.Image::name.name || story.mediaType == MediaTypeEnum.TextImage::name.name) {
            ShowStoryImage(imageUrl = story.mediaUrl) {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.some_error_occurred)
            }
        } else if (story.mediaType == MediaTypeEnum.Video::name.name || story.mediaType == MediaTypeEnum.TextVideo::name.name) {
            ShowStoryVideo(videoUrl = story.mediaUrl, context = context)
        }
    }
}

@Composable
private fun StoryCaptionField(story: StoryBean) {
    val captionOffset = story.textOffset.split(",")
    val captionOffsetX = captionOffset[0].trim().toFloat().toInt()
    val captionOffsetY = captionOffset[1].trim().toFloat().toInt()

    Text(
        modifier = Modifier
            .offset {
                IntOffset(
                    captionOffsetX,
                    captionOffsetY
                )
            },
        text = story.caption,
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 18.sp
    )
}

@Composable
private fun ShowStoryImage(imageUrl: String, onError: () -> Unit) {
    AsyncImage(
        model = imageUrl,
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
private fun ShowStoryVideo(videoUrl: String, context: Context) {
    val exoPlayer = remember {
        FunctionHelper.getExoPlayer(context, videoUrl)
    }
    DisposableEffect(AndroidView(factory = {
        PlayerView(context).apply {
            player = exoPlayer
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }, update = {
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
    })) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
private fun StoryActionButtons(storyId: String, viewModel: ShowStoryViewModel) {
    LaunchedEffect(Unit) {
        viewModel.getSeenList(storyId)
    }
    LaunchedEffect(Unit) {
        viewModel.deleteStory(storyId)
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 40.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        Icon(
            modifier = Modifier.clickable { viewModel.showSeenListBottomSheet.value = true },
            imageVector = Icons.Default.RemoveRedEye,
            contentDescription = stringResource(R.string.seen_by),
            tint = MaterialTheme.colorScheme.onPrimary
        )
        SpacerWidth32()
        Icon(
            modifier = Modifier.clickable { viewModel.deleteStory(storyId) },
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(R.string.delete_story),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun SeenListUserItem(
    user: UsersBean,
    postedAt: Long,
    context: Context,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        AsyncImage(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            model = user.profilePhoto,
            contentDescription = user.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp),
        ) {
            TextBold14(text = user.name, color = MaterialTheme.colorScheme.onPrimary)
            Text(
                text = FunctionHelper.getTimeAgo(postedAt, context),
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}