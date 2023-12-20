package com.example.connect.presentation.ui.home.requested_users

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.LoaderFullScreen
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.UsersListItem
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun RequestedListScreen(navigator: DestinationsNavigator) {
    Scaffold(topBar = {
        AppTopAppBar(title = stringResource(R.string.requested_users))
    }) {

        val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)

        val requestedUsers = homeSharedViewModel.usersDetails.requestedFriendRequestList
        val viewModel: RequestedUsersViewModel = hiltViewModel()

        val snackBarHostState = SnackbarHostState()
        val coroutineScope = rememberCoroutineScope()

        if (!viewModel.isRequestedListFetched) {
            viewModel.getRequestedUsers(requestedUsers)
        }

        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            HandleGetRequestedUsersState(viewModel, navigator)
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
}

@Composable
fun HandleGetRequestedUsersState(
    viewModel: RequestedUsersViewModel,
    navigator: DestinationsNavigator
) {
    val requestedUsersState = viewModel.getRequestedUsersStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (requestedUsersState.status) {
        RequestStatusEnum.LOADING -> {
            LoaderFullScreen()
            isExceptionHandled = false
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    requestedUsersState.message
                        ?: stringResource(id = R.string.some_error_occurred)
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.SUCCESS -> {
            requestedUsersState.data?.let { DisplayUsersList(navigator, it) }
        }

        RequestStatusEnum.NONE -> {
            // no need to handle this
        }
    }
}

@Composable
fun DisplayUsersList(navigator: DestinationsNavigator, requestedUsersList: List<UsersBean>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(requestedUsersList.size) {
            UsersListItem(requestedUsersList[it]) {
                navigator.navigate(OtherUserProfileScreenDestination(requestedUsersList[it]))
            }
        }
    }
}
