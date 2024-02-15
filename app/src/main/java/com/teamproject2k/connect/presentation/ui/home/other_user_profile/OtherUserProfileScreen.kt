package com.teamproject2k.connect.presentation.ui.home.other_user_profile

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.ui.chat.base_screen.ChatActivity
import com.teamproject2k.connect.presentation.ui.common.ColorsHelper
import com.teamproject2k.connect.presentation.ui.common.ExpandedImage
import com.teamproject2k.connect.presentation.ui.common.IconTextRowSection
import com.teamproject2k.connect.presentation.ui.common.LoaderDialog
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.common.SpacerHeight12
import com.teamproject2k.connect.presentation.ui.common.SpacerHeight24
import com.teamproject2k.connect.presentation.ui.common.SpacerWidth16
import com.teamproject2k.connect.presentation.ui.common.UserProfileFriendsListLoadingSection
import com.teamproject2k.connect.presentation.ui.common.UserProfileFriendsListSection
import com.teamproject2k.connect.presentation.ui.common.UserProfilePostLoadingSection
import com.teamproject2k.connect.presentation.ui.common.UserProfilePostSection
import com.teamproject2k.connect.presentation.ui.common.UserProfileUserInfoSection
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
import com.teamproject2k.connect.presentation.ui.enums.StatusWithCurrentUserUiEnum
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
fun OtherUserProfileScreen(navigator: DestinationsNavigator, requestedUser: UsersBean) {
    val viewModel: OtherUserProfileViewModel = hiltViewModel()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)

    if (!viewModel.isDataInitialized) {
        viewModel.initializeData(homeSharedViewModel.usersDetails, requestedUser)
    }
    var showBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }
    var refreshing by rememberSaveable { mutableStateOf(false) }

    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            refreshing = true
            if (context.isNetworkAvailable()) {
                viewModel.getUserDetails()
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.no_internet_connection)
                FunctionHelper.vibrateDevice(context)
            }
            refreshing = false
        })
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            ProfileScreen(
                viewModel = viewModel,
                navigator,
                requestedUser,
            ) {
                showBottomSheet = true
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                refreshState = pullRefreshState
            )
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
                ) {
                    showBottomSheet = false
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
        if (context.isNetworkAvailable()) {
            viewModel.getFriendListFromIds()
            viewModel.getPostDetails()
            viewModel.liveObserveOtherUsers()
            viewModel.liveObserveLoggedInUsers()
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
    }

    HandleSendFriendRequestStateFlow(viewModel = viewModel)
    HandleWithdrawFriendRequestStateFlow(viewModel = viewModel)
    HandleAcceptFriendRequestStateFlow(viewModel = viewModel)
    HandleRemoveFriendRequestStateFlow(viewModel = viewModel)
    HandleUnBlockUserStateFlow(viewModel = viewModel)
    HandleBlockUserStateFlow(viewModel = viewModel)
    HandleUnfriendUserStateFlow(viewModel = viewModel)
    HandleUnfriendAndBlockUserStateFlow(viewModel = viewModel)
    HandleLiveObserveOtherUsersStateFlow(viewModel)
    HandleLiveObserveCurrentUsersStateFlow(viewModel, homeSharedViewModel)
    HandleUserDetailsState(viewModel = viewModel, homeSharedViewModel = homeSharedViewModel)
}

@Composable
fun HandleLiveObserveCurrentUsersStateFlow(
    viewModel: OtherUserProfileViewModel,
    homeSharedViewModel: HomeSharedViewModel
) {
    val liveObserverState = viewModel.liveObserveCurrentUserDetailsStateFlow.collectAsState().value
    when (liveObserverState.status) {
        RequestStatusEnum.Success -> {
            val updatedDetails = liveObserverState.data
            if (updatedDetails != null) {
                viewModel.updateLoggedInUser(updatedDetails)
                homeSharedViewModel.usersDetails = updatedDetails
            }
        }

        else -> {
            //no need to handle it
        }
    }
}

