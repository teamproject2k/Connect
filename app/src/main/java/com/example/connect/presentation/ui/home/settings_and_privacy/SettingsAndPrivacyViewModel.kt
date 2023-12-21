package com.example.connect.presentation.ui.home.settings_and_privacy

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.data.models.user.UsersDbEntity
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.user.UpdateUserDetailsOnDbUseCase
import com.example.connect.domain.useCase.user.UpdateUserDetailsOnRemoteUseCase
import com.example.connect.domain.utils.VisibilityScopeEnum
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.models.VisibilityScope
import com.example.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class SettingsAndPrivacyViewModel @Inject constructor(
    private val updateUserDetailsOnRemoteUseCase: UpdateUserDetailsOnRemoteUseCase,
    private val updateUserDetailsOnDbUseCase: UpdateUserDetailsOnDbUseCase
) : BaseViewModel() {
    lateinit var genderVisibilityScopeList: List<VisibilityScope>
    lateinit var dobVisibilityScopeList: List<VisibilityScope>
    lateinit var friendListVisibilityScopeList: List<VisibilityScope>
    lateinit var genderVisibilityState: MutableState<VisibilityScope>
    lateinit var dobVisibilityState: MutableState<VisibilityScope>
    lateinit var friendListVisibilityState: MutableState<VisibilityScope>

    private val _updateGenderVisibilityStateFlow: MutableStateFlow<ResponseState<Nothing?>> =
        MutableStateFlow(ResponseState.none())
    val updateGenderVisibilityStateFlow: StateFlow<ResponseState<Nothing?>> get() = _updateGenderVisibilityStateFlow

    private val _updateDobVisibilityStateFlow: MutableStateFlow<ResponseState<Nothing?>> =
        MutableStateFlow(ResponseState.none())
    val updateDobVisibilityStateFlow: StateFlow<ResponseState<Nothing?>> get() = _updateDobVisibilityStateFlow

    private val _updateFriendListVisibilityStateFlow: MutableStateFlow<ResponseState<Nothing?>> =
        MutableStateFlow(ResponseState.none())
    val updateFriendListVisibilityStateFlow: StateFlow<ResponseState<Nothing?>> get() = _updateFriendListVisibilityStateFlow

    val snackBarMessageState = mutableStateOf("")
    var isFirstTimeSetup = true

    /**
     * Sets up the data for the user details screen.
     *
     * @param userDetails The user details bean.
     * @param context The context.
     */
    fun setUpData(userDetails: UsersBean, context: Context) {

        // Get the default selected visibility for the gender field.
        val defaultSelectedGenderVisibility =
            getDefaultSelectedVisibility(userDetails.genderVisibility)

        // Get the default selected visibility for the date of birth field.
        val defaultSelectedDobVisibility =
            getDefaultSelectedVisibility(userDetails.dobVisibility)

        // Get the default selected visibility for the friend list field.
        val defaultSelectedFriendListVisibility =
            getDefaultSelectedVisibility(userDetails.friendListVisibility)

        // Get the list of gender visibility options.
        genderVisibilityScopeList = FunctionHelper.getGenderVisibilityList(context)

        // Get the list of date of birth visibility options.
        dobVisibilityScopeList = FunctionHelper.getDobVisibilityList(context)

        // Get the list of friend list visibility options.
        friendListVisibilityScopeList = FunctionHelper.getFriendListVisibilityList(context)

        // Set the initial state of the gender visibility field.
        genderVisibilityState =
            mutableStateOf(genderVisibilityScopeList[defaultSelectedGenderVisibility])

        // Set the initial state of the date of birth visibility field.
        dobVisibilityState = mutableStateOf(dobVisibilityScopeList[defaultSelectedDobVisibility])

        // Set the initial state of the friend list visibility field.
        friendListVisibilityState =
            mutableStateOf(friendListVisibilityScopeList[defaultSelectedFriendListVisibility])

        // Set the flag to indicate that this is not the first time the data is being set up.
        isFirstTimeSetup = false
    }

    /**
     * Gets the default selected visibility based on the visibility string.
     *
     * @param visibility The visibility string.
     * @return The default selected visibility.
     */
    private fun getDefaultSelectedVisibility(visibility: String): Int {
        // Get the default selected visibility based on the visibility string.
        return when (visibility) {
            // If the visibility string is "Public", return 0.
            VisibilityScopeEnum.Public.name -> 0

            // If the visibility string is "FriendsOnly", return 1.
            VisibilityScopeEnum.FriendsOnly.name -> 1

            // If the visibility string is "Private", return 2.
            VisibilityScopeEnum.Private.name -> 2

            // Otherwise, return -1.
            else -> -1
        }
    }

    /**
     * Updates the gender visibility on the remote and local databases.
     *
     * @param firebaseUserId The firebase user id.
     */
    fun updateGenderVisibility(firebaseUserId: String) {
        // Launch a coroutine to update the gender visibility on the remote and local databases.
        viewModelScope.launch {
            // Perform the update on the remote database.
            withContext(Dispatchers.IO) {
                // Set the loading state.
                _updateGenderVisibilityStateFlow.value = ResponseState.loading()

                // Call the updateUserDetailsOnRemoteUseCase to update the gender visibility on the remote database.
                val result = updateUserDetailsOnRemoteUseCase.invoke(
                    // Create a map of the gender visibility field and its new value.
                    mutableMapOf(
                        UserRemoteEntity::genderVisibility.name to genderVisibilityState.value.scopeEnum.name
                    ),
                    // Pass the firebase user id.
                    firebaseUserId
                )

                // Check if the update was successful.
                if (result.status == RequestStatusEnum.Success) {
                    // Update the gender visibility on the local database.
                    updateUserDetailsOnDbUseCase.invoke(
                        // Create a map of the gender visibility field and its new value.
                        mutableMapOf(
                            UsersDbEntity::genderVisibility.name to genderVisibilityState.value.scopeEnum.name
                        ),
                        // Pass the firebase user id.
                        firebaseUserId
                    )

                    // Set the success state.
                    _updateGenderVisibilityStateFlow.value = result
                }
            }
        }
    }

    /**
     * Updates the user's DOB visibility.
     *
     * @param firebaseUserId The firebaseUserId of the user.
     */
    fun updateDobVisibility(firebaseUserId: String) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations.
            withContext(Dispatchers.IO) {
                // Set the loading state.
                _updateDobVisibilityStateFlow.value = ResponseState.loading()
                // Call the updateUserDetailsOnRemoteUseCase to update the user's details on the remote server.
                val result = updateUserDetailsOnRemoteUseCase.invoke(
                    // Create a map of the user's details to be updated.
                    mutableMapOf(
                        // The key is the name of the field to be updated.
                        UserRemoteEntity::dobVisibility.name to dobVisibilityState.value.scopeEnum.name
                    ),
                    // The firebaseUserId of the user.
                    firebaseUserId
                )
                // Check if the result is successful.
                if (result.status == RequestStatusEnum.Success) {
                    // Call the updateUserDetailsOnDbUseCase to update the user's details in the local database.
                    updateUserDetailsOnDbUseCase.invoke(
                        // Create a map of the user's details to be updated.
                        mutableMapOf(
                            // The key is the name of the field to be updated.
                            UsersDbEntity::dobVisibility.name to dobVisibilityState.value.scopeEnum.name
                        ),
                        // The firebaseUserId of the user.
                        firebaseUserId
                    )
                    // Set the success state.
                    _updateDobVisibilityStateFlow.value = result
                }
            }
        }
    }

    /**
     * Updates the friend list visibility of the current user.
     *
     * @param firebaseUserId The firebase user ID of the current user.
     */
    fun updateFriendListVisibility(firebaseUserId: String) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations.
            withContext(Dispatchers.IO) {
                // Set the loading state.
                _updateFriendListVisibilityStateFlow.value = ResponseState.loading()

                // Call the updateUserDetailsOnRemoteUseCase to update the user details on the remote server.
                val result = updateUserDetailsOnRemoteUseCase.invoke(
                    // Create a map of the user details to be updated.
                    mutableMapOf(
                        // The friend list visibility field.
                        UserRemoteEntity::friendListVisibility.name to friendListVisibilityState.value.scopeEnum.name
                    ),
                    // The firebase user ID.
                    firebaseUserId
                )

                // Check if the result is successful.
                if (result.status == RequestStatusEnum.Success) {
                    // Call the updateUserDetailsOnDbUseCase to update the user details in the local database.
                    updateUserDetailsOnDbUseCase.invoke(
                        // Create a map of the user details to be updated.
                        mutableMapOf(
                            // The friend list visibility field.
                            UsersDbEntity::friendListVisibility.name to friendListVisibilityState.value.scopeEnum.name
                        ),
                        // The firebase user ID.
                        firebaseUserId
                    )

                    // Set the success state.
                    _updateFriendListVisibilityStateFlow.value = result
                }
            }
        }
    }
}