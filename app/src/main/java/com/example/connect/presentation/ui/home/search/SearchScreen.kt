package com.example.connect.presentation.ui.home.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.common.ErrorCodes
import com.example.connect.common.RequestStatusEnum
import com.example.connect.domain.enums.StatusWithCurrentEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LoaderFullScreen
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch


@HomeNavGraph
@Destination
@Composable
fun SearchScreen() {
    val viewModel: SearchViewModel = hiltViewModel()
    val sharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    if (!viewModel.isUserDetailsFetched) {
        val fetchDetailsNotForList = arrayListOf<String>()
        fetchDetailsNotForList.add(sharedViewModel.usersBean.firebaseUserId)
        fetchDetailsNotForList.addAll(sharedViewModel.usersBean.blockedUsersList)
        viewModel.getAllUsers(fetchDetailsNotForList, sharedViewModel.usersBean.firebaseUserId)
    }
    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            HandleSearchUserState(viewModel, sharedViewModel.usersBean)
            HandleSendFriendRequestState(viewModel)
            HandleAcceptRequestState(viewModel = viewModel)
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
}

@Composable
fun HandleSendFriendRequestState(viewModel: SearchViewModel) {
    val sendFriendRequestState = viewModel.sendFriendRequestStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (sendFriendRequestState.status) {
        RequestStatusEnum.LOADING -> {
            LoaderDialog(loadingText = stringResource(R.string.sending_friend_request))
            isResponseHandled = false
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value = sendFriendRequestState.message
                    ?: stringResource(id = R.string.some_error_occurred)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.SUCCESS -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.friend_request_sent_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.NONE -> {

        }
    }
}

@Composable
fun HandleAcceptRequestState(viewModel: SearchViewModel) {
    val acceptRequestState = viewModel.acceptFriendRequestStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (acceptRequestState.status) {
        RequestStatusEnum.LOADING -> {
            LoaderDialog(loadingText = stringResource(R.string.adding_friend))
            isResponseHandled = false
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value = acceptRequestState.message
                    ?: stringResource(id = R.string.some_error_occurred)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.SUCCESS -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.friend_added_successfully)
                isResponseHandled = true
            }
        }

        RequestStatusEnum.NONE -> {

        }
    }
}

@Composable
private fun HandleSearchUserState(viewModel: SearchViewModel, currentUsersBean: UsersBean) {
    val context = LocalContext.current
    val searchUserState = viewModel.searchUserStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (searchUserState.status) {
        RequestStatusEnum.LOADING -> {
            LoaderFullScreen()
            isExceptionHandled = false
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                if (searchUserState.message == ErrorCodes.NoUserFound) {
                    context.showToast(stringResource(id = R.string.some_error_occurred_please_login_again))
                    (LocalActivity.current as BaseActivity).logout()
                } else {
                    viewModel.snackBarMessageState.value =
                        searchUserState.message ?: stringResource(id = R.string.some_error_occurred)
                }
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.SUCCESS -> {
            CreateUi(searchUserState.data ?: emptyList(), currentUsersBean, viewModel)
        }

        RequestStatusEnum.NONE -> {
            //no need to handle it
        }
    }
}

@Composable
private fun CreateUi(
    usersList: List<UsersBean>,
    currentUsersBean: UsersBean,
    viewModel: SearchViewModel
) {
    val showCancelFriendRequestAlertDialog by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    if (usersList.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.no_user_found))
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(usersList) { user ->
                UserListItem(usersBean = user, currentUsersBean) { status ->
                    when (status) {
                        StatusWithCurrentEnum.NotFriends.name -> {
                            viewModel.sendFriendRequest(
                                currentUsersBean,
                                user
                            )
                        }

                        StatusWithCurrentEnum.RequestedByOtherUser.name -> {
                            viewModel.acceptFriendRequest(
                                currentUsersBean,
                                user
                            )
                        }

                        StatusWithCurrentEnum.RequestedByCurrentUser.name -> {
                            viewModel.snackBarMessageState.value =
                                context.getString(R.string.friend_request_already_sent)
                        }

                        StatusWithCurrentEnum.Friends.name -> {
                            viewModel.snackBarMessageState.value =
                                context.getString(R.string.already_added_as_friends)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(
    usersBean: UsersBean,
    currentUsersBean: UsersBean,
    onClick: (String) -> Unit
) {
    val statusWithCurrentUser = getStatusAndDisplayIconWithCurrentUser(currentUsersBean, usersBean)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable {

                }
                .padding(16.dp)
        ) {
            UserDetailsSection(
                user = usersBean,
                modifier = Modifier
                    .weight(1f)
            )
            IconButton(onClick = {
                onClick(statusWithCurrentUser.first)
            }) {
                Icon(
                    imageVector = statusWithCurrentUser.second,
                    contentDescription = statusWithCurrentUser.first
                )
            }
        }
        Divider()
    }
}


private fun getStatusAndDisplayIconWithCurrentUser(
    currentUsersBean: UsersBean,
    requiredUsersBean: UsersBean
): Pair<String, ImageVector> {
    val statusAndDisplayIcon = when {
        currentUsersBean.friendList.contains(requiredUsersBean.firebaseUserId) -> {
            Pair(
                StatusWithCurrentEnum.Friends.name,
                Icons.Default.CheckCircleOutline
            )
        }

        currentUsersBean.blockedUsersList.contains(requiredUsersBean.firebaseUserId) -> {
            Pair(StatusWithCurrentEnum.Blocked.name, Icons.Default.Block)
        }

        currentUsersBean.receivedFriendRequestList.contains(requiredUsersBean.firebaseUserId) -> {
            Pair(StatusWithCurrentEnum.RequestedByOtherUser.name, Icons.Default.ArrowCircleDown)
        }

        currentUsersBean.requestedFriendRequestList.contains(requiredUsersBean.firebaseUserId) -> {
            Pair(StatusWithCurrentEnum.RequestedByCurrentUser.name, Icons.Default.AccessTime)
        }

        else -> {
            Pair(StatusWithCurrentEnum.NotFriends.name, Icons.Default.PersonAddAlt1)
        }
    }
    return statusAndDisplayIcon
}
