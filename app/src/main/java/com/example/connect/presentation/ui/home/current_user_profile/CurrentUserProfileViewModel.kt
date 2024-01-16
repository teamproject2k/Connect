package com.example.connect.presentation.ui.home.current_user_profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddPostListToLocalUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromLocalUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.user.AddUserListToLocalUseCase
import com.example.connect.domain.useCase.user.GetAllUserFromIdsFromLocal
import com.example.connect.domain.useCase.user.GetUserDetailsFromIdsFromRemoteUseCase
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
    private val addUserListToLocalUseCase: AddUserListToLocalUseCase,
    private val getUserDetailsFromIdsFromLocal: GetAllUserFromIdsFromLocal
) : BaseViewModel() {
    private val _friendsDetailsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())

    val friendsDetailsStateFlow = _friendsDetailsStateFlow.asStateFlow()

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostBean>>> =
        MutableStateFlow(ResponseState.none())

    val postDetailsStateFlow = _postDetailsStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")

    /**
     * Gets the details of the post.
     */
    fun getPostDetails(loggedInUserFirebaseId: String, whetherGetDataFomRemote: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _postDetailsStateFlow.value = ResponseState.loading()
                if (whetherGetDataFomRemote) {
                    val postDetailsFromServerResponseState =
                        getPostDetailsFromRemoteUseCase.invoke(
                            loggedInUserFirebaseId,
                            loggedInUserFirebaseId
                        )

                    if (postDetailsFromServerResponseState.status == RequestStatusEnum.Success && postDetailsFromServerResponseState.data != null) {
                        addPostListToLocalUseCase.invoke(postDetailsFromServerResponseState.data)
                    }
                    _postDetailsStateFlow.value = postDetailsFromServerResponseState
                } else {
                    _postDetailsStateFlow.value = ResponseState.success(
                        getPostDetailsFromLocalUseCase.invoke(loggedInUserFirebaseId)
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
    fun getFriendListFromIds(
        friendIdList: List<String>,
        whetherGetFriendsListFromRemote: Boolean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _friendsDetailsStateFlow.value = ResponseState.loading()
                if (friendIdList.isEmpty()) {
                    delay(500)
                    _friendsDetailsStateFlow.value = ResponseState.success(emptyList())
                } else {
                    if (whetherGetFriendsListFromRemote) {
                        val getUserDetailsResponse = getUserDetailsFromIds.invoke(friendIdList)
                        if (getUserDetailsResponse.status == RequestStatusEnum.Success && getUserDetailsResponse.data != null) {
                            addUserListToLocalUseCase.invoke(getUserDetailsResponse.data)
                        }
                        _friendsDetailsStateFlow.value = getUserDetailsResponse
                    } else {
                        _friendsDetailsStateFlow.value =
                            ResponseState.success(getUserDetailsFromIdsFromLocal.invoke(friendIdList))
                    }
                }
            }
        }
    }
}