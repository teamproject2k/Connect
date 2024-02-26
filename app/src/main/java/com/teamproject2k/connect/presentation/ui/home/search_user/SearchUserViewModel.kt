package com.teamproject2k.connect.presentation.ui.home.search_user

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.user.GetAllUsersNotInListFromRemoteUseCase
import com.teamproject2k.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class SearchUserViewModel @Inject constructor(
    private val getAllUsersNotInListFromRemoteUseCase: GetAllUsersNotInListFromRemoteUseCase
) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _searchUserStateFlow: MutableStateFlow<ResponseState<List<UserBean>>> =
        MutableStateFlow(ResponseState.none())
    val searchUserStateFlow = _searchUserStateFlow.asStateFlow()

    fun getAllUsers(currentUser: UserBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val fetchDetailsNotForList = arrayListOf<String>()
                fetchDetailsNotForList.add(currentUser.firebaseUserId)
                fetchDetailsNotForList.addAll(currentUser.blockedUsersList)
                _searchUserStateFlow.value = ResponseState.loading()
                _searchUserStateFlow.value =
                    getAllUsersNotInListFromRemoteUseCase(
                        fetchDetailsNotForList,
                        currentUser.firebaseUserId
                    )
            }
        }
    }
}