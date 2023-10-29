package com.example.connect.ui.auth.otp

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.base.BaseViewModel
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

    fun isValidOTP(): Boolean {
        return enteredOTP.length == 6
    }

    fun verifyOTP() {
        viewModelScope.launch {
            currentButtonLoadingState.value = ButtonLoadingState.Loading
            delay(8000)
            currentButtonLoadingState.value = ButtonLoadingState.NotLoading
        }
    }
}