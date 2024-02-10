package com.example.connect.presentation.ui.home.show_story

import android.content.Context
import android.view.MotionEvent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.StoriesWithUserBean
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.StorySeenTimeWithUserDetailsBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha50
import com.example.connect.presentation.ui.common.GetPlayerView
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.SpacerWidth16
import com.example.connect.presentation.ui.common.SpacerWidth6
import com.example.connect.presentation.ui.common.TextBold14
import com.example.connect.presentation.ui.common.TitleMessageIconOkCancelDialog
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.common.UsersListItemLoading
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.ConstantsHelper.ERROR_TAG
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun ShowStoryScreen(
    navigator: DestinationsNavigator,
    allBeanStoriesWithUsersList: ArrayList<StoriesWithUserBean>,
    currentStoryIndex: Int,
    loggedInUserFirebaseId: String
) {
    val viewModel: ShowStoryViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    if (!viewModel.areDetailsInitialized) {
        viewModel.init(allBeanStoriesWithUsersList, currentStoryIndex)
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            StoryMainSection(
                viewModel = viewModel,
                navigator = navigator,
                loggedInUserFirebaseId
            )
        }
        if (viewModel.showStorySeenListBottomSheetState.value) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.showStorySeenListBottomSheetState.value = false
                    viewModel.pauseTimerState.value = false
                },
                shape = RoundedCornerShape(
                    topEnd = ConstantsHelper.BottomSheetRoundness,
                    topStart = ConstantsHelper.BottomSheetRoundness
                )

            ) {
                Column(modifier = Modifier.padding(bottom = ConstantsHelper.NavigationBarHeight)) {
                    ShowStoryBottomSheet(viewModel = viewModel)
                }
            }
        }
    }
    HandleDeleteStoryState(viewModel = viewModel, navigator)
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
fun ShowStoryBottomSheet(viewModel: ShowStoryViewModel) {
    val getSeenListState =
        viewModel.getSeenListStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (getSeenListState.status) {
        RequestStatusEnum.Loading -> {
            repeat(3) {
                UsersListItemLoading()
            }
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    getSeenListState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ERROR_TAG,
                    ScreenNameEnum.ShowStoryScreen.name,
                    getSeenListState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                isResponseHandled = true
            }
            StorySeenListSection(getSeenListState.data ?: emptyList())
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
fun StorySeenListSection(storySeenList: List<StorySeenTimeWithUserDetailsBean>) {
    LazyColumn {
        if (storySeenList.isEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.no_user_found),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            items(storySeenList) {
                StorySeenListItem(it)
            }
        }

    }
}

@Composable
fun StorySeenListItem(storySeenTimeWithUserDetailsBean: StorySeenTimeWithUserDetailsBean) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            UserDetailsSection(
                user = storySeenTimeWithUserDetailsBean.seenBy,
                modifier = Modifier.weight(1f)
            )
            Text(
                fontSize = 14.sp,
                color = ColorsHelper.black(),
                fontWeight = FontWeight.Medium,
                text = FunctionHelper.getTimeAgo(
                    storySeenTimeWithUserDetailsBean.seenAt,
                    context,
                    true
                )
            )
        }
        DividerLightGrayAlpha50()
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryMainSection(
    viewModel: ShowStoryViewModel,
    navigator: DestinationsNavigator,
    loggedInUserFirebaseId: String
) {
    val pageState = rememberPagerState(viewModel.userStoriesIndexState.intValue) {
        viewModel.allStoriesWithUsersList.size
    }
    HorizontalPager(state = pageState, modifier = Modifier.fillMaxSize()) { index ->
        if (index !in 0..viewModel.allStoriesWithUsersList.lastIndex) {
            LoggingHelper.logData(
                LoggingLevelEnum.Error,
                ERROR_TAG,
                ScreenNameEnum.ShowStoryScreen.name,
                "Index $index not found for in stories with user list"
            )
            navigator.popBackStack()
            return@HorizontalPager
        }
        if (index != viewModel.userStoriesIndexState.intValue) {
            viewModel.currentStoryIndexState.intValue = 0
        }
        viewModel.userStoriesIndexState.intValue = index
        val currentStoriesWithUser =
            viewModel.allStoriesWithUsersList[viewModel.userStoriesIndexState.value]
        UserStories(
            viewModel = viewModel,
            storyList = currentStoriesWithUser.storiesList,
            storyPoster = currentStoriesWithUser.usersBean,
            navigator = navigator,
            loggedInUserFirebaseId
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserStories(
    viewModel: ShowStoryViewModel,
    storyList: ArrayList<StoryBean>,
    storyPoster: UsersBean,
    navigator: DestinationsNavigator,
    loggedInUserFirebaseId: String
) {
    val context = LocalContext.current
    val screenWidth = context.resources.displayMetrics.widthPixels
    val screenHeight = context.resources.displayMetrics.heightPixels
    if (storyList.isEmpty()) {
        LoggingHelper.logData(
            LoggingLevelEnum.Error,
            ERROR_TAG,
            ScreenNameEnum.ShowStoryScreen.name,
            "empty story list"
        )
        navigator.popBackStack()
        return
    }
    val currentStory = storyList[viewModel.currentStoryIndexState.intValue]
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
                if (tapLocationY > screenHeight * 0.3f) {
                    val screenSplitCoordinates = screenWidth.toFloat() / 3
                    if (tapLocationX in 0.0f..screenSplitCoordinates && it.action == MotionEvent.ACTION_DOWN) {
                        if (viewModel.currentStoryIndexState.intValue > 0) {
                            viewModel.currentStoryIndexState.intValue--
                        } else {
                            viewModel.currentStoryIndexState.intValue = 0
                        }
                    } else if (tapLocationX in screenSplitCoordinates..2 * screenSplitCoordinates) {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                viewModel.pauseTimerState.value = true
                            }

                            MotionEvent.ACTION_UP -> {
                                viewModel.pauseTimerState.value = false
                            }
                        }
                    } else if (it.action == MotionEvent.ACTION_DOWN) {
                        if (viewModel.currentStoryIndexState.intValue < storyList.lastIndex) {
                            viewModel.currentStoryIndexState.intValue++
                        } else {
                            viewModel.currentStoryIndexState.intValue = storyList.lastIndex
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
            for (index in 0 until storyList.size) {
                LinearIndicator(
                    modifier = Modifier.weight(1f),
                    currentPageIndex = viewModel.currentStoryIndexState.intValue,
                    progressBarIndex = index,
                    startProgress = index == viewModel.currentStoryIndexState.intValue && isMediaLoaded,
                    onPauseTimer = viewModel.pauseTimerState.value,
                    progressMaxTime = if (currentStory.mediaType == MediaTypeEnum.Video.name || currentStory.mediaType == MediaTypeEnum.TextVideo.name) currentStory.videoLength else ConstantsHelper.STORY_PROGRESS_MAX_TIME
                ) {
                    if (viewModel.currentStoryIndexState.intValue < storyList.lastIndex) {
                        viewModel.currentStoryIndexState.intValue++
                    }
                }
                SpacerWidth6()
            }
        }
        StoryTopSection(
            storyPoster = storyPoster,
            story = currentStory,
            context = context,
            navigator = navigator,
            loggedInUserFirebaseId = loggedInUserFirebaseId,
            viewModel = viewModel
        )
        StoryUi(
            viewModel = viewModel,
            story = currentStory,
        ) {
            val story =
                viewModel.allStoriesWithUsersList[viewModel.userStoriesIndexState.intValue].storiesList[viewModel.currentStoryIndexState.intValue]
            if (story.createdByUserFirebaseId != loggedInUserFirebaseId) {
                viewModel.addUserToSeenList(
                    story.storyFirebaseId,
                    loggedInUserFirebaseId,
                    FunctionHelper.getCurrentTimeInMillis()
                )
            }
            isMediaLoaded = true
        }
    }
}

@Composable
fun StoryUi(
    viewModel: ShowStoryViewModel,
    story: StoryBean,
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
    story: StoryBean,
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
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable {
                    if (storyPoster.firebaseUserId == loggedInUserFirebaseId) {
                        navigator.navigate(CurrentUserProfileScreenDestination)
                    } else {
                        navigator.navigate(OtherUserProfileScreenDestination(storyPoster))
                    }
                }, verticalAlignment = Alignment.CenterVertically
        ) {
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
                    text = if (storyPoster.firebaseUserId == loggedInUserFirebaseId) stringResource(
                        R.string.your_story
                    ) else storyPoster.name,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = FunctionHelper.getTimeAgo(story.createdAt, context),
                    fontSize = 12.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        if (storyPoster.firebaseUserId == loggedInUserFirebaseId) {
            Box {
                IconButton(onClick = {
                    viewModel.isDropdownMenuVisibleState.value = true
                    viewModel.pauseTimerState.value = true
                }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(id = R.string.more_options),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                if (viewModel.isDropdownMenuVisibleState.value) {
                    ShowStoryDropDownSection(viewModel, loggedInUserFirebaseId)
                }
            }
        }
    }
}

@Composable
fun ShowStoryDropDownSection(viewModel: ShowStoryViewModel, loggedInUserFirebaseId: String) {
    var showDeleteStoryAlertDialog by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    DropdownMenu(
        expanded = true,
        onDismissRequest = {
            viewModel.isDropdownMenuVisibleState.value = false
            viewModel.pauseTimerState.value = false
        }
    ) {
        DropdownMenuItem(text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = stringResource(id = R.string.seen_list)
                )
                SpacerWidth16()
                Text(text = stringResource(id = R.string.seen_list))
            }
        }, onClick = {
            viewModel.getSeenList(
                viewModel.allStoriesWithUsersList[viewModel.userStoriesIndexState.intValue].storiesList[viewModel.currentStoryIndexState.intValue].storyFirebaseId,
                loggedInUserFirebaseId
            )
            viewModel.showStorySeenListBottomSheetState.value = true
            viewModel.pauseTimerState.value = true
            viewModel.isDropdownMenuVisibleState.value = false
        })
        DropdownMenuItem(text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete_story)
                )
                SpacerWidth16()
                Text(text = stringResource(id = R.string.delete_story))
            }
        }, onClick = {
            showDeleteStoryAlertDialog = true
        })
    }
    if (showDeleteStoryAlertDialog) {
        TitleMessageIconOkCancelDialog(
            imageVector = Icons.Default.Warning,
            iconTint = ColorsHelper.warning(),
            title = stringResource(id = R.string.delete_story),
            subTitle = stringResource(R.string.are_you_sure_you_want_to_delete_this_story),
            positiveButtonText = stringResource(R.string.delete),
            onCancel = {
                showDeleteStoryAlertDialog = false
                viewModel.isDropdownMenuVisibleState.value = false
                viewModel.pauseTimerState.value = false
            }) {
            showDeleteStoryAlertDialog = false
            viewModel.isDropdownMenuVisibleState.value = false
            if (context.isNetworkAvailable()) {
                viewModel.deleteStory(viewModel.allStoriesWithUsersList[viewModel.userStoriesIndexState.intValue].storiesList[viewModel.currentStoryIndexState.intValue])
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.no_internet_connection)
            }
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
                    context.getString(R.string.something_went_wrong)
            }) {
                onMediaLoaded()
            }
        } else if (story.mediaType == MediaTypeEnum.Video.name || story.mediaType == MediaTypeEnum.TextVideo.name) {
            ShowStoryVideo(videoUrl = story.mediaUrl, viewModel, context = context) {
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

@Composable
private fun ShowStoryVideo(
    videoUrl: String,
    viewModel: ShowStoryViewModel,
    context: Context,
    onMediaLoaded: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GetPlayerView(
            context = context,
            uri = videoUrl,
            loadingColorRes = R.color.white,
            onStateChange = { isError ->
                if (isError) {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.something_went_wrong)
                } else {
                    onMediaLoaded()
                }
            }) { _, _ ->
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


@Composable
fun HandleDeleteStoryState(viewModel: ShowStoryViewModel, navigator: DestinationsNavigator) {
    val deleteStoryState =
        viewModel.deleteStoryStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (deleteStoryState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(id = R.string.deleting_story))
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    deleteStoryState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ERROR_TAG,
                    ScreenNameEnum.ShowStoryScreen.name,
                    deleteStoryState.message.toString()
                )
                viewModel.pauseTimerState.value = false
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.pauseTimerState.value = false
                if (deleteStoryState.data == true) {
                    navigator.popBackStack()
                }
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}