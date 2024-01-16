package com.example.connect.presentation.ui.home.blocked_users

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
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
class BlockedUsersViewModel @Inject constructor(
    private val getUserDetailsFromIdsFromRemoteUseCase: GetUserDetailsFromIdsFromRemoteUseCase,
) :
    BaseViewModel() {
    private val _getBlockedUsersStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val getBlockedUsersStateFlow = _getBlockedUsersStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")

    /**
     * Gets the blocked users.
     *
     * @param blockedUsersList The list of blocked user ids.
     */
    fun getBlockedUsers(blockedUsersList: List<String>) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Perform the operation in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the state to loading.
                _getBlockedUsersStateFlow.value = ResponseState.loading()
                // If the blocked user list is empty, set the state to success with an empty list.
                if (blockedUsersList.isEmpty()) {
                    delay(500)
                    _getBlockedUsersStateFlow.value = ResponseState.success(emptyList())
                } else {
                    // Otherwise, get the user details from the remote use case and set the state to success with the result.
                    _getBlockedUsersStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase.invoke(blockedUsersList)
                }
            }
        }
    }
}