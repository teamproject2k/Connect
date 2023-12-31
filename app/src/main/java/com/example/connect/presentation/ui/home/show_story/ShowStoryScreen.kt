package com.example.connect.presentation.ui.home.show_story

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.getColorFromHexString
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
) {
    val allStories: MutableMap<String, ArrayList<StoryBean>> = Gson().fromJson(
        allStoriesString,
        object : TypeToken<MutableMap<String, ArrayList<StoryBean>>>() {}.type
    )
    val viewModel: ShowStoryViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = SnackbarHostState()

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
                StoryContentSection(
                    currentStory,
                    viewModel,
                    allStoryPosters,
                    currentUserStories.size,
                    navigator,
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
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
fun StoryProgressBar(numberOfStories: Int) {
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isWithinFirstHalf by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var horizontalDrag = remember { 0f }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            horizontalDrag > 0 -> { // Right Swipe
                                val currentUserIndex =
                                    allStoryPosters.indexOf(viewModel.currentStoryPosterState.value)
                                val previousUser =
                                    allStoryPosters.getOrNull(currentUserIndex - 1)
                                if (previousUser == null) {
                                    navigator.popBackStack()
                                    return@detectHorizontalDragGestures
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
                                    return@detectHorizontalDragGestures
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