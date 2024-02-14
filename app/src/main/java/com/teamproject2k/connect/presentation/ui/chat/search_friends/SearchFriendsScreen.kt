package com.teamproject2k.connect.presentation.ui.chat.search_friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.presentation.ui.common.AppTopAppBar
import com.teamproject2k.connect.presentation.ui.common.SearchBarAndUserListUiLoading
import com.teamproject2k.connect.presentation.ui.common.SearchUi
import com.teamproject2k.connect.presentation.ui.common.UsersListItem
import com.teamproject2k.connect.presentation.ui.destinations.ChatDetailsScreenDestination
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
import com.teamproject2k.connect.presentation.ui.pull_refresh.PullRefreshIndicator
import com.teamproject2k.connect.presentation.ui.pull_refresh.pullRefresh
import com.teamproject2k.connect.presentation.ui.pull_refresh.rememberPullRefreshState
import com.teamproject2k.connect.presentation.utils.ChatNavGraph
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper.getLowerCaseTextWithOutExtraSpace
import com.teamproject2k.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@ChatNavGraph
@Destination
@Composable
fun SearchFriendsScreen(navigator: DestinationsNavigator, loggedInUser: UsersBean) {
    val viewModel: SearchFriendsViewModel = hiltViewModel()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var refreshing by rememberSaveable { mutableStateOf(false) }
    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            refreshing = true
            if (context.isNetworkAvailable()) {
                viewModel.getAllFriends(loggedInUser.firebaseUserId)
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.no_internet_connection)
                FunctionHelper.vibrateDevice(context)
            }
            refreshing = false
        })
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = { AppTopAppBar(title = stringResource(R.string.search_friends)) }) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            HandleSearchFriendsState(loggedInUser, viewModel, navigator)
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
            viewModel.getAllFriends(loggedInUser.firebaseUserId)
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
    }
}

@Composable
private fun HandleSearchFriendsState(
    loggedInUser: UsersBean,
    viewModel: SearchFriendsViewModel,
    navigator: DestinationsNavigator
) {
    val searchUserState = viewModel.searchFriendsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (searchUserState.status) {
        RequestStatusEnum.Loading -> {
            SearchBarAndUserListUiLoading()
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    searchUserState.message ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.SearchUserScreen.name,
                    searchUserState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            CreateUi(loggedInUser, searchUserState.data ?: emptyList(), navigator)
        }

        RequestStatusEnum.None -> {
            //no need to handle it
        }
    }
}


@Composable
private fun CreateUi(
    loggedInUser: UsersBean,
    usersList: List<UsersBean>,
    navigator: DestinationsNavigator
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    val filteredUserList = mutableListOf<UsersBean>()
    if (searchQuery.isBlank()) {
        filteredUserList.addAll(usersList)
    } else {
        val modifiedQuery = getLowerCaseTextWithOutExtraSpace(searchQuery)
        usersList.forEach {
            val lowerCaseName = getLowerCaseTextWithOutExtraSpace(it.name)
            if (lowerCaseName.contains(modifiedQuery) || it.connectUserId.contains(modifiedQuery)) {
                filteredUserList.add(it)
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        SearchUi(searchHint = stringResource(R.string.search_user_by_name_or_user_id)) {
            searchQuery = it
        }
        if (filteredUserList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = stringResource(R.string.no_user_found))
            }
            return
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredUserList, key = {
                it.firebaseUserId
            }) { user ->
                UsersListItem(usersBean = user) {
                    navigator.navigate(ChatDetailsScreenDestination(loggedInUser, user))
                }
            }
        }
    }
}


