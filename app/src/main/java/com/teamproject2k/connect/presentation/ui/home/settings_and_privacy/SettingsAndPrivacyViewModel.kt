package com.teamproject2k.connect.presentation.ui.home.settings_and_privacy

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.data.models.user.UserRemoteEntity
import com.teamproject2k.connect.data.models.user.UsersLocalEntity
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.user.UpdateUserDetailsOnLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateUserDetailsOnRemoteUseCase
import com.teamproject2k.connect.domain.utils.VisibilityScopeEnum
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.ui.models.VisibilityScope
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsAndPrivacyViewModel @Inject constructor(
    private val updateUserDetailsOnRemoteUseCase: UpdateUserDetailsOnRemoteUseCase,
    private val updateUserDetailsOnLocalUseCase: UpdateUserDetailsOnLocalUseCase
) : BaseViewModel() {

    lateinit var genderVisibilityScopeList: List<VisibilityScope>
    lateinit var dobVisibilityScopeList: List<VisibilityScope>
    lateinit var friendListVisibilityScopeList: List<VisibilityScope>
    lateinit var genderVisibilityState: MutableState<VisibilityScope>
    lateinit var dobVisibilityState: MutableState<VisibilityScope>
    lateinit var friendListVisibilityState: MutableState<VisibilityScope>

    var isFirstTimeSetup = true

    val snackBarMessageState = mutableStateOf("")

    private val _updateGenderVisibilityStateFlow: MutableStateFlow<ResponseState<VisibilityScope>> =
        MutableStateFlow(ResponseState.none())
    val updateGenderVisibilityStateFlow = _updateGenderVisibilityStateFlow.asStateFlow()

    private val _updateDobVisibilityStateFlow: MutableStateFlow<ResponseState<VisibilityScope>> =
        MutableStateFlow(ResponseState.none())
    val updateDobVisibilityStateFlow = _updateDobVisibilityStateFlow.asStateFlow()

    private val _updateFriendListVisibilityStateFlow: MutableStateFlow<ResponseState<VisibilityScope>> =
        MutableStateFlow(ResponseState.none())
    val updateFriendListVisibilityStateFlow = _updateFriendListVisibilityStateFlow.asStateFlow()

    /**
     * Sets up the data for the user details screen.
     *
     * @param userDetails The user details bean.
     * @param context The context.
     */
    fun setUpData(userDetails: UserBean, context: Context) {

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
    fun updateGenderVisibility(firebaseUserId: String, genderScope: VisibilityScope) {
        // Launch a coroutine to update the gender visibility on the remote and local databases.
        viewModelScope.launch {
            // Perform the update on the remote database.
            withContext(Dispatchers.IO) {
                // Set the loading state.
                _updateGenderVisibilityStateFlow.value = ResponseState.loading()

                // Call the updateUserDetailsOnRemoteUseCase to update the gender visibility on the remote database.
                val result = updateUserDetailsOnRemoteUseCase(
                    // Create a map of the gender visibility field and its new value.
                    mutableMapOf(
                        UserRemoteEntity::genderVisibility.name to genderScope.scopeEnum.name
                    ),
                    // Pass the firebase user id.
                    firebaseUserId
                )

                // Check if the update was successful.
                if (result.status == RequestStatusEnum.Success) {
                    // Update the gender visibility on the local database.
                    updateUserDetailsOnLocalUseCase(
                        // Create a map of the gender visibility field and its new value.
                        mutableMapOf(
                            UsersLocalEntity::genderVisibility.name to genderScope.scopeEnum.name
                        ),
                        // Pass the firebase user id.
                        firebaseUserId
                    )

                    // Set the success state.
                    _updateGenderVisibilityStateFlow.value = ResponseState.success(genderScope)
                } else {
                    _updateGenderVisibilityStateFlow.value =
                        ResponseState.error(result.message ?: "")
                }
            }
        }
    }

    /**
     * Updates the user's DOB visibility.
     *
     * @param firebaseUserId The firebaseUserId of the user.
     */
    fun updateDobVisibility(firebaseUserId: String, dobScope: VisibilityScope) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations.
            withContext(Dispatchers.IO) {
                // Set the loading state.
                _updateDobVisibilityStateFlow.value = ResponseState.loading()
                // Call the updateUserDetailsOnRemoteUseCase to update the user's details on the remote server.
                val result = updateUserDetailsOnRemoteUseCase(
                    // Create a map of the user's details to be updated.
                    mutableMapOf(
                        // The key is the name of the field to be updated.
                        UserRemoteEntity::dobVisibility.name to dobScope.scopeEnum.name
                    ),
                    // The firebaseUserId of the user.
                    firebaseUserId
                )
                // Check if the result is successful.
                if (result.status == RequestStatusEnum.Success) {
                    // Call the updateUserDetailsOnLocalUseCase to update the user's details in the local database.
                    updateUserDetailsOnLocalUseCase(
                        // Create a map of the user's details to be updated.
                        mutableMapOf(
                            // The key is the name of the field to be updated.
                            UsersLocalEntity::dobVisibility.name to dobScope.scopeEnum.name
                        ),
                        // The firebaseUserId of the user.
                        firebaseUserId
                    )
                    // Set the success state.
                    _updateDobVisibilityStateFlow.value = ResponseState.success(dobScope)
                } else {
                    _updateDobVisibilityStateFlow.value = ResponseState.error(result.message ?: "")
                }
            }
        }
    }

    /**
     * Updates the friend list visibility of the current user.
     *
     * @param firebaseUserId The firebase user ID of the current user.
     */
    fun updateFriendListVisibility(firebaseUserId: String, friendListScope: VisibilityScope) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations.
            withContext(Dispatchers.IO) {
                // Set the loading state.
                _updateFriendListVisibilityStateFlow.value = ResponseState.loading()

                // Call the updateUserDetailsOnRemoteUseCase to update the user details on the remote server.
                val result = updateUserDetailsOnRemoteUseCase(
                    // Create a map of the user details to be updated.
                    mutableMapOf(
                        // The friend list visibility field.
                        UserRemoteEntity::friendListVisibility.name to friendListScope.scopeEnum.name
                    ),
                    // The firebase user ID.
                    firebaseUserId
                )

                // Check if the result is successful.
                if (result.status == RequestStatusEnum.Success) {
                    // Call the updateUserDetailsOnLocalUseCase to update the user details in the local database.
                    updateUserDetailsOnLocalUseCase(
                        // Create a map of the user details to be updated.
                        mutableMapOf(
                            // The friend list visibility field.
                            UsersLocalEntity::friendListVisibility.name to friendListScope.scopeEnum.name
                        ),
                        // The firebase user ID.
                        firebaseUserId
                    )

                    // Set the success state.
                    _updateFriendListVisibilityStateFlow.value =
                        ResponseState.success(friendListScope)
                } else {
                    _updateFriendListVisibilityStateFlow.value =
                        ResponseState.error(result.message ?: "")
                }
            }
        }
    }
}