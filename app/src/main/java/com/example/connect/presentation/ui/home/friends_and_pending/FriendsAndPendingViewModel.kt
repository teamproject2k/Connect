package com.example.connect.presentation.ui.home.friends_and_pending

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FriendsAndPendingViewModel @Inject constructor(
    private val getUserDetailsFromIdsFromRemoteUseCase: GetUserDetailsFromIdsFromRemoteUseCase
) : BaseViewModel() {

    lateinit var selectedTabIndexState: MutableIntState

    private val _getFriendsListStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())

    val getFriendsListStateFlow = _getFriendsListStateFlow.asStateFlow()

    private val _getPendingFriendRequestListStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())

    val getPendingFriendRequestListStateFlow = _getPendingFriendRequestListStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")

    var isDataInitialized: Boolean = false


    fun initializeData(defaultSelectedTab: Int) {
        selectedTabIndexState = mutableIntStateOf(defaultSelectedTab)
        isDataInitialized = true
    }

    fun getFriendsList(friendsList: List<String>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getFriendsListStateFlow.value = ResponseState.loading()
                if (friendsList.isEmpty()) {
                    delay(500)
                    _getFriendsListStateFlow.value = ResponseState.success(emptyList())
                } else {
                    _getFriendsListStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase(friendsList)
                }
            }
        }
    }

    fun getPendingFriendRequestList(pendingList: List<String>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getPendingFriendRequestListStateFlow.value = ResponseState.loading()
                if (pendingList.isEmpty()) {
                    delay(500)
                    _getPendingFriendRequestListStateFlow.value =
                        ResponseState.success(emptyList())
                } else {
                    _getPendingFriendRequestListStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase(pendingList)
                }
            }
        }
    }
}