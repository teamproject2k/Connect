package com.example.connect.presentation.ui.home.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.chat.ChatActivity
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha40
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha50
import com.example.connect.presentation.ui.common.Dot
import com.example.connect.presentation.ui.common.ExpandingText
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.PostCaptionMediaSection
import com.example.connect.presentation.ui.common.PostListLoadingSection
import com.example.connect.presentation.ui.common.SpacerHeight16
import com.example.connect.presentation.ui.common.SpacerHeight6
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.common.shimmer
import com.example.connect.presentation.ui.destinations.AddStoryScreenDestination
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.PostDetailsScreenDestination
import com.example.connect.presentation.ui.destinations.ShowStoryScreenDestination
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.HomeNavGraph
import com.google.gson.Gson
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val viewModel: HomeViewModel = hiltViewModel()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(activity)
    val snackBarHostState = SnackbarHostState()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            AppTopAppBar(title = stringResource(id = R.string.app_name), actions = {
                IconButton(onClick = {
                    navigator.navigate(AddStoryScreenDestination())
                }) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = stringResource(R.string.add_story)
                    )
                }
                IconButton(onClick = {
                    val intent = Intent(context, ChatActivity::class.java)
                    activity.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = stringResource(id = R.string.chat)
                    )
                }
            })
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            HandleStoryDetailsWithUserDetails(
                viewModel = viewModel,
                homeSharedViewModel.usersDetails,
                navigator
            )
            HandlePostDetailsWithUserDetails(
                viewModel = viewModel,
                homeSharedViewModel.usersDetails,
                navigator
            )
        }
    }

    LaunchedEffect(viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
            viewModel.snackBarMessageState.value = ""
        }
    }
    LaunchedEffect(Unit) {
        viewModel.getStoryDetailsWithUserDetails(homeSharedViewModel.usersDetails.firebaseUserId)
    }
    LaunchedEffect(Unit) {
        viewModel.getPostDetailsWithUserDetails(homeSharedViewModel.usersDetails.firebaseUserId)
    }
    HandleLikeUnlikeState(viewModel = viewModel)
    HandleSaveUnSavePost(viewModel)
}

@Composable
private fun HandleStoryDetailsWithUserDetails(
    viewModel: HomeViewModel,
    currentUsersBean: UsersBean,
    navigator: DestinationsNavigator
) {
    val storyDetailsWithUserDetailsState = viewModel.storyDetailsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (storyDetailsWithUserDetailsState.status) {
        RequestStatusEnum.Loading -> {
            StoryLoaderItem()
            isExceptionHandled = false
        }

        RequestStatusEnum.Success -> {
            StoryUiSection(
                storiesPerUser = storyDetailsWithUserDetailsState.data,
                loggedInUserFirebaseId = currentUsersBean.firebaseUserId,
                navigator = navigator
            )
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    storyDetailsWithUserDetailsState.message ?: stringResource(
                        id = R.string.some_error_occurred
                    )
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.HomeScreen.name,
                    storyDetailsWithUserDetailsState.message.toString()
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
private fun StoryUiSection(
    storiesPerUser: Pair<MutableMap<String, ArrayList<StoryBean>>, ArrayList<UsersBean>>?,
    loggedInUserFirebaseId: String,
    navigator: DestinationsNavigator
) {
    if (storiesPerUser == null || storiesPerUser.first.isEmpty()) return
    Column(
        modifier = Modifier
            .padding(start = 12.dp)
            .fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(storiesPerUser.first.keys.toList()) { firebaseUserId ->

                val storyPoster =
                    storiesPerUser.second.find { it.firebaseUserId == firebaseUserId }

                if (storyPoster != null) {
                    StoryItem(
                        storyPoster,
                        storiesPerUser.first,
                        storiesPerUser.second,
                        loggedInUserFirebaseId,
                        navigator
                    )
                }
            }
        }
        DividerLightGrayAlpha50()
    }
}

@Composable
fun StoryLoaderItem() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            repeat(5) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(ConstantsHelper.StoryItemSize)
                            .clip(CircleShape)
                            .shimmer()
                    )
                    SpacerHeight6()
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(ConstantsHelper.StoryItemSize)
                            .shimmer()
                    )
                }
            }
        }
        DividerLightGrayAlpha50()
    }
}

