package com.example.connect.presentation.ui.home.post_details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha40
import com.example.connect.presentation.ui.common.Dot
import com.example.connect.presentation.ui.common.ExpandingText
import com.example.connect.presentation.ui.common.PostCaptionMediaSection
import com.example.connect.presentation.ui.common.SpacerHeight16
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.ui.common.SpacerWidth16
import com.example.connect.presentation.ui.common.SpacerWidth8
import com.example.connect.presentation.ui.common.TextBold18
import com.example.connect.presentation.ui.common.TitleMessageIconOkCancelDialog
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.enums.MediaTypeEnum
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
    postBean: PostBean,
    usersDetails: UsersBean,
    currentUserFirebaseId: String
) {
    val viewModel: PostDetailsViewModel = hiltViewModel()
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            PostDetails(
                usersDetails = usersDetails,
                postDetails = postBean,
                currentUserFirebaseId = currentUserFirebaseId,
                viewModel = viewModel,
                navigator = navigator
            )
            TextBold18(
                text = stringResource(R.string.comments),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HandleCommentSections(viewModel)
        }
    }
}

@Composable
fun HandleCommentSections(viewModel: PostDetailsViewModel) {

}

@Composable
private fun PostDetails(
    usersDetails: UsersBean,
    postDetails: PostBean,
    currentUserFirebaseId: String,
    viewModel: PostDetailsViewModel,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigator.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.go_back),
                )
            }
            SpacerWidth8()
            UserDetailsSection(
                user = usersDetails,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        if (currentUserFirebaseId == usersDetails.firebaseUserId) {
                            navigator.navigate(CurrentUserProfileScreenDestination)
                        } else {
                            navigator.navigate(OtherUserProfileScreenDestination(usersDetails))
                        }
                    }
            )
            Box {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(id = R.string.more_options),
                    )
                }
                PostDetailsDropDownSection(viewModel, postDetails.id)
            }
        }
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
        PostBottomSection(postDetails, viewModel, currentUserFirebaseId)
        SpacerHeight16()
        DividerLightGrayAlpha40()
    }
}

@Composable
fun PostDetailsDropDownSection(viewModel: PostDetailsViewModel, id: String) {
    val postDetailsDropdownList = listOf(stringResource(R.string.delete_post))
    var isDropdownMenuVisible by remember {
        mutableStateOf(false)
    }
    var showDeletePostAlertDialog by remember {
        mutableStateOf(false)
    }
    if (isDropdownMenuVisible) {
        DropdownMenu(
            expanded = true, onDismissRequest = { isDropdownMenuVisible = false }
        ) {
            postDetailsDropdownList.forEach { item ->
                DropdownMenuItem(text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.Default.Delete,
                            contentDescription = item
                        )
                        SpacerWidth16()
                        Text(text = item)
                    }
                }, onClick = {
                    showDeletePostAlertDialog = true
                })
            }
        }
    }
    if (showDeletePostAlertDialog) {
        TitleMessageIconOkCancelDialog(
            imageVector = Icons.Default.Warning,
            iconTint = ColorsHelper.warning(),
            title = stringResource(id = R.string.delete_post),
            subTitle = stringResource(R.string.are_you_sure_you_want_to_delete_this_post),
            positiveButtonText = stringResource(R.string.delete),
            onCancel = {
                showDeletePostAlertDialog = false
            }) {
            viewModel.deletePost(postId = id)
        }
    }
}

@Composable
private fun PostBottomSection(
    postDetails: PostBean,
    viewModel: PostDetailsViewModel,
    currentUserFirebaseId: String,
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
