package com.example.connect.presentation.ui.home.other_user_profile

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.posts.AddPostListToDbUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromDbUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.user.AcceptFriendRequestUseCase
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.BlockUserUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.user.RemoveFriendRequestUseCase
import com.example.connect.domain.useCase.user.SendFriendRequestUseCase
import com.example.connect.domain.useCase.user.UnBlockUserUseCase
import com.example.connect.domain.useCase.user.UnfriendAndBlockUserUseCase
import com.example.connect.domain.useCase.user.UnfriendUserUseCase
import com.example.connect.domain.useCase.user.UpdateOtherUserStatusOnDbUseCase
import com.example.connect.domain.useCase.user.WithdrawFriendRequestUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.utils.FunctionHelper
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
    private val getPostDetailsFromDbUseCase: GetPostDetailsFromDbUseCase,
    private val getPostDetailsFromRemoteUseCase: GetPostDetailsFromRemoteUseCase,
    private val addPostListToDbUseCase: AddPostListToDbUseCase,
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
    private val getUserDetailsFromRemoteUseCase: GetUserDetailsFromRemoteUseCase,
    private val addUserToDbUseCase: AddUserToDbUseCase
) : BaseViewModel() {
    var isDataInitialized = false
    lateinit var currentUserState: MutableState<UsersBean>
    private val _friendsDetailsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())

    val friendsDetailsStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _friendsDetailsStateFlow

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostBean>>> =
        MutableStateFlow(ResponseState.none())
    val postDetailsStateFlow: StateFlow<ResponseState<List<PostBean>>> get() = _postDetailsStateFlow

    val snackBarMessageState = mutableStateOf("")

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

    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val userDetailsStateFlow: StateFlow<ResponseState<Nothing>> get() = _userDetailsStateFlow

    val statusWithCurrentUserState: MutableState<String> = mutableStateOf("")
    fun initializeData(currentUser: UsersBean, requestedUser: UsersBean) {
        statusWithCurrentUserState.value =
            FunctionHelper.getStatusWithCurrentUser(currentUser, requestedUser)
        currentUserState = mutableStateOf(currentUser)
        isDataInitialized = true
    }

    /**
     * Gets the details of the post.
     */
    fun getPostDetails() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _postDetailsStateFlow.value = ResponseState.loading()
                val fireBaseId = fireBaseAuth.currentUser?.uid
                if (fireBaseId != null) {
                    val postDetails = getPostDetailsFromDbUseCase.invoke(fireBaseId)
                    if (postDetails.isNotEmpty()) {
                        _postDetailsStateFlow.value = ResponseState.success(postDetails)
                    } else {
                        val postDetailsFromServerResponseState =
                            getPostDetailsFromRemoteUseCase.invoke(fireBaseId)
                        if (postDetailsFromServerResponseState.status == RequestStatusEnum.SUCCESS) {
                            addPostListToDbUseCase.invoke(postDetailsFromServerResponseState.data!!)
                            _postDetailsStateFlow.value =
                                ResponseState.success(postDetailsFromServerResponseState.data)
                        } else {
                            _postDetailsStateFlow.value = postDetailsFromServerResponseState
                        }
                    }
                } else {
                    _postDetailsStateFlow.value = ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                }
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

    fun sendFriendRequest(
        currentUser: UsersBean,
        requestedUser: UsersBean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendFriendRequestStateFlow.value = ResponseState.loading()
                val responseState =
                    sendFriendRequestUseCase.invoke(
                        currentUser.firebaseUserId,
                        requestedUser.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.SUCCESS) {
                    currentUser.requestedFriendRequestList.add(requestedUser.firebaseUserId)
                    requestedUser.receivedFriendRequestList.add(currentUser.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUser.firebaseUserId,
                        currentUser.toUserDbEntity().otherUsersStatus
                    )
                    _sendFriendRequestStateFlow.value = responseState
                } else {
                    _sendFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    fun withdrawFriendRequest(
        currentUser: UsersBean,
        requestedUser: UsersBean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _withdrawFriendRequestStateFlow.value = ResponseState.loading()
                val responseState =
                    withdrawFriendRequestUseCase.invoke(
                        currentUser.firebaseUserId,
                        requestedUser.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.SUCCESS) {
                    currentUser.requestedFriendRequestList.remove(requestedUser.firebaseUserId)
                    requestedUser.receivedFriendRequestList.remove(currentUser.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUser.firebaseUserId,
                        currentUser.toUserDbEntity().otherUsersStatus
                    )
                    _withdrawFriendRequestStateFlow.value = responseState
                } else {
                    _withdrawFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    fun acceptFriendRequest(
        currentUser: UsersBean,
        requestedUser: UsersBean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _acceptFriendRequestStateFlow.value = ResponseState.loading()
                val responseState =
                    acceptFriendRequestUseCase.invoke(
                        currentUser.firebaseUserId,
                        requestedUser.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.SUCCESS) {
                    currentUser.receivedFriendRequestList.remove(requestedUser.firebaseUserId)
                    currentUser.friendList.add(requestedUser.firebaseUserId)
                    requestedUser.requestedFriendRequestList.remove(currentUser.firebaseUserId)
                    requestedUser.friendList.add(currentUser.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUser.firebaseUserId,
                        currentUser.toUserDbEntity().otherUsersStatus
                    )
                    _acceptFriendRequestStateFlow.value = responseState
                } else {
                    _acceptFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    fun removeFriendRequest(
        currentUser: UsersBean,
        requestedUser: UsersBean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _removeFriendRequestStateFlow.value = ResponseState.loading()
                val responseState =
                    removeFriendRequestUseCase.invoke(
                        currentUser.firebaseUserId,
                        requestedUser.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.SUCCESS) {
                    currentUser.receivedFriendRequestList.remove(requestedUser.firebaseUserId)
                    requestedUser.requestedFriendRequestList.remove(currentUser.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUser.firebaseUserId,
                        currentUser.toUserDbEntity().otherUsersStatus
                    )
                    _removeFriendRequestStateFlow.value = responseState
                } else {
                    _removeFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    fun unBlockUser(
        currentUser: UsersBean,
        requestedUser: UsersBean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _unBlockUserStateFlow.value = ResponseState.loading()
                val responseState =
                    unBlockUserUseCase.invoke(
                        currentUser.firebaseUserId,
                        requestedUser.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.SUCCESS) {
                    currentUser.blockedUsersList.remove(requestedUser.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUser.firebaseUserId,
                        currentUser.toUserDbEntity().otherUsersStatus
                    )
                    _unBlockUserStateFlow.value = responseState
                } else {
                    _unBlockUserStateFlow.value = responseState
                }
            }
        }
    }

    fun blockUser(
        currentUser: UsersBean,
        requestedUser: UsersBean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _blockUserStateFlow.value = ResponseState.loading()
                val responseState =
                    blockUserUseCase.invoke(
                        currentUser.firebaseUserId,
                        requestedUser.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.SUCCESS) {
                    currentUser.blockedUsersList.add(requestedUser.firebaseUserId)
                    currentUser.friendList.remove(requestedUser.firebaseUserId)
                    currentUser.requestedFriendRequestList.remove(requestedUser.firebaseUserId)
                    currentUser.receivedFriendRequestList.remove(requestedUser.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUser.firebaseUserId,
                        currentUser.toUserDbEntity().otherUsersStatus
                    )
                    _blockUserStateFlow.value = responseState
                } else {
                    _blockUserStateFlow.value = responseState
                }
            }
        }
    }

    fun unfriendUser(
        currentUser: UsersBean,
        requestedUser: UsersBean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _unfriendUserStateFlow.value = ResponseState.loading()
                val responseState =
                    unfriendUserUseCase.invoke(
                        currentUser.firebaseUserId,
                        requestedUser.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.SUCCESS) {
                    currentUser.friendList.remove(requestedUser.firebaseUserId)
                    requestedUser.friendList.remove(currentUser.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUser.firebaseUserId,
                        currentUser.toUserDbEntity().otherUsersStatus
                    )
                    _unfriendUserStateFlow.value = responseState
                } else {
                    _unfriendUserStateFlow.value = responseState
                }
            }
        }
    }

    fun unfriendAndBlockUser(
        currentUser: UsersBean,
        requestedUser: UsersBean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _unfriendAndBlockUserStateFlow.value = ResponseState.loading()
                val responseState =
                    unfriendAndBlockUserUseCase.invoke(
                        currentUser.firebaseUserId,
                        requestedUser.firebaseUserId
                    )
                if (responseState.status == RequestStatusEnum.SUCCESS) {
                    currentUser.friendList.remove(requestedUser.firebaseUserId)
                    currentUser.blockedUsersList.add(requestedUser.firebaseUserId)
                    requestedUser.friendList.remove(currentUser.firebaseUserId)
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUser.firebaseUserId,
                        currentUser.toUserDbEntity().otherUsersStatus
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