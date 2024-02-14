package com.teamproject2k.connect.presentation.ui.home.base_screen

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.useCase.device.GetDeviceIdFromRemoteUseCase
import com.teamproject2k.connect.domain.useCase.user.AddUserToLocalUseCase
import com.teamproject2k.connect.domain.useCase.user.GetUserDetailsFromLocalUseCase
import com.teamproject2k.connect.domain.useCase.user.GetUserDetailsFromRemoteUseCase
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeSharedViewModel @Inject constructor(
    private val getUserDetailsFromLocalUseCase: GetUserDetailsFromLocalUseCase,
    private val getUserDetailsFromRemoteUseCase: GetUserDetailsFromRemoteUseCase,
    private val addUserToLocalUseCase: AddUserToLocalUseCase,
    private val getDeviceIdFromRemoteUseCase: GetDeviceIdFromRemoteUseCase,
) :
    BaseViewModel() {
    lateinit var usersDetails: UsersBean
    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val userDetailsStateFlow: StateFlow<ResponseState<Nothing>> get() = _userDetailsStateFlow

    private val _deviceIdStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val deviceIdStateFlow: StateFlow<ResponseState<Nothing>> get() = _deviceIdStateFlow

    val isBottomBarHiddenState = mutableStateOf(false)

    /**
     * Gets the device ID from the remote server.
     */
    fun getDeviceIdFromRemote(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deviceIdStateFlow.value = ResponseState.loading()
                // Get the current user's Firebase ID.
                val firebaseId = fireBaseAuth.currentUser?.uid
                // If the Firebase ID is not null,
                if (firebaseId != null) {
                    // Get the device ID from the remote server using the Firebase ID.
                    val responseState = getDeviceIdFromRemoteUseCase(firebaseId)
                    // If the response state is successful,
                    if (responseState.status == RequestStatusEnum.Success) {
                        // If the device ID from the remote server does not match the device ID in the shared preferences,
                        if (sharedPreference.deviceId != responseState.data) {
                            // Set the device ID state flow to error with the NewLogin error code.
                            _deviceIdStateFlow.value =
                                ResponseState.error(FirebaseErrorCodes.NEW_LOGIN)
                        } else {
                            // Get the user details.
                            getUserDetails(context)
                            // Set the device ID state flow to success with null data.
                            _deviceIdStateFlow.value = ResponseState.success(null)
                        }
                    } else {
                        // Set the device ID state flow to error with the response state's message.
                        _deviceIdStateFlow.value = ResponseState.error(responseState.message ?: "")
                    }
                } else {
                    // Set the device ID state flow to error with the NoUserFound error code.
                    _deviceIdStateFlow.value = ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }
        }
    }


    /**
     * Gets the user details from the database or the server.
     */
    fun getUserDetails(context: Context) {
        // Launch a coroutine in the viewModelScope
        viewModelScope.launch {
            // Perform operations on the IO dispatcher
            withContext(Dispatchers.IO) {
                // Set the user details state flow to loading
                _userDetailsStateFlow.value = ResponseState.loading()

                // Get the current user's Firebase ID
                val fireBaseId = fireBaseAuth.currentUser?.uid

                // Check if the Firebase ID is not null
                if (fireBaseId != null) {
                    // Check if the device is connected to the internet
                    if (context.isNetworkAvailable()) {
                        // Get the user details from the server
                        val userDetailsFromServerResponseState =
                            getUserDetailsFromRemoteUseCase(fireBaseId)

                        // Check if the response state is successful
                        if (userDetailsFromServerResponseState.status == RequestStatusEnum.Success) {
                            // Add the user to the database
                            addUserToLocalUseCase(userDetailsFromServerResponseState.data!!)

                            // Update the usersDetails variable
                            usersDetails = userDetailsFromServerResponseState.data

                            // Set the user details state flow to success
                            _userDetailsStateFlow.value = ResponseState.success(null)
                        } else {
                            // Set the user details state flow to error
                            _userDetailsStateFlow.value = ResponseState.error(
                                userDetailsFromServerResponseState.message ?: ""
                            )
                        }
                    } else {
                        // Get the user details from the database
                        val userDetails = getUserDetailsFromLocalUseCase(fireBaseId)

                        // Check if the user details are not null
                        if (userDetails != null) {
                            // Update the usersDetails variable
                            usersDetails = userDetails

                            // Set the user details state flow to success
                            _userDetailsStateFlow.value = ResponseState.success(null)
                        } else {
                            // Set the user details state flow to error
                            _userDetailsStateFlow.value =
                                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                        }
                    }
                } else {
                    // Set the user details state flow to error
                    _userDetailsStateFlow.value =
                        ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }
        }
    }
}