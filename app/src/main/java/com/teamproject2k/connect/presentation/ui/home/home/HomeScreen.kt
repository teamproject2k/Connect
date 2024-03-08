package com.teamproject2k.connect.presentation.ui.home.home

import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.models.StoriesWithUserBean
import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.ui.chat.base_screen.ChatActivity
import com.teamproject2k.connect.presentation.ui.common.AppTopAppBar
import com.teamproject2k.connect.presentation.ui.common.ColorsHelper
import com.teamproject2k.connect.presentation.ui.common.DividerLightGrayAlpha40
import com.teamproject2k.connect.presentation.ui.common.DividerLightGrayAlpha50
import com.teamproject2k.connect.presentation.ui.common.Dot
import com.teamproject2k.connect.presentation.ui.common.ExpandingText
import com.teamproject2k.connect.presentation.ui.common.LoaderDialog
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.common.PostMediaSection
import com.teamproject2k.connect.presentation.ui.common.PostListLoadingSection
import com.teamproject2k.connect.presentation.ui.common.SpacerHeight16
import com.teamproject2k.connect.presentation.ui.common.SpacerHeight6
import com.teamproject2k.connect.presentation.ui.common.SpacerWidth12
import com.teamproject2k.connect.presentation.ui.common.UserDetailsSection
import com.teamproject2k.connect.presentation.ui.common.shimmer
import com.teamproject2k.connect.presentation.ui.destinations.AddStoryScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.LikedByScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.PostDetailsScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.ShowStoryScreenDestination
import com.teamproject2k.connect.presentation.ui.enums.MediaTypeEnum
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
import com.teamproject2k.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.teamproject2k.connect.presentation.ui.pull_refresh.PullRefreshIndicator
import com.teamproject2k.connect.presentation.ui.pull_refresh.pullRefresh
import com.teamproject2k.connect.presentation.ui.pull_refresh.rememberPullRefreshState
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.teamproject2k.connect.presentation.utils.HomeNavGraph

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val viewModel: HomeViewModel = hiltViewModel()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(activity)
    val snackBarHostState = remember { SnackbarHostState() }
    var refreshing by rememberSaveable { mutableStateOf(false) }

    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            refreshing = true
            viewModel.getPostDetailsWithUserDetails(
                homeSharedViewModel.usersDetails.firebaseUserId,
                homeSharedViewModel.usersDetails.blockedUsersList,
                true
            )
            viewModel.getStoryDetailsWithUserDetails(
                homeSharedViewModel.usersDetails.firebaseUserId,
                true
            )
            if (!context.isNetworkAvailable()) {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.viewing_in_offline_mode)
            }
            refreshing = false
        })

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
            PullRefreshIndicator(
                refreshing = refreshing,
                refreshState = pullRefreshState
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
        viewModel.getPostDetailsWithUserDetails(
            homeSharedViewModel.usersDetails.firebaseUserId,
            homeSharedViewModel.usersDetails.blockedUsersList,
            false
        )
        viewModel.getStoryDetailsWithUserDetails(
            homeSharedViewModel.usersDetails.firebaseUserId,
            false
        )
        if (!context.isNetworkAvailable()) {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.viewing_in_offline_mode)
        }
    }
    HandleLikeUnlikePostState(viewModel = viewModel)
    HandleSaveUnSavePost(viewModel)
}

@Composable
private fun HandleStoryDetailsWithUserDetails(
    viewModel: HomeViewModel,
    currentUserBean: UserBean,
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
                allStoriesWithUserBeanList = storyDetailsWithUserDetailsState.data,
                loggedInUserFirebaseId = currentUserBean.firebaseUserId,
                navigator = navigator
            )
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    storyDetailsWithUserDetailsState.message ?: stringResource(
                        id = R.string.something_went_wrong
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
    allStoriesWithUserBeanList: ArrayList<StoriesWithUserBean>?,
    loggedInUserFirebaseId: String,
    navigator: DestinationsNavigator
) {
    if (allStoriesWithUserBeanList.isNullOrEmpty()) return
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
            itemsIndexed(allStoriesWithUserBeanList) { index, _ ->
                StoryItem(
                    index,
                    allStoriesWithUserBeanList,
                    loggedInUserFirebaseId,
                    navigator
                )
            }
        }
        DividerLightGrayAlpha50()
    }
}

