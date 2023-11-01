package com.example.connect.presentation.ui.auth.mobile_number

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.utils.enums.ButtonLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MobileNumberInputViewModel @Inject constructor() : BaseViewModel() {
    val userMobileNumberState = mutableStateOf("")
    val snackBarMessage = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingState.NotLoading)

    fun isValidMobileNumber(): Boolean {
        val phoneRegex = "^[0-9]{10}$"
        return phoneRegex.toRegex().matches(userMobileNumberState.value)
    }


    fun sendOTP() {
        viewModelScope.launch {
            currentButtonLoadingState.value = ButtonLoadingState.Loading
            delay(8000)
            currentButtonLoadingState.value = ButtonLoadingState.NotLoading
        }
    }
}