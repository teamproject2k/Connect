package com.example.connect.presentation.ui.auth.mobile_number

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.ResponseState
import com.example.connect.domain.useCase.AuthenticationUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.utils.enums.ButtonLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MobileNumberInputViewModel @Inject constructor(private val authenticationUseCase: AuthenticationUseCase) :
    BaseViewModel() {
    val userMobileNumberState = mutableStateOf("")
    val snackBarMessage = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingState.NotLoading)
    val selectedCountryCode = "+91"

    val sendOtpUIState: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())

    fun isValidMobileNumber(): Boolean {
        val phoneRegex = "^[0-9]{10}$"
        return phoneRegex.toRegex().matches(userMobileNumberState.value)
    }

    fun sendOTP() {
        viewModelScope.launch {
            sendOtpUIState.value = ResponseState.loading()
            authenticationUseCase.sendOtp(
                selectedCountryCode,
                userMobileNumberState.value,
                sendOtpUIState
            )
        }
    }
}