@Composable
fun HandleLiveObserveOtherUsersStateFlow(viewModel: OtherUserProfileViewModel) {
    val liveObserverState = viewModel.liveObserveRequiredUserDetailsStateFlow.collectAsState().value
    when (liveObserverState.status) {
        RequestStatusEnum.Success -> {
            val updatedDetails = liveObserverState.data
            if (updatedDetails != null) {
                viewModel.updateOtherUser(updatedDetails)
            }
        }

        else -> {
            //no need to handle it 
        }
    }
}

@Composable
private fun ProfileScreen(
    viewModel: OtherUserProfileViewModel,
    navigator: DestinationsNavigator,
    userDetails: UsersBean,
    onOptionsMenuClick: () -> Unit
) {
    if (viewModel.statusWithCurrentUserState.value == StatusWithCurrentUserUiEnum.BlockedByOtherUser.name) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.users_details_not_found))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageSection(viewModel, onOptionsMenuClick)
            SpacerHeight12()
            UserProfileUserInfoSection(
                viewModel.otherUserState.value,
                viewModel.loggedInUserState.value.firebaseUserId
            )
            SpacerHeight24()
            ActionButtonsSection(viewModel)
            SpacerHeight24()
            HandleFriendListSection(viewModel = viewModel, navigator)
            HandlePostDetailsState(viewModel, navigator, userDetails)
        }
    }
}


