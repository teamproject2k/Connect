package com.example.connect.presentation.ui.auth.mobile_number

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
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
     * Sends an OTP to the user's mobile number.
     */
    fun sendOTP() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations.
            withContext(Dispatchers.IO) {
                // Update the UI state to loading.
                _sendOtpUIStateFlow.value = ResponseState.loading()

                // Call the sendOtpUseCase with the selected country code, user mobile number, and the UI state flow.
                sendOtpUseCase.invoke(
                    selectedCountryCodeState.value,
                    userMobileNumberState.value,
                    _sendOtpUIStateFlow
                )
            }
        }
    }

    /**
     * Gets the user details from the remote use case and adds the user to the database.
     *
     * @param userId The user id.
     */
    fun getUserDetails(userId: String) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the state of the _getUserDetailsStateFlow to loading.
                _getUserDetailsStateFlow.value = ResponseState.loading()
                // Get the user details from the remote use case.
                val userDetailsResponseState = getUserDetailsFromRemoteUseCase.invoke(userId)
                // Check if the response state is successful and the data is not null.
                if (userDetailsResponseState.status == RequestStatusEnum.SUCCESS && userDetailsResponseState.data != null) {
                    // Add the user to the database using the addUserToDbUseCase.
                    addUserToDbUseCase.invoke(userDetailsResponseState.data)
                    // Check if the current logged in device id is different from the shared preference device id.
                    if (userDetailsResponseState.data.currentLoggedInDeviceId != sharedPreference.deviceId
                        && sharedPreference.deviceId != null
                    ) {
                        // Update the device id on the remote using the updateDeviceIdOnRemoteUseCase.
                        val updateDeviceIdOnRemoteResponseState =
                            updateDeviceIdOnRemoteUseCase.invoke(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )
                        // Check if the update device id on remote response state is successful.
                        if (updateDeviceIdOnRemoteResponseState.status == RequestStatusEnum.SUCCESS) {
                            // Update the device id on the database using the updateDeviceIdOnDbUseCase.
                            updateDeviceIdOnDbUseCase.invoke(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )
                        }
                    }
                }
                // Set the state of the _getUserDetailsStateFlow to the userDetailsResponseState.
                _getUserDetailsStateFlow.value = userDetailsResponseState
            }
        }
    }

    /**
     * Resets the send OTP UI state flow and the get user details state flow to their initial values.
     */
    fun resetStateFlow() {
        // Reset the send OTP UI state flow to its initial value.
        _sendOtpUIStateFlow.value = ResponseState.none()

        // Reset the get user details state flow to its initial value.
        _getUserDetailsStateFlow.value = ResponseState.none()
    }
}