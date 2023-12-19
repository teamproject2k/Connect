package com.example.connect.presentation.ui.home.user_request

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
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
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LoaderFullScreen
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SearchUi
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.ui.common.UserDetailsSection
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
    val viewModel: UserRequestViewModel = hiltViewModel()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)

    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()

    if (!viewModel.isDataInitialized) {
        viewModel.initializeData(defaultSelectedTab)
    }

    var refreshing by rememberSaveable { mutableStateOf(false) }

    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            refreshing = true
            viewModel.getUserDetails()
            refreshing = false
        })
    if (viewModel.selectedTabIndexState == 0) {
        if (!viewModel.isFriendListFetched) {
            viewModel.getFriendsList(homeSharedViewModel.usersDetails.friendList)
        }
    } else {
        if (!viewModel.isPendingFriendRequestListFetched) {
            viewModel.getPendingFriendRequestList(homeSharedViewModel.usersDetails.receivedFriendRequestList)
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            SearchUi(searchHint = stringResource(id = R.string.search_user_by_name_or_user_id)) {

            }
            RequestTabs(viewModel = viewModel, navigator)
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
private fun HandleGetFriendsListStateFlow(
    viewModel: UserRequestViewModel,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val getFriendsListAsUsersState =
        viewModel.getFriendsListStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (getFriendsListAsUsersState.status) {
        RequestStatusEnum.LOADING -> {
            LoaderFullScreen()
            isExceptionHandled = false
        }

        RequestStatusEnum.EXCEPTION -> {
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

        RequestStatusEnum.SUCCESS -> {
            CreateUi(getFriendsListAsUsersState.data ?: emptyList(), navigator, viewModel)
        }

        RequestStatusEnum.NONE -> {
            // no need to handle this
        }
    }
}

@Composable
private fun HandleGetPendingFriendRequestListStateFlow(
    viewModel: UserRequestViewModel,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val getPendingFriendRequestListAsUsersState =
        viewModel.getPendingFriendRequestListStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (getPendingFriendRequestListAsUsersState.status) {
        RequestStatusEnum.LOADING -> {
            LoaderFullScreen()
            isExceptionHandled = false
        }

        RequestStatusEnum.EXCEPTION -> {
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

        RequestStatusEnum.SUCCESS -> {
            CreateUi(
                getPendingFriendRequestListAsUsersState.data ?: emptyList(),
                navigator,
                viewModel
            )
        }

        RequestStatusEnum.NONE -> {
            // no need to handle this
        }
    }
}

@Composable
private fun CreateUi(
    usersList: List<UsersBean>,
    navigator: DestinationsNavigator,
    viewModel: UserRequestViewModel
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    val filteredUserList = mutableListOf<UsersBean>()
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
    SearchUi(searchHint = stringResource(R.string.search_user_by_name_or_user_id)) {
        searchQuery = it
    }
}


@Composable
fun RequestTabs(viewModel: UserRequestViewModel, navigator: DestinationsNavigator) {
    val itemList = stringArrayResource(id = R.array.friends_tab_list)
    TabRow(selectedTabIndex = viewModel.selectedTabIndexState) {
        itemList.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = viewModel.selectedTabIndexState == index,
                onClick = { viewModel.selectedTabIndexState = index },
                unselectedContentColor = ColorsHelper.gray()
            )
        }
    }
    when (viewModel.selectedTabIndexState) {
        0 -> FriendsList(viewModel.filteredList, navigator)
        1 -> FriendRequestsList(viewModel.filteredList, navigator)
    }
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
                UserRequestListItem(usersBean = user) {
                    navigator.navigate(
                        OtherUserProfileScreenDestination(user)
                    )
                }
            }
        }
    }
}

@Composable
fun FriendRequestsList(friendRequestList: ArrayList<UsersBean>, navigator: DestinationsNavigator) {
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
                UserRequestListItem(usersBean = user) {
                    navigator.navigate(
                        OtherUserProfileScreenDestination(user)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserRequestListItem(usersBean: UsersBean, onClick: () -> Unit) {
    Column {
        UserDetailsSection(
            user = usersBean,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(16.dp)
        )
        Divider()
    }
}

@Composable
fun PosterDetails(
    modifier: Modifier = Modifier,
    userName: String,
    description: String,
    onClick: () -> Unit,
    isCTAVisible: Boolean = false,
    positiveButtonText: String = stringResource(id = R.string.ok),
    negativeButtonText: String = stringResource(id = R.string.cancel),
    onPositiveButtonClick: () -> Unit = {},
    onNegativeButtonClick: () -> Unit = {}
) {
    Row(modifier = modifier.clickable { onClick() }) {

        Image(
            modifier = Modifier
                .size(52.dp)
                .clip(shape = CircleShape),
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = description,
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .wrapContentSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = userName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(text = description, fontSize = 12.sp, color = Color.DarkGray)
            if (isCTAVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(vertical = 4.dp)
                            .clickable {
                                onPositiveButtonClick()
                            }
                            .weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        text = positiveButtonText,
                        fontSize = 14.sp)
                    SpacerWidth12()
                    Text(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.LightGray)
                            .padding(vertical = 4.dp)
                            .clickable {
                                onNegativeButtonClick()
                            }
                            .weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        text = negativeButtonText,
                        fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun HandleGetCurrentUserDetailsStateFlow(
    viewModel: UserRequestViewModel,
    homeSharedViewModel: HomeSharedViewModel
) {
    val getCurrentUserDetailsState = viewModel.userDetailsStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }
    when (getCurrentUserDetailsState.status) {
        RequestStatusEnum.LOADING -> {
            LoaderDialog(loadingText = stringResource(id = R.string.getting_user_details))
            isResponseHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            if (!isResponseHandled) {
                homeSharedViewModel.usersDetails = viewModel.currentUserState.value
                isResponseHandled = true
            }
        }

        RequestStatusEnum.EXCEPTION -> {
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

        RequestStatusEnum.NONE -> {
            // no need to handle this
        }
    }
}