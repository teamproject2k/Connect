package com.teamproject2k.connect.presentation.ui.auth.mobile_number

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.use_case.auth.SendOtpUseCase
import com.teamproject2k.connect.domain.use_case.device.UpdateDeviceIdOnLocalUseCase
import com.teamproject2k.connect.domain.use_case.device.UpdateDeviceIdOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.fcm.GetFCMTokenUseCase
import com.teamproject2k.connect.domain.use_case.fcm.UpdateFCMTokenOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.AddUserToLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.GetUserDetailsFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateFcmTokenOnLocalUseCase
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.ui.auth.AuthenticationActivity
import com.teamproject2k.connect.presentation.ui.enums.ButtonStateEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MobileNumberInputViewModel @Inject constructor(
    private val addUserToLocalUseCase: AddUserToLocalUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val getUserDetailsFromRemoteUseCase: GetUserDetailsFromRemoteUseCase,
    private val updateDeviceIdOnLocalUseCase: UpdateDeviceIdOnLocalUseCase,
    private val updateDeviceIdOnRemoteUseCase: UpdateDeviceIdOnRemoteUseCase,
    private val getFCMTokenUseCase: GetFCMTokenUseCase,
    private val updateFCMTokenOnRemoteUseCase: UpdateFCMTokenOnRemoteUseCase,
    private val updateFcmTokenOnLocalUseCase: UpdateFcmTokenOnLocalUseCase
) :
    BaseViewModel() {
    val userMobileNumberState = mutableStateOf("")
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonStateEnum.NotLoading)
    val selectedCountryCodeState = mutableStateOf("+91")

    private val _sendOtpUIStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>> =
        MutableStateFlow(ResponseState.none())
    val sendOtpUIStateFlow = _sendOtpUIStateFlow.asStateFlow()

    private val _getUserDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean?>> =
        MutableStateFlow(ResponseState.none())
    val getUserDetailsStateFlow = _getUserDetailsStateFlow.asStateFlow()

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
                sendOtpUseCase(
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
                val userDetailsResponseState = getUserDetailsFromRemoteUseCase(userId)
                // Check if the response state is successful and the data is not null.
                if (userDetailsResponseState.status == RequestStatusEnum.Success && userDetailsResponseState.data != null) {
                    // Add the user to the database using the addUserToLocalUseCase.
                    addUserToLocalUseCase(userDetailsResponseState.data)
                    // Check if the current logged in device id is different from the shared preference device id.
                    if (userDetailsResponseState.data.currentLoggedInDeviceId != sharedPreference.deviceId
                        && sharedPreference.deviceId != null
                    ) {
                        // Update the device id on the remote using the updateDeviceIdOnRemoteUseCase.
                        val updateDeviceIdOnRemoteResponseState =
                            updateDeviceIdOnRemoteUseCase(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )
                        // Check if the update device id on remote response state is successful.
                        if (updateDeviceIdOnRemoteResponseState.status == RequestStatusEnum.Success) {
                            // Update the device id on the database using the updateDeviceIdOnDbUseCase.
                            updateDeviceIdOnLocalUseCase(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )
                        }
                    }
                    val tokenResponseState = getFCMTokenUseCase()
                    if (tokenResponseState.status == RequestStatusEnum.Success && tokenResponseState.data != null) {
                        val responseState = updateFCMTokenOnRemoteUseCase(
                            userDetailsResponseState.data.firebaseUserId,
                            tokenResponseState.data
                        )
                        if (responseState.status == RequestStatusEnum.Success) {
                            updateFcmTokenOnLocalUseCase(
                                userDetailsResponseState.data.firebaseUserId,
                                tokenResponseState.data
                            )
                        }
                    }
                }
                // Set the state of the _getUserDetailsStateFlow to the userDetailsResponseState.
                _getUserDetailsStateFlow.value = userDetailsResponseState
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        AuthenticationActivity.Instance = null
    }
}