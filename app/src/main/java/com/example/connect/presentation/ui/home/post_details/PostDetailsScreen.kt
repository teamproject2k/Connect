package com.example.connect.presentation.ui.home.post_details

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.connect.domain.models.CommentWithUserBean
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.utils.FirebaseErrorCodes
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
import com.example.connect.presentation.ui.common.TitleMessageIconOkCancelDialog
import com.example.connect.presentation.ui.common.TransparentTextField
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.common.VisibilityItem
import com.example.connect.presentation.ui.common.VisibilityScopeBottomSheetItem
import com.example.connect.presentation.ui.common.shimmer
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.LikedByScreenDestination
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun PostDetailsScreen(
    navigator: DestinationsNavigator,
    postDetails: PostBean,
    postedByDetails: UsersBean
) {
    val viewModel: PostDetailsViewModel = hiltViewModel()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val context = LocalContext.current

    var showPostVisibilityScopeBottomSheet by remember {
        mutableStateOf(false)
    }

    if (!viewModel.isInitialized) {
        viewModel.initialize(context, postDetails, homeSharedViewModel.usersDetails)
    }

    if (homeSharedViewModel.usersDetails.blockedUsersList.contains(viewModel.post.createdByUserFirebaseId)) {
        LoggingHelper.logData(
            LoggingLevelEnum.Info,
            ConstantsHelper.INFO_TAG,
            ScreenNameEnum.PostDetailsScreen.name,
            "Blocked user post"
        )
        navigator.popBackStack()
    }
    val snackBarHostState = SnackbarHostState()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (viewModel.forceRecomposeState.value >= 0) {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .padding(it)
                        .weight(1f)
                ) {
                    item {
                        PostDetails(
                            usersDetails = postedByDetails,
                            loggedInUser = homeSharedViewModel.usersDetails,
                            viewModel = viewModel,
                            navigator = navigator
                        ) {
                            showPostVisibilityScopeBottomSheet = true
                        }
                    }
                    item {
                        TextBold18(
                            text = stringResource(R.string.comments),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    if (viewModel.getCommentListState.intValue == 0) {
                        items(8) {
                            CommentItemLoading()
                        }
                    } else if (viewModel.getCommentListState.intValue == 2) {
                        if (viewModel.commentDataMap.isEmpty()) {
                            item {
                                TextBold14(
                                    text = stringResource(R.string.no_comments_found),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 16.dp),
                                    alignment = TextAlign.Center
                                )
                            }
                        } else {
                            items(viewModel.commentDataMap.keys.toList()) { parent ->
                                val childCommentList = viewModel.commentDataMap[parent]
                                if (childCommentList != null) {
                                    ParentChildCommentItem(
                                        viewModel,
                                        parent,
                                        childCommentList,
                                        loggedInUserFirebaseId = homeSharedViewModel.usersDetails.firebaseUserId,
                                        navigator = navigator
                                    )
                                }
                            }
                        }
                    }

                }
                DividerLightGrayAlpha50()
                AddCommentSection(
                    viewModel,
                    homeSharedViewModel.usersDetails
                )
            }
        }

        if (showPostVisibilityScopeBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPostVisibilityScopeBottomSheet = false },
                shape = RoundedCornerShape(
                    topEnd = ConstantsHelper.BottomSheetRoundness,
                    topStart = ConstantsHelper.BottomSheetRoundness
                )
            ) {
                PostVisibilityScopeBottomSheet(
                    modifier = Modifier.padding(bottom = ConstantsHelper.NavigationBarHeight),
                    viewModel = viewModel,
                    homeSharedViewModel.usersDetails.firebaseUserId,
                    context
                ) {
                    showPostVisibilityScopeBottomSheet = false
                }
            }
        }
    }
    HandleGetAllCommentsSection(viewModel)
    HandleDeletePostState(viewModel = viewModel, navigator = navigator)
    HandleAddCommentSectionState(viewModel = viewModel, navigator)
    HandleDeleteCommentSectionState(viewModel = viewModel)
    HandleUpdatePostVisibilityState(viewModel = viewModel)
    HandleLikeUnlikeState(viewModel, navigator)
    HandleSaveUnSavePost(viewModel, navigator)
    LaunchedEffect(viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
            viewModel.snackBarMessageState.value = ""
        }
    }
    if (!viewModel.isCommentDataFetched) {
        if (context.isNetworkAvailable()) {
            viewModel.getAllCommentsWithUsers(homeSharedViewModel.usersDetails.firebaseUserId)

        } else {
            viewModel.snackBarMessageState.value =
                stringResource(id = R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
    }
}

@Composable
private fun PostDetails(
    usersDetails: UsersBean,
    loggedInUser: UsersBean,
    viewModel: PostDetailsViewModel,
    navigator: DestinationsNavigator,
    onBottomSheetItemClick: () -> Unit
) {
    val context = LocalContext.current
    var isDropDownMenuVisible by remember {
        mutableStateOf(false)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigator.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.go_back)
                )
            }
            UserDetailsSection(
                user = usersDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        if (loggedInUser.firebaseUserId == usersDetails.firebaseUserId) {
                            navigator.navigate(CurrentUserProfileScreenDestination)
                        } else {
                            navigator.navigate(OtherUserProfileScreenDestination(usersDetails))
                        }
                    }
            )
            if (viewModel.post.createdByUserFirebaseId == loggedInUser.firebaseUserId) {
                Box {
                    IconButton(onClick = { isDropDownMenuVisible = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(id = R.string.more_options),
                        )
                    }
                    if (isDropDownMenuVisible) {
                        PostDetailsDropDownSection(viewModel) {
                            isDropDownMenuVisible = false
                        }
                    }
                    if (viewModel.showDeletePostAlertDialogState.value) {
                        TitleMessageIconOkCancelDialog(
                            imageVector = Icons.Default.Warning,
                            iconTint = ColorsHelper.warning(),
                            title = stringResource(id = R.string.delete_post),
                            subTitle = stringResource(R.string.are_you_sure_you_want_to_delete_this_post),
                            positiveButtonText = stringResource(R.string.delete),
                            onCancel = {
                                viewModel.showDeletePostAlertDialogState.value = false
                            }) {
                            viewModel.deletePost(loggedInUser.firebaseUserId)
                            viewModel.showDeletePostAlertDialogState.value = false
                        }
                    }
                }
            }
        }
        if (viewModel.post.caption.isNotBlank()) {
            ExpandingText(
                modifier = Modifier.padding(16.dp),
                text = viewModel.post.caption,
                context = context,
                minimizedMaxLines = if (viewModel.post.postContentType == MediaTypeEnum.Text.name) 8 else ConstantsHelper.MINIMIZED_MAX_LINES
            )
        } else {
            SpacerHeight16()
        }
        if (
            viewModel.post.postContentType == MediaTypeEnum.Image.name
            || viewModel.post.postContentType == MediaTypeEnum.TextImage.name
            || viewModel.post.postContentType == MediaTypeEnum.Video.name
            || viewModel.post.postContentType == MediaTypeEnum.TextVideo.name
        ) {
            PostCaptionMediaSection(postDetails = viewModel.post)
        }
        PostBottomSection(viewModel, loggedInUser, context, navigator, onBottomSheetItemClick)
        SpacerHeight16()
        DividerLightGrayAlpha40()
    }
}

