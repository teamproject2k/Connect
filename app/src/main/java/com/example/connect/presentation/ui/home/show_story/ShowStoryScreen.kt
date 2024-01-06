package com.example.connect.presentation.ui.home.show_story

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
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
import com.example.connect.presentation.ui.common.SpacerWidth6
import com.example.connect.presentation.ui.common.TextBold14
import com.example.connect.presentation.ui.common.TitleMessageIconOkCancelDialog
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.models.StoryActions
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
    val context = LocalContext.current
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
                navigator = navigator,
                loggedInUserFirebaseId
            )
        }
    }
//    DeleteStoryDialog(viewModel, currentStory.id)
    HandleGetSeenListState(viewModel, context)
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
    navigator: DestinationsNavigator,
    loggedInUserFirebaseId: String
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
        UserStories(
            viewModel = viewModel,
            storyBeans = viewModel.allUsersStories[key],
            storyPoster = currentStoryPoster,
            navigator = navigator,
            loggedInUserFirebaseId
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserStories(
    viewModel: ShowStoryViewModel,
    storyBeans: ArrayList<StoryBean>?,
    storyPoster: UsersBean,
    navigator: DestinationsNavigator,
    loggedInUserFirebaseId: String
) {
    val context = LocalContext.current
    val screenWidth = context.resources.displayMetrics.widthPixels
    val screenHeight = context.resources.displayMetrics.heightPixels
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
    var isMediaLoaded by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(FunctionHelper.getColorListFromColorString(currentStory.backgroundGradientColor)))
            .pointerInteropFilter {
                val tapLocationX = it.x
                val tapLocationY = it.y
                if (tapLocationY > screenHeight * 0.4f) {
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
                    } else if (it.action == MotionEvent.ACTION_DOWN) {
                        if (currentStoryIndex < storyBeans.lastIndex) {
                            currentStoryIndex++
                        } else {
                            currentStoryIndex = storyBeans.lastIndex
                        }
                    }
                    true
                } else {
                    false
                }
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
                    startProgress = index == currentStoryIndex && isMediaLoaded,
                    onPauseTimer = pauseTimer,
                    progressMaxTime = if (currentStory.mediaType == MediaTypeEnum.Video.name || currentStory.mediaType == MediaTypeEnum.TextVideo.name) currentStory.videoLength else ConstantsHelper.STORY_PROGRESS_MAX_TIME
                ) {
                    if (currentStoryIndex < storyBeans.lastIndex) {
                        currentStoryIndex++
                    }
                }
                SpacerWidth6()
            }
        }
        StoryTopSection(
            storyPoster = storyPoster,
            createdAt = currentStory.createdAt,
            context = context,
            navigator = navigator,
            loggedInUserFirebaseId = loggedInUserFirebaseId,
            viewModel = viewModel
        )
        StoryUi(
            viewModel = viewModel,
            story = currentStory,
            storyPoster = storyPoster,
            navigator = navigator
        ) {
            isMediaLoaded = true
        }
    }
}

@Composable
fun StoryUi(
    viewModel: ShowStoryViewModel,
    story: StoryBean,
    storyPoster: UsersBean,
    navigator: DestinationsNavigator,
    onMediaLoaded: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (story.mediaType == MediaTypeEnum.Text.name) {
            onMediaLoaded()
        }
        MediaSection(story, viewModel, context) {
            onMediaLoaded()
        }
        StoryCaptionField(story)
    }
}


@Composable
private fun StoryTopSection(
    storyPoster: UsersBean,
    createdAt: Long,
    context: Context,
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
    loggedInUserFirebaseId: String,
    viewModel: ShowStoryViewModel
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            modifier = Modifier.clickable { navigator.popBackStack() },
            imageVector = Icons.Default.ArrowBack,
            contentDescription = stringResource(id = R.string.go_back),
            tint = MaterialTheme.colorScheme.onPrimary
        )
        SpacerWidth16()
        AsyncImage(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
            model = storyPoster.profilePhoto,
            contentDescription = storyPoster.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
        ) {
            TextBold14(
                text = if (storyPoster.firebaseUserId == loggedInUserFirebaseId) stringResource(R.string.your_story) else storyPoster.name,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = FunctionHelper.getTimeAgo(createdAt, context),
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        if (storyPoster.firebaseUserId == loggedInUserFirebaseId) {
            StoryDropDownSection(viewModel)
        }
    }
}

