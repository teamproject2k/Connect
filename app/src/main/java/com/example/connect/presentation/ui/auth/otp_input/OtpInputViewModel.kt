package com.example.connect.presentation.ui.auth.otp_input

import android.os.CountDownTimer
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewModelScope
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.enums.ButtonLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpInputViewModel @Inject constructor() : BaseViewModel() {
    val snackBarMessage = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingState.NotLoading)
    val otpState = mutableStateOf(" ".repeat(ConstantsHelper.OTPCharCount))
    val showTimerState = mutableStateOf(true)
    val timeLeftState = mutableLongStateOf(ConstantsHelper.OTPTimeOutTime)

    fun isValidOTP(): Boolean {
        return otpState.value.isDigitsOnly() && otpState.value.length == ConstantsHelper.OTPCharCount
    }


    fun startTimer() {
        showTimerState.value = true
        val countDownTimer = object : CountDownTimer(ConstantsHelper.OTPTimeOutTime * 1000, 1000) {
            override fun onTick(timeLeft: Long) {
                timeLeftState.value = timeLeft / 1000
            }

            override fun onFinish() {
                showTimerState.value = false
            }
        }
        countDownTimer.start()
    }


    fun resendOtp() {

    }

    fun verifyOTP() {
        viewModelScope.launch {
            currentButtonLoadingState.value = ButtonLoadingState.Loading
            delay(8000)
            currentButtonLoadingState.value = ButtonLoadingState.NotLoading
        }
    }
}