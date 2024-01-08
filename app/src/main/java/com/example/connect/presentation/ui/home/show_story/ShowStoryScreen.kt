package com.example.connect.presentation.ui.home.show_story

import android.annotation.SuppressLint
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.GetPlayerView
import com.example.connect.presentation.ui.common.SpacerWidth16
import com.example.connect.presentation.ui.common.SpacerWidth6
import com.example.connect.presentation.ui.common.TextBold14
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.ui.models.StoryActions
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
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
        viewModel.init(allStoriesString, allStoryPosters, currentStoryPosterFirebaseId)
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            StoryMainSection(
                viewModel = viewModel,
                initialPage = viewModel.mapKeyList.indexOf(viewModel.storyVisibleForUserId.value),
                navigator = navigator,
                loggedInUserFirebaseId
            )
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
        val key = viewModel.mapKeyList[index]
        viewModel.storyVisibleForUserId.value = key
        viewModel.currentStoryIndex.intValue = 0
        val currentStoryPoster = viewModel.allUsersList.find { it.firebaseUserId == key }
        if (currentStoryPoster == null) {
            navigator.popBackStack()
            return@HorizontalPager
        }
        UserStories(
            viewModel = viewModel,
            storyBeans = viewModel.allUsersStories[viewModel.storyVisibleForUserId.value],
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
    val currentStory = storyBeans[viewModel.currentStoryIndex.intValue]
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
                        if (viewModel.currentStoryIndex.intValue > 0) {
                            viewModel.currentStoryIndex.intValue--
                        } else {
                            viewModel.currentStoryIndex.intValue = 0
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
                        if (viewModel.currentStoryIndex.intValue < storyBeans.lastIndex) {
                            viewModel.currentStoryIndex.intValue++
                        } else {
                            viewModel.currentStoryIndex.intValue = storyBeans.lastIndex
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
                    currentPageIndex = viewModel.currentStoryIndex.intValue,
                    progressBarIndex = index,
                    startProgress = index == viewModel.currentStoryIndex.intValue && isMediaLoaded,
                    onPauseTimer = pauseTimer,
                    progressMaxTime = if (currentStory.mediaType == MediaTypeEnum.Video.name || currentStory.mediaType == MediaTypeEnum.TextVideo.name) currentStory.videoLength else ConstantsHelper.STORY_PROGRESS_MAX_TIME
                ) {
                    if (viewModel.currentStoryIndex.intValue < storyBeans.lastIndex) {
                        viewModel.currentStoryIndex.intValue++
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
        )
        StoryUi(
            viewModel = viewModel,
            story = currentStory,
        ) {
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

@SuppressLint("OpaqueUnitKey")
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
            onStateChange = {isError->
                if (isError) {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.some_error_occurred)
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