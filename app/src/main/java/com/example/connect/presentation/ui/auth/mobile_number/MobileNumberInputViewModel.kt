package com.example.connect.presentation.ui.auth.mobile_number

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.useCase.AuthenticationUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.utils.enums.ButtonLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MobileNumberInputViewModel @Inject constructor(private val authenticationUseCase: AuthenticationUseCase) :
    BaseViewModel() {
    val userMobileNumberState = mutableStateOf("")
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingState.NotLoading)
    val selectedCountryCode = "+91"

    private val _sendOtpUIStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>> =
        MutableStateFlow(ResponseState.none())
    val sendOtpUIStateFlow: StateFlow<ResponseState<Pair<String, String>>> get() = _sendOtpUIStateFlow

    private val _getUserDetailsStateFlow: MutableStateFlow<ResponseState<UserDetails?>> =
        MutableStateFlow(ResponseState.none())
    val getUserDetailsStateFlow: StateFlow<ResponseState<UserDetails?>> get() = _getUserDetailsStateFlow

    fun isValidMobileNumber(): Boolean {
        val phoneRegex = "^[0-9]{10}$"
        return phoneRegex.toRegex().matches(userMobileNumberState.value)
    }

    fun sendOTP() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendOtpUIStateFlow.value = ResponseState.loading()
                authenticationUseCase.sendOtp(
                    selectedCountryCode,
                    userMobileNumberState.value,
                    _sendOtpUIStateFlow
                )
            }
        }
    }


    fun getUserDetails(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getUserDetailsStateFlow.value = ResponseState.loading()
                val userDetailsResponseState = authenticationUseCase.getUserDetails(userId)
                if (userDetailsResponseState.status == RequestStatusEnum.SUCCESS && userDetailsResponseState.data != null) {
                    authenticationUseCase.addUserToLocalDb(userDetailsResponseState.data)
                }
                _getUserDetailsStateFlow.value = userDetailsResponseState
            }
        }
    }

    fun resetStateFlow() {
        _sendOtpUIStateFlow.value = ResponseState.none()
        _getUserDetailsStateFlow.value = ResponseState.none()
    }
}