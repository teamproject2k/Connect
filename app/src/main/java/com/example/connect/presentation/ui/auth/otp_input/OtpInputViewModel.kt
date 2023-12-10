package com.example.connect.presentation.ui.auth.otp_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.useCase.AuthenticationUseCase
import com.example.connect.domain.useCase.VerifyOtpUseCase
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
    private val authenticationUseCase: AuthenticationUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase
) :
    BaseViewModel() {
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonStateEnum.NotLoading)
    val otpState = mutableStateOf(" ".repeat(ConstantsHelper.OTPCharCount))
    val showTimerState = mutableStateOf(true)
    lateinit var verificationId: String
    lateinit var mobileNumber: String
    lateinit var countryCode: String
    private val _verifyOtpStateFlow: MutableStateFlow<ResponseState<FirebaseUser?>> =
        MutableStateFlow(ResponseState.none())
    val verifyOtpStateFlow: StateFlow<ResponseState<FirebaseUser?>> get() = _verifyOtpStateFlow
    private val _getUserDetailsStateFlow: MutableStateFlow<ResponseState<UserDetails?>> =
        MutableStateFlow(ResponseState.none())
    val getUserDetailsStateFlow: StateFlow<ResponseState<UserDetails?>> get() = _getUserDetailsStateFlow

    private val _resendOtpStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>> =
        MutableStateFlow(ResponseState.none())
    val resendOtpStateFlow: StateFlow<ResponseState<Pair<String, String>>> get() = _resendOtpStateFlow


    val timeLeftFlow = flow {
        var countDownTime = ConstantsHelper.OTPTimeOutTime
        while (countDownTime >= 0) {
            emit(countDownTime)
            delay(1000)
            countDownTime--
        }
        showTimerState.value = false
    }


    fun resendOtp() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _resendOtpStateFlow.value = ResponseState.loading()
                authenticationUseCase.sendOtp(
                    countryCode,
                    mobileNumber,
                    _resendOtpStateFlow
                )
            }
        }
    }


    fun getUserDetails(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getUserDetailsStateFlow.value = ResponseState.loading()
                val userDetailsResponseState = authenticationUseCase.getUserDetails(userId)
                if (userDetailsResponseState.status == RequestStatusEnum.SUCCESS && userDetailsResponseState.data != null) {
                    authenticationUseCase.addUserToLocalDb(userDetailsResponseState.data)
                    if (userDetailsResponseState.data.currentLoggedInDeviceId != sharedPreference.deviceId
                        && sharedPreference.deviceId != null
                    ) {
                        val updateDeviceIdOnRemoteResponseState =
                            authenticationUseCase.updateDeviceIdOnRemote(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )
                        if (updateDeviceIdOnRemoteResponseState.status == RequestStatusEnum.SUCCESS) {
                            authenticationUseCase.updateDeviceIdOnLocal(
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