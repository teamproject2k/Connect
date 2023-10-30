package com.example.connect.ui.auth.otp

import android.os.CountDownTimer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.base.BaseViewModel
import com.example.connect.utils.ConstantsHelper
import com.example.connect.utils.enums.ButtonLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor() : BaseViewModel() {
    var enteredOTP = ""
    val snackBarMessage = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingState.NotLoading)
    val otpState = mutableStateOf(arrayOf("", "", "", "", "", ""))
    val showTimerState = mutableStateOf(true)
    val timeLeftState = mutableStateOf(ConstantsHelper.OTPTimeOutTime)

    fun isValidOTP(): Boolean {
        return enteredOTP.length == 6
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