@Composable
private fun StoryLoaderItem() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
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
    currentStoryIndex: Int,
    allStoriesList: ArrayList<StoriesWithUserBean>,
    loggedInUserFirebaseId: String,
    navigator: DestinationsNavigator
) {
    val storiesWithUser = allStoriesList[currentStoryIndex]
    val isLoggedInUser = loggedInUserFirebaseId == storiesWithUser.userBean.firebaseUserId
    Column(modifier = Modifier.clickable {
        navigator.navigate(
            ShowStoryScreenDestination(
                allStoriesList,
                currentStoryIndex,
                loggedInUserFirebaseId
            )
        )
    }, horizontalAlignment = Alignment.CenterHorizontally) {
        BreakCircularBorder(
            storiesWithUser.userBean.profilePhoto,
            parts = storiesWithUser.storiesList.size,
            getColorListFromStories(storiesWithUser.storiesList, loggedInUserFirebaseId)
        )
        SpacerHeight6()
        Text(
            text = if (isLoggedInUser) stringResource(R.string.your_story) else storiesWithUser.userBean.name,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun BreakCircularBorder(
    imageUrl: String?,
    parts: Int,
    colorList: List<List<Color>>,
    gapAngle: Float = 10f,
    strokeWidth: Dp = 4.dp
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

@Composable
private fun HandlePostDetailsWithUserDetails(
    viewModel: HomeViewModel,
    currentUserBean: UserBean,
    navigator: DestinationsNavigator
) {
    val postDetailsWithUserDetailsState = viewModel.postDetailsStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (postDetailsWithUserDetailsState.status) {
        RequestStatusEnum.Loading -> {
            PostListLoadingSection()
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.postListWithUsersState.clear()
                viewModel.postListWithUsersState.addAll(
                    postDetailsWithUserDetailsState.data ?: emptyList()
                )
                isResponseHandled = true
            }
            PostListUiSection(
                loggedInUserBean = currentUserBean,
                navigator = navigator,
                viewModel = viewModel
            )
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (postDetailsWithUserDetailsState.message == FirebaseErrorCodes.UNKNOWN_ERROR) {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.something_went_wrong)
                } else {
                    viewModel.snackBarMessageState.value =
                        postDetailsWithUserDetailsState.message ?: stringResource(
                            id = R.string.something_went_wrong
                        )
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.HomeScreen.name,
                    postDetailsWithUserDetailsState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
private fun PostListUiSection(
    loggedInUserBean: UserBean,
    viewModel: HomeViewModel,
    navigator: DestinationsNavigator
) {
    if (viewModel.postListWithUsersState.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.no_posts_found))
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(viewModel.postListWithUsersState) { postWithUser ->
                PostListItem(
                    usersDetails = postWithUser.userDetail,
                    postDetails = postWithUser.postDetail,
                    loggedInUserBean = loggedInUserBean,
                    navigator = navigator,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun PostListItem(
    usersDetails: UserBean,
    postDetails: PostBean,
    loggedInUserBean: UserBean,
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
                    if (loggedInUserBean.firebaseUserId == usersDetails.firebaseUserId) {
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
            PostMediaSection(postDetails = postDetails)
        }
        PostBottomSection(postDetails, viewModel, usersDetails, loggedInUserBean, navigator)
        SpacerHeight16()
        DividerLightGrayAlpha40()
    }
}

@Composable
private fun PostBottomSection(
    postDetails: PostBean,
    viewModel: HomeViewModel,
    userDetails: UserBean,
    loggedInUserBean: UserBean,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    var likeCount by remember {
        mutableIntStateOf(postDetails.likedBy.size)
    }
    var isSavedByCurrentUser by remember {
        mutableStateOf(loggedInUserBean.savedPosts.contains(postDetails.postFirebaseId))
    }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row {
                IconButton(onClick = {
                    if (context.isNetworkAvailable()) {
                        if (postDetails.likedBy.contains(loggedInUserBean.firebaseUserId)) {
                            viewModel.removeLikeForPost(
                                postDetails,
                                loggedInUserBean.firebaseUserId
                            ) {
                                likeCount--
                            }
                        } else {
                            viewModel.addLikeOnPost(postDetails, loggedInUserBean.firebaseUserId) {
                                likeCount++
                            }
                        }
                    } else {
                        viewModel.snackBarMessageState.value =
                            context.getString(R.string.no_internet_connection)
                        FunctionHelper.vibrateDevice(context)
                    }
                }) {
                    Icon(
                        painter = if (postDetails.likedBy.contains(loggedInUserBean.firebaseUserId)) painterResource(
                            id = R.drawable.ic_heart_filled
                        ) else painterResource(id = R.drawable.ic_heart),
                        contentDescription = stringResource(
                            id = R.string.like_post
                        ),
                        tint = if (postDetails.likedBy.contains(loggedInUserBean.firebaseUserId)) ColorsHelper.red() else LocalContentColor.current
                    )
                }
                IconButton(onClick = {
                    navigator.navigate(
                        PostDetailsScreenDestination(
                            postDetails,
                            userDetails
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
                if (context.isNetworkAvailable()) {
                    if (loggedInUserBean.savedPosts.contains(postDetails.postFirebaseId)) {
                        viewModel.unSavePost(loggedInUserBean, postDetails.postFirebaseId) {
                            isSavedByCurrentUser = false
                        }
                    } else {
                        viewModel.savePost(loggedInUserBean, postDetails.postFirebaseId) {
                            isSavedByCurrentUser = true
                        }
                    }
                } else {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.no_internet_connection)
                    FunctionHelper.vibrateDevice(context)
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
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable {
                        navigator.navigate(LikedByScreenDestination(postDetails.likedBy))
                    },
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
private fun HandleLikeUnlikePostState(viewModel: HomeViewModel) {
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
                if (likeUnlikeState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                    val postFirebaseId = likeUnlikeState.data
                    viewModel.postListWithUsersState.removeIf { it.postDetail.postFirebaseId == postFirebaseId }
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.post_not_found)
                } else {
                    viewModel.snackBarMessageState.value =
                        likeUnlikeState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
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
                if (saveUnSavePostState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                    val postFirebaseId = saveUnSavePostState.data
                    viewModel.postListWithUsersState.removeIf { it.postDetail.postFirebaseId == postFirebaseId }
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.post_not_found)
                } else {
                    viewModel.snackBarMessageState.value =
                        saveUnSavePostState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
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

/**
 * Generates a list of color combinations based on the user's interaction with stories.
 * This function creates color combinations for stories seen and unseen by the logged-in user.
 * @param currentUserStories The list of stories belonging to the current user.
 * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
 * @return A list of color combinations, where each combination represents a story's visual state.
 */
private fun getColorListFromStories(
    currentUserStories: ArrayList<StoryBean>,
    loggedInUserFirebaseId: String
): List<List<Color>> {
    val colorList = mutableListOf<List<Color>>()
    val numberOfStoriesSeen =
        currentUserStories.count {
            it.seenBy.map { list -> list.seenUserId }.contains(loggedInUserFirebaseId)
        }

    repeat(numberOfStoriesSeen) {
        colorList.add(listOf(Color.Gray, Color.LightGray))
    }

    repeat(currentUserStories.size - numberOfStoriesSeen) {
        colorList.add(listOf(Color(0xFF00668B), Color(0xff0083b3)))
    }

    return colorList.toList()
}

/**
 * Draws a circular border with the specified brush, stroke width, start angle, and sweep angle.
 * @param brush The brush used to fill the circular border.
 * @param strokeWidth The width of the stroke for the circular border.
 * @param startAngle The starting angle (in degrees) of the circular border.
 * @param sweepAngle The sweep angle (in degrees) of the circular border.
 */
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
