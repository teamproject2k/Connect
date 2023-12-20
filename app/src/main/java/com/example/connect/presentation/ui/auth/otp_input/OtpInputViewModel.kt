package com.example.connect.presentation.ui.auth.otp_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.auth.SendOtpUseCase
import com.example.connect.domain.useCase.auth.VerifyOtpUseCase
import com.example.connect.domain.useCase.device.UpdateDeviceIdOnDbUseCase
import com.example.connect.domain.useCase.device.UpdateDeviceIdOnRemoteUseCase
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.ButtonStateEnum
import com.example.connect.presentation.utils.ConstantsHelper
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OtpInputViewModel @Inject constructor(
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val getUserDetailsFromRemoteUseCase: GetUserDetailsFromRemoteUseCase,
    private val updateDeviceIdOnDbUseCase: UpdateDeviceIdOnDbUseCase,
    private val updateDeviceIdOnRemoteUseCase: UpdateDeviceIdOnRemoteUseCase,
    private val addUserToDbUseCase: AddUserToDbUseCase
) :
    BaseViewModel() {
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonStateEnum.NotLoading)
    val otpState = mutableStateOf(" ".repeat(ConstantsHelper.OTP_CHAR_COUNT))
    val showTimerState = mutableStateOf(true)
    lateinit var verificationId: String
    lateinit var mobileNumber: String
    lateinit var countryCode: String
    private val _verifyOtpStateFlow: MutableStateFlow<ResponseState<FirebaseUser?>> =
        MutableStateFlow(ResponseState.none())
    val verifyOtpStateFlow: StateFlow<ResponseState<FirebaseUser?>> get() = _verifyOtpStateFlow
    private val _getUserDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean?>> =
        MutableStateFlow(ResponseState.none())
    val getUserDetailsStateFlow: StateFlow<ResponseState<UsersBean?>> get() = _getUserDetailsStateFlow

    private val _resendOtpStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>> =
        MutableStateFlow(ResponseState.none())
    val resendOtpStateFlow: StateFlow<ResponseState<Pair<String, String>>> get() = _resendOtpStateFlow

    /**
     * A flow that emits the remaining time in seconds until the OTP expires.
     */
    val timeLeftFlow = flow {
        // Initialize the countdown time with the OTP timeout time.
        var countDownTime = ConstantsHelper.OTP_TIMEOUT_TIME

        // Emit the countdown time every second.
        while (countDownTime >= 0) {
            emit(countDownTime)
            delay(1000)
            countDownTime--
        }

        // When the countdown time reaches 0, set the showTimerState to false.
        showTimerState.value = false
    }

    /**
     * Resends the OTP to the user.
     */
    fun resendOtp() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations.
            withContext(Dispatchers.IO) {
                // Set the resendOtpStateFlow to loading state.
                _resendOtpStateFlow.value = ResponseState.loading()
                // Call the sendOtpUseCase with the country code, mobile number, and resendOtpStateFlow.
                sendOtpUseCase.invoke(
                    countryCode,
                    mobileNumber,
                    _resendOtpStateFlow
                )
            }
        }
    }

    /**
     * Gets the user details from the remote server and adds the user to the database.
     *
     * @param userId The user ID.
     */
    fun getUserDetails(userId: String) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations.
            withContext(Dispatchers.IO) {
                // Set the state of the _getUserDetailsStateFlow to loading.
                _getUserDetailsStateFlow.value = ResponseState.loading()

                // Get the user details from the remote server.
                val userDetailsResponseState = getUserDetailsFromRemoteUseCase.invoke(userId)

                // Check if the response is successful and the data is not null.
                if (userDetailsResponseState.status == RequestStatusEnum.Success && userDetailsResponseState.data != null) {
                    // Add the user to the database.
                    addUserToDbUseCase.invoke(userDetailsResponseState.data)

                    // Check if the current logged in device ID is different from the shared preference device ID.
                    if (userDetailsResponseState.data.currentLoggedInDeviceId != sharedPreference.deviceId
                        && sharedPreference.deviceId != null
                    ) {
                        // Update the device ID on the remote server.
                        val updateDeviceIdOnRemoteResponseState =
                            updateDeviceIdOnRemoteUseCase.invoke(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )

                        // Check if the response is successful.
                        if (updateDeviceIdOnRemoteResponseState.status == RequestStatusEnum.Success) {
                            // Update the device ID on the database.
                            updateDeviceIdOnDbUseCase.invoke(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )
                        }
                    }
                }

                // Set the state of the _getUserDetailsStateFlow to the response state.
                _getUserDetailsStateFlow.value = userDetailsResponseState
            }
        }
    }

    /**
     * Verifies the OTP.
     *
     * @param verificationId The verification ID.
     */
    fun verifyOTP(verificationId: String) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the verifyOtpStateFlow to a loading state.
                _verifyOtpStateFlow.value = ResponseState.loading()

                // Call the verifyOtpUseCase and update the verifyOtpStateFlow with the result.
                _verifyOtpStateFlow.value =
                    verifyOtpUseCase.invoke(verificationId, otpState.value)
            }
        }
    }
}