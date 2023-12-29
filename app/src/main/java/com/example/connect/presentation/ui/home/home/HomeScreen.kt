package com.example.connect.presentation.ui.home.home

import android.annotation.SuppressLint
import android.content.Intent
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.PostBean
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
import com.example.connect.presentation.ui.common.SpacerHeight12
import com.example.connect.presentation.ui.common.SpacerHeight16
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.ui.common.StoryItem
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.common.UserDetailsSectionLoading
import com.example.connect.presentation.ui.common.shimmer
import com.example.connect.presentation.ui.destinations.AddStoryScreenDestination
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.PostDetailsScreenDestination
import com.example.connect.presentation.ui.enums.PostTypeEnum
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.HomeNavGraph
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
            Surface(tonalElevation = 0.dp) {
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
            }
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            StorySection(homeSharedViewModel.usersDetails)
            SpacerHeight12()
            DividerLightGrayAlpha50()
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
        viewModel.getPostDetailsWithUserDetails(homeSharedViewModel.usersDetails.firebaseUserId)
    }
    HandleLikeUnlikeState(viewModel = viewModel)
    HandleSaveUnSavePost(viewModel)
}

@Composable
fun StorySection(usersDetails: UsersBean) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        items(12) {
            StoryItem(user = usersDetails)
        }
    }
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
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
fun PostListLoadingSection() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(4) {
            UserDetailsSectionLoading(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(14.dp)
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(24.dp)
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(14.dp)
                    .shimmer()
            )
            SpacerHeight16()
            DividerLightGrayAlpha40()
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
            items(postWithUser.first) { post ->
                val userDetails =
                    postWithUser.second.find { it.firebaseUserId == post.fireBaseUserId }
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
                minimizedMaxLines = if (postDetails.postType == PostTypeEnum.Text.name) 8 else ConstantsHelper.MINIMIZED_MAX_LINES
            )
        } else {
            SpacerHeight16()
        }
        if (
            postDetails.postType == PostTypeEnum.Image.name
            || postDetails.postType == PostTypeEnum.TextImage.name
            || postDetails.postType == PostTypeEnum.Video.name
            || postDetails.postType == PostTypeEnum.TextVideo.name
        ) {
            PostCaptionMediaSection(postDetails = postDetails)
        }
        PostBottomSection(postDetails, viewModel, currentUserFirebaseId, navigator)
        SpacerHeight16()
        DividerLightGrayAlpha40()
    }
}

@SuppressLint("OpaqueUnitKey")
@Composable
private fun PostCaptionMediaSection(postDetails: PostBean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        if (postDetails.postType == PostTypeEnum.Image.name || postDetails.postType == PostTypeEnum.TextImage.name) {
            var isImageLoadingFailed by remember {
                mutableStateOf(false)
            }
            var isPostLoading by remember {
                mutableStateOf(false)
            }
            if (!isImageLoadingFailed) {
                val modifier = Modifier.fillMaxSize()
                AsyncImage(
                    model = postDetails.mediaUrl,
                    contentDescription = postDetails.caption,
                    contentScale = ContentScale.Crop,
                    modifier = if (isPostLoading) modifier.shimmer() else modifier,
                    onError = {
                        isImageLoadingFailed = true
                        isPostLoading = false
                    },
                    onLoading = {
                        isPostLoading = true
                    },
                    onSuccess = {
                        isPostLoading = false
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ColorsHelper.lightGray()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.unable_to_load_media))
                }
            }
        } else if (postDetails.postType == PostTypeEnum.Video.name || postDetails.postType == PostTypeEnum.TextVideo.name) {
            val context = LocalContext.current
            val exoPlayer = remember {
                FunctionHelper.getExoPlayer(context, postDetails.mediaUrl)
            }
            DisposableEffect(AndroidView(factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            })) {
                onDispose {
                    exoPlayer.release()
                }
            }
        }
    }
}

@Composable
private fun PostBottomSection(
    postDetails: PostBean,
    viewModel: HomeViewModel,
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
                    navigator.navigate(PostDetailsScreenDestination(postDetails))
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
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            //do not handle this

        }

        RequestStatusEnum.None -> {
            //do not handle this

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
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            //do not handle this
        }

        RequestStatusEnum.None -> {
            //do not handle this

        }
    }
}

