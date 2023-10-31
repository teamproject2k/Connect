package com.example.connect.ui.auth.userDetails

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.base.BaseViewModel
import com.example.connect.utils.enums.ButtonLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor() : BaseViewModel() {
    val snackBarMessage = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingState.NotLoading)
    val userName = mutableStateOf("")
    var dob = mutableStateOf("Date of Birth")

    val expanded = mutableStateOf(false)
    val selectedItem = mutableStateOf("Select Gender")
    val itemList = listOf("Male", "Female", "Other")

    fun isValidName(): Boolean = userName.value.isNotBlank()
    fun isGenderSelected(): Boolean = selectedItem.value != "Select Gender"
    fun isDobSelected(): Boolean = dob.value != "Date of Birth"
    fun createUserProfile() {
        viewModelScope.launch {
            currentButtonLoadingState.value = ButtonLoadingState.Loading
            delay(8000)
            currentButtonLoadingState.value = ButtonLoadingState.NotLoading
        }
    }

}