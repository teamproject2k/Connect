package com.teamproject2k.connect.presentation.ui.home.saved_posts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.ui.common.AppTopAppBar
import com.teamproject2k.connect.presentation.ui.common.ColorsHelper
import com.teamproject2k.connect.presentation.ui.common.DividerLightGrayAlpha40
import com.teamproject2k.connect.presentation.ui.common.Dot
import com.teamproject2k.connect.presentation.ui.common.ExpandingText
import com.teamproject2k.connect.presentation.ui.common.LoaderDialog
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.common.PostCaptionMediaSection
import com.teamproject2k.connect.presentation.ui.common.PostListLoadingSection
import com.teamproject2k.connect.presentation.ui.common.SpacerHeight16
import com.teamproject2k.connect.presentation.ui.common.SpacerWidth12
import com.teamproject2k.connect.presentation.ui.common.UserDetailsSection
import com.teamproject2k.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.LikedByScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.PostDetailsScreenDestination
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun SavedPostsScreen(navigator: DestinationsNavigator) {
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val viewModel: SavedPostsViewModel = hiltViewModel()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var refreshing by rememberSaveable { mutableStateOf(false) }

    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            refreshing = true
            if (context.isNetworkAvailable()) {
                viewModel.getSavedPosts(
                    homeSharedViewModel.usersDetails.firebaseUserId,
                    homeSharedViewModel.usersDetails.blockedUsersList,
                    homeSharedViewModel.usersDetails.savedPosts,
                )
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.no_internet_connection)
                FunctionHelper.vibrateDevice(context)
            }
            refreshing = false
        })
    Scaffold(topBar = {
        AppTopAppBar(
            title = stringResource(R.string.saved_posts),
            showNavigationIcon = true,
            onNavigationIconClick = { navigator.popBackStack() })
    }, snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            HandleGetSavedPostsState(
                viewModel,
                navigator,
                homeSharedViewModel.usersDetails
            )
            PullRefreshIndicator(
                refreshing = refreshing,
                refreshState = pullRefreshState
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
    LaunchedEffect(Unit) {
        if (context.isNetworkAvailable()) {
            viewModel.getSavedPosts(
                homeSharedViewModel.usersDetails.firebaseUserId,
                homeSharedViewModel.usersDetails.blockedUsersList,
                homeSharedViewModel.usersDetails.savedPosts,
            )
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
    }
    HandleLikeUnlikeState(viewModel)
    HandleSaveUnSavePost(viewModel)
}


@Composable
fun HandleGetSavedPostsState(
    viewModel: SavedPostsViewModel,
    navigator: DestinationsNavigator,
    loggedInUsersBean: UsersBean
) {
    val savedPostsState = viewModel.getSavedPostsWithUsersStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }

    when (savedPostsState.status) {
        RequestStatusEnum.Loading -> {
            PostListLoadingSection()
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (savedPostsState.message == FirebaseErrorCodes.UNKNOWN_ERROR) {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.something_went_wrong)
                } else {
                    viewModel.snackBarMessageState.value =
                        savedPostsState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.SavedPostsScreen.name,
                    savedPostsState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.postListWithUserDetailsListState.clear()
                viewModel.postListWithUserDetailsListState.addAll(
                    savedPostsState.data ?: emptyList()
                )
                isResponseHandled = true
            }
            DisplaySavedPostsList(
                navigator,
                loggedInUsersBean,
                viewModel
            )
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
fun DisplaySavedPostsList(
    navigator: DestinationsNavigator,
    loggedInUsersBean: UsersBean,
    viewModel: SavedPostsViewModel
) {
    if (viewModel.postListWithUserDetailsListState.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(R.string.no_posts_found))
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(viewModel.postListWithUserDetailsListState) { postWithUser ->
                PostListItem(
                    usersDetails = postWithUser.userDetail,
                    postDetails = postWithUser.postDetail,
                    loggedInUsersBean = loggedInUsersBean,
                    navigator = navigator,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun PostListItem(
    usersDetails: UsersBean,
    postDetails: PostBean,
    loggedInUsersBean: UsersBean,
    viewModel: SavedPostsViewModel,
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
                    if (loggedInUsersBean.firebaseUserId == usersDetails.firebaseUserId) {
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
        PostBottomSection(postDetails, viewModel, usersDetails, loggedInUsersBean, navigator)
        SpacerHeight16()
        DividerLightGrayAlpha40()
    }
}

@Composable
private fun PostBottomSection(
    postDetails: PostBean,
    viewModel: SavedPostsViewModel,
    userDetails: UsersBean,
    loggedInUsersBean: UsersBean,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    var likeCount by remember {
        mutableIntStateOf(postDetails.likedBy.size)
    }
    var isSavedByCurrentUser by remember {
        mutableStateOf(loggedInUsersBean.savedPosts.contains(postDetails.postFirebaseId))
    }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row {
                IconButton(onClick = {
                    if (context.isNetworkAvailable()) {
                        if (postDetails.likedBy.contains(loggedInUsersBean.firebaseUserId)) {
                            viewModel.removeLikeForPost(
                                postDetails,
                                loggedInUsersBean.firebaseUserId
                            ) {
                                likeCount--
                            }
                        } else {
                            viewModel.addLikeOnPost(
                                postDetails,
                                loggedInUsersBean.firebaseUserId
                            ) {
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
                        painter = if (postDetails.likedBy.contains(loggedInUsersBean.firebaseUserId)) painterResource(
                            id = R.drawable.ic_heart_filled
                        ) else painterResource(id = R.drawable.ic_heart),
                        contentDescription = stringResource(
                            id = R.string.like_post
                        ),
                        tint = if (postDetails.likedBy.contains(loggedInUsersBean.firebaseUserId)) ColorsHelper.red() else LocalContentColor.current
                    )
                }
                IconButton(onClick = {
                    navigator.navigate(
                        PostDetailsScreenDestination(
                            postDetails,
                            userDetails,
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
                    if (loggedInUsersBean.savedPosts.contains(postDetails.postFirebaseId)) {
                        viewModel.unSavePost(loggedInUsersBean, postDetails.postFirebaseId) {
                            isSavedByCurrentUser = false
                        }
                    } else {
                        viewModel.savePost(loggedInUsersBean, postDetails.postFirebaseId) {
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
                    contentDescription = stringResource(R.string.comment_on_post)
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
private fun HandleLikeUnlikeState(viewModel: SavedPostsViewModel) {
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
                    viewModel.postListWithUserDetailsListState.removeIf { it.postDetail.postFirebaseId == likeUnlikeState.data }
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.post_not_found)
                } else {
                    viewModel.snackBarMessageState.value =
                        likeUnlikeState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
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
private fun HandleSaveUnSavePost(viewModel: SavedPostsViewModel) {
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
                    viewModel.postListWithUserDetailsListState.removeIf { it.postDetail.postFirebaseId == saveUnSavePostState.data }
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.post_not_found)
                } else {
                    viewModel.snackBarMessageState.value =
                        saveUnSavePostState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
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