@Composable
fun StoryDropDownSection(viewModel: ShowStoryViewModel) {
    val context = LocalContext.current
    val storyActionsList = getStoryActions(context)
    var isDropdownMenuVisible by remember {
        mutableStateOf(false)
    }

    var showDeleteStoryAlertDialog by remember {
        mutableStateOf(false)
    }

    Box {
        Icon(
            modifier = Modifier.clickable { isDropdownMenuVisible = true },
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(id = R.string.more_options),
            tint = MaterialTheme.colorScheme.onPrimary
        )
        if (isDropdownMenuVisible) {
            DropdownMenu(
                expanded = true, onDismissRequest = { isDropdownMenuVisible = false }
            ) {
                storyActionsList.forEach { item ->
                    DropdownMenuItem(text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = item.icon,
                                contentDescription = item.text
                            )
                            SpacerWidth16()
                            Text(text = item.text)
                        }
                    }, onClick = {
                        if (item.id == 1) {
                            viewModel.showSeenListBottomSheet.value = true
                        } else if (item.id == 2) {
                            showDeleteStoryAlertDialog = true
                        }
                        isDropdownMenuVisible = false
                    })
                }
            }
        }
        if (showDeleteStoryAlertDialog) {
            TitleMessageIconOkCancelDialog(
                imageVector = Icons.Default.Warning,
                iconTint = ColorsHelper.warning(),
                title = stringResource(id = R.string.delete_story),
                subTitle = stringResource(R.string.are_you_sure_you_want_to_delete_your_story),
                positiveButtonText = stringResource(R.string.delete),
                onCancel = {
                    showDeleteStoryAlertDialog = false
                }) {
                // viewModel.deleteStory(currentStoryId)
            }
        }
    }
}

private fun getStoryActions(context: Context): ArrayList<StoryActions> {
    val storyActionsList = arrayListOf<StoryActions>()
    storyActionsList.add(
        StoryActions(
            1,
            context.getString(R.string.seen_list),
            Icons.Default.RemoveRedEye,
            null
        )
    )
    storyActionsList.add(
        StoryActions(
            2,
            context.getString(R.string.delete_story),
            Icons.Default.Delete,
            null
        )
    )
    return storyActionsList
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
//                currentUserStories.remove(currentStory)
//                if (currentUserStories.isEmpty()) {
//                    navigator.popBackStack()
//                } else if (viewModel.currentStoryState.intValue > 0) {
//                    viewModel.currentStoryState.intValue--
//                }
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
private fun MediaSection(
    story: StoryBean,
    viewModel: ShowStoryViewModel,
    context: Context,
    onMediaLoaded: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (story.mediaType == MediaTypeEnum.Image.name || story.mediaType == MediaTypeEnum.TextImage.name) {
            ShowStoryImage(imageUrl = story.mediaUrl, onError = {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.some_error_occurred)
            }) {
                onMediaLoaded()
            }
        } else if (story.mediaType == MediaTypeEnum.Video.name || story.mediaType == MediaTypeEnum.TextVideo.name) {
            ShowStoryVideo(videoUrl = story.mediaUrl, context = context) {
                onMediaLoaded()
            }
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
        color = FunctionHelper.getColorFromColorString(story.textColor),
        fontSize = 18.sp
    )
}

@Composable
private fun ShowStoryImage(imageUrl: String, onError: () -> Unit, onMediaLoaded: () -> Unit) {
    var isImageLoading by remember {
        mutableStateOf(false)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.story_image),
            contentScale = ContentScale.Crop,
            onLoading = {
                isImageLoading = true
            },
            onError = {
                isImageLoading = false
                onError()
            },
            onSuccess = {
                isImageLoading = false
                onMediaLoaded()
            }
        )
        if (isImageLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@SuppressLint("OpaqueUnitKey")
@Composable
private fun ShowStoryVideo(videoUrl: String, context: Context, onMediaLoaded: () -> Unit) {
    val exoPlayer = remember {
        FunctionHelper.getExoPlayer(context, videoUrl)
    }
    var isPlayerLoading by remember {
        mutableStateOf(false)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        DisposableEffect(AndroidView(factory = {
            PlayerView(context).apply {
                player = exoPlayer
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                exoPlayer.addListener(object : Player.Listener {
                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        isPlayerLoading = isLoading
                    }
                })
                setShowNextButton(false)
                setShowPreviousButton(false)
                exoPlayer.prepare()
                exoPlayer.playWhenReady
            }
        })) {
            onDispose {
                exoPlayer.release()
            }
        }
        if (isPlayerLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        }
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
    progressMaxTime: Long = ConstantsHelper.STORY_PROGRESS_MAX_TIME,
    onPauseTimer: Boolean = false,
    onAnimationEnd: () -> Unit
) {

    val delayInMillis = rememberSaveable {
        progressMaxTime / 100
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