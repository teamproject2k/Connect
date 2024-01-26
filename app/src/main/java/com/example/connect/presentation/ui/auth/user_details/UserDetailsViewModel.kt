package com.example.connect.presentation.ui.auth.user_details

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.fcm.GetFCMTokenUseCase
import com.example.connect.domain.useCase.user.AddUserToLocalUseCase
import com.example.connect.domain.useCase.user.AddUserToRemoteUseCase
import com.example.connect.domain.useCase.user.GetUsersCountFromNameInitialsFromRemoteUseCase
import com.example.connect.domain.utils.VisibilityScopeEnum
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.ButtonStateEnum
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.getUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val addUserToRemoteUseCase: AddUserToRemoteUseCase,
    private val addUserToLocalUseCase: AddUserToLocalUseCase,
    private val getUsersFromNameUseCase: GetUsersCountFromNameInitialsFromRemoteUseCase,
    private val getFCMTokenUseCase: GetFCMTokenUseCase
) :
    BaseViewModel() {
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonStateEnum.NotLoading)
    val userNameState = mutableStateOf("")
    val selectedDOBState = mutableLongStateOf(-1)
    val selectedGenderState = mutableStateOf("")
    private val _addUserStateFlow: MutableStateFlow<ResponseState<Int>> =
        MutableStateFlow(ResponseState.none())
    val addUserStateFlow = _addUserStateFlow.asStateFlow()

    /**
     * Creates a user profile.
     */
    fun createUserProfile() {
        // Create a coroutine scope to run the asynchronous tasks.
        viewModelScope.launch {
            // Run the tasks in the IO dispatcher to avoid blocking the main thread.
            withContext(Dispatchers.IO) {
                // Set the loading state of the user profile creation process.
                _addUserStateFlow.value = ResponseState.loading()

                // Get the formatted user name.
                val lowerCaseUserNameWithoutAnyExtraSpace =
                    FunctionHelper.getLowerCaseTextWithOutExtraSpace(userNameState.value)

                // Get the number of users with the same name to set the user ID.
                val currentUserByNameResponseState =
                    getUsersFromNameUseCase.invoke(
                        FunctionHelper.getConnectIdFirstPart(
                            lowerCaseUserNameWithoutAnyExtraSpace
                        )
                    )

                // Check if the response is not an exception and the device ID is not null.
                if (currentUserByNameResponseState.status != RequestStatusEnum.Exception && sharedPreference.deviceId != null) {
                    // Get the current time in milliseconds.
                    val createdDate = FunctionHelper.getCurrentTimeInMillis()
                    val fcmTokenResponseState = getFCMTokenUseCase.invoke()
                    if (fcmTokenResponseState.status == RequestStatusEnum.Success && !fcmTokenResponseState.data.isNullOrBlank()) {
                        // Create a user object with the user's information.
                        val user = UsersBean(
                            fireBaseAuth.currentUser!!.uid,
                            getUserId(
                                lowerCaseUserNameWithoutAnyExtraSpace,
                                currentUserByNameResponseState.data ?: 0
                            ),
                            fcmTokenResponseState.data,
                            sharedPreference.mobileNumber,
                            lowerCaseUserNameWithoutAnyExtraSpace,
                            selectedGenderState.value,
                            selectedDOBState.longValue,
                            createdDate,
                            createdDate,
                            sharedPreference.deviceId!!,
                            "Connect User",
                            genderVisibility = VisibilityScopeEnum.Public.name,
                            dobVisibility = VisibilityScopeEnum.Public.name,
                            friendListVisibility = VisibilityScopeEnum.Public.name
                        )

                        // Add the user to the remote database.
                        val userDetailsResponseState = addUserToRemoteUseCase.invoke(user)

                        // Check if the response is successful.
                        if (userDetailsResponseState.status == RequestStatusEnum.Success) {
                            // Add the user to the local database.
                            addUserToLocalUseCase.invoke(user)
                        }

                        // Set the response state of the user profile creation process.
                        _addUserStateFlow.value = userDetailsResponseState
                    } else {
                        // Set the error state of the user profile creation process.
                        _addUserStateFlow.value =
                            ResponseState.error(fcmTokenResponseState.message ?: "")
                    }
                } else {
                    // Set the error state of the user profile creation process.
                    _addUserStateFlow.value =
                        ResponseState.error(currentUserByNameResponseState.message ?: "")
                }
            }
        }
    }
}