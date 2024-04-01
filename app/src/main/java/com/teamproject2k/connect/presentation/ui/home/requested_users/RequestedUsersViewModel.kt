package com.teamproject2k.connect.presentation.ui.home.requested_users

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.user.GetLoggedInUserRequestedUserListFromRemoteUseCase
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
class RequestedUsersViewModel @Inject constructor(
    private val getLoggedInUserRequestedUserListFromRemoteUseCase: GetLoggedInUserRequestedUserListFromRemoteUseCase,
    private val updateUserOnLocalUseCase: UpdateUserOnLocalUseCase

) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _getRequestedUsersStateFlow: MutableStateFlow<ResponseState<Pair<UserBean, List<UserBean>>>> =
        MutableStateFlow(ResponseState.none())
    val getRequestedUsersStateFlow = _getRequestedUsersStateFlow.asStateFlow()

    /**
     * Gets the details of the friend-requested users.
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