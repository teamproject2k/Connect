package com.example.connect.presentation.ui.home.current_user_profile

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddPostListToLocalUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromLocalUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromDbUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromRemoteUseCase
import com.example.connect.domain.utils.FirebaseErrorCodes
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
class CurrentUserProfileViewModel @Inject constructor(
    private val getPostDetailsFromLocalUseCase: GetPostDetailsFromLocalUseCase,
    private val getPostDetailsFromRemoteUseCase: GetPostDetailsFromRemoteUseCase,
    private val addPostListToLocalUseCase: AddPostListToLocalUseCase,
    private val getUserDetailsFromIds: GetUserDetailsFromIdsFromRemoteUseCase,
    private val getUserDetailsFromRemoteUseCase: GetUserDetailsFromRemoteUseCase,
    private val addUserToDbUseCase: AddUserToDbUseCase,
    private val getUserDetailsFromLocalUseCase: GetUserDetailsFromDbUseCase
) : BaseViewModel() {
    private val _friendsDetailsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())

    val friendsDetailsStateFlow = _friendsDetailsStateFlow.asStateFlow()

    private val _loggedInUserDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean>> =
        MutableStateFlow(ResponseState.none())

    val loggedInUserDetailsStateFlow = _loggedInUserDetailsStateFlow.asStateFlow()

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostBean>>> =
        MutableStateFlow(ResponseState.none())

    val postDetailsStateFlow = _postDetailsStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")

    lateinit var loggedInUserDetailsState: MutableState<UsersBean>

    var isDataInitialized = false

    fun init(loggedInUserDetails: UsersBean) {
        this.loggedInUserDetailsState = mutableStateOf(loggedInUserDetails)
        isDataInitialized = true
    }

    /**
     * Gets the details of the post.
     */
    fun getPostDetails(isForceRefresh: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _postDetailsStateFlow.value = ResponseState.loading()
                if (isForceRefresh) {
                    val postDetailsFromServerResponseState =
                        getPostDetailsFromRemoteUseCase.invoke(
                            loggedInUserDetailsState.value.firebaseUserId,
                            loggedInUserDetailsState.value.firebaseUserId
                        )
                    if (postDetailsFromServerResponseState.status == RequestStatusEnum.Success && postDetailsFromServerResponseState.data != null) {
                        addPostListToLocalUseCase.invoke(postDetailsFromServerResponseState.data)
                    }
                    _postDetailsStateFlow.value = ResponseState.success(
                        getPostDetailsFromLocalUseCase.invoke(loggedInUserDetailsState.value.firebaseUserId)
                    )
                } else {
                    _postDetailsStateFlow.value = ResponseState.success(
                        getPostDetailsFromLocalUseCase.invoke(loggedInUserDetailsState.value.firebaseUserId)
                    )
                }
            }
        }
    }

    /**
     * Gets the friend list from the given list of friend IDs.
     *
     * @param friendIdList The list of friend IDs.
     */
    fun getFriendListFromIds() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _friendsDetailsStateFlow.value = ResponseState.loading()
                if (loggedInUserDetailsState.value.friendList.isEmpty()) {
                    delay(500)
                    _friendsDetailsStateFlow.value = ResponseState.success(emptyList())
                } else {
                    _friendsDetailsStateFlow.value =
                        getUserDetailsFromIds.invoke(loggedInUserDetailsState.value.friendList)
                }
            }
        }
    }


    fun getUserDetails() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _loggedInUserDetailsStateFlow.value = ResponseState.loading()
                val getUserDetailsResponse =
                    getUserDetailsFromRemoteUseCase.invoke(loggedInUserDetailsState.value.firebaseUserId)
                if (getUserDetailsResponse.status == RequestStatusEnum.Success && getUserDetailsResponse.data != null) {
                    addUserToDbUseCase.invoke(getUserDetailsResponse.data)
                    val userDetails =
                        getUserDetailsFromLocalUseCase.invoke(loggedInUserDetailsState.value.firebaseUserId)
                    if (userDetails != null) {
                        _loggedInUserDetailsStateFlow.value = ResponseState.success(userDetails)
                    } else {
                        _loggedInUserDetailsStateFlow.value =
                            ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                    }

                } else if (getUserDetailsResponse.data == null) {
                    _loggedInUserDetailsStateFlow.value =
                        ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                } else {
                    _loggedInUserDetailsStateFlow.value =
                        ResponseState.error(getUserDetailsResponse.message ?: "")
                }
            }
        }
    }
}