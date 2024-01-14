package com.example.connect.presentation.ui.home.current_user_profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddPostListToLocalUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromDbUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class CurrentUserProfileViewModel @Inject constructor(
    private val getPostDetailsFromDbUseCase: GetPostDetailsFromDbUseCase,
    private val getPostDetailsFromRemoteUseCase: GetPostDetailsFromRemoteUseCase,
    private val addPostListToLocalUseCase: AddPostListToLocalUseCase,
    private val getUserDetailsFromIds: GetUserDetailsFromIdsFromRemoteUseCase
) : BaseViewModel() {
    private val _friendsDetailsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())

    val friendsDetailsStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _friendsDetailsStateFlow

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostBean>>> =
        MutableStateFlow(ResponseState.none())

    val postDetailsStateFlow: StateFlow<ResponseState<List<PostBean>>> get() = _postDetailsStateFlow

    val snackBarMessageState = mutableStateOf("")

    /**
     * Gets the details of the post.
     */
    fun getPostDetails(currentUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _postDetailsStateFlow.value =
                    ResponseState.loading() // Set the state of the post details state flow to loading.
                val postDetails =
                    getPostDetailsFromDbUseCase.invoke(currentUserFirebaseId) // Get the post details from the database.
                if (postDetails.isNotEmpty()) { // If the post details are not empty, then we can set the state of the post details state flow to success and emit the post details.
                    _postDetailsStateFlow.value =
                        ResponseState.success(postDetails) // Set the state of the post details state flow to success and emit the post details.
                } else { // If the post details are empty, then we can get the post details from the server.
                    val postDetailsFromServerResponseState =
                        getPostDetailsFromRemoteUseCase.invoke(
                            currentUserFirebaseId,
                            currentUserFirebaseId
                        ) // Get the post details from the server.

                    if (postDetailsFromServerResponseState.status == RequestStatusEnum.Success) { // If the status of the post details from server response state is success, then we can add the post details to the database and emit the post details.
                        if (postDetailsFromServerResponseState.data != null) {
                            addPostListToLocalUseCase.invoke(postDetailsFromServerResponseState.data) // Add the post details to the database.
                        }
                        _postDetailsStateFlow.value =
                            ResponseState.success(postDetailsFromServerResponseState.data) // Set the state of the post details state flow to success and emit the post details.

                    } else { // If the status of the post details from server response state is not success, then we can set the state of the post details state flow to error and emit the error code.
                        _postDetailsStateFlow.value =
                            postDetailsFromServerResponseState // Set the state of the post details state flow to error and emit the error code.
                    }
                }
            }
        }
    }

    /**
     * Gets the friend list from the given list of friend IDs.
     *
     * @param friendIdList The list of friend IDs.
     */
    fun getFriendListFromIds(friendIdList: List<String>) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations.
            withContext(Dispatchers.IO) {
                // Set the state of the friendsDetailsStateFlow to loading.
                _friendsDetailsStateFlow.value = ResponseState.loading()

                // Check if the friendIdList is empty.
                if (friendIdList.isEmpty()) {
                    // If the friendIdList is empty, set the state of the friendsDetailsStateFlow to success with an empty list.
                    _friendsDetailsStateFlow.value = ResponseState.success(emptyList())
                } else {
                    // If the friendIdList is not empty, call the getUserDetailsFromIds function to get the user details from the IDs.
                    _friendsDetailsStateFlow.value = getUserDetailsFromIds.invoke(friendIdList)
                }
            }
        }
    }
}