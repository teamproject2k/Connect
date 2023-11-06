package com.example.connect.presentation.ui.auth.otp_input

import android.os.CountDownTimer
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewModelScope
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.useCase.AuthenticationUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.enums.ButtonLoadingEnum
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OtpInputViewModel @Inject constructor(private val authenticationUseCase: AuthenticationUseCase) :
    BaseViewModel() {
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingEnum.NotLoading)
    val otpState = mutableStateOf(" ".repeat(ConstantsHelper.OTPCharCount))
    val showTimerState = mutableStateOf(true)
    val timeLeftState = mutableLongStateOf(ConstantsHelper.OTPTimeOutTime)
    lateinit var verificationId: String
    lateinit var mobileNumber: String
    lateinit var countryCode: String
    private val _verifyOtpStateFlow: MutableStateFlow<ResponseState<FirebaseUser?>> =
        MutableStateFlow(ResponseState.none())
    val verifyOtpStateFlow: StateFlow<ResponseState<FirebaseUser?>> get() = _verifyOtpStateFlow
    private val _getUserDetailsStateFlow: MutableStateFlow<ResponseState<UserDetails?>> =
        MutableStateFlow(ResponseState.none())
    val getUserDetailsStateFlow: StateFlow<ResponseState<UserDetails?>> get() = _getUserDetailsStateFlow

    private val _resendOtpStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>> =
        MutableStateFlow(ResponseState.none())
    val resendOtpStateFlow: StateFlow<ResponseState<Pair<String, String>>> get() = _resendOtpStateFlow

    fun isValidOTP(): Boolean {
        return otpState.value.isDigitsOnly() && otpState.value.length == ConstantsHelper.OTPCharCount
    }


    fun startTimer() {
        showTimerState.value = true
        val countDownTimer = object : CountDownTimer(ConstantsHelper.OTPTimeOutTime * 1000, 1000) {
            override fun onTick(timeLeft: Long) {
                timeLeftState.longValue = timeLeft / 1000
            }

            override fun onFinish() {
                showTimerState.value = false
            }
        }
        countDownTimer.start()
    }


    fun resendOtp() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _resendOtpStateFlow.value = ResponseState.loading()
                authenticationUseCase.sendOtp(
                    countryCode,
                    mobileNumber,
                    _resendOtpStateFlow
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

    fun verifyOTP(verificationId: String) {
        viewModelScope.launch {
            _verifyOtpStateFlow.value = ResponseState.loading()
            _verifyOtpStateFlow.value =
                authenticationUseCase.verifyOtp(verificationId, otpState.value)
        }
    }
}