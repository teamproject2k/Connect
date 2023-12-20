package com.example.connect.presentation.ui.auth.mobile_number

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.auth.SendOtpUseCase
import com.example.connect.domain.useCase.device.UpdateDeviceIdOnDbUseCase
import com.example.connect.domain.useCase.device.UpdateDeviceIdOnRemoteUseCase
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromRemoteUseCase
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

    private val _getUserDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean?>> =
        MutableStateFlow(ResponseState.none())
    val getUserDetailsStateFlow: StateFlow<ResponseState<UsersBean?>> get() = _getUserDetailsStateFlow

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
                if (userDetailsResponseState.status == RequestStatusEnum.Success && userDetailsResponseState.data != null) {
                    addUserToDbUseCase.invoke(userDetailsResponseState.data)
                    if (userDetailsResponseState.data.currentLoggedInDeviceId != sharedPreference.deviceId
                        && sharedPreference.deviceId != null
                    ) {
                        val updateDeviceIdOnRemoteResponseState =
                            updateDeviceIdOnRemoteUseCase.invoke(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )
                        if (updateDeviceIdOnRemoteResponseState.status == RequestStatusEnum.Success) {
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