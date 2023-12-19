package com.example.connect.presentation.ui.home.user_request

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromRemoteUseCase
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

    fun getFriendsList(friendsList: List<String>) {
        isFriendListFetched = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getFriendsListStateFlow.value = ResponseState.loading()
                if (friendsList.isEmpty()) {
                    _getFriendsListStateFlow.value = ResponseState.success(emptyList())
                } else {
                    _getFriendsListStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase.invoke(friendsList)
                }
            }
        }
    }

    fun getPendingFriendRequestList(pendingList: List<String>) {
        isPendingFriendRequestListFetched = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getPendingFriendRequestListStateFlow.value = ResponseState.loading()
                if (pendingList.isEmpty()) {
                    _getPendingFriendRequestListStateFlow.value =
                        ResponseState.success(emptyList())
                } else {
                    _getPendingFriendRequestListStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase.invoke(pendingList)
                }
            }
        }
    }

    /**
     * Gets the user details from the server.
     */
    fun getUserDetails() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _userDetailsStateFlow.value = ResponseState.loading()

                val fireBaseId = fireBaseAuth.currentUser?.uid

                if (fireBaseId != null) {
                    val userDetailsFromServerResponseState =
                        getUserDetailsFromRemoteUseCase.invoke(fireBaseId)

                    if (userDetailsFromServerResponseState.status == RequestStatusEnum.SUCCESS) {
                        addUserToDbUseCase.invoke(userDetailsFromServerResponseState.data!!)
                        currentUserState.value = userDetailsFromServerResponseState.data
                        _userDetailsStateFlow.value = ResponseState.success(null)
                    } else {
                        _userDetailsStateFlow.value = ResponseState.error(
                            userDetailsFromServerResponseState.message ?: ""
                        )
                    }
                } else {
                    _userDetailsStateFlow.value = ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }
        }
    }
}