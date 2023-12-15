package com.example.connect.presentation.ui.home.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.user.AcceptFriendRequestUseCase
import com.example.connect.domain.useCase.user.GetAllUsersNotInListFromRemoteUseCase
import com.example.connect.domain.useCase.user.SendFriendRequestUseCase
import com.example.connect.domain.useCase.user.UpdateOtherUserStatusUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getAllUsersNotInListFromRemoteUseCase: GetAllUsersNotInListFromRemoteUseCase,
    private val sendFriendRequestUseCase: SendFriendRequestUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val updateOtherUserStatusUseCase: UpdateOtherUserStatusUseCase
) :
    BaseViewModel() {
    private val _searchUserStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val searchUserStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _searchUserStateFlow

    private val _sendFriendRequestStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val sendFriendRequestStateFlow: StateFlow<ResponseState<Nothing>> get() = _sendFriendRequestStateFlow


    private val _acceptFriendRequestStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val acceptFriendRequestStateFlow: StateFlow<ResponseState<Nothing>> get() = _acceptFriendRequestStateFlow

    val snackBarMessageState = mutableStateOf("")

    var isUserDetailsFetched: Boolean = false

    fun getAllUsers(fetchDetailsNotForList: List<String>, currentUserFirebaseId: String) {
        isUserDetailsFetched = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _searchUserStateFlow.value = ResponseState.loading()
                _searchUserStateFlow.value =
                    getAllUsersNotInListFromRemoteUseCase.invoke(
                        fetchDetailsNotForList,
                        currentUserFirebaseId
                    )
            }
        }
    }


    fun sendFriendRequest(currentUser: UsersBean, requestUser: UsersBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendFriendRequestStateFlow.value = ResponseState.loading()
                val serverResponseState = sendFriendRequestUseCase.invoke(
                    currentUser.firebaseUserId,
                    requestUser.firebaseUserId
                )
                if (serverResponseState.status == RequestStatusEnum.SUCCESS) {
                    currentUser.requestedFriendRequestList.add(requestUser.firebaseUserId)
                    requestUser.receivedFriendRequestList.add(currentUser.firebaseUserId)
                    updateOtherUserStatusUseCase.invoke(
                        currentUser.firebaseUserId,
                        currentUser.toUserDbEntity().otherUsersStatus
                    )
                    _sendFriendRequestStateFlow.value = ResponseState.success(null)
                } else {
                    _sendFriendRequestStateFlow.value = serverResponseState
                }
            }
        }
    }


    fun acceptFriendRequest(currentUser: UsersBean, requestUser: UsersBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _acceptFriendRequestStateFlow.value = ResponseState.loading()
                val serverResponseState = acceptFriendRequestUseCase.invoke(
                    currentUser.firebaseUserId,
                    requestUser.firebaseUserId
                )
                if (serverResponseState.status == RequestStatusEnum.SUCCESS) {
                    currentUser.friendList.add(requestUser.firebaseUserId)
                    currentUser.receivedFriendRequestList.remove(requestUser.firebaseUserId)
                    requestUser.friendList.add(currentUser.firebaseUserId)
                    requestUser.requestedFriendRequestList.remove(currentUser.firebaseUserId)
                    updateOtherUserStatusUseCase.invoke(
                        currentUser.firebaseUserId,
                        currentUser.toUserDbEntity().otherUsersStatus
                    )
                    _acceptFriendRequestStateFlow.value = ResponseState.success(null)
                } else {
                    _acceptFriendRequestStateFlow.value = serverResponseState
                }
            }
        }
    }

}