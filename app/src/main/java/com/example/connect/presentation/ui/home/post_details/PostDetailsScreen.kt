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
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha40
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha50
import com.example.connect.presentation.ui.common.Dot
import com.example.connect.presentation.ui.common.ExpandingText
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.PostCaptionMediaSection
import com.example.connect.presentation.ui.common.SpacerHeight12
import com.example.connect.presentation.ui.common.SpacerHeight16
import com.example.connect.presentation.ui.common.SpacerHeight4
import com.example.connect.presentation.ui.common.SpacerHeight8
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.ui.common.SpacerWidth16
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
import com.example.connect.presentation.ui.enums.ScreenNameEnum
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
    posterDetails: UsersBean,
    loggedInUserFirebaseId: String
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
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
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
                HandleGetAllCommentsSection(viewModel, loggedInUserFirebaseId, navigator)
            }
            DividerLightGrayAlpha50()
            AddCommentSection(
                viewModel,
                homeSharedViewModel.usersDetails
            )
            HandleAddCommentSection(viewModel = viewModel)
            HandleDeleteCommentSection(viewModel = viewModel)
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
fun HandleGetAllCommentsSection(
    viewModel: PostDetailsViewModel,
    loggedInUserFirebaseId: String,
    navigator: DestinationsNavigator
) {
    val getAllCommentsState = viewModel.getAllCommentsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (getAllCommentsState.status) {
        RequestStatusEnum.Loading -> {
            CommentUiLoading()
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    getAllCommentsState.message ?: stringResource(id = R.string.some_error_occurred)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.PostDetailsScreen.name,
                    getAllCommentsState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            CommentUi(
                getAllCommentsState.data?.second,
                viewModel,
                loggedInUserFirebaseId, navigator
            )
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
fun CommentUi(
    userList: List<UsersBean>?,
    viewModel: PostDetailsViewModel,
    loggedInUserFirebaseId: String,
    navigator: DestinationsNavigator
) {
    if (userList.isNullOrEmpty() || viewModel.commentsMapState.isEmpty()) {
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
            viewModel.commentsMapState.keys.sortedByDescending { it.createdAt }

        parentList.forEach { parent ->
            val childCommentList = viewModel.commentsMapState[parent]
            if (childCommentList != null) {
                ParentCommentItem(
                    viewModel,
                    parent,
                    childCommentList,
                    userList,
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    navigator = navigator
                )
            }
        }
    }
}

@Composable
fun ParentCommentItem(
    viewModel: PostDetailsViewModel,
    parentComment: CommentBean,
    childCommentList: List<CommentBean>,
    userList: List<UsersBean>,
    loggedInUserFirebaseId: String,
    navigator: DestinationsNavigator,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        val parentCommentPoster =
            userList.find { user -> user.firebaseUserId == parentComment.commentedBy }
        if (parentCommentPoster != null) {
            CommentItem(
                comment = parentComment,
                commentPoster = parentCommentPoster,
                viewModel = viewModel,
                loggedInUserFirebaseId = loggedInUserFirebaseId,
                navigator = navigator
            ) {
                if (!parentComment.whetherDeleted) {
                    val deleteCount = childCommentList.count { !it.whetherDeleted } + 1
                    viewModel.deleteComment(parentComment, deleteCount)

                }
            }
        }
        Column(modifier = Modifier.padding(start = 32.dp)) {
            childCommentList.forEach { comment ->
                val childCommentPoster =
                    userList.find { user -> user.firebaseUserId == comment.commentedBy }
                if (childCommentPoster != null) {
                    CommentItem(
                        comment = comment,
                        commentPoster = childCommentPoster,
                        viewModel = viewModel,
                        loggedInUserFirebaseId = loggedInUserFirebaseId,
                        navigator = navigator
                    ) {
                        if (!comment.whetherDeleted) {
                            viewModel.deleteComment(comment, 1)
                        }
                    }
                }
            }
        }
        if (childCommentList.isNotEmpty()) {
            SpacerHeight8()
        }
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
    navigator: DestinationsNavigator,
    loggedInUserFirebaseId: String,
    onDeleteCommentClicked: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
            .clickable {
                if (commentPoster.firebaseUserId == loggedInUserFirebaseId) {
                    navigator.navigate(CurrentUserProfileScreenDestination)
                } else {
                    navigator.navigate(OtherUserProfileScreenDestination(commentPoster))
                }
            },
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
                .weight(1f)
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
                    append(comment.commentMessage)
                },
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
            SpacerHeight12()
            Row {
                Text(
                    modifier = Modifier.clickable {
                        viewModel.repliedCommentPosterConnectIdState.value =
                            commentPoster.connectUserId
                        viewModel.commentedOnState.value = comment
                    },
                    text = stringResource(R.string.reply),
                    fontSize = 12.sp,
                    color = ColorsHelper.gray(),
                    fontWeight = FontWeight.Medium
                )
                SpacerWidth16()
                if (viewModel.post.fireBaseUserId == loggedInUserFirebaseId || comment.commentedBy == loggedInUserFirebaseId) {
                    Text(
                        modifier = Modifier.clickable {
                            onDeleteCommentClicked()
                        },
                        text = stringResource(R.string.delete),
                        fontSize = 12.sp,
                        color = ColorsHelper.gray(),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Box(modifier = Modifier.padding(top = 12.dp)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(12.dp),
                    strokeWidth = 1.5.dp
                )
            } else {
                IconButton(onClick = {
                    isLoading = true
                    if (comment.likedBy.contains(loggedInUserFirebaseId)) {
                        viewModel.removeLikeForComment(
                            comment,
                            loggedInUserFirebaseId,
                            onSuccess = {
                                isLoading = false
                            }) { errorMessage ->
                            viewModel.snackBarMessageState.value =
                                if (errorMessage.isNullOrBlank()) context.getString(
                                    R.string.some_error_occurred
                                ) else errorMessage
                            isLoading = false
                        }
                    } else {
                        viewModel.addLikeForComment(
                            comment,
                            loggedInUserFirebaseId,
                            onSuccess = {
                                isLoading = false
                            }) { errorMessage ->
                            viewModel.snackBarMessageState.value =
                                if (errorMessage.isNullOrBlank()) context.getString(
                                    R.string.some_error_occurred
                                ) else errorMessage
                            isLoading = false
                        }
                    }
                }) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = if (comment.likedBy.contains(loggedInUserFirebaseId)) {
                            painterResource(id = R.drawable.ic_heart_filled)
                        } else {
                            painterResource(id = R.drawable.ic_heart)
                        }, contentDescription = stringResource(R.string.like_comment),
                        tint = if (comment.likedBy.contains(loggedInUserFirebaseId)) ColorsHelper.red() else LocalContentColor.current

                    )
                }
            }
        }

    }
}

