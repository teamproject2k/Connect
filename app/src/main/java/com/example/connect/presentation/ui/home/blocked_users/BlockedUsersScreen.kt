package com.example.connect.presentation.ui.home.blocked_users

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
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.UserListLoading
import com.example.connect.presentation.ui.common.UsersListItem
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
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

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun BlockedListScreen(navigator: DestinationsNavigator) {
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val viewModel: BlockedUsersViewModel = hiltViewModel()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var refreshing by rememberSaveable { mutableStateOf(false) }

    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            refreshing = true
            if (context.isNetworkAvailable()) {
                viewModel.getBlockedUsers(homeSharedViewModel.usersDetails.firebaseUserId)
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.no_internet_connection)
                FunctionHelper.vibrateDevice(context)
            }
            refreshing = false
        })
    Scaffold(topBar = {
        AppTopAppBar(
            title = stringResource(R.string.blocked_users),
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
            HandleGetBlockedUsersState(viewModel, homeSharedViewModel, navigator)
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
            viewModel.getBlockedUsers(homeSharedViewModel.usersDetails.firebaseUserId)
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
    }
}

@Composable
private fun HandleGetBlockedUsersState(
    viewModel: BlockedUsersViewModel,
    homeSharedViewModel: HomeSharedViewModel,
    navigator: DestinationsNavigator
) {
    val blockedUsersState = viewModel.getBlockedUsersStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (blockedUsersState.status) {
        RequestStatusEnum.Loading -> {
            UserListLoading()
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    blockedUsersState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.BlockedUsersScreen.name,
                    blockedUsersState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                if (blockedUsersState.data != null) {
                    homeSharedViewModel.usersDetails = blockedUsersState.data.first
                }
                isResponseHandled = true
            }
            DisplayUsersList(navigator, blockedUsersState.data?.second ?: emptyList())
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
private fun DisplayUsersList(navigator: DestinationsNavigator, blockedUsersList: List<UsersBean>) {
    if (blockedUsersList.isEmpty()) {
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
                    if (blockedUsersList.size == 1) {
                        stringResource(R.string.you_blocked_user, blockedUsersList.size)
                    } else {
                        stringResource(R.string.you_blocked_users, blockedUsersList.size)
                    }
                Text(
                    text = textToShow,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
            items(blockedUsersList, key = {
                it.firebaseUserId
            }) { blockedUser ->
                UsersListItem(blockedUser) {
                    navigator.navigate(OtherUserProfileScreenDestination(blockedUser))
                }
            }
        }
    }
}
