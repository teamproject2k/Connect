package com.example.connect.presentation.ui.home.post_details

import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha40
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha50
import com.example.connect.presentation.ui.common.Dot
import com.example.connect.presentation.ui.common.ExpandingText
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.PostCaptionMediaSection
import com.example.connect.presentation.ui.common.SpacerHeight16
import com.example.connect.presentation.ui.common.SpacerHeight4
import com.example.connect.presentation.ui.common.SpacerHeight6
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.ui.common.SpacerWidth8
import com.example.connect.presentation.ui.common.TextBold13
import com.example.connect.presentation.ui.common.TextBold14
import com.example.connect.presentation.ui.common.TextBold18
import com.example.connect.presentation.ui.common.TransparentTextField
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.common.shimmer
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@HomeNavGraph
@Destination
@Composable
fun PostDetailsScreen(
    navigator: DestinationsNavigator,
    post: PostBean,
    posterDetails: UsersBean
) {
    val viewModel: PostDetailsViewModel = hiltViewModel()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)

    if (!viewModel.isInitialized) {
        viewModel.initialize(post)
    }

    val snackBarHostState = SnackbarHostState()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                PostDetails(
                    usersDetails = posterDetails,
                    loggedInUserFirebaseId = homeSharedViewModel.usersDetails.firebaseUserId,
                    viewModel = viewModel,
                    navigator = navigator
                )
                TextBold18(
                    text = stringResource(R.string.comments),
                    modifier = Modifier.padding(16.dp)
                )
                HandleGetAllCommentsSection(viewModel)
            }
            DividerLightGrayAlpha50()
            AddCommentSection(
                viewModel,
                homeSharedViewModel.usersDetails
            )
            HandleAddCommentSection(viewModel = viewModel)
        }
    }
    LaunchedEffect(viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
            viewModel.snackBarMessageState.value = ""
        }
    }
    LaunchedEffect(Unit) {
        viewModel.getAllCommentsWithUsers(homeSharedViewModel.usersDetails.firebaseUserId)
    }
}

@Composable
private fun PostDetails(
    usersDetails: UsersBean,
    loggedInUserFirebaseId: String,
    viewModel: PostDetailsViewModel,
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
        if (viewModel.post.caption.isNotBlank()) {
            ExpandingText(
                modifier = Modifier.padding(16.dp),
                text = viewModel.post.caption,
                context = context,
                minimizedMaxLines = if (viewModel.post.postType == MediaTypeEnum.Text.name) 8 else ConstantsHelper.MINIMIZED_MAX_LINES
            )
        } else {
            SpacerHeight16()
        }
        if (
            viewModel.post.postType == MediaTypeEnum.Image.name
            || viewModel.post.postType == MediaTypeEnum.TextImage.name
            || viewModel.post.postType == MediaTypeEnum.Video.name
            || viewModel.post.postType == MediaTypeEnum.TextVideo.name
        ) {
            PostCaptionMediaSection(postDetails = viewModel.post)
        }
        PostBottomSection(viewModel, loggedInUserFirebaseId)
        SpacerHeight16()
        DividerLightGrayAlpha40()
    }
}

