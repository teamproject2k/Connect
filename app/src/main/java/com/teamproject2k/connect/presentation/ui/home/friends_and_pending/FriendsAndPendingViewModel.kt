package com.teamproject2k.connect.presentation.ui.home.friends_and_pending

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.user.GetLoggedInUserFriendListFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.GetLoggedInUserReceivedFriendRequestListFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateUserOnLocalUseCase
import com.teamproject2k.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FriendsAndPendingViewModel @Inject constructor(
    private val getLoggedInUserReceivedFriendRequestListFromRemoteUseCase: GetLoggedInUserReceivedFriendRequestListFromRemoteUseCase,
    private val getLoggedInUserFriendListFromRemoteUseCase: GetLoggedInUserFriendListFromRemoteUseCase,
    private val updateUserDetailsOnLocalUseCase: UpdateUserOnLocalUseCase
) : BaseViewModel() {

    lateinit var selectedTabIndexState: MutableIntState

    var isDataInitialized: Boolean = false

    val snackBarMessageState = mutableStateOf("")

    private val _getFriendsListStateFlow: MutableStateFlow<ResponseState<Pair<UserBean, List<UserBean>>>> =
        MutableStateFlow(ResponseState.none())
    val getFriendsListStateFlow = _getFriendsListStateFlow.asStateFlow()

    private val _getPendingFriendRequestListStateFlow: MutableStateFlow<ResponseState<Pair<UserBean, List<UserBean>>>> =
        MutableStateFlow(ResponseState.none())
    val getPendingFriendRequestListStateFlow = _getPendingFriendRequestListStateFlow.asStateFlow()

    /**
     * Initializes the data for the view model.
     *
     * @param defaultSelectedTab The default index of the selected tab.
     */
    fun initializeData(defaultSelectedTab: Int) {
        selectedTabIndexState = mutableIntStateOf(defaultSelectedTab)
        isDataInitialized = true
    }

    /**
     * Retrieves the list of friends of the logged-in user.
     * This function fetches the list both remotely and locally and updates the state flow accordingly.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     */
    fun getFriendsList(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getFriendsListStateFlow.value = ResponseState.loading()
                val friendListResponse =
                    getLoggedInUserFriendListFromRemoteUseCase.invoke(
                        loggedInUserFirebaseId
                    )
                if (friendListResponse.status == RequestStatusEnum.Success && friendListResponse.data != null) {
                    updateUserDetailsOnLocalUseCase.invoke(friendListResponse.data.first)
                }
                _getFriendsListStateFlow.value = friendListResponse
            }
        }
    }

    /**
     * Retrieves the list of pending friend requests received by the logged-in user.
     * This function fetches the list both remotely and locally and updates the state flow accordingly.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     */
    fun getPendingFriendRequestList(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getPendingFriendRequestListStateFlow.value = ResponseState.loading()
                val pendingListResponse =
                    getLoggedInUserReceivedFriendRequestListFromRemoteUseCase.invoke(
                        loggedInUserFirebaseId
                    )
                if (pendingListResponse.status == RequestStatusEnum.Success && pendingListResponse.data != null) {
                    updateUserDetailsOnLocalUseCase.invoke(pendingListResponse.data.first)
                }
                _getPendingFriendRequestListStateFlow.value = pendingListResponse
            }
        }
    }
}