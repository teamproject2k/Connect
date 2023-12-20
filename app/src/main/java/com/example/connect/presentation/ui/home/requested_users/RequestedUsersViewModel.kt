package com.example.connect.presentation.ui.home.requested_users

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
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
class RequestedUsersViewModel @Inject constructor(
    private val getUserDetailsFromIdsFromRemoteUseCase: GetUserDetailsFromIdsFromRemoteUseCase,
) :
    BaseViewModel() {
    private val _getRequestedUsersStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val getRequestedUsersStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _getRequestedUsersStateFlow

    val snackBarMessageState = mutableStateOf("")

    /**
     * Gets the details of the requested users.
     *
     * @param requestedUsersList The list of requested users.
     */
    fun getRequestedUsers(requestedUsersList: List<String>) {
        // Launch a coroutine in the viewModelScope
        viewModelScope.launch {
            // Switch to the IO dispatcher for network operations
            withContext(Dispatchers.IO) {
                // Set the state to loading
                _getRequestedUsersStateFlow.value = ResponseState.loading()
                // Check if the list of requested users is empty
                if (requestedUsersList.isEmpty()) {
                    // If the list is empty, set the state to success with an empty list
                    _getRequestedUsersStateFlow.value = ResponseState.success(emptyList())
                } else {
                    // If the list is not empty, get the user details from the remote use case
                    _getRequestedUsersStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase.invoke(requestedUsersList)
                }
            }
        }
    }
}