@Composable
private fun ImageSection(
    viewModel: OtherUserProfileViewModel,
    onOptionsMenuClick: () -> Unit
) {
    val showOptionsMenu =
        viewModel.statusWithCurrentUserState.value != StatusWithCurrentUserUiEnum.BlockedByCurrentUser.name
    var isProfilePhotoExpanded by remember {
        mutableStateOf(false)
    }
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (
            coverImageRef, profileImageRef, moreOptionsRef
        ) = createRefs()
        AsyncImage(
            model = viewModel.otherUserState.value.coverPhoto,
            contentDescription = stringResource(R.string.cover_photo),
            modifier = Modifier
                .fillMaxWidth()
                .height(ConstantsHelper.CoverImageHeight)
                .background(ColorsHelper.lightGray())
                .constrainAs(coverImageRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            contentScale = ContentScale.Crop,
        )
        AsyncImage(
            model = viewModel.otherUserState.value.profilePhoto,
            contentDescription = stringResource(R.string.profile_image),
            modifier = Modifier
                .size(ConstantsHelper.ProfileImageHeight)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .border(4.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                .constrainAs(profileImageRef) {
                    start.linkTo(parent.start, 16.dp)
                    top.linkTo(coverImageRef.bottom)
                    bottom.linkTo(coverImageRef.bottom)
                }
                .clickable {
                    isProfilePhotoExpanded = !isProfilePhotoExpanded
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
        if (isProfilePhotoExpanded && viewModel.otherUserState.value.profilePhoto != null) {
            ExpandedImage(imageUrl = viewModel.otherUserState.value.profilePhoto) {
                isProfilePhotoExpanded = false
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(viewModel: OtherUserProfileViewModel) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        when (viewModel.statusWithCurrentUserState.value) {
            StatusWithCurrentUserUiEnum.Friends.name -> {
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Default.Message,
                    buttonText = stringResource(R.string.message),
                    textColor = ColorsHelper.black(),
                    buttonBackgroundColor = ColorsHelper.grayButtonBackground(),
                    onButtonClick = {
                        val intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra(
                            ConstantsHelper.USER_DETAILS_KEY,
                            viewModel.loggedInUserState.value
                        )
                        context.startActivity(intent)
                    }
                )
            }

            StatusWithCurrentUserUiEnum.BlockedByCurrentUser.name -> {
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Default.LockReset,
                    buttonText = stringResource(R.string.unblock_user),
                    onButtonClick = {
                        if (context.isNetworkAvailable()) {
                            viewModel.unBlockUser()
                        } else {
                            viewModel.snackBarMessageState.value =
                                context.getString(R.string.no_internet_connection)
                            FunctionHelper.vibrateDevice(context)
                        }
                    }
                )
            }

            StatusWithCurrentUserUiEnum.RequestedByCurrentUser.name -> {
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Outlined.ArrowCircleLeft,
                    buttonText = stringResource(R.string.withdraw_request),
                    onButtonClick = {
                        if (context.isNetworkAvailable()) {
                            viewModel.withdrawFriendRequest()
                        } else {
                            viewModel.snackBarMessageState.value =
                                context.getString(R.string.no_internet_connection)
                            FunctionHelper.vibrateDevice(context)
                        }
                    }
                )
            }

            StatusWithCurrentUserUiEnum.RequestedByOtherUser.name -> {
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Default.CheckCircleOutline,
                    buttonText = stringResource(R.string.accept),
                    onButtonClick = {
                        if (context.isNetworkAvailable()) {
                            viewModel.acceptFriendRequest(context = context)
                        } else {
                            viewModel.snackBarMessageState.value =
                                context.getString(R.string.no_internet_connection)
                            FunctionHelper.vibrateDevice(context)
                        }
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
                        if (context.isNetworkAvailable()) {
                            viewModel.removeFriendRequest()
                        } else {
                            viewModel.snackBarMessageState.value =
                                context.getString(R.string.no_internet_connection)
                            FunctionHelper.vibrateDevice(context)
                        }
                    }
                )
            }

            StatusWithCurrentUserUiEnum.NotFriends.name -> {
                IconTextButton(
                    modifier = Modifier
                        .weight(1f),
                    buttonImage = Icons.Default.PersonAddAlt1,
                    buttonText = stringResource(R.string.add_friend),
                    onButtonClick = {
                        if (context.isNetworkAvailable()) {
                            viewModel.sendFriendRequest(context)
                        } else {
                            viewModel.snackBarMessageState.value =
                                context.getString(R.string.no_internet_connection)
                            FunctionHelper.vibrateDevice(context)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun IconTextButton(
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
        RequestStatusEnum.Loading -> {
            UserProfileFriendsListLoadingSection()
            isExceptionHandled = false
        }

        RequestStatusEnum.Success -> {
            UserProfileFriendsListSection(
                navigator = navigator,
                friendsList = friendsDetailsState.data!!,
                loggedInUserFirebaseId = viewModel.loggedInUserState.value.firebaseUserId
            )
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    friendsDetailsState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.OtherUserProfileScreen.name,
                    friendsDetailsState.message.toString()
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
private fun HandlePostDetailsState(
    viewModel: OtherUserProfileViewModel,
    navigator: DestinationsNavigator,
    usersBean: UsersBean,
) {
    val postDetailState = viewModel.postDetailsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (postDetailState.status) {
        RequestStatusEnum.Loading -> {
            UserProfilePostLoadingSection()
            isExceptionHandled = false
        }

        RequestStatusEnum.Success -> {
            UserProfilePostSection(
                navigator,
                postDetailsList = postDetailState.data,
                false,
                usersBean
            )
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                if (postDetailState.message == FirebaseErrorCodes.NO_USER_FOUND) {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.something_went_wrong_while_getting_post_details)
                } else {
                    viewModel.snackBarMessageState.value =
                        postDetailState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.OtherUserProfileScreen.name,
                    postDetailState.message.toString()
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
private fun BottomSheetSection(
    modifier: Modifier,
    viewModel: OtherUserProfileViewModel,
    onBottomSheetStateClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (viewModel.statusWithCurrentUserState.value == StatusWithCurrentUserUiEnum.Friends.name) {
            IconTextRowSection(
                imageVector = Icons.Default.PersonRemove,
                text = stringResource(R.string.unfriend_user)
            ) {
                viewModel.unfriendUser()
                onBottomSheetStateClick()
            }
            IconTextRowSection(
                imageVector = Icons.Default.PersonOff,
                text = stringResource(R.string.unfriend_and_block_user)
            ) {
                viewModel.unfriendAndBlockUser()
                onBottomSheetStateClick()
            }
        } else if (viewModel.statusWithCurrentUserState.value != StatusWithCurrentUserUiEnum.BlockedByCurrentUser.name) {
            IconTextRowSection(
                imageVector = Icons.Default.PersonOff,
                text = stringResource(R.string.block_user)
            ) {
                viewModel.blockUser()
                onBottomSheetStateClick()
            }
        }
    }
}

@Composable
private fun HandleSendFriendRequestStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val sendFriendRequestState = viewModel.sendFriendRequestStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (sendFriendRequestState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(id = R.string.sending_friend_request))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentUserUiEnum.RequestedByCurrentUser.name
                viewModel.snackBarMessageState.value =
                    stringResource(id = R.string.friend_request_sent_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (sendFriendRequestState.message == FirebaseErrorCodes.UPDATE_ACCOUNT) {
                    viewModel.snackBarMessageState.value =
                        stringResource(R.string.user_details_updated_since_last_update_please_refresh_the_page_to_see_the_latest_updates)
                } else {
                    viewModel.snackBarMessageState.value =
                        sendFriendRequestState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.OtherUserProfileScreen.name,
                    sendFriendRequestState.message.toString()
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
private fun HandleWithdrawFriendRequestStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val withDrawRequestState = viewModel.withdrawFriendRequestStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (withDrawRequestState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(R.string.removing_friend_request))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentUserUiEnum.NotFriends.name
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.friend_request_removed_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (withDrawRequestState.message == FirebaseErrorCodes.UPDATE_ACCOUNT) {
                    viewModel.snackBarMessageState.value =
                        stringResource(R.string.user_details_updated_since_last_update_please_refresh_the_page_to_see_the_latest_updates)
                } else {
                    viewModel.snackBarMessageState.value =
                        withDrawRequestState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.OtherUserProfileScreen.name,
                    withDrawRequestState.message.toString()
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
private fun HandleAcceptFriendRequestStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val acceptFriendRequestState = viewModel.acceptFriendRequestStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (acceptFriendRequestState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(R.string.accepting_friend_request))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentUserUiEnum.Friends.name
                viewModel.snackBarMessageState.value =
                    stringResource(id = R.string.friend_added_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (acceptFriendRequestState.message == FirebaseErrorCodes.UPDATE_ACCOUNT) {
                    viewModel.snackBarMessageState.value =
                        stringResource(R.string.user_details_updated_since_last_update_please_refresh_the_page_to_see_the_latest_updates)
                } else {
                    viewModel.snackBarMessageState.value =
                        acceptFriendRequestState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.OtherUserProfileScreen.name,
                    acceptFriendRequestState.message.toString()
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
private fun HandleRemoveFriendRequestStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val removeFriendRequestState = viewModel.removeFriendRequestStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (removeFriendRequestState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(id = R.string.removing_friend_request))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentUserUiEnum.NotFriends.name
                viewModel.snackBarMessageState.value =
                    stringResource(id = R.string.friend_request_removed_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (removeFriendRequestState.message == FirebaseErrorCodes.UPDATE_ACCOUNT) {
                    viewModel.snackBarMessageState.value =
                        stringResource(R.string.user_details_updated_since_last_update_please_refresh_the_page_to_see_the_latest_updates)
                } else {
                    viewModel.snackBarMessageState.value =
                        removeFriendRequestState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.OtherUserProfileScreen.name,
                    removeFriendRequestState.message.toString()
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
private fun HandleUnBlockUserStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val unblockUserState = viewModel.unBlockUserStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (unblockUserState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(R.string.unblocking_user))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentUserUiEnum.NotFriends.name
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.user_unblocked_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (unblockUserState.message == FirebaseErrorCodes.UPDATE_ACCOUNT) {
                    viewModel.snackBarMessageState.value =
                        stringResource(R.string.user_details_updated_since_last_update_please_refresh_the_page_to_see_the_latest_updates)
                } else {
                    viewModel.snackBarMessageState.value =
                        unblockUserState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.OtherUserProfileScreen.name,
                    unblockUserState.message.toString()
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
private fun HandleBlockUserStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val blockUserState = viewModel.blockUserStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (blockUserState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(R.string.blocking_user))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentUserUiEnum.BlockedByCurrentUser.name
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.user_blocked_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (blockUserState.message == FirebaseErrorCodes.UPDATE_ACCOUNT) {
                    viewModel.snackBarMessageState.value =
                        stringResource(R.string.user_details_updated_since_last_update_please_refresh_the_page_to_see_the_latest_updates)
                } else {
                    viewModel.snackBarMessageState.value =
                        blockUserState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.OtherUserProfileScreen.name,
                    blockUserState.message.toString()
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
private fun HandleUnfriendUserStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val unfriendUserState = viewModel.unfriendUserStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (unfriendUserState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(R.string.removing_friend))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentUserUiEnum.NotFriends.name
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.friend_removed_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (unfriendUserState.message == FirebaseErrorCodes.UPDATE_ACCOUNT) {
                    viewModel.snackBarMessageState.value =
                        stringResource(R.string.user_details_updated_since_last_update_please_refresh_the_page_to_see_the_latest_updates)
                } else {
                    viewModel.snackBarMessageState.value =
                        unfriendUserState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.OtherUserProfileScreen.name,
                    unfriendUserState.message.toString()
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
private fun HandleUnfriendAndBlockUserStateFlow(
    viewModel: OtherUserProfileViewModel
) {
    val unfriendAndBlockUserState = viewModel.unfriendAndBlockUserStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (unfriendAndBlockUserState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(id = R.string.blocking_user))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.statusWithCurrentUserState.value =
                    StatusWithCurrentUserUiEnum.BlockedByCurrentUser.name
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.friend_removed_and_blocked_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (unfriendAndBlockUserState.message == FirebaseErrorCodes.UPDATE_ACCOUNT) {
                    viewModel.snackBarMessageState.value =
                        stringResource(R.string.user_details_updated_since_last_update_please_refresh_the_page_to_see_the_latest_updates)
                } else {
                    viewModel.snackBarMessageState.value =
                        unfriendAndBlockUserState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.OtherUserProfileScreen.name,
                    unfriendAndBlockUserState.message.toString()
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
private fun HandleUserDetailsState(
    viewModel: OtherUserProfileViewModel,
    homeSharedViewModel: HomeSharedViewModel,
) {
    val getCurrentUserDetailsState = viewModel.userDetailsStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (getCurrentUserDetailsState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(id = R.string.getting_user_details))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                val loggedInUser = getCurrentUserDetailsState.data?.first ?: return
                val otherUser = getCurrentUserDetailsState.data.second
                viewModel.loggedInUserState.value = loggedInUser
                viewModel.otherUserState.value = otherUser
                homeSharedViewModel.usersDetails = viewModel.loggedInUserState.value
                viewModel.statusWithCurrentUserState.value =
                    FunctionHelper.getStatusWithCurrentUser(
                        homeSharedViewModel.usersDetails,
                        viewModel.otherUserState.value
                    )
                // Get the post details of the required user
                viewModel.getPostDetails()
                // Get the friend list of the required user
                viewModel.getFriendListFromIds()

                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (getCurrentUserDetailsState.message == FirebaseErrorCodes.NO_USER_FOUND) {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.something_went_wrong)
                } else {
                    viewModel.snackBarMessageState.value =
                        getCurrentUserDetailsState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                    LoggingHelper.logData(
                        LoggingLevelEnum.Error,
                        ConstantsHelper.ERROR_TAG,
                        ScreenNameEnum.OtherUserProfileScreen.name,
                        getCurrentUserDetailsState.message.toString()
                    )
                }
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

