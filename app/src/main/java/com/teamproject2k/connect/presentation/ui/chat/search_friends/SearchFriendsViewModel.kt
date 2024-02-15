package com.teamproject2k.connect.presentation.ui.chat.search_friends

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.use_case.user.GetLoggedInUserFriendListFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateUserOnLocalUseCase
import com.teamproject2k.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchFriendsViewModel @Inject constructor(
    private val getLoggedInUserFriendListFromRemoteUseCase: GetLoggedInUserFriendListFromRemoteUseCase,
    private val updateUserOnLocalUseCase: UpdateUserOnLocalUseCase
) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _searchFriendsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val searchFriendsStateFlow = _searchFriendsStateFlow.asStateFlow()

    /**
     * Retrieves the list of friends for the logged-in user from the remote server.
     * This function sets up a background task to fetch the friend list and updates the UI accordingly.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     */
    fun getAllFriends(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _searchFriendsStateFlow.value = ResponseState.loading() // Setting loading state.

                // Retrieving the list of friends for the logged-in user from the remote server.
                val response = getLoggedInUserFriendListFromRemoteUseCase(loggedInUserFirebaseId)

                // Handling the response from the remote server.
                if (response.status == RequestStatusEnum.Success) {
                    // If the response contains data, update the local user information.
                    if (response.data != null) {
                        updateUserOnLocalUseCase(response.data.first)
                    }
                    // Setting the state flow to success and passing the list of friends.
                    _searchFriendsStateFlow.value = ResponseState.success(response.data?.second)
                } else {
                    // Setting the state flow to error if there was an issue fetching the friend list.
                    _searchFriendsStateFlow.value = ResponseState.error(response.message ?: "")
                }
            }
        }
    }
}