package com.example.connect.presentation.ui.home.friends_and_pending

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.user.GetUserFriendListFromRemoteUseCase
import com.example.connect.domain.useCase.user.GetUserReceivedFriendRequestListFromRemoteUseCase
import com.example.connect.domain.useCase.user.UpdateUserOnLocalUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FriendsAndPendingViewModel @Inject constructor(
    private val getUserReceivedFriendRequestListFromRemoteUseCase: GetUserReceivedFriendRequestListFromRemoteUseCase,
    private val getUserFriendListFromRemoteUseCase: GetUserFriendListFromRemoteUseCase,
    private val updateUserDetailsOnLocalUseCase: UpdateUserOnLocalUseCase
) : BaseViewModel() {

    lateinit var selectedTabIndexState: MutableIntState

    private val _getFriendsListStateFlow: MutableStateFlow<ResponseState<Pair<UsersBean, List<UsersBean>>>> =
        MutableStateFlow(ResponseState.none())

    val getFriendsListStateFlow = _getFriendsListStateFlow.asStateFlow()

    private val _getPendingFriendRequestListStateFlow: MutableStateFlow<ResponseState<Pair<UsersBean, List<UsersBean>>>> =
        MutableStateFlow(ResponseState.none())

    val getPendingFriendRequestListStateFlow = _getPendingFriendRequestListStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")

    var isDataInitialized: Boolean = false


    fun initializeData(defaultSelectedTab: Int) {
        selectedTabIndexState = mutableIntStateOf(defaultSelectedTab)
        isDataInitialized = true
    }

    fun getFriendsList(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getFriendsListStateFlow.value = ResponseState.loading()
                val friendListResponse =
                    getUserFriendListFromRemoteUseCase.invoke(
                        loggedInUserFirebaseId
                    )
                if (friendListResponse.status == RequestStatusEnum.Success) {
                    if (friendListResponse.data != null) {
                        updateUserDetailsOnLocalUseCase.invoke(friendListResponse.data.first)
                    }

                }
                _getFriendsListStateFlow.value = friendListResponse
            }
        }
    }

    fun getPendingFriendRequestList(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getPendingFriendRequestListStateFlow.value = ResponseState.loading()
                val pendingListResponse =
                    getUserReceivedFriendRequestListFromRemoteUseCase.invoke(
                        loggedInUserFirebaseId
                    )
                if (pendingListResponse.status == RequestStatusEnum.Success) {
                    if (pendingListResponse.data != null) {
                        updateUserDetailsOnLocalUseCase.invoke(pendingListResponse.data.first)
                    }

                }
                _getPendingFriendRequestListStateFlow.value = pendingListResponse
            }
        }
    }
}