@Composable
private fun StoryItem(
    storyPoster: UsersBean,
    allStories: MutableMap<String, ArrayList<StoryBean>>,
    allStoryPosters: ArrayList<UsersBean>,
    loggedInUserFirebaseId: String,
    navigator: DestinationsNavigator
) {
    val isLoggedInUser = loggedInUserFirebaseId == storyPoster.firebaseUserId
    val currentUserStories = allStories[storyPoster.firebaseUserId]
    if (currentUserStories != null) {
        Column(modifier = Modifier.clickable {
            val gsonString: String = Gson().toJson(allStories)
            navigator.navigate(
                ShowStoryScreenDestination(
                    storyPoster.firebaseUserId,
                    gsonString,
                    allStoryPosters,
                    loggedInUserFirebaseId
                )
            )
        }, horizontalAlignment = Alignment.CenterHorizontally) {
            BreakCircularBorder(
                storyPoster.profilePhoto,
                parts = currentUserStories.size,
                getColorListFromStories(currentUserStories, loggedInUserFirebaseId)
            )
            SpacerHeight6()
            Text(
                text = if (isLoggedInUser) stringResource(R.string.your_story) else storyPoster.name,
                fontSize = 12.sp
            )
        }
    }
}

private fun getColorListFromStories(
    currentUserStories: ArrayList<StoryBean>,
    loggedInUserFirebaseId: String
): List<List<Color>> {
    val colorList = mutableListOf<List<Color>>()
    val numberOfStoriesSeen = 0
//        currentUserStories.count {
////            it.seenList.map { list -> list.first }.contains(loggedInUserFirebaseId)
//        }

    repeat(numberOfStoriesSeen) {
        colorList.add(listOf(Color.Gray, Color.LightGray))
    }

    repeat(currentUserStories.size - numberOfStoriesSeen) {
        colorList.add(listOf(Color(0xFF00668B), Color(0xff0083b3)))
    }

    return colorList.toList()
}

@Composable
private fun BreakCircularBorder(
    imageUrl: String?,
    parts: Int,
    colorList: List<List<Color>>,
    gapAngle: Float = 10f,
    strokeWidth: Dp = 4.dp,
) {
    if (colorList.size != parts || parts == 0) throw IllegalArgumentException("either parts is 0 or color list size not equal to parts")

    val partAngle = if (parts > 1) (360f - parts * gapAngle) / parts else 360f
    Box(
        modifier = Modifier
            .size(ConstantsHelper.StoryItemSize)
            .clip(CircleShape)
    ) {
        repeat(parts) { index ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .drawWithContent {
                        drawCircularBorder(
                            brush = Brush.linearGradient(colorList[index]),
                            strokeWidth = strokeWidth.toPx(),
                            startAngle = index * (partAngle + gapAngle) + 90,
                            sweepAngle = partAngle
                        )
                    }
            )
        }
        AsyncImage(
            modifier = Modifier
                .padding(strokeWidth + 1.dp)
                .fillMaxSize()
                .clip(CircleShape),
            model = imageUrl,
            contentDescription = stringResource(id = R.string.profile_image),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
    }
}

private fun DrawScope.drawCircularBorder(
    brush: Brush,
    strokeWidth: Float,
    startAngle: Float,
    sweepAngle: Float
) {
    drawArc(
        brush = brush,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(width = strokeWidth)
    )
}

