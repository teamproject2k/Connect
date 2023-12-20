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

    fun getRequestedUsers(requestedUsersList: List<String>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getRequestedUsersStateFlow.value = ResponseState.loading()
                if (requestedUsersList.isEmpty()) {
                    _getRequestedUsersStateFlow.value = ResponseState.success(emptyList())
                } else {
                    _getRequestedUsersStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase.invoke(requestedUsersList)
                }
            }
        }
    }
}