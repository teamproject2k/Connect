package com.example.connect.presentation.ui.auth.userDetails

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.utils.enums.ButtonLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor() : BaseViewModel() {
    val snackBarMessage = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingState.NotLoading)
    val userName = mutableStateOf("")
    var selectedDOBState = mutableStateOf("")
    var selectedGenderState = mutableStateOf("")


    fun isValidName(): Boolean = userName.value.isNotBlank()
    fun isGenderSelected(): Boolean = selectedGenderState.value.isNotBlank()
    fun isDobSelected(): Boolean = selectedDOBState.value.isNotBlank()
    fun createUserProfile() {
        viewModelScope.launch {
            currentButtonLoadingState.value = ButtonLoadingState.Loading
            delay(8000)
            currentButtonLoadingState.value = ButtonLoadingState.NotLoading
        }
    }

}