@Composable
fun AddCommentSection(
    viewModel: PostDetailsViewModel,
    loggedInUser: UsersBean
) {
    val context = LocalContext.current
    val isReply = viewModel.commentedOnState.value != null
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
                    viewModel.repliedCommentPosterConnectIdState.value
                ),
                fontSize = 12.sp,
                color = ColorsHelper.black(),
                fontWeight = FontWeight.Medium
            )
        }
        TransparentTextField(
            modifier = Modifier.weight(1f),
            value = viewModel.commentTextState.value,
            singleLine = true,
            maxLines = 1,
            onValueChange = { text -> viewModel.commentTextState.value = text },
            textStyle = TextStyle(fontSize = 14.sp),
            placeholder = {
                Text(
                    text = stringResource(R.string.add_a_comment),
                    color = ColorsHelper.gray(),
                    fontSize = 13.sp
                )
            })
        if (isReply) {
            IconButton(onClick = { viewModel.commentedOnState.value = null }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove_tag),
                    tint = ColorsHelper.gray()
                )
            }
        }
        if (viewModel.commentTextState.value.isNotBlank() && !viewModel.isSendingCommentState.value) {
            IconButton(onClick = {
                viewModel.addComment(loggedInUser.firebaseUserId)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = stringResource(R.string.post_comment)
                )
            }
        } else if (viewModel.isSendingCommentState.value) {
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
            viewModel.isSendingCommentState.value = true
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    if (addCommentState.message.isNullOrBlank()) stringResource(
                        id = R.string.some_error_occurred
                    ) else addCommentState.message
                viewModel.isSendingCommentState.value = false
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.PostDetailsScreen.name,
                    addCommentState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.post.commentCount++
                val comment = addCommentState.data
                viewModel.commentedOnState.value = null
                viewModel.commentTextState.value = ""
                if (comment != null) {
                    if (comment.parentCommentId == null) {
                        viewModel.commentsMapState[comment] = arrayListOf()
                    } else {
                        val parent =
                            viewModel.commentsMapState.keys.find { it.commentFirebaseId == comment.parentCommentId }
                        if (parent != null) {
                            val updatedChildList = arrayListOf<CommentBean>()
                            val currentChildList = viewModel.commentsMapState[parent]
                            if (currentChildList != null) {
                                updatedChildList.addAll(currentChildList)
                                updatedChildList.add(comment)
                                viewModel.commentsMapState[parent] = updatedChildList
                            }
                        }
                    }
                }
                viewModel.isSendingCommentState.value = false
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
fun HandleDeleteCommentSection(viewModel: PostDetailsViewModel) {
    val deleteCommentState = viewModel.deleteCommentStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (deleteCommentState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(R.string.deleting_comment))
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    deleteCommentState.message ?: stringResource(id = R.string.some_error_occurred)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.PostDetailsScreen.name,
                    deleteCommentState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                val commentId = deleteCommentState.data?.first
                val parentCommentId = deleteCommentState.data?.second
                if (commentId != null) {
                    if (parentCommentId == null) {
                        val comment =
                            viewModel.commentsMapState.keys.find { it.commentFirebaseId == commentId }
                        if (comment != null) {
                            viewModel.commentsMapState.keys.removeIf { commentId == it.commentFirebaseId }
                        }
                    } else {
                        val updatedChildList = arrayListOf<CommentBean>()
                        val parent =
                            viewModel.commentsMapState.keys.find { it.commentFirebaseId == parentCommentId }
                        viewModel.commentsMapState[parent]?.removeIf { it.commentFirebaseId == commentId }
                    }
                    viewModel.post.commentCount--
                }
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}