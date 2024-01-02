package com.example.connect.presentation.ui.home.show_story

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
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
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.SpacerWidth16
import com.example.connect.presentation.ui.common.SpacerWidth32
import com.example.connect.presentation.ui.common.SpacerWidth6
import com.example.connect.presentation.ui.common.TextBold14
import com.example.connect.presentation.ui.common.TitleMessageIconOkCancelDialog
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HomeNavGraph
@Destination
@Composable
fun ShowStoryScreen(
    navigator: DestinationsNavigator,
    currentStoryPosterFirebaseId: String,
    allStoriesString: String,
    allStoryPosters: ArrayList<UsersBean>,
    loggedInUserFirebaseId: String
) {
    val viewModel: ShowStoryViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = SnackbarHostState()
    if (!viewModel.areDetailsInitialized) {
        viewModel.init(allStoriesString, allStoryPosters)
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            StoryMainSection(
                viewModel = viewModel,
                initialPage = viewModel.allUsersStories.keys.toList()
                    .indexOf(currentStoryPosterFirebaseId),
                navigator = navigator
            )
        }
    }
//    DeleteStoryDialog(viewModel, currentStory.id)
//    HandleGetSeenListState(viewModel, context)
//    HandleDeleteStoryState(
//        currentUserStories,
//        currentStory,
//        viewModel,
//        navigator,
//        context
//    )

    LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            coroutineScope.launch {
                snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
                viewModel.snackBarMessageState.value = ""
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryMainSection(
    viewModel: ShowStoryViewModel,
    initialPage: Int,
    navigator: DestinationsNavigator
) {
    val pageState = rememberPagerState(initialPage) {
        viewModel.allUsersStories.size
    }
    HorizontalPager(state = pageState, modifier = Modifier.fillMaxSize()) { index ->
        val key = viewModel.allUsersStories.keys.toList()[index]
        val currentStoryPoster = viewModel.allUsersList.find { it.firebaseUserId == key }
        if (currentStoryPoster == null) {
            navigator.popBackStack()
            return@HorizontalPager
        }
        UserStores(
            viewModel = viewModel,
            storyBeans = viewModel.allUsersStories[key],
            storyPoster = currentStoryPoster,
            navigator = navigator
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserStores(
    viewModel: ShowStoryViewModel,
    storyBeans: ArrayList<StoryBean>?,
    storyPoster: UsersBean,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val screenWidth = context.resources.displayMetrics.widthPixels
    if (storyBeans.isNullOrEmpty()) {
        navigator.popBackStack()
        return
    }
    var currentStoryIndex by remember {
        mutableIntStateOf(0)
    }
    val currentStory = storyBeans[currentStoryIndex]
    var pauseTimer by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(FunctionHelper.getColorListFromColorString(currentStory.backgroundGradientColor)))
            .pointerInteropFilter {
                val tapLocationX = it.x
                val screenSplitCoordinates = screenWidth.toFloat() / 3
                if (tapLocationX in 0.0f..screenSplitCoordinates && it.action == MotionEvent.ACTION_DOWN) {
                    if (currentStoryIndex > 0) {
                        currentStoryIndex--
                    } else {
                        currentStoryIndex = 0
                    }
                } else if (tapLocationX in screenSplitCoordinates..2 * screenSplitCoordinates) {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            pauseTimer = true
                        }

                        MotionEvent.ACTION_UP -> {
                            pauseTimer = false
                        }
                    }
                } else if(it.action == MotionEvent.ACTION_DOWN){
                    if (currentStoryIndex < storyBeans.lastIndex) {
                        currentStoryIndex++
                    } else {
                        currentStoryIndex = storyBeans.lastIndex
                    }
                }

                true
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SpacerWidth6()
            for (index in 0 until storyBeans.size) {
                LinearIndicator(
                    modifier = Modifier.weight(1f),
                    currentPageIndex = currentStoryIndex,
                    progressBarIndex = index,
                    startProgress = index == currentStoryIndex,
                    onPauseTimer = pauseTimer
                ) {
                    if (currentStoryIndex < storyBeans.lastIndex) {
                        currentStoryIndex++
                    }
                }
                SpacerWidth6()
            }
        }
        StoryTopSection(
            user = storyPoster,
            createdAt = currentStory.createdAt,
            context = context,
            navigator = navigator
        )
        StoryUi(
            viewModel = viewModel,
            story = currentStory,
            storyPoster = storyPoster,
            navigator = navigator
        )
    }
}

@Composable
fun StoryUi(
    viewModel: ShowStoryViewModel,
    story: StoryBean,
    storyPoster: UsersBean,
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = if (story.mediaUrl.isBlank()) Alignment.Center else Alignment.BottomCenter
    ) {
        MediaSection(story, viewModel, context)
        StoryCaptionField(story)
    }
}


@Composable
private fun StoryTopSection(
    user: UsersBean,
    createdAt: Long,
    context: Context,
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            modifier = Modifier.clickable { navigator.popBackStack() },
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onPrimary
        )
        SpacerWidth16()
        AsyncImage(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
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
                text = FunctionHelper.getTimeAgo(createdAt, context),
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun DeleteStoryDialog(viewModel: ShowStoryViewModel, currentStoryId: String) {
    if (viewModel.showDeleteStoryAlertDialog.value) {
        TitleMessageIconOkCancelDialog(
            title = stringResource(id = R.string.delete_story),
            subTitle = stringResource(R.string.are_you_sure_you_want_to_delete_your_story),
            positiveButtonText = stringResource(R.string.delete),
            onCancel = {
                viewModel.showDeleteStoryAlertDialog.value = false
            }) {
            viewModel.deleteStory(currentStoryId)
        }
    }
}

@Composable
private fun HandleGetSeenListState(viewModel: ShowStoryViewModel, context: Context) {
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
private fun HandleDeleteStoryState(
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
        modifier = Modifier.fillMaxSize(),
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
            modifier = Modifier.clickable { viewModel.showDeleteStoryAlertDialog.value = true },
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


@Composable
fun LinearIndicator(
    modifier: Modifier,
    currentPageIndex: Int,
    progressBarIndex: Int,
    startProgress: Boolean = false,
    indicatorBackgroundColor: Color = ColorsHelper.gray(),
    indicatorProgressColor: Color = MaterialTheme.colorScheme.onPrimary,
    slideDurationInSeconds: Long = 10,
    onPauseTimer: Boolean = false,
    onAnimationEnd: () -> Unit
) {

    val delayInMillis = rememberSaveable {
        (slideDurationInSeconds * 1000) / 100
    }

    var progress by remember {
        mutableFloatStateOf(0.00f)
    }
    LaunchedEffect(key1 = currentPageIndex) {
        progress = if (progressBarIndex < currentPageIndex) {
            1f
        } else 0f
    }

    if (startProgress) {
        LaunchedEffect(key1 = onPauseTimer) {
            while (progress < 1f && isActive && onPauseTimer.not()) {
                progress += 0.01f
                delay(delayInMillis)
            }

            //When the timer is not paused and animation completes then move to next page.
            if (onPauseTimer.not()) {
                delay(200)
                onAnimationEnd()
            }
        }
    }
    LinearProgressIndicator(
        trackColor = indicatorBackgroundColor,
        color = indicatorProgressColor,
        modifier = modifier
            .padding(top = 12.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(12.dp)),
        progress = progress
    )
}