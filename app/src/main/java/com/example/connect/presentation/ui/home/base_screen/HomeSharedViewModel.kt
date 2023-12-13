package com.example.connect.presentation.ui.home.base_screen

import androidx.lifecycle.viewModelScope
import com.example.connect.common.ErrorCodes
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.device.GetDeviceIdFromRemoteUseCase
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromDbUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeSharedViewModel @Inject constructor(
    private val getUserDetailsFromDbUseCase: GetUserDetailsFromDbUseCase,
    private val getUserDetailsFromRemoteUseCase: GetUserDetailsFromRemoteUseCase,
    private val addUserToDbUseCase: AddUserToDbUseCase,
    private val getDeviceIdFromRemoteUseCase: GetDeviceIdFromRemoteUseCase
) :
    BaseViewModel() {
    lateinit var usersBean: UsersBean
    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val userDetailsStateFlow: StateFlow<ResponseState<Nothing>> get() = _userDetailsStateFlow

    private val _deviceIdStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val deviceIdStateFlow: StateFlow<ResponseState<Nothing>> get() = _deviceIdStateFlow

    /**
     * Gets the device ID from the remote server.
     */
    fun getDeviceIdFromRemote() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deviceIdStateFlow.value = ResponseState.loading()
                // Get the current user's Firebase ID.
                val firebaseId = fireBaseAuth.currentUser?.uid
                // If the Firebase ID is not null,
                if (firebaseId != null) {
                    // Get the device ID from the remote server using the Firebase ID.
                    val responseState = getDeviceIdFromRemoteUseCase.invoke(firebaseId)
                    // If the response state is successful,
                    if (responseState.status == RequestStatusEnum.SUCCESS) {
                        // If the device ID from the remote server does not match the device ID in the shared preferences,
                        if (sharedPreference.deviceId != responseState.data) {
                            // Set the device ID state flow to error with the NewLogin error code.
                            _deviceIdStateFlow.value = ResponseState.error(ErrorCodes.NewLogin)
                        } else {
                            // Get the user details.
                            getUserDetails()
                            // Set the device ID state flow to success with null data.
                            _deviceIdStateFlow.value = ResponseState.success(null)
                        }
                    } else {
                        // Set the device ID state flow to error with the response state's message.
                        _deviceIdStateFlow.value = ResponseState.error(responseState.message ?: "")
                    }
                } else {
                    // Set the device ID state flow to error with the NoUserFound error code.
                    _deviceIdStateFlow.value = ResponseState.error(ErrorCodes.NoUserFound)
                }
            }
        }
    }

    /**
     * Gets the user details from the database or the server.
     */
    private fun getUserDetails() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _userDetailsStateFlow.value = ResponseState.loading()

                // Get the current user's Firebase ID.
                val fireBaseId = fireBaseAuth.currentUser?.uid

                // If the Firebase ID is not null, get the user details from the database.
                if (fireBaseId != null) {
                    val userDetails = getUserDetailsFromDbUseCase.invoke(fireBaseId)

                    // If the user details are not null, set the user details and set the user details state flow to success.
                    if (userDetails != null) {
                        usersBean = userDetails
                        _userDetailsStateFlow.value = ResponseState.success(null)

                        // If the user details are null, get the user details from the server.
                    } else {
                        val userDetailsFromServerResponseState =
                            getUserDetailsFromRemoteUseCase.invoke(fireBaseId)

                        // If the user details from the server are successful, add the user to the database and set the user details and set the user details state flow to success.
                        if (userDetailsFromServerResponseState.status == RequestStatusEnum.SUCCESS) {
                            addUserToDbUseCase.invoke(userDetailsFromServerResponseState.data!!)
                            usersBean = userDetailsFromServerResponseState.data
                            _userDetailsStateFlow.value = ResponseState.success(null)
                            // If the user details from the server are not successful, set the user details state flow to error.
                        } else {
                            _userDetailsStateFlow.value = ResponseState.error(
                                userDetailsFromServerResponseState.message ?: ""
                            )
                        }
                    }
                    // If the Firebase ID is null, set the user details state flow to error.
                } else {
                    _userDetailsStateFlow.value = ResponseState.error(ErrorCodes.NoUserFound)
                }
            }
        }
    }

}