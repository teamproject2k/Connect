package com.example.connect.presentation.ui.home

import androidx.lifecycle.viewModelScope
import com.example.connect.common.ErrorCodes
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromDbUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromRemoteUseCase
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
    lateinit var _userDetails: UsersBean
    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val userDetailsStateFlow: StateFlow<ResponseState<Nothing>> get() = _userDetailsStateFlow

    init {
        getUserDetails()
    }

    private fun getUserDetails() {
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