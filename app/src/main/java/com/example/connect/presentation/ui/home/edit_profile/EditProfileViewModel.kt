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
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.upload_file.UploadFileToRemoteUseCase
import com.example.connect.domain.useCase.user.GetUsersFromNameUseCase
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
    private val getUsersFromNameUseCase: GetUsersFromNameUseCase,
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

                    if (fieldsToUpdate.containsKey(UsersBean::profilePhoto.name) && profilePhotoState.value != null) {
                        val updateProfilePhotoResponseState =
                            uploadFileToRemoteUseCase.invoke(
                                profilePhotoState.value!!.uri,
                                "${FirebaseConstants.ProfilePhotoKey}/${userDetails.firebaseUserId}"
                            )
                        if (updateProfilePhotoResponseState.status == RequestStatusEnum.EXCEPTION) {
                            _updateUserStateFlow.value = updateProfilePhotoResponseState
                            return@withContext
                        } else {
                            remoteProfilePhotoUrl = updateProfilePhotoResponseState.data
                        }
                    }
                    if (fieldsToUpdate.containsKey(UsersBean::coverPhoto.name) && coverPhotoState.value != null) {
                        val updateCoverPhotoResponseState =
                            uploadFileToRemoteUseCase.invoke(
                                coverPhotoState.value!!.uri,
                                "${FirebaseConstants.CoverPhotoKey}/${userDetails.firebaseUserId}"
                            )
                        if (updateCoverPhotoResponseState.status == RequestStatusEnum.EXCEPTION) {
                            _updateUserStateFlow.value = updateCoverPhotoResponseState
                            return@withContext
                        } else {
                            remoteCoverPhotoUrl = updateCoverPhotoResponseState.data
                        }
                    }
                    if (!remoteProfilePhotoUrl.isNullOrEmpty()) {
                        fieldsToUpdate[UsersBean::profilePhoto.name] = remoteProfilePhotoUrl
                    }
                    if (!remoteCoverPhotoUrl.isNullOrEmpty()) {
                        fieldsToUpdate[UsersBean::coverPhoto.name] = remoteCoverPhotoUrl
                    }
                    if (fieldsToUpdate.containsKey(UsersBean::name.name)) {
                        val userName = fieldsToUpdate[UsersBean::name.name]
                        val currentUserByNameResponseState = userName?.let {
                            getUsersFromNameUseCase.invoke(it.toString())
                        }
                        if (userName == null || currentUserByNameResponseState?.status == RequestStatusEnum.EXCEPTION) {
                            _updateUserStateFlow.value =
                                ResponseState.error(currentUserByNameResponseState?.message ?: "")
                            return@withContext
                        } else {
                            fieldsToUpdate[UsersBean::connectUserId.name] =
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

    private fun getFieldsToUpdate(): MutableMap<String, Any> {

        val fieldsToUpdate: MutableMap<String, Any> = mutableMapOf()

        val isCoverImageUpdated =
            coverPhotoState.value?.uri.toString() != userDetails.coverPhoto.toString()

        val isProfileImageUpdated =
            profilePhotoState.value?.uri.toString() != userDetails.profilePhoto.toString()

        val lowerCaseUserName = FunctionHelper.getLowerCaseUserName(userNameState.value)
        val isUserNameUpdated = userDetails.name != lowerCaseUserName
        val isBioUpdated = userDetails.bio != userBioState.value
        val isGenderUpdated = userDetails.gender != selectedGenderState.value
        val isDobUpdated = userDetails.dateOfBirth != selectedDOBState.longValue

        if (isProfileImageUpdated) {
            fieldsToUpdate[UsersBean::profilePhoto.name] = profilePhotoState.value?.uri.toString()
        }
        if (isCoverImageUpdated) {
            fieldsToUpdate[UsersBean::coverPhoto.name] = coverPhotoState.value?.uri.toString()
        }
        if (isUserNameUpdated) {
            fieldsToUpdate[UsersBean::name.name] = lowerCaseUserName
        }
        if (isBioUpdated) {
            fieldsToUpdate[UsersBean::bio.name] = userBioState.value
        }
        if (isGenderUpdated) {
            fieldsToUpdate[UsersBean::gender.name] = selectedGenderState.value
        }
        if (isDobUpdated) {
            fieldsToUpdate[UsersBean::dateOfBirth.name] =
                selectedDOBState.longValue
        }

        return fieldsToUpdate
    }
}