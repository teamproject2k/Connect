package com.example.connect.ui.auth.mobile_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.base.BaseViewModel
import com.example.connect.utils.enums.ButtonLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MobileNumberInputVM @Inject constructor() : BaseViewModel() {
    var userMobileNumber = ""
    val snackBarMessage = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingState.NotLoading)

    fun isValidMobileNumber(): Boolean {
        val phoneRegex = "^[0-9]{10}$"
        return phoneRegex.toRegex().matches(userMobileNumber)
    }


    fun sendOTP() {
        viewModelScope.launch {
            currentButtonLoadingState.value = ButtonLoadingState.Loading
            delay(8000)
            currentButtonLoadingState.value = ButtonLoadingState.NotLoading
        }
    }
}