@Composable
private fun HandlePostDetailsWithUserDetails(
    viewModel: HomeViewModel,
    currentUsersBean: UsersBean,
    navigator: DestinationsNavigator
) {
    val postDetailsWithUserDetailsState = viewModel.postDetailsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (postDetailsWithUserDetailsState.status) {
        RequestStatusEnum.Loading -> {
            PostListLoadingSection()
            isExceptionHandled = false
        }

        RequestStatusEnum.Success -> {
            PostListUiSection(
                postWithUser = postDetailsWithUserDetailsState.data,
                currentUsersBean = currentUsersBean,
                navigator = navigator,
                viewModel = viewModel
            )
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    postDetailsWithUserDetailsState.message ?: stringResource(
                        id = R.string.some_error_occurred
                    )
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.HomeScreen.name,
                    postDetailsWithUserDetailsState.message.toString()
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
private fun PostListUiSection(
    postWithUser: Pair<List<PostBean>, List<UsersBean>>?,
    currentUsersBean: UsersBean,
    viewModel: HomeViewModel,
    navigator: DestinationsNavigator
) {
    if (postWithUser == null || postWithUser.first.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.no_posts_found))
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(postWithUser.first, key = {
                it.postFirebaseId
            }) { post ->
                val userDetails =
                    postWithUser.second.find { it.firebaseUserId == post.createdByUserFirebaseId }
                if (userDetails != null) {
                    PostListItem(
                        usersDetails = userDetails,
                        postDetails = post,
                        currentUserFirebaseId = currentUsersBean.firebaseUserId,
                        navigator = navigator,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun PostListItem(
    usersDetails: UsersBean,
    postDetails: PostBean,
    currentUserFirebaseId: String,
    viewModel: HomeViewModel,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        UserDetailsSection(
            user = usersDetails,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .clickable {
                    if (currentUserFirebaseId == usersDetails.firebaseUserId) {
                        navigator.navigate(CurrentUserProfileScreenDestination)
                    } else {
                        navigator.navigate(OtherUserProfileScreenDestination(usersDetails))
                    }
                }
        )
        if (postDetails.caption.isNotBlank()) {
            ExpandingText(
                modifier = Modifier.padding(16.dp),
                text = postDetails.caption,
                context = context,
                minimizedMaxLines = if (postDetails.postContentType == MediaTypeEnum.Text.name) 8 else ConstantsHelper.MINIMIZED_MAX_LINES
            )
        } else {
            SpacerHeight16()
        }
        if (
            postDetails.postContentType == MediaTypeEnum.Image.name
            || postDetails.postContentType == MediaTypeEnum.TextImage.name
            || postDetails.postContentType == MediaTypeEnum.Video.name
            || postDetails.postContentType == MediaTypeEnum.TextVideo.name
        ) {
            PostCaptionMediaSection(postDetails = postDetails)
        }
        PostBottomSection(postDetails, viewModel, usersDetails, currentUserFirebaseId, navigator)
        SpacerHeight16()
        DividerLightGrayAlpha40()
    }
}

@Composable
private fun PostBottomSection(
    postDetails: PostBean,
    viewModel: HomeViewModel,
    userDetails: UsersBean,
    currentUserFirebaseId: String,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    var likeCount by remember {
        mutableIntStateOf(postDetails.likedBy.size)
    }
    var isSavedByCurrentUser by remember {
        mutableStateOf(postDetails.isSavedByCurrentUser)
    }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row {
                IconButton(onClick = {
                    if (postDetails.likedBy.contains(currentUserFirebaseId)) {
                        viewModel.removeLike(postDetails.postFirebaseId, currentUserFirebaseId) {
                            postDetails.likedBy.remove(currentUserFirebaseId)
                            likeCount--
                        }
                    } else {
                        viewModel.addLike(postDetails.postFirebaseId, currentUserFirebaseId) {
                            postDetails.likedBy.add(currentUserFirebaseId)
                            likeCount++
                        }
                    }
                }) {
                    Icon(
                        painter = if (postDetails.likedBy.contains(currentUserFirebaseId)) painterResource(
                            id = R.drawable.ic_heart_filled
                        ) else painterResource(id = R.drawable.ic_heart),
                        contentDescription = stringResource(
                            id = R.string.like_post
                        ),
                        tint = if (postDetails.likedBy.contains(currentUserFirebaseId)) ColorsHelper.red() else LocalContentColor.current
                    )
                }
                IconButton(onClick = {
                    navigator.navigate(
                        PostDetailsScreenDestination(
                            postDetails,
                            userDetails,
                            currentUserFirebaseId
                        )
                    )
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_comment),
                        contentDescription = stringResource(R.string.comment_on_post)
                    )
                }
            }
            IconButton(onClick = {
                if (postDetails.isSavedByCurrentUser) {
                    viewModel.unSavePost(currentUserFirebaseId, postDetails.postFirebaseId) {
                        postDetails.isSavedByCurrentUser = false
                        isSavedByCurrentUser = false
                    }
                } else {
                    viewModel.savePost(currentUserFirebaseId, postDetails.postFirebaseId) {
                        postDetails.isSavedByCurrentUser = true
                        isSavedByCurrentUser = true
                    }
                }
            }) {
                Icon(
                    imageVector = if (isSavedByCurrentUser) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = stringResource(R.string.save_post)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (likeCount == 1) stringResource(R.string._1_like) else stringResource(
                    R.string.like_count_likes,
                    likeCount
                ),
                modifier = Modifier.padding(start = 16.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
            SpacerWidth12()
            Dot()
            SpacerWidth12()
            Text(
                text = if (postDetails.commentCount == 1L) stringResource(R.string._1_comment) else stringResource(
                    R.string.comment_count_comments,
                    postDetails.commentCount
                ),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
            SpacerWidth12()
            Dot()
            SpacerWidth12()
            Text(
                text = FunctionHelper.getTimeAgo(postDetails.createdAt, context),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun HandleLikeUnlikeState(viewModel: HomeViewModel) {
    val likeUnlikeState = viewModel.likeUnlikePostStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (likeUnlikeState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(id = R.string.please_wait))
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    likeUnlikeState.message ?: stringResource(id = R.string.some_error_occurred)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.HomeScreen.name,
                    likeUnlikeState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            // do not handle this
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
private fun HandleSaveUnSavePost(viewModel: HomeViewModel) {
    val saveUnSavePostState = viewModel.saveUnSavePostStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (saveUnSavePostState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(id = R.string.please_wait))
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    saveUnSavePostState.message ?: stringResource(id = R.string.some_error_occurred)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.HomeScreen.name,
                    saveUnSavePostState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            // do not handle this
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