@Composable
private fun PostBottomSection(
    viewModel: PostDetailsViewModel,
    currentUserFirebaseId: String,
) {
    val context = LocalContext.current
    var likeCount by remember {
        mutableIntStateOf(viewModel.post.likedBy.size)
    }
    var isSavedByCurrentUser by remember {
        mutableStateOf(viewModel.post.isSavedByCurrentUser)
    }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = {
                if (viewModel.post.likedBy.contains(currentUserFirebaseId)) {
                    viewModel.removeLike(currentUserFirebaseId) {
                        viewModel.post.likedBy.remove(currentUserFirebaseId)
                        likeCount--
                    }
                } else {
                    viewModel.addLike(currentUserFirebaseId) {
                        viewModel.post.likedBy.add(currentUserFirebaseId)
                        likeCount++
                    }
                }
            }) {
                Icon(
                    painter = if (viewModel.post.likedBy.contains(currentUserFirebaseId)) painterResource(
                        id = R.drawable.ic_heart_filled
                    ) else painterResource(id = R.drawable.ic_heart),
                    contentDescription = stringResource(
                        id = R.string.like_post
                    ),
                    tint = if (viewModel.post.likedBy.contains(currentUserFirebaseId)) ColorsHelper.red() else LocalContentColor.current
                )
            }
            IconButton(onClick = {
                if (viewModel.post.isSavedByCurrentUser) {
                    viewModel.unSavePost(currentUserFirebaseId) {
                        viewModel.post.isSavedByCurrentUser = false
                        isSavedByCurrentUser = false
                    }
                } else {
                    viewModel.savePost(currentUserFirebaseId) {
                        viewModel.post.isSavedByCurrentUser = true
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
                text = if (viewModel.post.commentCount == 1L) stringResource(R.string._1_comment) else stringResource(
                    R.string.comment_count_comments,
                    viewModel.post.commentCount
                ),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
            SpacerWidth12()
            Dot()
            SpacerWidth12()
            Text(
                text = FunctionHelper.getTimeAgo(viewModel.post.createdAt, context),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun HandleGetAllCommentsSection(viewModel: PostDetailsViewModel) {
    val getAllCommentsState = viewModel.getAllCommentsStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (getAllCommentsState.status) {
        RequestStatusEnum.Loading -> {
            CommentUiLoading()
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    getAllCommentsState.message ?: stringResource(id = R.string.some_error_occurred)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            CommentUi(
                getAllCommentsState.data?.second,
                viewModel
            )
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
fun CommentUi(
    usersBeans: List<UsersBean>?,
    viewModel: PostDetailsViewModel
) {
    if (usersBeans.isNullOrEmpty() || viewModel.commentsStateMap.isEmpty()) {
        Column {
            TextBold14(
                text = stringResource(R.string.no_comments_found),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                alignment = TextAlign.Center
            )
        }
        return
    }
    Column {
        val parentList =
            viewModel.commentsStateMap.keys.filter { it.repliedOnCommentId == null }
        parentList.forEach { parent ->
            AddComment(usersBeans, parent, viewModel, true)
            viewModel.commentsStateMap[parent]?.forEach { child ->
                AddComment(usersBeans = usersBeans, comment = child, viewModel = viewModel, false)
            }
        }
    }
}

@Composable
fun AddComment(
    usersBeans: List<UsersBean>,
    comment: CommentBean,
    viewModel: PostDetailsViewModel,
    isParent: Boolean
) {
    val commentPoster =
        usersBeans.find { user -> user.firebaseUserId == comment.commentedBy }
    if (commentPoster != null) {
        CommentItem(
            comment = comment,
            commentPoster = commentPoster,
            viewModel = viewModel,
            isParent = isParent
        )
    }
}

@Composable
fun CommentUiLoading() {
    repeat(4) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .shimmer()
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(13.dp)
                            .fillMaxWidth()
                            .shimmer()
                            .weight(1f)
                    )
                    SpacerWidth8()
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(40.dp)
                            .shimmer()
                    )
                }
                SpacerHeight4()
                Box(
                    modifier = Modifier
                        .height(13.dp)
                        .fillMaxWidth()
                        .shimmer()
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentBean,
    commentPoster: UsersBean,
    viewModel: PostDetailsViewModel,
    isParent: Boolean
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.padding(horizontal = if (isParent) 16.dp else 40.dp, vertical = 8.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, ColorsHelper.black(), CircleShape),
            model = commentPoster.profilePhoto,
            contentDescription = commentPoster.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextBold13(text = commentPoster.connectUserId)
                SpacerWidth8()
                Text(
                    text = FunctionHelper.getTimeAgo(comment.createdAt, context, true),
                    color = ColorsHelper.gray(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            SpacerHeight4()
            Text(
                buildAnnotatedString {
                    if (comment.postId != comment.repliedOnCommentId) {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = ColorsHelper.gray()
                            )
                        ) {
                            append("${commentPoster.connectUserId}  ")
                        }
                    }
                    append(comment.comment)
                },
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
            SpacerHeight6()
            Text(
                modifier = Modifier.clickable {
                    viewModel.repliedCommentPosterConnectId.value = commentPoster.connectUserId
                    viewModel.commentedOn.value = comment
                },
                text = stringResource(R.string.reply),
                fontSize = 12.sp,
                color = ColorsHelper.gray(),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AddCommentSection(
    viewModel: PostDetailsViewModel,
    loggedInUser: UsersBean
) {
    val context = LocalContext.current
    val isReply = viewModel.commentedOn.value != null
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(1.dp, ColorsHelper.gray(), CircleShape),
            model = loggedInUser.profilePhoto,
            contentDescription = loggedInUser.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
        if (isReply) {
            SpacerWidth8()
            Text(
                text = context.getString(
                    R.string.tag_poster,
                    viewModel.repliedCommentPosterConnectId.value
                ),
                fontSize = 12.sp,
                color = ColorsHelper.black(),
                fontWeight = FontWeight.Medium
            )
        }
        TransparentTextField(
            modifier = Modifier.weight(1f),
            value = viewModel.commentText.value,
            singleLine = true,
            maxLines = 1,
            onValueChange = { text -> viewModel.commentText.value = text },
            textStyle = TextStyle(fontSize = 14.sp),
            placeholder = {
                Text(
                    text = stringResource(R.string.add_a_comment),
                    color = ColorsHelper.gray(),
                    fontSize = 13.sp
                )
            })
        if (isReply) {
            IconButton(onClick = { viewModel.commentedOn.value = null }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove_tag),
                    tint = ColorsHelper.gray()
                )
            }
        }
        if (viewModel.commentText.value.isNotBlank() && !viewModel.isSendingComment.value) {
            IconButton(onClick = {
                viewModel.addComment(loggedInUser.firebaseUserId)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = stringResource(R.string.post_comment)
                )
            }
        } else if (viewModel.isSendingComment.value) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp)
            )
        }
    }
}

@Composable
fun HandleAddCommentSection(viewModel: PostDetailsViewModel) {
    val addCommentState = viewModel.addCommentStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (addCommentState.status) {
        RequestStatusEnum.Loading -> {
            viewModel.isSendingComment.value = true
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.isSendingComment.value = false
                viewModel.snackBarMessageState.value =
                    addCommentState.message ?: stringResource(id = R.string.some_error_occurred)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            viewModel.isSendingComment.value = false
            val comment = addCommentState.data
            if (comment != null) {
                val parent =
                    viewModel.commentsStateMap.keys.find { it.commentFirebaseId == comment.repliedOnCommentId }
                if (parent != null) {
                    viewModel.commentsStateMap.getOrPut(parent) { arrayListOf() }.add(0, comment)
                }
            }
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}