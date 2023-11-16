package com.example.connect.presentation.ui.home.editProfile

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.utils.enums.ButtonLoadingEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class EditProfileViewModel @Inject constructor() : BaseViewModel() {
    lateinit var userNameState: MutableState<String>
    lateinit var userBioState: MutableState<String>
    lateinit var selectedDOBState: MutableLongState
    lateinit var selectedGenderState: MutableState<String>
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingEnum.NotLoading)
    var isDataInitialized = false
    fun initializeStates(userDetails: UserDetails) {
        userNameState = mutableStateOf(userDetails.name)
        userBioState = mutableStateOf(userDetails.bio)
        selectedGenderState = mutableStateOf(userDetails.gender)
        selectedDOBState = mutableLongStateOf(userDetails.dateOfBirth)
        isDataInitialized = true
    }

    fun updateUserProfile() {
       currentButtonLoadingState.value = ButtonLoadingEnum.Loading
    }
}