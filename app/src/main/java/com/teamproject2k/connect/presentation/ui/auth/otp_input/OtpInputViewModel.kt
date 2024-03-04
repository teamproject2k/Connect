package com.teamproject2k.connect.presentation.ui.auth.otp_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.auth.SendOtpUseCase
import com.teamproject2k.connect.domain.use_case.auth.VerifyOtpUseCase
import com.teamproject2k.connect.domain.use_case.device.UpdateDeviceIdOnLocalUseCase
import com.teamproject2k.connect.domain.use_case.device.UpdateDeviceIdOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.fcm.GetFCMTokenUseCase
import com.teamproject2k.connect.domain.use_case.fcm.UpdateFCMTokenOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.AddUserToLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.GetUserDetailsFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateFcmTokenOnLocalUseCase
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.ui.enums.ButtonStateEnum
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OtpInputViewModel @Inject constructor(
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val getUserDetailsFromRemoteUseCase: GetUserDetailsFromRemoteUseCase,
    private val updateDeviceIdOnLocalUseCase: UpdateDeviceIdOnLocalUseCase,
    private val updateDeviceIdOnRemoteUseCase: UpdateDeviceIdOnRemoteUseCase,
    private val addUserToLocalUseCase: AddUserToLocalUseCase,
    private val getFCMTokenUseCase: GetFCMTokenUseCase,
    private val updateFCMTokenOnRemoteUseCase: UpdateFCMTokenOnRemoteUseCase,
    private val updateFcmTokenOnLocalUseCase: UpdateFcmTokenOnLocalUseCase
) :
    BaseViewModel() {

    lateinit var verificationId: String
    lateinit var mobileNumber: String
    lateinit var countryCode: String

    var isDataInitialized = false

    val snackBarMessageState = mutableStateOf("")
    val otpState = mutableStateOf(" ".repeat(ConstantsHelper.OTP_CHAR_COUNT))
    val currentButtonLoadingState = mutableStateOf(ButtonStateEnum.NotLoading)
    val showTimerState = mutableStateOf(true)

    private val _verifyOtpStateFlow: MutableStateFlow<ResponseState<FirebaseUser?>> =
        MutableStateFlow(ResponseState.none())
    val verifyOtpStateFlow: StateFlow<ResponseState<FirebaseUser?>> get() = _verifyOtpStateFlow

    private val _getUserDetailsStateFlow: MutableStateFlow<ResponseState<UserBean?>> =
        MutableStateFlow(ResponseState.none())
    val getUserDetailsStateFlow = _getUserDetailsStateFlow.asStateFlow()

    private val _resendOtpStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>> =
        MutableStateFlow(ResponseState.none())
    val resendOtpStateFlow = _resendOtpStateFlow.asStateFlow()

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
     * Initializes the data required.
     *
     * @param mobileNumber The mobile number to be verified.
     * @param verificationId The verification ID obtained from the verification process.
     * @param countryCode The country code associated with the mobile number.
     */
    fun initializeData(mobileNumber: String, verificationId: String, countryCode: String) {
        this.mobileNumber = mobileNumber
        this.verificationId = verificationId
        this.countryCode = countryCode
        isDataInitialized = true
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
                sendOtpUseCase(
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
                val userDetailsResponseState = getUserDetailsFromRemoteUseCase(userId)

                // Check if the response is successful and the data is not null.
                if (userDetailsResponseState.status == RequestStatusEnum.Success && userDetailsResponseState.data != null) {
                    // Add the user to the database.
                    addUserToLocalUseCase(userDetailsResponseState.data)

                    // Check if the current logged in device ID is different from the shared preference device ID.
                    if (userDetailsResponseState.data.currentLoggedInDeviceId != sharedPreference.deviceId
                        && sharedPreference.deviceId != null
                    ) {
                        // Update the device ID on the remote server.
                        val updateDeviceIdOnRemoteResponseState =
                            updateDeviceIdOnRemoteUseCase(
                                userDetailsResponseState.data.firebaseUserId,
                                sharedPreference.deviceId!!
                            )

                        // Check if the response is successful.
                        if (updateDeviceIdOnRemoteResponseState.status == RequestStatusEnum.Success) {
                            // Update the device ID on the database.
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
                    verifyOtpUseCase(verificationId, otpState.value)
            }
        }
    }
}