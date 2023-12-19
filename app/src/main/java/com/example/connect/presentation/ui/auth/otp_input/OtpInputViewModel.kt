package com.example.connect.presentation.ui.auth.otp_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.models.UsersBean
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
        var countDownTime = ConstantsHelper.OTP_TIMEOUT_TIME
        while (countDownTime >= 0) {
            emit(countDownTime)
            delay(1000)
            countDownTime--
        }
        showTimerState.value = false
    }

    /**
     * Resends the OTP to the user.
     */
    fun resendOtp() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _resendOtpStateFlow.value = ResponseState.loading()
                sendOtpUseCase.invoke(
                    countryCode,
                    mobileNumber,
                    _resendOtpStateFlow
                )
            }
        }
    }

    /**
     * Gets the user details from the remote server.
     *
     * @param userId The user's ID.
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
     * Verifies the OTP entered by the user.
     *
     * @param verificationId The verification ID.
     */
    fun verifyOTP(verificationId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _verifyOtpStateFlow.value = ResponseState.loading()
                _verifyOtpStateFlow.value =
                    verifyOtpUseCase.invoke(verificationId, otpState.value)
            }
        }
    }
}