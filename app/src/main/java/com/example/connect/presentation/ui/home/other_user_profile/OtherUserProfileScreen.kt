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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.ui.auth.AuthenticationActivity
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SpacerHeight12
import com.example.connect.presentation.ui.common.SpacerHeight24
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

@HomeNavGraph
@Destination
@Composable
fun OtherUserProfileScreen(navigator: DestinationsNavigator, userDetails: UsersBean) {
    val viewModel: OtherUserProfileViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()

    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)

    Column(modifier = Modifier.fillMaxSize()) {
        ProfileScreen(
            userDetails = homeSharedViewModel.usersDetails,
            viewModel = viewModel
        )
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
}

@Composable
private fun ProfileScreen(
    userDetails: UsersBean,
    viewModel: OtherUserProfileViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageSection(userDetails)
        SpacerHeight12()
        UserProfileUserInfoSection(userDetails)
        SpacerHeight24()
        ActionButtonsSection()
        SpacerHeight24()
        HandleFriendListSection(viewModel = viewModel)
        SpacerHeight24()
        HandlePostSection(viewModel)
    }
}

@Composable
private fun ImageSection(
    userDetails: UsersBean
) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (
            coverImageRef, profileImageRef
        ) = createRefs()
        AsyncImage(
            model = userDetails.coverPhoto,
            contentDescription = stringResource(R.string.cover_photo),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray)
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
    }
}

@Composable
private fun ActionButtonsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CustomIconButton(Icons.Default.PersonAddAlt1, stringResource(R.string.add_friend))
        CustomIconButton(Icons.Default.Block, stringResource(R.string.block_user))
    }
}

@Composable
fun CustomIconButton(buttonImage: ImageVector, buttonText: String) {
    Button(
        onClick = { },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                imageVector = buttonImage,
                contentDescription = buttonText,
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onPrimary
                )
            )
            Text(text = buttonText)
        }
    }
}

@Composable
private fun HandleFriendListSection(viewModel: OtherUserProfileViewModel) {
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
            UserProfileFriendsListSection(friendsList = friendsDetailsState.data!!)
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
private fun HandlePostSection(viewModel: OtherUserProfileViewModel) {
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
            UserProfilePostSection(postDetailsList = postDetailState.data!!.reversed())
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

