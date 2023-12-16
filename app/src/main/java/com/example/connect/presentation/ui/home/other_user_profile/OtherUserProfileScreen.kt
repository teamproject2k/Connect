package com.example.connect.presentation.ui.home.other_user_profile

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.common.ErrorCodes
import com.example.connect.common.LoggingHelper
import com.example.connect.common.LoggingLevelEnum
import com.example.connect.common.RequestStatusEnum
import com.example.connect.domain.enums.StatusWithCurrentEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.ui.auth.AuthenticationActivity
import com.example.connect.presentation.ui.common.BottomSheetItem
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SpacerHeight12
import com.example.connect.presentation.ui.common.SpacerHeight24
import com.example.connect.presentation.ui.common.SpacerWidth16
import com.example.connect.presentation.ui.common.UserProfileFriendsListLoadingSection
import com.example.connect.presentation.ui.common.UserProfileFriendsListSection
import com.example.connect.presentation.ui.common.UserProfilePostLoadingSection
import com.example.connect.presentation.ui.common.UserProfilePostSection
import com.example.connect.presentation.ui.common.UserProfileUserInfoSection
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun OtherUserProfileScreen(navigator: DestinationsNavigator, requestedUser: UsersBean) {
    val viewModel: OtherUserProfileViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()

    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)

    if (!viewModel.isDataInitialized) {
        viewModel.initializeData(homeSharedViewModel.usersDetails, requestedUser)
    }

    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            ProfileScreen(
                currentUser = homeSharedViewModel.usersDetails,
                requestedUser = requestedUser,
                viewModel = viewModel,
                navigator
            ) {
                showBottomSheet = true
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }, shape = RoundedCornerShape(
                    topEnd = ConstantsHelper.BottomSheetRoundness,
                    topStart = ConstantsHelper.BottomSheetRoundness
                )
            ) {
                BottomSheetSection(
                    Modifier.padding(bottom = ConstantsHelper.NavigationBarHeight),
                    viewModel
                ) { showSheet ->
                    showBottomSheet = !showSheet
                }
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
    LaunchedEffect(key1 = true) {
        viewModel.getFriendListFromIds(homeSharedViewModel.usersDetails.friendList)
        viewModel.getPostDetails()
    }

    HandleSendFriendRequestStateFlow(viewModel = viewModel)
    HandleWithdrawFriendRequestStateFlow(viewModel = viewModel)
    HandleAcceptFriendRequestStateFlow(viewModel = viewModel)
    HandleRemoveFriendRequestStateFlow(viewModel = viewModel)
    HandleUnBlockUserStateFlow(viewModel = viewModel)
    HandleBlockUserStateFlow(viewModel = viewModel)
}

@Composable
private fun ProfileScreen(
    currentUser: UsersBean,
    requestedUser: UsersBean,
    viewModel: OtherUserProfileViewModel,
    navigator: DestinationsNavigator,
    onOptionsMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageSection(
            requestedUser,
            viewModel,
            onOptionsMenuClick
        )
        SpacerHeight12()
        UserProfileUserInfoSection(requestedUser)
        SpacerHeight24()
        ActionButtonsSection(currentUser, requestedUser, viewModel)
        SpacerHeight24()
        HandleFriendListSection(viewModel = viewModel, navigator)
        SpacerHeight24()
        HandlePostSection(viewModel, navigator)
    }
}

