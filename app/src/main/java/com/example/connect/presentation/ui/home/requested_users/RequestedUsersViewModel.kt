package com.example.connect.presentation.ui.home.requested_users

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.user.GetLoggedInUserRequestedUserListFromRemoteUseCase
import com.example.connect.domain.useCase.user.UpdateUserOnLocalUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RequestedUsersViewModel @Inject constructor(
    private val getLoggedInUserRequestedUserListFromRemoteUseCase: GetLoggedInUserRequestedUserListFromRemoteUseCase,
    private val updateUserOnLocalUseCase: UpdateUserOnLocalUseCase

) : BaseViewModel() {

    private val _getRequestedUsersStateFlow: MutableStateFlow<ResponseState<Pair<UsersBean, List<UsersBean>>>> =
        MutableStateFlow(ResponseState.none())

    val getRequestedUsersStateFlow = _getRequestedUsersStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")

    /**
     * Gets the details of the requested users.
     *
     * @param loggedInUserFirebaseId The list of requested users.
     */
    fun getRequestedUsers(loggedInUserFirebaseId: String) {
        // Launch a coroutine in the viewModelScope
        viewModelScope.launch {
            // Perform the operation in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the state to loading.
                _getRequestedUsersStateFlow.value = ResponseState.loading()
                // If the blocked user list is empty, set the state to success with an empty list.
                val requestedListResponse =
                    getLoggedInUserRequestedUserListFromRemoteUseCase(loggedInUserFirebaseId)
                if (requestedListResponse.status == RequestStatusEnum.Success && requestedListResponse.data != null) {
                    updateUserOnLocalUseCase(requestedListResponse.data.first)
                }
                _getRequestedUsersStateFlow.value = requestedListResponse
            }
        }
    }
}