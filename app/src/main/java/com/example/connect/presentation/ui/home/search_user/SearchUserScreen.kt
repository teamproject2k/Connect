package com.example.connect.presentation.ui.home.search_user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.connect.R
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.common.LoaderFullScreen
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SearchUi
import com.example.connect.presentation.ui.common.UsersListItem
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.FunctionHelper.getLowerCaseTextWithOutExtraSpace
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@HomeNavGraph
@Destination
@Composable
fun SearchScreen(navigator: DestinationsNavigator) {
    val viewModel: SearchUserViewModel = hiltViewModel()
    val sharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)

    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            HandleSearchUserState(viewModel, navigator)
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
        val fetchDetailsNotForList = arrayListOf<String>()
        fetchDetailsNotForList.add(sharedViewModel.usersDetails.firebaseUserId)
        fetchDetailsNotForList.addAll(sharedViewModel.usersDetails.blockedUsersList)
        viewModel.getAllUsers(fetchDetailsNotForList, sharedViewModel.usersDetails.firebaseUserId)
    }
}

@Composable
private fun HandleSearchUserState(
    viewModel: SearchUserViewModel,
    navigator: DestinationsNavigator
) {
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
                if (searchUserState.message == FirebaseErrorCodes.NO_USER_FOUND) {
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
            CreateUi(searchUserState.data ?: emptyList(), navigator)
        }

        RequestStatusEnum.NONE -> {
            //no need to handle it
        }
    }
}

@Composable
private fun CreateUi(
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
            if (lowerCaseName.contains(modifiedQuery) || it.connectUserId.contains(modifiedQuery)
            ) {
                filteredUserList.add(it)
            }
        }
    }
    SearchUi(searchHint = stringResource(R.string.search_user_by_name_or_user_id)) {
        searchQuery = it
    }
    if (filteredUserList.isEmpty()) {
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
            items(filteredUserList) { user ->
                UsersListItem(usersBean = user) {
                    navigator.navigate(OtherUserProfileScreenDestination(user))
                }
            }
        }
    }
}


