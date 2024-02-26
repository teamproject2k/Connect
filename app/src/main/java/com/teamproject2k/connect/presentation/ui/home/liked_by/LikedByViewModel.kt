package com.teamproject2k.connect.presentation.ui.home.liked_by

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.teamproject2k.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LikedByViewModel @Inject constructor(
    private val getUserDetailsFromIdsFromRemoteUseCase: GetUserDetailsFromIdsFromRemoteUseCase,
) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _getLikedByUsersStateFlow: MutableStateFlow<ResponseState<List<UserBean>>> =
        MutableStateFlow(ResponseState.none())
    val getLikedByUsersStateFlow = _getLikedByUsersStateFlow.asStateFlow()

    /**
     * Gets the liked by users.
     *
     * @param likedByUsersList The list of liked by user ids.
     */
    fun getLikedByUsers(likedByUsersList: List<String>) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Perform the operation in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the state to loading.
                _getLikedByUsersStateFlow.value = ResponseState.loading()
                // If the liked by user list is empty, set the state to success with an empty list.
                if (likedByUsersList.isEmpty()) {
                    delay(500)
                    _getLikedByUsersStateFlow.value = ResponseState.success(emptyList())
                } else {
                    // Otherwise, get the user details from the remote use case and set the state to success with the result.
                    _getLikedByUsersStateFlow.value =
                        getUserDetailsFromIdsFromRemoteUseCase(likedByUsersList)
                }
            }
        }
    }
}