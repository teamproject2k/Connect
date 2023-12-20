package com.example.connect.presentation.ui.home.user_request

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromRemoteUseCase
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class UserRequestViewModel @Inject constructor(
    private val getUserDetailsFromIdsFromRemoteUseCase: GetUserDetailsFromIdsFromRemoteUseCase,
    private val getUserDetailsFromRemoteUseCase: GetUserDetailsFromRemoteUseCase,
    private val addUserToDbUseCase: AddUserToDbUseCase
) :
    BaseViewModel() {
    lateinit var currentUserState: MutableState<UsersBean>
    var selectedTabIndexState by mutableIntStateOf(0)

    private val _getFriendsListStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val getFriendsListStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _getFriendsListStateFlow

    private val _getPendingFriendRequestListStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val getPendingFriendRequestListStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _getPendingFriendRequestListStateFlow

    val snackBarMessageState = mutableStateOf("")

    var isDataInitialized: Boolean = false

    var isFriendListFetched: Boolean = false

    var isPendingFriendRequestListFetched: Boolean = false

    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val userDetailsStateFlow: StateFlow<ResponseState<Nothing>> get() = _userDetailsStateFlow

    val filteredList: ArrayList<UsersBean> = arrayListOf()

    fun initializeData(defaultSelectedTab: Int) {
        selectedTabIndexState = defaultSelectedTab
        isDataInitialized = true
    }

    /**
     * Gets the list of friends.
     *
     * @param friendsList The list of friends IDs.
     */
    fun getFriendsList(friendsList: List<String>) {
        // Set the flag to true to indicate that the friends list has been fetched.
        isFriendListFetched = true

        // Launch a coroutine to fetch the friends list from the remote server.
        viewModelScope.launch {
            // Perform the network operation in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the state of the friends list state flow to loading.
                _getFriendsListStateFlow.value = ResponseState.loading()

                // Check if the friends list is empty.
                if (friendsList.isEmpty()) {
                    // If the friends list is empty, set the state of the friends list state flow to success with an empty list.
                    _getFriendsListStateFlow.value = ResponseState.success(emptyList())
                } else {
                    // If the friends list is not empty, fetch the user details from the remote server using the getUserDetailsFromIdsFromRemoteUseCase.
                    _getFriendsListStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase.invoke(friendsList)
                }
            }
        }
    }

    /**
     * Gets the list of pending friend requests.
     *
     * @param pendingList The list of pending friend request IDs.
     */
    fun getPendingFriendRequestList(pendingList: List<String>) {
        // Set the flag to true to indicate that the pending friend request list has been fetched.
        isPendingFriendRequestListFetched = true

        // Launch a coroutine to fetch the pending friend request list.
        viewModelScope.launch {
            // Perform the network request in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the state of the pending friend request list state flow to loading.
                _getPendingFriendRequestListStateFlow.value = ResponseState.loading()

                // Check if the pending friend request list is empty.
                if (pendingList.isEmpty()) {
                    // If the list is empty, set the state of the pending friend request list state flow to success with an empty list.
                    _getPendingFriendRequestListStateFlow.value =
                        ResponseState.success(emptyList())
                } else {
                    // If the list is not empty, set the state of the pending friend request list state flow to success with the list of user details.
                    _getPendingFriendRequestListStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase.invoke(pendingList)
                }
            }
        }
    }

    /**
     * Gets the user details from the remote server.
     */
    fun getUserDetails() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Perform the network request in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the user details state flow to loading.
                _userDetailsStateFlow.value = ResponseState.loading()

                // Get the current user's Firebase ID.
                val fireBaseId = fireBaseAuth.currentUser?.uid

                // If the Firebase ID is not null, then make the network request to get the user details.
                if (fireBaseId != null) {
                    // Get the user details from the remote server.
                    val userDetailsFromServerResponseState =
                        getUserDetailsFromRemoteUseCase.invoke(fireBaseId)

                    // If the request was successful, then add the user to the database and set the current user state.
                    if (userDetailsFromServerResponseState.status == RequestStatusEnum.Success) {
                        addUserToDbUseCase.invoke(userDetailsFromServerResponseState.data!!)
                        currentUserState.value = userDetailsFromServerResponseState.data
                        _userDetailsStateFlow.value = ResponseState.success(null)
                    } else {
                        // If the request was unsuccessful, then set the user details state flow to error.
                        _userDetailsStateFlow.value = ResponseState.error(
                            userDetailsFromServerResponseState.message ?: ""
                        )
                    }
                } else {
                    // If the Firebase ID is null, then set the user details state flow to error with the NO_USER_FOUND error code.
                    _userDetailsStateFlow.value =
                        ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }
        }
    }
}