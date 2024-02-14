package com.teamproject2k.connect.presentation.ui.home.requested_users

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.presentation.ui.common.AppTopAppBar
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.common.UserListLoading
import com.teamproject2k.connect.presentation.ui.common.UsersListItem
import com.teamproject2k.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
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
fun RequestedListScreen(navigator: DestinationsNavigator) {
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val viewModel: RequestedUsersViewModel = hiltViewModel()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var refreshing by rememberSaveable { mutableStateOf(false) }

    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            refreshing = true
            if (context.isNetworkAvailable()) {
                viewModel.getRequestedUsers(homeSharedViewModel.usersDetails.firebaseUserId)
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.no_internet_connection)
                FunctionHelper.vibrateDevice(context)
            }
            refreshing = false
        })
    Scaffold(topBar = {
        AppTopAppBar(
            title = stringResource(R.string.requested_users),
            showNavigationIcon = true,
            onNavigationIconClick = { navigator.popBackStack() })
    }, snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            HandleGetRequestedUsersState(viewModel, homeSharedViewModel, navigator)
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
    LaunchedEffect(Unit) {
        if (context.isNetworkAvailable()) {
            viewModel.getRequestedUsers(homeSharedViewModel.usersDetails.firebaseUserId)
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
    }
}

@Composable
fun HandleGetRequestedUsersState(
    viewModel: RequestedUsersViewModel,
    homeSharedViewModel: HomeSharedViewModel,
    navigator: DestinationsNavigator
) {
    val requestedUsersState = viewModel.getRequestedUsersStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (requestedUsersState.status) {
        RequestStatusEnum.Loading -> {
            UserListLoading()
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    requestedUsersState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.RequestedUsersScreen.name,
                    requestedUsersState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled && requestedUsersState.data != null) {
                homeSharedViewModel.usersDetails = requestedUsersState.data.first
                isResponseHandled = true
            }
            DisplayUsersList(navigator, requestedUsersState.data?.second ?: emptyList())
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
private fun DisplayUsersList(
    navigator: DestinationsNavigator,
    requestedUsersList: List<UsersBean>
) {
    if (requestedUsersList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(id = R.string.no_user_found))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                val textToShow =
                    if (requestedUsersList.size == 1) {
                        stringResource(R.string.you_requested_1_user)
                    } else {
                        stringResource(R.string.you_requested_users, requestedUsersList.size)
                    }
                Text(
                    text = textToShow,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
            items(requestedUsersList, key = {
                it.firebaseUserId
            }) { requestedUser ->
                UsersListItem(requestedUser) {
                    navigator.navigate(OtherUserProfileScreenDestination(requestedUser))
                }
            }
        }
    }
}