@Composable
fun PostDetailsDropDownSection(
    viewModel: PostDetailsViewModel,
    onDropDownMenuDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = { onDropDownMenuDismiss() }
    ) {
        DropdownMenuItem(text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete_post)
                )
                SpacerWidth16()
                Text(text = stringResource(id = R.string.delete_post))
            }
        }, onClick = {
            viewModel.showDeletePostAlertDialogState.value = true
            onDropDownMenuDismiss()
        })
    }

}

@Composable
private fun PostBottomSection(
    viewModel: PostDetailsViewModel,
    loggedInUser: UsersBean,
    context: Context,
    navigator: DestinationsNavigator,
    onBottomSheetItemClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (context.isNetworkAvailable()) {
                        if (viewModel.post.likedBy.contains(loggedInUser.firebaseUserId)) {
                            viewModel.removeLikeForPost(loggedInUser.firebaseUserId)
                        } else {
                            viewModel.addLikeOnPost(loggedInUser.firebaseUserId)
                        }
                    } else {
                        viewModel.snackBarMessageState.value =
                            context.getString(R.string.no_internet_connection)
                        FunctionHelper.vibrateDevice(context)
                    }
                }) {
                    Icon(
                        painter = if (viewModel.isPostLikedByLoggedInUserState.value) painterResource(
                            id = R.drawable.ic_heart_filled
                        ) else painterResource(id = R.drawable.ic_heart),
                        contentDescription = stringResource(
                            id = R.string.like_post
                        ),
                        tint = if (viewModel.post.likedBy.contains(loggedInUser.firebaseUserId)) ColorsHelper.red() else LocalContentColor.current
                    )
                }
                if (viewModel.post.createdByUserFirebaseId == loggedInUser.firebaseUserId) {
                    SpacerWidth8()
                    VisibilityItem(
                        drawableId = viewModel.currentPostVisibilityState.value.drawableId,
                        scopeName = viewModel.currentPostVisibilityState.value.scopeName
                    ) {
                        onBottomSheetItemClick()
                    }
                }
            }
            IconButton(onClick = {
                if (context.isNetworkAvailable()) {
                    if (loggedInUser.savedPosts.contains(viewModel.post.postFirebaseId)) {
                        viewModel.unSavePost(loggedInUser)
                    } else {
                        viewModel.savePost(loggedInUser)
                    }
                } else {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.no_internet_connection)
                    FunctionHelper.vibrateDevice(context)
                }
            }) {
                Icon(
                    imageVector = if (viewModel.isPostSavedByLoggedInUserState.value) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = stringResource(R.string.save_post)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (viewModel.post.likedBy.size == 1) stringResource(R.string._1_like) else stringResource(
                    R.string.like_count_likes,
                    viewModel.post.likedBy.size
                ),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable {
                        navigator.navigate(LikedByScreenDestination(viewModel.post.likedBy))
                    },
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
private fun PostVisibilityScopeBottomSheet(
    modifier: Modifier,
    viewModel: PostDetailsViewModel,
    loggedInUserFirebaseId: String,
    context: Context,
    onDismissRequest: () -> Unit
) {
    Column(modifier = modifier) {
        viewModel.postVisibilityScopeList.forEach { postScope ->
            VisibilityScopeBottomSheetItem(postScope) {
                if (context.isNetworkAvailable()) {
                    viewModel.updatePostVisibility(postScope, loggedInUserFirebaseId)
                } else {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.no_internet_connection)
                    FunctionHelper.vibrateDevice(context)
                }
                onDismissRequest()
            }
        }
    }
}

@Composable
fun HandleUpdatePostVisibilityState(
    viewModel: PostDetailsViewModel
) {
    val updatePostVisibilityState = viewModel.updatePostVisibilityStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (updatePostVisibilityState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(R.string.updating_post_visibility))
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                if (updatePostVisibilityState.message == FirebaseErrorCodes.UNAUTHORIZED_ACCESS) {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.something_went_wrong)
                } else {
                    viewModel.snackBarMessageState.value =
                        updatePostVisibilityState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }

                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.PostDetailsScreen.name,
                    updatePostVisibilityState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (updatePostVisibilityState.data != null) {
                viewModel.currentPostVisibilityState.value = updatePostVisibilityState.data
            }
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
fun HandleDeletePostState(
    viewModel: PostDetailsViewModel,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val deletePostState = viewModel.deletePostStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (deletePostState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(R.string.deleting_post))
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (deletePostState.message == FirebaseErrorCodes.UNAUTHORIZED_ACCESS) {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.something_went_wrong)
                } else {
                    viewModel.snackBarMessageState.value =
                        deletePostState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }

                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.PostDetailsScreen.name,
                    deletePostState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                context.showToast(stringResource(R.string.post_deleted_successfully))
                viewModel.post.whetherDeleted = true
                navigator.popBackStack()
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
fun HandleGetAllCommentsSection(viewModel: PostDetailsViewModel) {
    val getAllCommentsState = viewModel.getAllCommentsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (getAllCommentsState.status) {
        RequestStatusEnum.Loading -> {
            isExceptionHandled = false
            viewModel.getCommentListState.intValue = 0
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.getCommentListState.intValue = 1
                viewModel.snackBarMessageState.value =
                    getAllCommentsState.message
                        ?: stringResource(id = R.string.something_went_wrong)
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
            viewModel.getCommentListState.intValue = 2
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
fun ParentChildCommentItem(
    viewModel: PostDetailsViewModel,
    parentCommentWithUserBean: CommentWithUserBean,
    childCommentList: List<CommentWithUserBean>,
    loggedInUserFirebaseId: String,
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        CommentItem(
            parentCommentWithUserBean,
            viewModel = viewModel,
            loggedInUserFirebaseId = loggedInUserFirebaseId,
            navigator = navigator
        ) {
            if (context.isNetworkAvailable()) {
                val deleteCount = childCommentList.count { !it.comment.whetherDeleted } + 1
                viewModel.deleteComment(parentCommentWithUserBean.comment, deleteCount)
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.no_internet_connection)
                FunctionHelper.vibrateDevice(context)
            }
        }
        Column(modifier = Modifier.padding(start = 32.dp)) {
            childCommentList.forEach { commentWithUser ->
                CommentItem(
                    commentWithUser,
                    viewModel = viewModel,
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    navigator = navigator
                ) {
                    if (context.isNetworkAvailable()) {
                        viewModel.deleteComment(commentWithUser.comment, 1)
                    } else {
                        viewModel.snackBarMessageState.value =
                            context.getString(R.string.no_internet_connection)
                        FunctionHelper.vibrateDevice(context)
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
fun CommentItemLoading() {
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CommentItem(
    commentWithCommentPoster: CommentWithUserBean,
    viewModel: PostDetailsViewModel,
    navigator: DestinationsNavigator,
    loggedInUserFirebaseId: String,
    onDeleteCommentClicked: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    var isLoading by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
            .clickable {
                if (commentWithCommentPoster.userDetails.firebaseUserId == loggedInUserFirebaseId) {
                    navigator.navigate(CurrentUserProfileScreenDestination)
                } else {
                    navigator.navigate(OtherUserProfileScreenDestination(commentWithCommentPoster.userDetails))
                }
            },
    ) {
        AsyncImage(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, ColorsHelper.black(), CircleShape),
            model = commentWithCommentPoster.userDetails.profilePhoto,
            contentDescription = commentWithCommentPoster.userDetails.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextBold13(text = commentWithCommentPoster.userDetails.connectUserId)
                SpacerWidth8()
                Text(
                    text = FunctionHelper.getTimeAgo(
                        commentWithCommentPoster.comment.createdAt,
                        context,
                        true
                    ),
                    color = ColorsHelper.gray(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            SpacerHeight4()
            Text(
                buildAnnotatedString {
                    if (commentWithCommentPoster.comment.repliedOnCommentId != null) {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = ColorsHelper.gray()
                            )
                        ) {
                            append("${commentWithCommentPoster.commentedOnUserConnectId}  ")
                        }
                    }
                    append(commentWithCommentPoster.comment.commentMessage)
                },
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
            SpacerHeight12()
            Row {
                Text(
                    modifier = Modifier.clickable {
                        keyboardController?.show()
                        viewModel.repliedCommentPosterConnectIdState.value =
                            commentWithCommentPoster.userDetails.connectUserId
                        viewModel.commentedOnState.value = commentWithCommentPoster.comment
                    },
                    text = stringResource(R.string.reply),
                    fontSize = 12.sp,
                    color = ColorsHelper.gray(),
                    fontWeight = FontWeight.Medium
                )
                SpacerWidth16()
                if (viewModel.post.createdByUserFirebaseId == loggedInUserFirebaseId || commentWithCommentPoster.comment.commentedBy == loggedInUserFirebaseId) {
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(8.dp)
                        .pointerInput(true) {
                            detectTapGestures(onTap = {
                                if (context.isNetworkAvailable()) {
                                    isLoading = true
                                    if (commentWithCommentPoster.comment.likedBy.contains(
                                            loggedInUserFirebaseId
                                        )
                                    ) {
                                        viewModel.removeLikeForComment(
                                            commentWithCommentPoster.comment,
                                            loggedInUserFirebaseId,
                                            onSuccess = {
                                                isLoading = false
                                            }) { errorMessage ->
                                            viewModel.snackBarMessageState.value =
                                                if (errorMessage.isNullOrBlank()) context.getString(
                                                    R.string.something_went_wrong
                                                ) else errorMessage
                                            isLoading = false
                                        }
                                    } else {
                                        viewModel.addLikeForComment(
                                            commentWithCommentPoster.comment,
                                            loggedInUserFirebaseId,
                                            onSuccess = {
                                                isLoading = false
                                            }) { errorMessage ->
                                            viewModel.snackBarMessageState.value =
                                                if (errorMessage.isNullOrBlank()) context.getString(
                                                    R.string.something_went_wrong
                                                ) else errorMessage
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    viewModel.snackBarMessageState.value =
                                        context.getString(R.string.no_internet_connection)
                                    FunctionHelper.vibrateDevice(context)
                                }

                            }, onLongPress = {
                                FunctionHelper.vibrateDevice(context, 100)
                                navigator.navigate(LikedByScreenDestination(commentWithCommentPoster.comment.likedBy))

                            })
                        }) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = if (commentWithCommentPoster.comment.likedBy.contains(
                                loggedInUserFirebaseId
                            )
                        ) {
                            painterResource(id = R.drawable.ic_heart_filled)
                        } else {
                            painterResource(id = R.drawable.ic_heart)
                        }, contentDescription = stringResource(R.string.like_comment),
                        tint = if (commentWithCommentPoster.comment.likedBy.contains(
                                loggedInUserFirebaseId
                            )
                        ) ColorsHelper.red() else LocalContentColor.current
                    )
                    SpacerHeight4()
                    Text(
                        text = commentWithCommentPoster.comment.likedBy.size.toString(),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddCommentSection(
    viewModel: PostDetailsViewModel,
    loggedInUser: UsersBean
) {
    val context = LocalContext.current
    val isReply = viewModel.commentedOnState.value != null
    val keyboardController = LocalSoftwareKeyboardController.current

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
                keyboardController?.hide()
                if (context.isNetworkAvailable()) {
                    viewModel.addComment(loggedInUser)
                } else {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.no_internet_connection)
                    FunctionHelper.vibrateDevice(context)
                }
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
fun HandleAddCommentSectionState(
    viewModel: PostDetailsViewModel,
    navigator: DestinationsNavigator
) {
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
                if (addCommentState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                    viewModel.snackBarMessageState.value = stringResource(
                        id = R.string.something_went_wrong
                    )
                    navigator.popBackStack()
                } else {
                    viewModel.snackBarMessageState.value =
                        if (addCommentState.message.isNullOrBlank()) stringResource(
                            id = R.string.something_went_wrong
                        ) else addCommentState.message
                }
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
                isResponseHandled = true
                viewModel.commentedOnState.value = null
                viewModel.commentTextState.value = ""
                viewModel.isSendingCommentState.value = false
                viewModel.forceRecomposeState.value++
            }
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
fun HandleDeleteCommentSectionState(viewModel: PostDetailsViewModel) {
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
                    deleteCommentState.message ?: stringResource(id = R.string.something_went_wrong)
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
                viewModel.forceRecomposeState.intValue++
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
private fun HandleLikeUnlikeState(
    viewModel: PostDetailsViewModel,
    navigator: DestinationsNavigator
) {
    val likeUnlikeState = viewModel.likeUnlikePostStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (likeUnlikeState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(id = R.string.please_wait))
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (likeUnlikeState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.post_not_found)
                    navigator.popBackStack()
                } else {
                    viewModel.snackBarMessageState.value =
                        likeUnlikeState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.forceRecomposeState.intValue++
                isResponseHandled = true
            }

        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
private fun HandleSaveUnSavePost(
    viewModel: PostDetailsViewModel,
    navigator: DestinationsNavigator
) {
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
            if (saveUnSavePostState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                viewModel.snackBarMessageState.value =
                    stringResource(id = R.string.post_not_found)
                navigator.popBackStack()
            } else {
                viewModel.snackBarMessageState.value =
                    saveUnSavePostState.message
                        ?: stringResource(id = R.string.something_went_wrong)
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