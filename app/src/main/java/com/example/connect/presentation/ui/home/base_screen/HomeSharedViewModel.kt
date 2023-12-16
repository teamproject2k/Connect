package com.example.connect.presentation.ui.home.base_screen

import android.content.Context
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
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
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
    lateinit var usersDetails: UsersBean
    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val userDetailsStateFlow: StateFlow<ResponseState<Nothing>> get() = _userDetailsStateFlow

    private val _deviceIdStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val deviceIdStateFlow: StateFlow<ResponseState<Nothing>> get() = _deviceIdStateFlow

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
                    val responseState = getDeviceIdFromRemoteUseCase.invoke(firebaseId)
                    // If the response state is successful,
                    if (responseState.status == RequestStatusEnum.SUCCESS) {
                        // If the device ID from the remote server does not match the device ID in the shared preferences,
                        if (sharedPreference.deviceId != responseState.data) {
                            // Set the device ID state flow to error with the NewLogin error code.
                            _deviceIdStateFlow.value = ResponseState.error(ErrorCodes.NewLogin)
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
                    _deviceIdStateFlow.value = ResponseState.error(ErrorCodes.NoUserFound)
                }
            }
        }
    }

    /**
     * Gets the user details from the database or the server.
     */
    fun getUserDetails(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _userDetailsStateFlow.value = ResponseState.loading()

                val fireBaseId = fireBaseAuth.currentUser?.uid

                if (fireBaseId != null) {
                    if (context.isNetworkAvailable()) {
                        val userDetailsFromServerResponseState =
                            getUserDetailsFromRemoteUseCase.invoke(fireBaseId)

                        if (userDetailsFromServerResponseState.status == RequestStatusEnum.SUCCESS) {
                            addUserToDbUseCase.invoke(userDetailsFromServerResponseState.data!!)
                            usersDetails = userDetailsFromServerResponseState.data
                            _userDetailsStateFlow.value = ResponseState.success(null)
                        } else {
                            _userDetailsStateFlow.value = ResponseState.error(
                                userDetailsFromServerResponseState.message ?: ""
                            )
                        }
                    } else {
                        val userDetails = getUserDetailsFromDbUseCase.invoke(fireBaseId)

                        if (userDetails != null) {
                            usersDetails = userDetails
                            _userDetailsStateFlow.value = ResponseState.success(null)
                        } else {
                            _userDetailsStateFlow.value =
                                ResponseState.error(ErrorCodes.NoUserFound)
                        }
                    }
                } else {
                    _userDetailsStateFlow.value = ResponseState.error(ErrorCodes.NoUserFound)
                }
            }
        }
    }

}