@Composable
private fun ImageSection(
    requestedUser: UsersBean,
    viewModel: OtherUserProfileViewModel,
    onOptionsMenuClick: () -> Unit
) {
    val showOptionsMenu =
        viewModel.statusWithCurrentUserState.value != StatusWithCurrentEnum.Blocked.name
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (
            coverImageRef, profileImageRef, moreOptionsRef
        ) = createRefs()
        AsyncImage(
            model = requestedUser.coverPhoto,
            contentDescription = stringResource(R.string.cover_photo),
            modifier = Modifier
                .fillMaxWidth()
                .height(ConstantsHelper.CoverImageHeight)
                .background(Color.LightGray)
                .constrainAs(coverImageRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            contentScale = ContentScale.Crop,
        )
        AsyncImage(
            model = requestedUser.profilePhoto,
            contentDescription = stringResource(R.string.profile_image),
            modifier = Modifier
                .size(ConstantsHelper.ProfileImageHeight)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .border(4.dp, Color.White, CircleShape)
                .constrainAs(profileImageRef) {
                    start.linkTo(parent.start, 16.dp)
                    top.linkTo(coverImageRef.bottom)
                    bottom.linkTo(coverImageRef.bottom)
                },
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user),
            placeholder = painterResource(id = R.drawable.ic_default_user)
        )
        if (showOptionsMenu) {
            IconButton(onClick = {
                onOptionsMenuClick()
            },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier.constrainAs(moreOptionsRef) {
                    top.linkTo(coverImageRef.top, 16.dp)
                    end.linkTo(coverImageRef.end, 16.dp)
                }
            ) {
                Image(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more_options)
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    currentUser: UsersBean,
    requestedUser: UsersBean,
    viewModel: OtherUserProfileViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        when (viewModel.statusWithCurrentUserState.value) {
            StatusWithCurrentEnum.Friends.name -> {
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Default.Message,
                    buttonText = stringResource(R.string.message),
                    textColor = ColorsHelper.black(),
                    buttonBackgroundColor = ColorsHelper.grayButtonBackground(),
                    onButtonClick = {
                        // TODO: 16/12/23 aryan Navigate to chats screen
                    }
                )
            }

            StatusWithCurrentEnum.Blocked.name -> {
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Default.LockReset,
                    buttonText = stringResource(R.string.unblock_user),
                    onButtonClick = {
                        viewModel.unBlockUser(
                            currentUser.firebaseUserId,
                            requestedUser.firebaseUserId
                        )
                    }
                )
            }

            StatusWithCurrentEnum.RequestedByCurrentUser.name -> {
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Outlined.ArrowCircleLeft,
                    buttonText = stringResource(R.string.withdraw_request),
                    onButtonClick = {
                        viewModel.withdrawFriendRequest(
                            currentUser.firebaseUserId,
                            requestedUser.firebaseUserId
                        )
                    }
                )
            }

            StatusWithCurrentEnum.RequestedByOtherUser.name -> {
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Default.CheckCircleOutline,
                    buttonText = stringResource(R.string.accept),
                    onButtonClick = {
                        viewModel.acceptFriendRequest(
                            currentUser.firebaseUserId,
                            requestedUser.firebaseUserId
                        )
                    }
                )
                SpacerWidth16()
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Default.RemoveCircleOutline,
                    buttonText = stringResource(R.string.remove),
                    textColor = ColorsHelper.black(),
                    buttonBackgroundColor = ColorsHelper.grayButtonBackground(),
                    onButtonClick = {
                        viewModel.removeFriendRequest(
                            currentUser.firebaseUserId,
                            requestedUser.firebaseUserId
                        )
                    }
                )
            }

            StatusWithCurrentEnum.NotFriends.name -> {
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Default.PersonAddAlt1,
                    buttonText = stringResource(R.string.add_friend),
                    onButtonClick = {
                        viewModel.sendFriendRequest(
                            currentUser.firebaseUserId,
                            requestedUser.firebaseUserId
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun IconTextButton(
    modifier: Modifier = Modifier,
    buttonImage: ImageVector,
    buttonText: String,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    buttonBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    onButtonClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = { onButtonClick() },
        colors = ButtonDefaults.buttonColors(containerColor = buttonBackgroundColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                imageVector = buttonImage,
                contentDescription = buttonText,
                colorFilter = ColorFilter.tint(textColor)
            )
            Text(text = buttonText, color = textColor)
        }
    }
}

@Composable
private fun HandleFriendListSection(
    viewModel: OtherUserProfileViewModel,
    navigator: DestinationsNavigator
) {
    val friendsDetailsState = viewModel.friendsDetailsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (friendsDetailsState.status) {
        RequestStatusEnum.LOADING -> {
            UserProfileFriendsListLoadingSection()
            if (isExceptionHandled) {
                isExceptionHandled = false
            }
        }

        RequestStatusEnum.SUCCESS -> {
            UserProfileFriendsListSection(
                navigator = navigator,
                friendsList = friendsDetailsState.data!!
            )
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    friendsDetailsState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "OtherUserProfileScreen",
                    friendsDetailsState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.NONE -> {
            // no need to handle it
        }
    }
}

@Composable
private fun HandlePostSection(
    viewModel: OtherUserProfileViewModel,
    navigator: DestinationsNavigator
) {
    val postDetailState = viewModel.postDetailsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    when (postDetailState.status) {
        RequestStatusEnum.LOADING -> {
            UserProfilePostLoadingSection()
            if (isExceptionHandled) {
                isExceptionHandled = false
            }
        }

        RequestStatusEnum.SUCCESS -> {
            UserProfilePostSection(navigator, postDetailsList = postDetailState.data!!.reversed())
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                if (postDetailState.message == ErrorCodes.NoUserFound) {
                    viewModel.sharedPreference.isUserDetailsEntered = false
                    context.showToast(stringResource(R.string.no_user_found_please_reenter_details))
                    val intent = Intent(context, AuthenticationActivity::class.java)
                    context.startActivity(intent)
                    LocalActivity.current.finish()
                } else {
                    viewModel.snackBarMessageState.value =
                        postDetailState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "OtherUserProfileScreen",
                    postDetailState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.NONE -> {
            //no need to handle it
        }
    }
}


@Composable
private fun BottomSheetSection(
    modifier: Modifier,
    viewModel: OtherUserProfileViewModel,
    onBottomSheetStateChange: (showSheet: Boolean) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (viewModel.statusWithCurrentUserState.value == StatusWithCurrentEnum.Friends.name) {
            BottomSheetItem(
                imageVector = Icons.Default.PersonRemove,
                text = stringResource(R.string.unfriend_user)
            ) {
                onBottomSheetStateChange(false)
            }
            BottomSheetItem(
                imageVector = Icons.Default.PersonOff,
                text = stringResource(R.string.unfriend_and_block_user)
            ) {
                onBottomSheetStateChange(false)
            }
        } else if (viewModel.statusWithCurrentUserState.value != StatusWithCurrentEnum.Blocked.name) {
            BottomSheetItem(
                imageVector = Icons.Default.PersonOff,
                text = stringResource(R.string.block_user)
            ) {
                onBottomSheetStateChange(false)
            }
        }
    }
}

@Composable
fun HandleSendFriendRequestStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val sendFriendRequestState = viewModel.sendFriendRequestStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (sendFriendRequestState.status) {
        RequestStatusEnum.LOADING -> {
            isResponseHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentEnum.RequestedByCurrentUser.name
                isResponseHandled = true
                viewModel.snackBarMessageState.value =
                    stringResource(id = R.string.friend_request_sent_successfully)
            }
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    sendFriendRequestState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "OtherUserProfileScreen",
                    sendFriendRequestState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.NONE -> {
            // no need to handle this
        }
    }
}

@Composable
fun HandleWithdrawFriendRequestStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val withDrawRequestState = viewModel.withdrawFriendRequestStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (withDrawRequestState.status) {
        RequestStatusEnum.LOADING -> {
            isResponseHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentEnum.NotFriends.name
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.friend_request_removed_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    withDrawRequestState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "OtherUserProfileScreen",
                    withDrawRequestState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.NONE -> {
            // no need to handle this
        }
    }
}

@Composable
fun HandleAcceptFriendRequestStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val acceptFriendRequestState = viewModel.acceptFriendRequestStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (acceptFriendRequestState.status) {
        RequestStatusEnum.LOADING -> {
            isResponseHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentEnum.Friends.name
                viewModel.snackBarMessageState.value =
                    stringResource(id = R.string.friend_added_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    acceptFriendRequestState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "OtherUserProfileScreen",
                    acceptFriendRequestState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.NONE -> {
            // no need to handle this
        }
    }
}

@Composable
fun HandleRemoveFriendRequestStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val removeFriendRequestState = viewModel.removeFriendRequestStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (removeFriendRequestState.status) {
        RequestStatusEnum.LOADING -> {
            isResponseHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentEnum.NotFriends.name
                viewModel.snackBarMessageState.value =
                    stringResource(id = R.string.friend_request_removed_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    removeFriendRequestState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "OtherUserProfileScreen",
                    removeFriendRequestState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.NONE -> {
            // no need to handle this
        }
    }
}

@Composable
fun HandleUnBlockUserStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val unblockUserState = viewModel.unBlockUserStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (unblockUserState.status) {
        RequestStatusEnum.LOADING -> {
            isResponseHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentEnum.NotFriends.name
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.user_unblocked_successfully)
                isResponseHandled = true
            }

        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    unblockUserState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "OtherUserProfileScreen",
                    unblockUserState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.NONE -> {
            // no need to handle this
        }
    }
}

@Composable
fun HandleBlockUserStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val blockUserState = viewModel.blockUserStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (blockUserState.status) {
        RequestStatusEnum.LOADING -> {
            isResponseHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentEnum.Blocked.name
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.user_blocked_successfully)
                isResponseHandled = true
            }

        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    blockUserState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "OtherUserProfileScreen",
                    blockUserState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.NONE -> {
            // no need to handle this
        }
    }
}

