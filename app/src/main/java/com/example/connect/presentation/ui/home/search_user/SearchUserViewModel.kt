package com.example.connect.presentation.ui.home.search_user

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.user.GetAllUsersNotInListFromRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class SearchUserViewModel @Inject constructor(
    private val getAllUsersNotInListFromRemoteUseCase: GetAllUsersNotInListFromRemoteUseCase
) :
    BaseViewModel() {
    private val _searchUserStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val searchUserStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _searchUserStateFlow

    val snackBarMessageState = mutableStateOf("")

    var isUserDetailsFetched: Boolean = false

    fun getAllUsers(fetchDetailsNotForList: List<String>, currentUserFirebaseId: String) {
        isUserDetailsFetched = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _searchUserStateFlow.value = ResponseState.loading()
                _searchUserStateFlow.value =
                    getAllUsersNotInListFromRemoteUseCase.invoke(
                        fetchDetailsNotForList,
                        currentUserFirebaseId
                    )
            }
        }
    }

}