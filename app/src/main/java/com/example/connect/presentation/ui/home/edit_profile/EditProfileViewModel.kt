package com.example.connect.presentation.ui.home.edit_profile

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.upload_file.UploadFileToRemoteUseCase
import com.example.connect.domain.useCase.user.GetUsersFromNameUseCaseFromRemote
import com.example.connect.domain.useCase.user.UpdateUserDetailsOnDbUseCase
import com.example.connect.domain.useCase.user.UpdateUserDetailsOnRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.ButtonStateEnum
import com.example.connect.presentation.ui.models.PostMediaData
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val uploadFileToRemoteUseCase: UploadFileToRemoteUseCase,
    private val getUsersFromNameUseCaseFromRemote: GetUsersFromNameUseCaseFromRemote,
    private val updateUserDetailsOnRemoteUseCase: UpdateUserDetailsOnRemoteUseCase,
    private val updateUserDetailsOnDbUseCase: UpdateUserDetailsOnDbUseCase
) :
    BaseViewModel() {

    private val _updateUserStateFlow: MutableStateFlow<ResponseState<String?>> =
        MutableStateFlow(ResponseState.none())
    val updateUserStateFlow = _updateUserStateFlow.asStateFlow()

    lateinit var userNameState: MutableState<String>
    lateinit var connectUserIdState: MutableState<String>
    lateinit var userBioState: MutableState<String>
    lateinit var selectedDOBState: MutableLongState
    lateinit var selectedGenderState: MutableState<String>
    lateinit var profilePhotoState: MutableState<PostMediaData?>
    lateinit var coverPhotoState: MutableState<PostMediaData?>
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
    fun initializeStates(userDetails: UsersBean) {
        this.userDetails = userDetails
        userNameState = mutableStateOf(userDetails.name)
        userBioState = mutableStateOf(userDetails.bio)
        selectedGenderState = mutableStateOf(userDetails.gender)
        selectedDOBState = mutableLongStateOf(userDetails.dateOfBirth)
        profilePhotoState =
            mutableStateOf(
                PostMediaData(
                    userDetails.profilePhoto?.toUri() ?: Uri.EMPTY,
                    ConstantsHelper.MediaTypeImage
                )
            )
        coverPhotoState = mutableStateOf(
            PostMediaData(
                userDetails.coverPhoto?.toUri() ?: Uri.EMPTY,
                ConstantsHelper.MediaTypeImage
            )
        )
        connectUserIdState = mutableStateOf(userDetails.connectUserId)
        isDataInitialized = true
    }

    /**
     * Updates the user's profile.
     */
    fun updateUserProfile() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _updateUserStateFlow.value = ResponseState.loading()
                val fieldsToUpdate = getFieldsToUpdate()
                if (fieldsToUpdate.isEmpty()) {
                    _updateUserStateFlow.value = ResponseState.success(null)
                } else {
                    var remoteProfilePhotoUrl: String? = null
                    var remoteCoverPhotoUrl: String? = null

                    if (fieldsToUpdate.containsKey(UserRemoteEntity::profilePhoto.name) && profilePhotoState.value != null) {
                        val updateProfilePhotoResponseState =
                            uploadFileToRemoteUseCase.invoke(
                                profilePhotoState.value!!.uri,
                                "${userDetails.firebaseUserId}/${FirebaseConstants.ProfilePhotoKey}"
                            )
                        if (updateProfilePhotoResponseState.status == RequestStatusEnum.EXCEPTION) {
                            _updateUserStateFlow.value = updateProfilePhotoResponseState
                            return@withContext
                        } else {
                            remoteProfilePhotoUrl = updateProfilePhotoResponseState.data
                        }
                    }
                    if (fieldsToUpdate.containsKey(UserRemoteEntity::coverPhoto.name) && coverPhotoState.value != null) {
                        val updateCoverPhotoResponseState =
                            uploadFileToRemoteUseCase.invoke(
                                coverPhotoState.value!!.uri,
                                "${userDetails.firebaseUserId}/${FirebaseConstants.CoverPhotoKey}"
                            )
                        if (updateCoverPhotoResponseState.status == RequestStatusEnum.EXCEPTION) {
                            _updateUserStateFlow.value = updateCoverPhotoResponseState
                            return@withContext
                        } else {
                            remoteCoverPhotoUrl = updateCoverPhotoResponseState.data
                        }
                    }
                    if (!remoteProfilePhotoUrl.isNullOrEmpty()) {
                        fieldsToUpdate[UserRemoteEntity::profilePhoto.name] = remoteProfilePhotoUrl
                    }
                    if (!remoteCoverPhotoUrl.isNullOrEmpty()) {
                        fieldsToUpdate[UserRemoteEntity::coverPhoto.name] = remoteCoverPhotoUrl
                    }
                    if (fieldsToUpdate.containsKey(UserRemoteEntity::name.name)) {
                        val userName = fieldsToUpdate[UserRemoteEntity::name.name]
                        val currentUserByNameResponseState = userName?.let {
                            getUsersFromNameUseCaseFromRemote.invoke(it.toString())
                        }
                        if (userName == null || currentUserByNameResponseState?.status == RequestStatusEnum.EXCEPTION) {
                            _updateUserStateFlow.value =
                                ResponseState.error(currentUserByNameResponseState?.message ?: "")
                            return@withContext
                        } else {
                            fieldsToUpdate[UserRemoteEntity::connectUserId.name] =
                                FunctionHelper.getUserId(
                                    userName.toString(),
                                    currentUserByNameResponseState?.data ?: 0
                                )
                        }
                    }
                    val updatedUserResponseState =
                        updateUserDetailsOnRemoteUseCase.invoke(
                            fieldsToUpdate,
                            userDetails.firebaseUserId
                        )

                    if (updatedUserResponseState.status != RequestStatusEnum.EXCEPTION) {
                        updateUserDetailsOnDbUseCase.invoke(
                            fieldsToUpdate,
                            userDetails.firebaseUserId
                        )

                    }
                    _updateUserStateFlow.value = updatedUserResponseState
                }
            }
        }
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
            coverPhotoState.value?.uri.toString() != userDetails.coverPhoto.toString()

        // Check if the profile image has been updated.
        val isProfileImageUpdated =
            profilePhotoState.value?.uri.toString() != userDetails.profilePhoto.toString()

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