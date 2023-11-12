package com.example.connect.presentation.ui.auth.user_details

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.useCase.AuthenticationUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.utils.FunctionHelper.getUserId
import com.example.connect.presentation.utils.enums.ButtonLoadingEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(private val authenticationUseCase: AuthenticationUseCase) :
    BaseViewModel() {
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingEnum.NotLoading)
    val userNameState = mutableStateOf("")
    val selectedDOBState = mutableLongStateOf(-1)
    val selectedGenderState = mutableStateOf("")
    private val _addUserStateFlow: MutableStateFlow<ResponseState<Int>> =
        MutableStateFlow(ResponseState.none())
    val addUserStateFlow: StateFlow<ResponseState<Int>> get() = _addUserStateFlow


    fun isValidName(): Boolean = userNameState.value.isNotBlank()
    fun isGenderSelected(): Boolean = selectedGenderState.value.isNotBlank()
    fun isDobSelected(): Boolean = selectedDOBState.longValue != -1L
    fun createUserProfile() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _addUserStateFlow.value = ResponseState.loading()
                val formattedUserName = getFormattedUserName()
                //get no of users with name to set user id
                val currentUserByNameResponseState =
                    authenticationUseCase.getUsersFromName(formattedUserName)
                if (currentUserByNameResponseState.status != RequestStatusEnum.EXCEPTION && sharedPreference.deviceId != null) {
                    val createdDate = Date().time
                    val user = UserDetails(
                        fireBaseAuth.currentUser!!.uid,
                        getUserId(formattedUserName, currentUserByNameResponseState.data ?: 0),
                        formattedUserName,
                        selectedGenderState.value,
                        selectedDOBState.longValue,
                        createdDate,
                        createdDate,
                        sharedPreference.deviceId!!
                    )
                    val userDetailsResponseState = authenticationUseCase.addUserToRemote(user)
                    if (userDetailsResponseState.status == RequestStatusEnum.SUCCESS) {
                        authenticationUseCase.addUserToLocalDb(user)
                    }
                    _addUserStateFlow.value = authenticationUseCase.addUserToRemote(user)
                } else {
                    _addUserStateFlow.value =
                        ResponseState.error(currentUserByNameResponseState.message ?: "")
                }
            }
        }
    }

    private fun getFormattedUserName(): String {
        var formattedUserName = ""
        val formattedUserNameList = userNameState.value.trim().split(" ")
        formattedUserNameList.forEach {
            if (it.isNotBlank()) {
                formattedUserName += "$it "
            }
        }
        return formattedUserName.trimEnd().lowercase()
    }


}