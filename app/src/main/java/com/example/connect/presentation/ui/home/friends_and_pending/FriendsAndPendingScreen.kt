package com.example.connect.presentation.ui.home.friends_and_pending

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LoaderFullScreen
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SearchUi
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.ui.common.UserDetailsSection
import com.example.connect.presentation.ui.common.UsersListItem
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.ui.pull_refresh.rememberPullRefreshState
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.ConstantsHelper.ERROR_TAG
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@HomeNavGraph
@Destination
@Composable
fun UserRequestScreen(navigator: DestinationsNavigator, defaultSelectedTab: Int = 0) {
    if (defaultSelectedTab !in 0..1) {
        navigator.popBackStack()
        LoggingHelper.logData(
            LoggingLevelEnum.Error,
            ERROR_TAG,
            "UserRequestScreen",
            "Tab Index $defaultSelectedTab not in range 0..1"
        )
    }
    val viewModel: FriendsAndPendingViewModel = hiltViewModel()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    if (!viewModel.isDataInitialized) {
        viewModel.initializeData(defaultSelectedTab)
    }
    if (viewModel.selectedTabIndexState.intValue == 0) {
        viewModel.getFriendsList(homeSharedViewModel.usersDetails.friendList)
    } else {
        viewModel.getPendingFriendRequestList(homeSharedViewModel.usersDetails.receivedFriendRequestList)
    }
    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()


    var refreshing by rememberSaveable { mutableStateOf(false) }

    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            refreshing = true
            viewModel.getUserDetails()
            refreshing = false
        })

    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            FriendsAndPendingTabs(viewModel = viewModel, navigator = navigator)
        }
//        Box(
//            modifier = Modifier
//                .padding(it)
//                .fillMaxSize()
//                .pullRefresh(pullRefreshState),
//            contentAlignment = Alignment.TopCenter
//        ) {
//            Column(
//                modifier = Modifier.fillMaxSize()
//            ) {
//                // FriendsTabs(filteredUserList, navigator, viewModel)
//                HandleGetFriendsListStateFlow(viewModel, navigator)
//                HandleGetPendingFriendRequestListStateFlow(viewModel, navigator)
//                HandleGetCurrentUserDetailsStateFlow(
//                    viewModel = viewModel,
//                    homeSharedViewModel = homeSharedViewModel
//                )
//            }
//            PullRefreshIndicator(
//                refreshing = refreshing,
//                refreshState = pullRefreshState
//            )
//        }
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
fun FriendsAndPendingTabs(viewModel: FriendsAndPendingViewModel, navigator: DestinationsNavigator) {
    val itemList = stringArrayResource(id = R.array.friends_tab_list)
    TabRow(selectedTabIndex = viewModel.selectedTabIndexState.intValue) {
        itemList.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = viewModel.selectedTabIndexState.intValue == index,
                onClick = { viewModel.selectedTabIndexState.intValue = index },
                unselectedContentColor = ColorsHelper.gray()
            )
        }
    }
    when (viewModel.selectedTabIndexState.intValue) {
        0 -> HandleGetFriendsListStateFlow(viewModel = viewModel, navigator = navigator)
        1 -> HandleGetPendingFriendRequestListStateFlow(viewModel, navigator)
    }
}

@Composable
private fun HandleGetFriendsListStateFlow(
    viewModel: FriendsAndPendingViewModel,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val getFriendsListAsUsersState =
        viewModel.getFriendsListStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (getFriendsListAsUsersState.status) {
        RequestStatusEnum.Loading -> {
            LoaderFullScreen()
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                if (getFriendsListAsUsersState.message == FirebaseErrorCodes.NO_USER_FOUND) {
                    context.showToast(stringResource(id = R.string.some_error_occurred_please_login_again))
                    (LocalActivity.current as BaseActivity).logout()
                } else {
                    viewModel.snackBarMessageState.value =
                        getFriendsListAsUsersState.message
                            ?: stringResource(id = R.string.some_error_occurred)
                }
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            FriendsListUI(getFriendsListAsUsersState.data ?: emptyList(), navigator)
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}


@Composable
private fun FriendsListUI(
    usersList: List<UsersBean>,
    navigator: DestinationsNavigator,
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    val filteredUserList = arrayListOf<UsersBean>()
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
    Column {
        SearchUi(searchHint = stringResource(R.string.search_user_by_name_or_user_id)) {
            searchQuery = it
        }
    }
    FriendsList(friendList = filteredUserList, navigator = navigator)
}


@Composable
fun FriendsList(friendList: ArrayList<UsersBean>, navigator: DestinationsNavigator) {
    if (friendList.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.no_friends_added))
        }
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(friendList) { user ->
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
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val getPendingFriendRequestListAsUsersState =
        viewModel.getPendingFriendRequestListStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (getPendingFriendRequestListAsUsersState.status) {
        RequestStatusEnum.Loading -> {
            LoaderFullScreen()
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                if (getPendingFriendRequestListAsUsersState.message == FirebaseErrorCodes.NO_USER_FOUND) {
                    context.showToast(stringResource(id = R.string.some_error_occurred_please_login_again))
                    (LocalActivity.current as BaseActivity).logout()
                } else {
                    viewModel.snackBarMessageState.value =
                        getPendingFriendRequestListAsUsersState.message
                            ?: stringResource(id = R.string.some_error_occurred)
                }
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            PendingListUI(
                getPendingFriendRequestListAsUsersState.data ?: emptyList(),
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
    usersList: List<UsersBean>,
    navigator: DestinationsNavigator,
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    val filteredUserList = arrayListOf<UsersBean>()
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
    Column {
        SearchUi(searchHint = stringResource(R.string.search_user_by_name_or_user_id)) {
            searchQuery = it
        }
    }
    PendingList(friendRequestList = filteredUserList, navigator = navigator)
}


@Composable
fun PendingList(friendRequestList: ArrayList<UsersBean>, navigator: DestinationsNavigator) {
    if (friendRequestList.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.no_pending_requests))
        }
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(friendRequestList) { user ->
                UsersListItem(usersBean = user) {
                    navigator.navigate(
                        OtherUserProfileScreenDestination(user)
                    )
                }
            }
        }
    }
}

@Composable
fun HandleGetCurrentUserDetailsStateFlow(
    viewModel: FriendsAndPendingViewModel,
    homeSharedViewModel: HomeSharedViewModel
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
                homeSharedViewModel.usersDetails = viewModel.currentUserState.value
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    getCurrentUserDetailsState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "OtherUserProfileScreen",
                    getCurrentUserDetailsState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}