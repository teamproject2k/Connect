package com.teamproject2k.connect.presentation.ui.home.friends_and_pending

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.presentation.ui.common.ColorsHelper
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.common.SearchBarAndUserListUiLoading
import com.teamproject2k.connect.presentation.ui.common.SearchUi
import com.teamproject2k.connect.presentation.ui.common.UsersListItem
import com.teamproject2k.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
import com.teamproject2k.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.teamproject2k.connect.presentation.ui.pull_refresh.PullRefreshIndicator
import com.teamproject2k.connect.presentation.ui.pull_refresh.pullRefresh
import com.teamproject2k.connect.presentation.ui.pull_refresh.rememberPullRefreshState
import com.teamproject2k.connect.presentation.utils.ConstantsHelper.ERROR_TAG
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.teamproject2k.connect.presentation.utils.HomeNavGraph
import kotlinx.coroutines.launch

@HomeNavGraph
@Destination
@Composable
fun FriendsAndPendingScreen(navigator: DestinationsNavigator, defaultSelectedTab: Int = 0) {
    if (defaultSelectedTab !in 0..1) {
        navigator.popBackStack()
        LoggingHelper.logData(
            LoggingLevelEnum.Error,
            ERROR_TAG,
            ScreenNameEnum.FriendsAndPendingScreen.name,
            "Tab Index $defaultSelectedTab not in range 0..1"
        )
    }
    val viewModel: FriendsAndPendingViewModel = hiltViewModel()
    val context = LocalContext.current
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    if (!viewModel.isDataInitialized) {
        viewModel.initializeData(defaultSelectedTab)
    }
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var refreshing by rememberSaveable { mutableStateOf(false) }

    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            refreshing = true
            if (context.isNetworkAvailable()) {
                if (viewModel.selectedTabIndexState.intValue == 0) {
                    viewModel.getFriendsList(homeSharedViewModel.usersDetails.firebaseUserId)
                } else {
                    viewModel.getPendingFriendRequestList(homeSharedViewModel.usersDetails.firebaseUserId)
                }
                refreshing = false
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.no_internet_connection)
                FunctionHelper.vibrateDevice(context)
            }
        })

    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            FriendsAndPendingTabs(viewModel = viewModel, homeSharedViewModel, navigator = navigator)
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
            viewModel.getFriendsList(homeSharedViewModel.usersDetails.firebaseUserId)
            viewModel.getPendingFriendRequestList(homeSharedViewModel.usersDetails.firebaseUserId)
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FriendsAndPendingTabs(
    viewModel: FriendsAndPendingViewModel,
    homeSharedViewModel: HomeSharedViewModel,
    navigator: DestinationsNavigator
) {
    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()
    val itemList = stringArrayResource(id = R.array.friends_tab_list)
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = viewModel.selectedTabIndexState.intValue) {
            itemList.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = viewModel.selectedTabIndexState.intValue == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    unselectedContentColor = ColorsHelper.gray()
                )
            }
        }
        HorizontalPager(state = pagerState) {
            viewModel.selectedTabIndexState.intValue = it
            when (viewModel.selectedTabIndexState.intValue) {
                0 -> HandleGetFriendsListStateFlow(
                    viewModel = viewModel,
                    homeSharedViewModel,
                    navigator = navigator
                )

                1 -> HandleGetPendingFriendRequestListStateFlow(
                    viewModel,
                    homeSharedViewModel,
                    navigator
                )
            }
        }
    }
}

