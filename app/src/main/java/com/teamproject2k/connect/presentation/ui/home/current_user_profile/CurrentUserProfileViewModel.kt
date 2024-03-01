package com.teamproject2k.connect.presentation.ui.home.current_user_profile

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.posts.AddPostListToLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.GetPostDetailsFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.GetPostDetailsFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.AddUserToLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.GetUserDetailsFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.GetUserDetailsFromRemoteUseCase
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.base.BaseViewModel
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
    private val addUserToLocalUseCase: AddUserToLocalUseCase,
    private val getUserDetailsFromLocalUseCase: GetUserDetailsFromLocalUseCase
) : BaseViewModel() {

    lateinit var loggedInUserDetailsState: MutableState<UserBean>

    var isDataInitialized = false

    val snackBarMessageState = mutableStateOf("")

    private val _friendsDetailsStateFlow: MutableStateFlow<ResponseState<List<UserBean>>> =
        MutableStateFlow(ResponseState.none())
    val friendsDetailsStateFlow = _friendsDetailsStateFlow.asStateFlow()

    private val _loggedInUserDetailsStateFlow: MutableStateFlow<ResponseState<UserBean>> =
        MutableStateFlow(ResponseState.none())
    val loggedInUserDetailsStateFlow = _loggedInUserDetailsStateFlow.asStateFlow()

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostBean>>> =
        MutableStateFlow(ResponseState.none())
    val postDetailsStateFlow = _postDetailsStateFlow.asStateFlow()

    /**
     * Initializes the data for the view model with the provided logged-in user details.
     *
     * @param loggedInUserDetails The details of the logged-in user.
     */
    fun initializeData(loggedInUserDetails: UserBean) {
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
                        getPostDetailsFromRemoteUseCase(
                            loggedInUserDetailsState.value.firebaseUserId,
                            loggedInUserDetailsState.value.firebaseUserId
                        )
                    if (postDetailsFromServerResponseState.status == RequestStatusEnum.Success && postDetailsFromServerResponseState.data != null) {
                        addPostListToLocalUseCase(postDetailsFromServerResponseState.data)
                    }
                    _postDetailsStateFlow.value = ResponseState.success(
                        getPostDetailsFromLocalUseCase(loggedInUserDetailsState.value.firebaseUserId)
                    )
                } else {
                    _postDetailsStateFlow.value = ResponseState.success(
                        getPostDetailsFromLocalUseCase(loggedInUserDetailsState.value.firebaseUserId)
                    )
                }
            }
        }
    }

    /**
     * Gets the friend list from the given list of friend IDs.
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
                        getUserDetailsFromIds(loggedInUserDetailsState.value.friendList)
                }
            }
        }
    }

    /**
     * Retrieves the details of the logged-in user.
     * This function fetches the user details both remotely and locally and updates the state flow accordingly.
     */
    fun getUserDetails() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _loggedInUserDetailsStateFlow.value = ResponseState.loading()
                val getUserDetailsResponse =
                    getUserDetailsFromRemoteUseCase(loggedInUserDetailsState.value.firebaseUserId)
                if (getUserDetailsResponse.status == RequestStatusEnum.Success && getUserDetailsResponse.data != null) {
                    addUserToLocalUseCase(getUserDetailsResponse.data)
                    val userDetails =
                        getUserDetailsFromLocalUseCase(loggedInUserDetailsState.value.firebaseUserId)
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