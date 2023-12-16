package com.example.connect.presentation.ui.home.other_user_profile

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.ErrorCodes
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.domain.enums.StatusWithCurrentEnum
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.posts.AddPostListToDbUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromDbUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.example.connect.domain.useCase.user.SendFriendRequestUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OtherUserProfileViewModel @Inject constructor(
    private val getPostDetailsFromDbUseCase: GetPostDetailsFromDbUseCase,
    private val getPostDetailsFromRemoteUseCase: GetPostDetailsFromRemoteUseCase,
    private val addPostListToDbUseCase: AddPostListToDbUseCase,
    private val getUserDetailsFromIdsUseCase: GetUserDetailsFromIdsFromRemoteUseCase,
    private val sendFriendRequestUseCase: SendFriendRequestUseCase
) : BaseViewModel() {
    var isDataInitialized = false
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

    val statusWithCurrentUserState: MutableState<String> = mutableStateOf("")
    fun initializeData(currentUser: UsersBean, requestedUser: UsersBean) {
        // statusWithCurrentUser = FunctionHelper.getStatusWithCurrentUser(currentUser, requestedUser)
        statusWithCurrentUserState.value = StatusWithCurrentEnum.NotFriends.name
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
                    _postDetailsStateFlow.value = ResponseState.error(ErrorCodes.NoUserFound)
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
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendFriendRequestStateFlow.value = ResponseState.loading()
                _sendFriendRequestStateFlow.value =
                    sendFriendRequestUseCase.invoke(currentUserFirebaseId, requestedUserFirebaseId)
            }
        }
    }

    fun withdrawFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _withdrawFriendRequestStateFlow.value = ResponseState.loading()
                _withdrawFriendRequestStateFlow.value =
                    sendFriendRequestUseCase.invoke(currentUserFirebaseId, requestedUserFirebaseId)
            }
        }
    }

    fun acceptFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _acceptFriendRequestStateFlow.value = ResponseState.loading()
                _acceptFriendRequestStateFlow.value =
                    sendFriendRequestUseCase.invoke(currentUserFirebaseId, requestedUserFirebaseId)
            }
        }
    }

    fun removeFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _removeFriendRequestStateFlow.value = ResponseState.loading()
                _removeFriendRequestStateFlow.value =
                    sendFriendRequestUseCase.invoke(currentUserFirebaseId, requestedUserFirebaseId)
            }
        }
    }

    fun unBlockUser(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _unBlockUserStateFlow.value = ResponseState.loading()
                _unBlockUserStateFlow.value =
                    sendFriendRequestUseCase.invoke(currentUserFirebaseId, requestedUserFirebaseId)
            }
        }
    }

    fun blockUser(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _blockUserStateFlow.value = ResponseState.loading()
                _blockUserStateFlow.value =
                    sendFriendRequestUseCase.invoke(currentUserFirebaseId, requestedUserFirebaseId)
            }
        }
    }
}