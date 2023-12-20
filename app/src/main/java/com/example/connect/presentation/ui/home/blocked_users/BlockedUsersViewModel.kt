package com.example.connect.presentation.ui.home.blocked_users

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
class BlockedUsersViewModel @Inject constructor(
    private val getUserDetailsFromIdsFromRemoteUseCase: GetUserDetailsFromIdsFromRemoteUseCase,
) :
    BaseViewModel() {
    private val _getBlockedUsersStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val getBlockedUsersStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _getBlockedUsersStateFlow

    val snackBarMessageState = mutableStateOf("")

    fun getBlockedUsers(blockedUsersList: List<String>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getBlockedUsersStateFlow.value = ResponseState.loading()
                if (blockedUsersList.isEmpty()) {
                    _getBlockedUsersStateFlow.value = ResponseState.success(emptyList())
                } else {
                    _getBlockedUsersStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase.invoke(blockedUsersList)
                }
            }
        }
    }
}