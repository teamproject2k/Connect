package com.teamproject2k.connect.presentation.ui.home.blocked_users

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.user.GetLoggedInUserBlockedUserListFromRemoteUseCase
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
class BlockedUsersViewModel @Inject constructor(
    private val getLoggedInUserBlockedUserListFromRemoteUseCase: GetLoggedInUserBlockedUserListFromRemoteUseCase,
    private val updateUserOnLocalUseCase: UpdateUserOnLocalUseCase
) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _getBlockedUsersStateFlow: MutableStateFlow<ResponseState<Pair<UserBean, List<UserBean>>>> =
        MutableStateFlow(ResponseState.none())
    val getBlockedUsersStateFlow = _getBlockedUsersStateFlow.asStateFlow()

    fun getBlockedUsers(loggedInUserFirebaseId: String) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Perform the operation in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the state to loading.
                _getBlockedUsersStateFlow.value = ResponseState.loading()
                // If the blocked user list is empty, set the state to success with an empty list.
                val blockedListResponse =
                    getLoggedInUserBlockedUserListFromRemoteUseCase(loggedInUserFirebaseId)
                if (blockedListResponse.status == RequestStatusEnum.Success && blockedListResponse.data != null) {
                    updateUserOnLocalUseCase(blockedListResponse.data.first)
                }
                _getBlockedUsersStateFlow.value = blockedListResponse
            }
        }
    }
}