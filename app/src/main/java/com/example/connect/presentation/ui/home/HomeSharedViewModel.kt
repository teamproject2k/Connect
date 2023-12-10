package com.example.connect.presentation.ui.home

import androidx.lifecycle.viewModelScope
import com.example.connect.common.ErrorCodes
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.useCase.AddUserToDbUseCase
import com.example.connect.domain.useCase.GetPostDetailsFromDbUseCase
import com.example.connect.domain.useCase.GetUserDetailsFromDbUseCase
import com.example.connect.domain.useCase.GetUserDetailsFromRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeSharedViewModel @Inject constructor(
    private val getUserDetailsFromDbUseCase: GetUserDetailsFromDbUseCase,
    private val getUserDetailsFromRemoteUseCase: GetUserDetailsFromRemoteUseCase,
    private val addUserToDbUseCase: AddUserToDbUseCase
) :
    BaseViewModel() {
    lateinit var _userDetails: UserDetails
    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val userDetailsStateFlow: StateFlow<ResponseState<Nothing>> get() = _userDetailsStateFlow

    init {
        getUserDetails()
    }

    fun getUserDetails() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _userDetailsStateFlow.value = ResponseState.loading()
                val fireBaseId = fireBaseAuth.currentUser?.uid
                if (fireBaseId != null) {
                    val userDetails = getUserDetailsFromDbUseCase.invoke(fireBaseId)
                    if (userDetails != null) {
                        _userDetails = userDetails
                        _userDetailsStateFlow.value = ResponseState.success(null)

                    } else {
                        val userDetailsFromServerResponseState =
                            getUserDetailsFromRemoteUseCase.invoke(fireBaseId)
                        if (userDetailsFromServerResponseState.status == RequestStatusEnum.SUCCESS) {
                            addUserToDbUseCase.invoke(userDetailsFromServerResponseState.data!!)
                            _userDetails = userDetailsFromServerResponseState.data
                            _userDetailsStateFlow.value = ResponseState.success(null)
                        } else {
                            _userDetailsStateFlow.value = ResponseState.error(
                                userDetailsFromServerResponseState.message ?: ""
                            )
                        }
                    }
                } else {
                    _userDetailsStateFlow.value = ResponseState.error(ErrorCodes.NoUserFound)
                }
            }
        }
    }

}