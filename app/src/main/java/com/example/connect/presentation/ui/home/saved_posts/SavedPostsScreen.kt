package com.example.connect.presentation.ui.home.saved_posts

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
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
import com.example.connect.R
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha40
import com.example.connect.presentation.ui.common.Dot
import com.example.connect.presentation.ui.common.ExpandingText
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.PostCaptionMediaSection
import com.example.connect.presentation.ui.common.PostListLoadingSection
import com.example.connect.presentation.ui.common.SpacerHeight16
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.PostDetailsScreenDestination
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator
import com.example.connect.presentation.ui.pull_refresh.pullRefresh
import com.example.connect.presentation.ui.pull_refresh.rememberPullRefreshState
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun SavedPostsScreen(navigator: DestinationsNavigator) {
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val viewModel: SavedPostsViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()

    var refreshing by rememberSaveable { mutableStateOf(false) }

    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            refreshing = true
            viewModel.getSavedPosts(
                homeSharedViewModel.usersDetails.firebaseUserId,
                homeSharedViewModel.usersDetails.savedPosts
            )
            refreshing = false
        })
    Scaffold(topBar = {
        AppTopAppBar(title = stringResource(R.string.saved_posts))
    }) {
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
                homeSharedViewModel.usersDetails.firebaseUserId
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
    if (!viewModel.isSavedListFetched) {
        viewModel.getSavedPosts(
            homeSharedViewModel.usersDetails.firebaseUserId,
            homeSharedViewModel.usersDetails.savedPosts
        )
        viewModel.isSavedListFetched = true
    }
    HandleLikeUnlikeState(viewModel)
    HandleSaveUnSavePost(viewModel)
}

@Composable
fun HandleGetSavedPostsState(
    viewModel: SavedPostsViewModel,
    navigator: DestinationsNavigator,
    loggedInUserFirebaseId: String
) {
    val savedPostsState = viewModel.getSavedPostsWithUsersStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (savedPostsState.status) {
        RequestStatusEnum.Loading -> {
            PostListLoadingSection()
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    savedPostsState.message
                        ?: stringResource(id = R.string.some_error_occurred)
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            DisplaySavedPostsList(
                navigator,
                savedPostsState.data,
                loggedInUserFirebaseId,
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
    postWithUsers: Pair<ArrayList<PostBean>, ArrayList<UsersBean>>?,
    loggedInUserFirebaseId: String,
    viewModel: SavedPostsViewModel
) {
    if (postWithUsers == null || postWithUsers.first.isEmpty() || postWithUsers.second.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.no_posts_found))
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(postWithUsers.first.toList(), key = {
                it.id
            }) { post ->
                val userDetails =
                    postWithUsers.second.find { it.firebaseUserId == post.fireBaseUserId }
                if (userDetails != null) {
                    PostListItem(
                        usersDetails = userDetails,
                        postDetails = post,
                        loggedInUserFirebaseId = loggedInUserFirebaseId,
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
    loggedInUserFirebaseId: String,
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
                    if (loggedInUserFirebaseId == usersDetails.firebaseUserId) {
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
                minimizedMaxLines = if (postDetails.postType == MediaTypeEnum.Text.name) 8 else ConstantsHelper.MINIMIZED_MAX_LINES
            )
        } else {
            SpacerHeight16()
        }
        if (
            postDetails.postType == MediaTypeEnum.Image.name
            || postDetails.postType == MediaTypeEnum.TextImage.name
            || postDetails.postType == MediaTypeEnum.Video.name
            || postDetails.postType == MediaTypeEnum.TextVideo.name
        ) {
            PostCaptionMediaSection(postDetails = postDetails)
        }
        PostBottomSection(postDetails, viewModel, usersDetails, loggedInUserFirebaseId, navigator)
        SpacerHeight16()
        DividerLightGrayAlpha40()
    }
}

@Composable
private fun PostBottomSection(
    postDetails: PostBean,
    viewModel: SavedPostsViewModel,
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
                        viewModel.removeLike(postDetails.id, currentUserFirebaseId) {
                            postDetails.likedBy.remove(currentUserFirebaseId)
                            likeCount--
                        }
                    } else {
                        viewModel.addLike(postDetails.id, currentUserFirebaseId) {
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
                    viewModel.unSavePost(currentUserFirebaseId, postDetails.id) {
                        postDetails.isSavedByCurrentUser = false
                        isSavedByCurrentUser = false
                    }
                } else {
                    viewModel.savePost(currentUserFirebaseId, postDetails.id) {
                        postDetails.isSavedByCurrentUser = true
                        isSavedByCurrentUser = true
                    }
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
                viewModel.snackBarMessageState.value =
                    likeUnlikeState.message ?: stringResource(id = R.string.some_error_occurred)
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
                viewModel.snackBarMessageState.value =
                    saveUnSavePostState.message ?: stringResource(id = R.string.some_error_occurred)
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
