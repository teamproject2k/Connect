package com.example.connect.presentation.ui.home.current_user_profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.ExpandedImage
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SpacerHeight12
import com.example.connect.presentation.ui.common.SpacerHeight24
import com.example.connect.presentation.ui.common.UserProfileFriendsListLoadingSection
import com.example.connect.presentation.ui.common.UserProfileFriendsListSection
import com.example.connect.presentation.ui.common.UserProfilePostLoadingSection
import com.example.connect.presentation.ui.common.UserProfilePostSection
import com.example.connect.presentation.ui.common.UserProfileUserInfoSection
import com.example.connect.presentation.ui.destinations.EditProfileScreenDestination
import com.example.connect.presentation.ui.destinations.SettingsAndPrivacyScreenDestination
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.ui.pull_refresh.PullRefreshIndicator
import com.example.connect.presentation.ui.pull_refresh.pullRefresh
import com.example.connect.presentation.ui.pull_refresh.rememberPullRefreshState
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@HomeNavGraph
@Destination
@Composable
fun CurrentUserProfileScreen(navigator: DestinationsNavigator) {
    val viewModel: CurrentUserProfileViewModel = hiltViewModel()
    val sharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (!viewModel.isDataInitialized) {
        viewModel.init(sharedViewModel.usersDetails)
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
                .padding(it)
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            ProfileScreen(viewModel.loggedInUserDetailsState.value, viewModel, navigator)
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
    LaunchedEffect(key1 = true) {
        if (context.isNetworkAvailable()) {
            viewModel.getFriendListFromIds()
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
        viewModel.getPostDetails(false)
    }
    HandleUserDetailsState(viewModel = viewModel, homeSharedViewModel = sharedViewModel)
}

@Composable
private fun ProfileScreen(
    loggedInUserDetails: UsersBean,
    viewModel: CurrentUserProfileViewModel,
    navigator: DestinationsNavigator
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageSection(loggedInUserDetails, navigator)
        SpacerHeight12()
        UserProfileUserInfoSection(loggedInUserDetails, loggedInUserDetails.firebaseUserId)
        SpacerHeight24()
        HandleFriendListSectionState(viewModel = viewModel, loggedInUserDetails, navigator)
        HandlePostListSectionState(viewModel, navigator, loggedInUserDetails)
    }
}

@Composable
private fun ImageSection(loggedInUserDetails: UsersBean, navigator: DestinationsNavigator) {
    var isProfilePhotoExpanded by remember {
        mutableStateOf(false)
    }
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (
            coverImageRef, profileImageRef, editImageRef, moreOptionsRef
        ) = createRefs()
        AsyncImage(
            model = loggedInUserDetails.coverPhoto,
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
            model = loggedInUserDetails.profilePhoto,
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
                    isProfilePhotoExpanded =
                        loggedInUserDetails.profilePhoto != null && !isProfilePhotoExpanded
                },
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user),
            placeholder = painterResource(id = R.drawable.ic_default_user)
        )

        IconButton(onClick = {
            navigator.navigate(SettingsAndPrivacyScreenDestination)
        },
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier.constrainAs(moreOptionsRef) {
                top.linkTo(coverImageRef.top, 16.dp)
                end.linkTo(coverImageRef.end, 16.dp)
            }
        ) {
            Image(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings_and_privacy)
            )
        }

        IconButton(
            onClick = {
                navigator.navigate(EditProfileScreenDestination())
            },
            modifier = Modifier.constrainAs(editImageRef) {
                top.linkTo(coverImageRef.bottom, 16.dp)
                end.linkTo(parent.end, 16.dp)
            },
        ) {
            Image(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit_profile)
            )
        }
        if (isProfilePhotoExpanded) {
            ExpandedImage(imageUrl = loggedInUserDetails.profilePhoto) {
                isProfilePhotoExpanded = false
            }
        }
    }
}

@Composable
private fun HandleFriendListSectionState(
    viewModel: CurrentUserProfileViewModel,
    userDetails: UsersBean,
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
                navigator,
                friendsList = friendsDetailsState.data,
                loggedInUserFirebaseId = userDetails.firebaseUserId,
                isLoggedInUser = true
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
                    ScreenNameEnum.CurrentUserProfileScreen.name,
                    friendsDetailsState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle it
        }
    }
}

@Composable
private fun HandlePostListSectionState(
    viewModel: CurrentUserProfileViewModel,
    navigator: DestinationsNavigator,
    userDetails: UsersBean,
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
                true,
                userDetails
            )
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                if (postDetailState.message == FirebaseErrorCodes.NO_USER_FOUND) {
                    viewModel.snackBarMessageState.value =
                        stringResource(R.string.something_went_wrong_while_getting_post_details)
                } else {
                    viewModel.snackBarMessageState.value =
                        postDetailState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.CurrentUserProfileScreen.name,
                    postDetailState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.None -> {
            //no need to handle it
        }
    }
}


@Composable
fun HandleUserDetailsState(
    viewModel: CurrentUserProfileViewModel,
    homeSharedViewModel: HomeSharedViewModel
) {
    val userDetailsState = viewModel.loggedInUserDetailsStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    when (userDetailsState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(id = R.string.getting_user_details))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                homeSharedViewModel.usersDetails = userDetailsState.data ?: return
                viewModel.loggedInUserDetailsState.value = homeSharedViewModel.usersDetails
                if (context.isNetworkAvailable()) {
                    viewModel.getPostDetails(true)
                    viewModel.getFriendListFromIds()
                } else {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.no_internet_connection)
                    FunctionHelper.vibrateDevice(context)
                }
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (userDetailsState.message == FirebaseErrorCodes.NO_USER_FOUND) {
                    viewModel.snackBarMessageState.value =
                        stringResource(R.string.some_error_occurred_please_login_again)
                    (LocalActivity.current as BaseActivity).logout()
                } else {
                    viewModel.snackBarMessageState.value =
                        userDetailsState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

