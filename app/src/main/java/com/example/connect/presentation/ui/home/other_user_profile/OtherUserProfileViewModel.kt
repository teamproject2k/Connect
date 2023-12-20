package com.example.connect.presentation.ui.home.other_user_profile

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.GetPostDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.user.AcceptFriendRequestUseCase
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.BlockUserUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.example.connect.domain.useCase.user.LiveUserObserverFromRemoteUseCase
import com.example.connect.domain.useCase.user.RemoveFriendRequestUseCase
import com.example.connect.domain.useCase.user.SendFriendRequestUseCase
import com.example.connect.domain.useCase.user.UnBlockUserUseCase
import com.example.connect.domain.useCase.user.UnfriendAndBlockUserUseCase
import com.example.connect.domain.useCase.user.UnfriendUserUseCase
import com.example.connect.domain.useCase.user.UpdateOtherUserStatusOnDbUseCase
import com.example.connect.domain.useCase.user.WithdrawFriendRequestUseCase
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.utils.FunctionHelper
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class OtherUserProfileViewModel @Inject constructor(
    private val getPostDetailsFromRemoteUseCase: GetPostDetailsFromRemoteUseCase,
    private val getUserDetailsFromIdsUseCase: GetUserDetailsFromIdsFromRemoteUseCase,
    private val sendFriendRequestUseCase: SendFriendRequestUseCase,
    private val withdrawFriendRequestUseCase: WithdrawFriendRequestUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val removeFriendRequestUseCase: RemoveFriendRequestUseCase,
    private val blockUserUseCase: BlockUserUseCase,
    private val unBlockUserUseCase: UnBlockUserUseCase,
    private val updateOtherUserStatusOnDbUseCase: UpdateOtherUserStatusOnDbUseCase,
    private val unfriendAndBlockUserUseCase: UnfriendAndBlockUserUseCase,
    private val unfriendUserUseCase: UnfriendUserUseCase,
    private val addUserToDbUseCase: AddUserToDbUseCase,
    private val liveUserObserverFromRemoteUseCase: LiveUserObserverFromRemoteUseCase
) : BaseViewModel() {

    var isDataInitialized = false
    val snackBarMessageState = mutableStateOf("")
    private val _friendsDetailsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val friendsDetailsStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _friendsDetailsStateFlow

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostBean>>> =
        MutableStateFlow(ResponseState.none())
    val postDetailsStateFlow: StateFlow<ResponseState<List<PostBean>>> get() = _postDetailsStateFlow

    private val _sendFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val sendFriendRequestStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _sendFriendRequestStateFlow

    private val _acceptFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val acceptFriendRequestStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _acceptFriendRequestStateFlow

    private val _withdrawFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val withdrawFriendRequestStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _withdrawFriendRequestStateFlow

    private val _removeFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val removeFriendRequestStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _removeFriendRequestStateFlow

    private val _unBlockUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val unBlockUserStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _unBlockUserStateFlow

    private val _blockUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val blockUserStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _blockUserStateFlow

    private val _unfriendUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val unfriendUserStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _unfriendUserStateFlow

    private val _unfriendAndBlockUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val unfriendAndBlockUserStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _unfriendAndBlockUserStateFlow

    private val _liveObserveRequiredUserDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean>> =
        MutableStateFlow(ResponseState.none())
    val liveObserveRequiredUserDetailsStateFlow: StateFlow<ResponseState<UsersBean>> get() = _liveObserveRequiredUserDetailsStateFlow

    private val _liveObserveCurrentUserDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean>> =
        MutableStateFlow(ResponseState.none())
    val liveObserveCurrentUserDetailsStateFlow: StateFlow<ResponseState<UsersBean>> get() = _liveObserveCurrentUserDetailsStateFlow

    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean>> =
        MutableStateFlow(ResponseState.none())
    val userDetailsStateFlow: StateFlow<ResponseState<UsersBean>> get() = _userDetailsStateFlow

    val statusWithCurrentUserState: MutableState<String> = mutableStateOf("")
    private lateinit var liveObserveRequiredUserListener: ListenerRegistration
    private lateinit var liveObserveCurrentUserListener: ListenerRegistration

    lateinit var requiredUserState: MutableState<UsersBean>
    lateinit var currentUserState: MutableState<UsersBean>

    fun initializeData(currentUser: UsersBean, requestedUser: UsersBean) {
        statusWithCurrentUserState.value =
            FunctionHelper.getStatusWithCurrentUser(currentUser, requestedUser)
        currentUserState = mutableStateOf(currentUser)
        requiredUserState = mutableStateOf(requestedUser)
        isDataInitialized = true
    }

    fun getPostDetails(fireBaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _postDetailsStateFlow.value = ResponseState.loading()
                _postDetailsStateFlow.value = getPostDetailsFromRemoteUseCase.invoke(fireBaseId)
            }
        }
    }

    /**
     * Gets the list of friends from their ids.
     *
     * @param friendIdList The list of friend ids.
     */
    fun getFriendListFromIds(friendIdList: List<String>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (friendIdList.isEmpty()) {
                    _friendsDetailsStateFlow.value = ResponseState.success(emptyList())
                } else {
                    _friendsDetailsStateFlow.value = ResponseState.loading()
                    _friendsDetailsStateFlow.value =
                        getUserDetailsFromIdsUseCase.invoke(friendIdList)
                }
            }
        }
    }

    fun sendFriendRequest() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendFriendRequestStateFlow.value = ResponseState.loading()
                val responseState =
                    sendFriendRequestUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    currentUserState.value.requestedFriendRequestList.add(requiredUserState.value.firebaseUserId)
                    requiredUserState.value.receivedFriendRequestList.add(currentUserState.value.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
                    _sendFriendRequestStateFlow.value = responseState
                } else {
                    _sendFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    fun withdrawFriendRequest() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _withdrawFriendRequestStateFlow.value = ResponseState.loading()
                val responseState =
                    withdrawFriendRequestUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    currentUserState.value.requestedFriendRequestList.remove(requiredUserState.value.firebaseUserId)
                    requiredUserState.value.receivedFriendRequestList.remove(currentUserState.value.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
                    _withdrawFriendRequestStateFlow.value = responseState
                } else {
                    _withdrawFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    fun acceptFriendRequest() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _acceptFriendRequestStateFlow.value = ResponseState.loading()
                val responseState =
                    acceptFriendRequestUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    currentUserState.value.receivedFriendRequestList.remove(requiredUserState.value.firebaseUserId)
                    currentUserState.value.friendList.add(requiredUserState.value.firebaseUserId)
                    requiredUserState.value.requestedFriendRequestList.remove(currentUserState.value.firebaseUserId)
                    requiredUserState.value.friendList.add(currentUserState.value.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
                    _acceptFriendRequestStateFlow.value = responseState
                } else {
                    _acceptFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    fun removeFriendRequest() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _removeFriendRequestStateFlow.value = ResponseState.loading()
                val responseState =
                    removeFriendRequestUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    currentUserState.value.receivedFriendRequestList.remove(requiredUserState.value.firebaseUserId)
                    requiredUserState.value.requestedFriendRequestList.remove(currentUserState.value.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
                    _removeFriendRequestStateFlow.value = responseState
                } else {
                    _removeFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    fun unBlockUser() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _unBlockUserStateFlow.value = ResponseState.loading()
                val responseState =
                    unBlockUserUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    currentUserState.value.blockedUsersList.remove(requiredUserState.value.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
                    _unBlockUserStateFlow.value = responseState
                } else {
                    _unBlockUserStateFlow.value = responseState
                }
            }
        }
    }

    fun blockUser() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _blockUserStateFlow.value = ResponseState.loading()
                val responseState =
                    blockUserUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    currentUserState.value.blockedUsersList.add(requiredUserState.value.firebaseUserId)
                    currentUserState.value.friendList.remove(requiredUserState.value.firebaseUserId)
                    currentUserState.value.requestedFriendRequestList.remove(requiredUserState.value.firebaseUserId)
                    currentUserState.value.receivedFriendRequestList.remove(requiredUserState.value.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
                    _blockUserStateFlow.value = responseState
                } else {
                    _blockUserStateFlow.value = responseState
                }
            }
        }
    }

    fun unfriendUser() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _unfriendUserStateFlow.value = ResponseState.loading()
                val responseState =
                    unfriendUserUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    currentUserState.value.friendList.remove(requiredUserState.value.firebaseUserId)
                    requiredUserState.value.friendList.remove(currentUserState.value.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
                    _unfriendUserStateFlow.value = responseState
                } else {
                    _unfriendUserStateFlow.value = responseState
                }
            }
        }
    }

    fun unfriendAndBlockUser() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _unfriendAndBlockUserStateFlow.value = ResponseState.loading()
                val responseState =
                    unfriendAndBlockUserUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    currentUserState.value.friendList.remove(requiredUserState.value.firebaseUserId)
                    currentUserState.value.blockedUsersList.add(requiredUserState.value.firebaseUserId)
                    requiredUserState.value.friendList.remove(currentUserState.value.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
                    _unfriendAndBlockUserStateFlow.value = responseState
                } else {
                    _unfriendAndBlockUserStateFlow.value = responseState
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
                val response = getUserDetailsFromIdsUseCase.invoke(
                    listOf(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )
                )
                if (response.status == RequestStatusEnum.Success) {
                    val userDetailList = response.data ?: emptyList()
                    if (userDetailList.size == 2) {
                        val currentUser =
                            userDetailList.find { it.firebaseUserId == currentUserState.value.firebaseUserId }
                        val requiredUser =
                            userDetailList.find { it.firebaseUserId == requiredUserState.value.firebaseUserId }
                        if (currentUser == null || requiredUser == null) {
                            _userDetailsStateFlow.value =
                                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                        } else {
                            addUserToDbUseCase.invoke(currentUser)
                            getPostDetails(requiredUser.firebaseUserId)
                            getFriendListFromIds(requiredUser.friendList)
                            currentUserState.value=currentUser
                            requiredUserState.value=requiredUser
                            _userDetailsStateFlow.value = ResponseState.success(currentUser)
                        }
                    } else {
                        _userDetailsStateFlow.value =
                            ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                    }
                } else {
                    _userDetailsStateFlow.value = ResponseState.error(response.message ?: "")
                }
            }
        }
    }

    fun liveObserveRequiredUsers() {
        viewModelScope.launch {
            liveObserveRequiredUserListener = liveUserObserverFromRemoteUseCase.invoke(
                requiredUserState.value.firebaseUserId,
                _liveObserveRequiredUserDetailsStateFlow
            )
        }
    }

    fun liveObserveCurrentUsers() {
        viewModelScope.launch {
            liveObserveCurrentUserListener = liveUserObserverFromRemoteUseCase.invoke(
                currentUserState.value.firebaseUserId,
                _liveObserveCurrentUserDetailsStateFlow
            )
        }
    }

    fun updateRequiredUser(updatedDetails: UsersBean) {
        _liveObserveRequiredUserDetailsStateFlow.value = ResponseState.none()
        requiredUserState.value = updatedDetails
        statusWithCurrentUserState.value =
            FunctionHelper.getStatusWithCurrentUser(currentUserState.value, requiredUserState.value)
    }

    fun updateCurrentUser(updatedDetails: UsersBean) {
        _liveObserveCurrentUserDetailsStateFlow.value = ResponseState.none()
        currentUserState.value = updatedDetails
        statusWithCurrentUserState.value =
            FunctionHelper.getStatusWithCurrentUser(currentUserState.value, requiredUserState.value)
    }

    override fun onCleared() {
        super.onCleared()
        liveObserveRequiredUserListener.remove()
        liveObserveCurrentUserListener.remove()
    }
}