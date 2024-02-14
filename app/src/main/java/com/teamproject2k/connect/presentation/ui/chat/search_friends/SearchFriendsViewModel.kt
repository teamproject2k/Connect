package com.teamproject2k.connect.presentation.ui.chat.search_friends

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.useCase.user.GetLoggedInUserFriendListFromRemoteUseCase
import com.teamproject2k.connect.domain.useCase.user.UpdateUserOnLocalUseCase
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

    private val _searchFriendsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())

    val searchFriendsStateFlow = _searchFriendsStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")

    fun getAllFriends(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _searchFriendsStateFlow.value = ResponseState.loading()
                val response = getLoggedInUserFriendListFromRemoteUseCase(loggedInUserFirebaseId)
                if (response.status == RequestStatusEnum.Success) {
                    if (response.data != null) {
                        updateUserOnLocalUseCase(response.data.first)
                    }
                    _searchFriendsStateFlow.value = ResponseState.success(response.data?.second)
                } else {
                    _searchFriendsStateFlow.value = ResponseState.error(response.message ?: "")
                }
            }
        }
    }
}