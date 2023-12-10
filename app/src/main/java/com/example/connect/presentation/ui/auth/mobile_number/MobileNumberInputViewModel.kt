package com.example.connect.presentation.ui.auth.mobile_number

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.useCase.AddUserToDbUseCase
import com.example.connect.domain.useCase.GetUserDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.SendOtpUseCase
import com.example.connect.domain.useCase.UpdateDeviceIdOnDbUseCase
import com.example.connect.domain.useCase.UpdateDeviceIdOnRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.ButtonStateEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MobileNumberInputViewModel @Inject constructor(
    private val addUserToDbUseCase: AddUserToDbUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val getUserDetailsFromRemoteUseCase: GetUserDetailsFromRemoteUseCase,
    private val updateDeviceIdOnDbUseCase: UpdateDeviceIdOnDbUseCase,
    private val updateDeviceIdOnRemoteUseCase: UpdateDeviceIdOnRemoteUseCase
) :
    BaseViewModel() {
    val userMobileNumberState = mutableStateOf("")
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonStateEnum.NotLoading)
    val selectedCountryCodeState = mutableStateOf("+91")

    private val _sendOtpUIStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>> =
        MutableStateFlow(ResponseState.none())
    val sendOtpUIStateFlow: StateFlow<ResponseState<Pair<String, String>>> get() = _sendOtpUIStateFlow

    private val _getUserDetailsStateFlow: MutableStateFlow<ResponseState<UserDetails?>> =
        MutableStateFlow(ResponseState.none())
    val getUserDetailsStateFlow: StateFlow<ResponseState<UserDetails?>> get() = _getUserDetailsStateFlow

    /**
     * Sends OTP to the user's mobile number.
     */
    fun sendOTP() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendOtpUIStateFlow.value = ResponseState.loading()
                sendOtpUseCase.invoke(
                    selectedCountryCodeState.value,
                    userMobileNumberState.value,
                    _sendOtpUIStateFlow
                )
            }
        }
    }


    /**
     * Gets user details from remote and updates the user in the database.
     *
     * @param userId The user ID.
     */
    fun getUserDetails(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getUserDetailsStateFlow.value = ResponseState.loading()
                val userDetailsResponseState = getUserDetailsFromRemoteUseCase.invoke(userId)
                if (userDetailsResponseState.status == RequestStatusEnum.SUCCESS && userDetailsResponseState.data != null) {
                    addUserToDbUseCase.invoke(userDetailsResponseState.data)
                    if (userDetailsResponseState.data.currentLoggedInDeviceId != sharedPreference.deviceId
                        && sharedPreference.deviceId != null
                    ) {
                        val updateDeviceIdOnRemoteResponseState =
                            updateDeviceIdOnRemoteUseCase.invoke(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )
                        if (updateDeviceIdOnRemoteResponseState.status == RequestStatusEnum.SUCCESS) {
                            updateDeviceIdOnDbUseCase.invoke(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )
                        }
                    }
                }
                _getUserDetailsStateFlow.value = userDetailsResponseState
            }
        }
    }

    /**
     * Resets the state of the UI state flows.
     */
    fun resetStateFlow() {
        _sendOtpUIStateFlow.value = ResponseState.none()
        _getUserDetailsStateFlow.value = ResponseState.none()
    }

}