@Composable
private fun HandleGetFriendsListStateFlow(
    viewModel: FriendsAndPendingViewModel,
    homeSharedViewModel: HomeSharedViewModel,
    navigator: DestinationsNavigator
) {
    val getFriendsListAsUsersState =
        viewModel.getFriendsListStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (getFriendsListAsUsersState.status) {
        RequestStatusEnum.Loading -> {
            SearchBarAndUserListUiLoading()
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    getFriendsListAsUsersState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ERROR_TAG,
                    ScreenNameEnum.FriendsAndPendingScreen.name,
                    getFriendsListAsUsersState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled && getFriendsListAsUsersState.data != null) {
                homeSharedViewModel.usersDetails = getFriendsListAsUsersState.data.first
                isResponseHandled = true
            }
            FriendsListUI(getFriendsListAsUsersState.data?.second ?: emptyList(), navigator)
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
private fun FriendsListUI(
    usersList: List<UserBean>,
    navigator: DestinationsNavigator,
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    val filteredUserList = arrayListOf<UserBean>()
    if (searchQuery.isBlank()) {
        filteredUserList.addAll(usersList)
    } else {
        val modifiedQuery = FunctionHelper.getLowerCaseTextWithOutExtraSpace(searchQuery)
        usersList.forEach {
            val lowerCaseName = FunctionHelper.getLowerCaseTextWithOutExtraSpace(it.name)
            if (lowerCaseName.contains(modifiedQuery) || it.connectUserId.contains(modifiedQuery)
            ) {
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
                Text(text = stringResource(R.string.no_friends_added))
            }
            return
        }
        LazyColumn {
            item {
                val textToShow =
                    if (usersList.size == 1) {
                        stringResource(R.string.you_have_1_friend)
                    } else {
                        stringResource(R.string.you_have_friends, usersList.size)
                    }
                Text(
                    text = textToShow,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
            items(filteredUserList, key = {
                it.firebaseUserId
            }) { user ->
                UsersListItem(user) {
                    navigator.navigate(OtherUserProfileScreenDestination(user))
                }
            }
        }
    }
}

@Composable
private fun HandleGetPendingFriendRequestListStateFlow(
    viewModel: FriendsAndPendingViewModel,
    homeSharedViewModel: HomeSharedViewModel,
    navigator: DestinationsNavigator
) {
    val getPendingFriendRequestListAsUsersState =
        viewModel.getPendingFriendRequestListStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (getPendingFriendRequestListAsUsersState.status) {
        RequestStatusEnum.Loading -> {
            SearchBarAndUserListUiLoading()
            isResponseHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    getPendingFriendRequestListAsUsersState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ERROR_TAG,
                    ScreenNameEnum.FriendsAndPendingScreen.name,
                    getPendingFriendRequestListAsUsersState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            if (getPendingFriendRequestListAsUsersState.data != null && !isResponseHandled) {
                homeSharedViewModel.usersDetails =
                    getPendingFriendRequestListAsUsersState.data.first
                isResponseHandled = true
            }
            PendingListUI(
                getPendingFriendRequestListAsUsersState.data?.second ?: emptyList(),
                navigator,
            )
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
private fun PendingListUI(
    usersList: List<UserBean>,
    navigator: DestinationsNavigator,
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    val filteredUserList = arrayListOf<UserBean>()
    if (searchQuery.isBlank()) {
        filteredUserList.addAll(usersList)
    } else {
        val modifiedQuery = FunctionHelper.getLowerCaseTextWithOutExtraSpace(searchQuery)
        usersList.forEach {
            val lowerCaseName = FunctionHelper.getLowerCaseTextWithOutExtraSpace(it.name)
            if (lowerCaseName.contains(modifiedQuery) || it.connectUserId.contains(modifiedQuery)
            ) {
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
                Text(text = stringResource(R.string.no_pending_requests))
            }
            return
        }
        LazyColumn {
            item {
                val textToShow =
                    if (usersList.size == 1) {
                        stringResource(R.string.you_have_1_pending_request)
                    } else {
                        stringResource(R.string.you_have_pending_requests, usersList.size)
                    }
                Text(
                    text = textToShow,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
            items(filteredUserList, key = {
                it.firebaseUserId
            }) { user ->
                UsersListItem(userBean = user) {
                    navigator.navigate(
                        OtherUserProfileScreenDestination(user)
                    )
                }
            }
        }
    }
}