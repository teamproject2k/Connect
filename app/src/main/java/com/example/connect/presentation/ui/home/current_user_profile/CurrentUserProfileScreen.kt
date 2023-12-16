package com.example.connect.presentation.ui.home.current_user_profile

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.common.ErrorCodes
import com.example.connect.common.LoggingHelper
import com.example.connect.common.LoggingLevelEnum
import com.example.connect.common.RequestStatusEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.auth.AuthenticationActivity
import com.example.connect.presentation.ui.common.BottomSheetItem
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.ColorsHelper.warning
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SpacerHeight12
import com.example.connect.presentation.ui.common.SpacerHeight24
import com.example.connect.presentation.ui.common.SpacerWidth6
import com.example.connect.presentation.ui.common.TitleMessageIconOkCancelDialog
import com.example.connect.presentation.ui.common.UserProfileFriendsListLoadingSection
import com.example.connect.presentation.ui.common.UserProfileFriendsListSection
import com.example.connect.presentation.ui.common.UserProfilePostLoadingSection
import com.example.connect.presentation.ui.common.UserProfilePostSection
import com.example.connect.presentation.ui.common.UserProfileUserInfoSection
import com.example.connect.presentation.ui.destinations.EditProfileScreenDestination
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
fun CurrentUserProfileScreen(navigator: DestinationsNavigator) {
    val viewModel: CurrentUserProfileViewModel = hiltViewModel()
    val sharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember {
        mutableStateOf(false)
    }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            ProfileScreen(sharedViewModel.usersDetails, viewModel, navigator) {
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
        viewModel.getFriendListFromIds(sharedViewModel.usersDetails.friendList)
        viewModel.getPostDetails()
    }
}

@Composable
private fun BottomSheetSection(
    modifier: Modifier,
    onBottomSheetStateChange: (showSheet: Boolean) -> Unit
) {
    val currentActivity = LocalActivity.current as BaseActivity
    var showLogoutDialog by remember {
        mutableStateOf(false)
    }
    Column(modifier = modifier.fillMaxWidth()) {
        BottomSheetItem(
            imageVector = Icons.Default.Settings,
            text = stringResource(R.string.settings)
        ) {
            onBottomSheetStateChange(false)
            // TODO: navigate to settings screen
        }
        BottomSheetItem(
            imageVector = Icons.Default.Logout,
            text = stringResource(id = R.string.logout)
        ) {
            showLogoutDialog = true
            onBottomSheetStateChange(false)
        }
    }
    if (showLogoutDialog) {
        TitleMessageIconOkCancelDialog(title = stringResource(id = R.string.logout),
            subTitle = stringResource(id = R.string.do_you_really_want_to_logout_from_the_app),
            imageVector = Icons.Default.Warning,
            iconTint = warning(),
            onCancel = { showLogoutDialog = false }) {
            currentActivity.logout()
            showLogoutDialog = false
        }
    }
}

@Composable
private fun ProfileScreen(
    userDetails: UsersBean,
    viewModel: CurrentUserProfileViewModel,
    navigator: DestinationsNavigator,
    onOptionsMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageSection(userDetails, navigator, onOptionsMenuClick)
        SpacerHeight12()
        UserProfileUserInfoSection(userDetails)
        SpacerHeight24()
        HandleFriendListSection(viewModel = viewModel, navigator)
        SpacerHeight24()
        HandlePostSection(viewModel, navigator)
    }
}

@Composable
private fun ImageSection(
    userDetails: UsersBean, navigator: DestinationsNavigator, onOptionsMenuClick: () -> Unit
) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (
            coverImageRef, profileImageRef, editImageRef, moreOptionsRef
        ) = createRefs()
        AsyncImage(
            model = userDetails.coverPhoto,
            contentDescription = stringResource(R.string.cover_photo),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(ColorsHelper.lightGray())
                .constrainAs(coverImageRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            contentScale = ContentScale.Crop,
        )
        AsyncImage(
            model = userDetails.profilePhoto,
            contentDescription = stringResource(R.string.profile_image),
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .border(4.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                .constrainAs(profileImageRef) {
                    start.linkTo(parent.start, 16.dp)
                    top.linkTo(coverImageRef.bottom)
                    bottom.linkTo(coverImageRef.bottom)
                },
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user),
            placeholder = painterResource(id = R.drawable.ic_default_user)
        )

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
    }
}

@Composable
fun ImageTextItem(imageVector: ImageVector, text: String, fontWeight: FontWeight? = null) {
    Row(horizontalArrangement = Arrangement.Center) {
        Image(
            imageVector = imageVector,
            contentDescription = text,
            modifier = Modifier.size(16.dp)
        )
        SpacerWidth6()
        Text(text = text, fontSize = 12.sp, fontWeight = fontWeight)
    }
}

@Composable
private fun HandleFriendListSection(
    viewModel: CurrentUserProfileViewModel,
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
            UserProfileFriendsListSection(navigator, friendsList = friendsDetailsState.data!!, true)
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    friendsDetailsState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "UserProfileScreen",
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
    viewModel: CurrentUserProfileViewModel,
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
            UserProfilePostSection(
                navigator,
                postDetailsList = postDetailState.data!!.reversed(),
                true
            )
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
                    "UserProfileScreen",
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

