package com.teamproject2k.connect.presentation.ui.home.edit_profile

import android.net.Uri
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.data.models.user.UserRemoteEntity
import com.teamproject2k.connect.data.models.user.UsersLocalEntity
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.use_case.file.UploadFileToRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateUserDetailsOnLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateUserDetailsOnRemoteUseCase
import com.teamproject2k.connect.domain.utils.FirebaseConstants
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.ui.enums.ButtonStateEnum
import com.teamproject2k.connect.presentation.ui.models.MediaData
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val uploadFileToRemoteUseCase: UploadFileToRemoteUseCase,
    private val updateUserDetailsOnRemoteUseCase: UpdateUserDetailsOnRemoteUseCase,
    private val updateUserDetailsOnLocalUseCase: UpdateUserDetailsOnLocalUseCase,
) :
    BaseViewModel() {
    private val _updateUserStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val updateUserStateFlow = _updateUserStateFlow.asStateFlow()
    lateinit var userNameState: MutableState<String>
    lateinit var connectUserIdState: MutableState<String>
    lateinit var userBioState: MutableState<String>
    lateinit var selectedDOBState: MutableLongState
    lateinit var selectedGenderState: MutableState<String>
    lateinit var profilePhotoState: MutableState<MediaData?>
    lateinit var coverPhotoState: MutableState<MediaData?>
    lateinit var userDetails: UsersBean
    var isProfileUri = true
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonStateEnum.NotLoading)
    var isDataInitialized = false

    /**
     * Initializes the states of the user profile.
     *
     * @param userDetails The user details to initialize the states with.
     */
    fun init(userDetails: UsersBean) {
        // Initialize the user details.
        this.userDetails = userDetails

        // Initialize the user name state.
        userNameState = mutableStateOf(userDetails.name)

        // Initialize the user bio state.
        userBioState = mutableStateOf(userDetails.bio)

        // Initialize the selected gender state.
        selectedGenderState = mutableStateOf(userDetails.gender)

        // Initialize the selected date of birth state.
        selectedDOBState = mutableLongStateOf(userDetails.dateOfBirth)

        // Initialize the profile photo state.
        profilePhotoState =
            mutableStateOf(
                MediaData(
                    userDetails.profilePhoto?.toUri() ?: Uri.EMPTY,
                    ConstantsHelper.MEDIA_TYPE_IMAGE
                )
            )

        // Initialize the cover photo state.
        coverPhotoState = mutableStateOf(
            MediaData(
                userDetails.coverPhoto?.toUri() ?: Uri.EMPTY,
                ConstantsHelper.MEDIA_TYPE_IMAGE
            )
        )

        // Initialize the connect user id state.
        connectUserIdState = mutableStateOf(userDetails.connectUserId)

        // Set the data initialized flag to true.
        isDataInitialized = true
    }

    /**
     * Updates the user's profile.
     */
    fun updateUserProfile() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _updateUserStateFlow.value = ResponseState.loading()

                // Get the fields to update.
                val fieldsToUpdate = getFieldsToUpdate()

                // If there are no fields to update, set the success state.
                if (fieldsToUpdate.isEmpty()) {
                    _updateUserStateFlow.value = ResponseState.success(null)
                } else {
                    // Get the remote profile photo URL.
                    var remoteProfilePhotoUrl: String? = null

                    // If the profile photo state is not null, upload the profile photo to the remote server.
                    if (fieldsToUpdate.containsKey(UserRemoteEntity::profilePhoto.name) && profilePhotoState.value != null) {
                        val updateProfilePhotoResponseState =
                            uploadFileToRemoteUseCase(
                                profilePhotoState.value!!.uri,
                                "${userDetails.firebaseUserId}/${FirebaseConstants.PROFILE_PHOTO_KEY}"
                            )

                        // If the upload failed, set the error state.
                        if (updateProfilePhotoResponseState.status == RequestStatusEnum.Exception || updateProfilePhotoResponseState.data.isNullOrBlank()) {
                            _updateUserStateFlow.value = ResponseState.error(
                                msg = updateProfilePhotoResponseState.message ?: ""
                            )
                            return@withContext
                        } else {
                            // Set the remote profile photo URL.
                            remoteProfilePhotoUrl = updateProfilePhotoResponseState.data
                        }
                    }

                    // Get the remote cover photo URL.
                    var remoteCoverPhotoUrl: String? = null

                    // If the cover photo state is not null, upload the cover photo to the remote server.
                    if (fieldsToUpdate.containsKey(UserRemoteEntity::coverPhoto.name) && coverPhotoState.value != null) {
                        val updateCoverPhotoResponseState =
                            uploadFileToRemoteUseCase(
                                coverPhotoState.value!!.uri,
                                "${userDetails.firebaseUserId}/${FirebaseConstants.COVER_PHOTO_KEY}"
                            )

                        // If the upload failed, set the error state.
                        if (updateCoverPhotoResponseState.status == RequestStatusEnum.Exception || updateCoverPhotoResponseState.data.isNullOrBlank()) {
                            _updateUserStateFlow.value = ResponseState.error(
                                msg = updateCoverPhotoResponseState.message ?: ""
                            )
                            return@withContext
                        } else {
                            // Set the remote cover photo URL.
                            remoteCoverPhotoUrl = updateCoverPhotoResponseState.data
                        }
                    }

                    // If the remote profile photo URL is not null, add it to the fields to update.
                    if (!remoteProfilePhotoUrl.isNullOrEmpty()) {
                        fieldsToUpdate[UserRemoteEntity::profilePhoto.name] = remoteProfilePhotoUrl
                    }

                    // If the remote cover photo URL is not null, add it to the fields to update.
                    if (!remoteCoverPhotoUrl.isNullOrEmpty()) {
                        fieldsToUpdate[UserRemoteEntity::coverPhoto.name] = remoteCoverPhotoUrl
                    }

                    // Update the user details on the remote server.
                    val updatedUserResponseState =
                        updateUserDetailsOnRemoteUseCase(
                            fieldsToUpdate,
                            userDetails.firebaseUserId
                        )

                    // If the update was successful, update the user details on the database and in the UI.
                    if (updatedUserResponseState.status != RequestStatusEnum.Exception) {
                        updateUserDetailsOnLocalUseCase(
                            getFieldsToUpdateInDbMap(fieldsToUpdate),
                            userDetails.firebaseUserId
                        )
                        updateUserDetails(fieldsToUpdate)
                        _updateUserStateFlow.value =
                            ResponseState.success(null)
                    } else {
                        // Set the error state.
                        _updateUserStateFlow.value =
                            ResponseState.error(updatedUserResponseState.message ?: "")
                    }
                }
            }
        }
    }

    /**
     * Updates the user details.
     *
     * @param fieldsToUpdate The fields to update.
     */
    private fun updateUserDetails(fieldsToUpdate: MutableMap<String, Any>) {
        // Update the user's name if it is present in the fields to update.
        if (fieldsToUpdate.containsKey(UserRemoteEntity::name.name)) {
            userDetails.name =
                FunctionHelper.getFormattedDisplayName(fieldsToUpdate[UserRemoteEntity::name.name] as String)
        }
        // Update the user's profile photo if it is present in the fields to update.
        if (fieldsToUpdate.containsKey(UserRemoteEntity::profilePhoto.name)) {
            userDetails.profilePhoto =
                fieldsToUpdate[UserRemoteEntity::profilePhoto.name] as String
        }
        // Update the user's cover photo if it is present in the fields to update.
        if (fieldsToUpdate.containsKey(UserRemoteEntity::coverPhoto.name)) {
            userDetails.coverPhoto =
                fieldsToUpdate[UserRemoteEntity::coverPhoto.name] as String
        }
        // Update the user's bio if it is present in the fields to update.
        if (fieldsToUpdate.containsKey(UserRemoteEntity::bio.name)) {
            userDetails.bio =
                fieldsToUpdate[UserRemoteEntity::bio.name] as String
        }
        // Update the user's gender if it is present in the fields to update.
        if (fieldsToUpdate.containsKey(UserRemoteEntity::gender.name)) {
            userDetails.gender =
                fieldsToUpdate[UserRemoteEntity::gender.name] as String
        }
        // Update the user's date of birth if it is present in the fields to update.
        if (fieldsToUpdate.containsKey(UserRemoteEntity::dateOfBirth.name)) {
            userDetails.dateOfBirth =
                fieldsToUpdate[UserRemoteEntity::dateOfBirth.name] as Long
        }
    }


    /**
     * Gets the fields to update in the database.
     *
     * @param fieldsToUpdate The fields to update.
     * @return The fields to update in the database.
     */
    private fun getFieldsToUpdateInDbMap(fieldsToUpdate: MutableMap<String, Any>): MutableMap<String, Any> {
        val fieldsToUpdateInDb = mutableMapOf<String, Any>()
        if (fieldsToUpdate.containsKey(UserRemoteEntity::name.name)) {
            // Get the formatted display name.
            val formattedDisplayName = FunctionHelper.getFormattedDisplayName(
                fieldsToUpdate[UserRemoteEntity::name.name] as String
            )
            // Add the formatted display name to the map.
            fieldsToUpdateInDb[UsersLocalEntity::name.name] = formattedDisplayName
        }
        if (fieldsToUpdate.containsKey(UserRemoteEntity::profilePhoto.name)) {
            // Get the profile photo URL.
            val profilePhotoUrl = fieldsToUpdate[UserRemoteEntity::profilePhoto.name] as String

            // Add the profile photo URL to the map.
            fieldsToUpdateInDb[UsersLocalEntity::profilePhoto.name] = profilePhotoUrl
        }
        if (fieldsToUpdate.containsKey(UserRemoteEntity::coverPhoto.name)) {
            // Get the cover photo URL.
            val coverPhotoUrl = fieldsToUpdate[UserRemoteEntity::coverPhoto.name] as String

            // Add the cover photo URL to the map.
            fieldsToUpdateInDb[UsersLocalEntity::coverPhoto.name] = coverPhotoUrl
        }
        if (fieldsToUpdate.containsKey(UserRemoteEntity::bio.name)) {
            // Get the bio.
            val bio = fieldsToUpdate[UserRemoteEntity::bio.name] as String

            // Add the bio to the map.
            fieldsToUpdateInDb[UsersLocalEntity::bio.name] = bio
        }
        if (fieldsToUpdate.containsKey(UserRemoteEntity::gender.name)) {
            // Get the gender.
            val gender = fieldsToUpdate[UserRemoteEntity::gender.name] as String

            // Add the gender to the map.
            fieldsToUpdateInDb[UsersLocalEntity::gender.name] = gender
        }
        if (fieldsToUpdate.containsKey(UserRemoteEntity::dateOfBirth.name)) {
            // Get the date of birth.
            val dateOfBirth = fieldsToUpdate[UserRemoteEntity::dateOfBirth.name] as Long

            // Add the date of birth to the map.
            fieldsToUpdateInDb[UsersLocalEntity::dateOfBirth.name] = dateOfBirth
        }
        return fieldsToUpdateInDb
    }

    /**
     * Gets the fields that need to be updated.
     *
     * @return A map of fields to their updated values.
     */
    private fun getFieldsToUpdate(): MutableMap<String, Any> {

        val fieldsToUpdate: MutableMap<String, Any> = mutableMapOf()

        // Check if the cover image has been updated.
        val isCoverImageUpdated =
            coverPhotoState.value?.uri != Uri.EMPTY && (coverPhotoState.value?.uri.toString() != userDetails.coverPhoto)

        // Check if the profile image has been updated.
        val isProfileImageUpdated =
            profilePhotoState.value?.uri != Uri.EMPTY && (profilePhotoState.value?.uri.toString() != userDetails.profilePhoto)

        // Get the lower case version of the user name.
        val lowerCaseUserName =
            FunctionHelper.getLowerCaseTextWithOutExtraSpace(userNameState.value)

        // Check if the user name has been updated.
        val isUserNameUpdated =
            FunctionHelper.getLowerCaseTextWithOutExtraSpace(userDetails.name) != lowerCaseUserName

        // Check if the bio has been updated.
        val isBioUpdated = userDetails.bio != userBioState.value

        // Check if the gender has been updated.
        val isGenderUpdated = userDetails.gender != selectedGenderState.value

        // Check if the date of birth has been updated.
        val isDobUpdated = userDetails.dateOfBirth != selectedDOBState.longValue

        // If the profile image has been updated, add it to the map of fields to update.
        if (isProfileImageUpdated) {
            fieldsToUpdate[UserRemoteEntity::profilePhoto.name] =
                profilePhotoState.value?.uri.toString()
        }

        // If the cover image has been updated, add it to the map of fields to update.
        if (isCoverImageUpdated) {
            fieldsToUpdate[UserRemoteEntity::coverPhoto.name] =
                coverPhotoState.value?.uri.toString()
        }

        // If the user name has been updated, add it to the map of fields to update.
        if (isUserNameUpdated) {
            fieldsToUpdate[UserRemoteEntity::name.name] = lowerCaseUserName
        }

        // If the bio has been updated, add it to the map of fields to update.
        if (isBioUpdated) {
            fieldsToUpdate[UserRemoteEntity::bio.name] = userBioState.value
        }

        // If the gender has been updated, add it to the map of fields to update.
        if (isGenderUpdated) {
            fieldsToUpdate[UserRemoteEntity::gender.name] = selectedGenderState.value
        }

        // If the date of birth has been updated, add it to the map of fields to update.
        if (isDobUpdated) {
            fieldsToUpdate[UserRemoteEntity::dateOfBirth.name] =
                selectedDOBState.longValue
        }

        // Return the map of fields to update.
        return fieldsToUpdate
    }
}