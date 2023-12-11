package com.example.connect.presentation.ui.home.edit_profile

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.useCase.HomeUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.ButtonLoadingEnum
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
class EditProfileViewModel @Inject constructor(private val homeUseCase: HomeUseCase) :
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
    lateinit var userDetails: UserDetails
    var isProfileUri = true
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingEnum.NotLoading)
    var isDataInitialized = false

    fun initializeStates(userDetails: UserDetails) {
        this.userDetails = userDetails
        userNameState = mutableStateOf(userDetails.name)
        userBioState = mutableStateOf(userDetails.bio)
        selectedGenderState = mutableStateOf(userDetails.gender)
        selectedDOBState = mutableLongStateOf(userDetails.dateOfBirth)
        profilePhotoState =
            mutableStateOf(
                PostMediaData(
                    userDetails.profilePhoto?.toUri(),
                    ConstantsHelper.MediaTypeImage
                )
            )
        coverPhotoState = mutableStateOf(
            PostMediaData(userDetails.coverPhoto?.toUri(), ConstantsHelper.MediaTypeImage)
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

                    if (fieldsToUpdate.containsKey(UserDetails::profilePhoto.name)) {
                        val updateProfilePhotoResponseState =
                            homeUseCase.updateImageOnRemoteStorage(
                                profilePhotoState.value?.uri,
                                userDetails.firebaseUserId,
                                UserDetails::profilePhoto.name
                            )
                        if (updateProfilePhotoResponseState.status == RequestStatusEnum.EXCEPTION) {
                            _updateUserStateFlow.value = updateProfilePhotoResponseState
                            return@withContext
                        } else {
                            remoteProfilePhotoUrl = updateProfilePhotoResponseState.data
                        }
                    }
                    if (fieldsToUpdate.containsKey(UserDetails::coverPhoto.name)) {
                        val updateCoverPhotoResponseState =
                            homeUseCase.updateImageOnRemoteStorage(
                                coverPhotoState.value?.uri,
                                userDetails.firebaseUserId,
                                UserDetails::coverPhoto.name
                            )
                        if (updateCoverPhotoResponseState.status == RequestStatusEnum.EXCEPTION) {
                            _updateUserStateFlow.value = updateCoverPhotoResponseState
                            return@withContext
                        } else {
                            remoteCoverPhotoUrl = updateCoverPhotoResponseState.data
                        }
                    }
                    if (!remoteProfilePhotoUrl.isNullOrEmpty()) {
                        fieldsToUpdate[UserDetails::profilePhoto.name] = remoteProfilePhotoUrl
                    }
                    if (!remoteCoverPhotoUrl.isNullOrEmpty()) {
                        fieldsToUpdate[UserDetails::coverPhoto.name] = remoteCoverPhotoUrl
                    }
                    if (fieldsToUpdate.containsKey(UserDetails::name.name)) {
                        val userName = fieldsToUpdate[UserDetails::name.name]
                        val currentUserByNameResponseState = userName?.let {
                            homeUseCase.getUsersFromName(it)
                        }
                        if (userName == null || currentUserByNameResponseState?.status == RequestStatusEnum.EXCEPTION) {
                            _updateUserStateFlow.value =
                                ResponseState.error(currentUserByNameResponseState?.message ?: "")
                            return@withContext
                        } else {
                            fieldsToUpdate[UserDetails::connectUserId.name] =
                                FunctionHelper.getUserId(
                                    userName.toString(),
                                    currentUserByNameResponseState?.data ?: 0
                                )
                        }
                    }
                    val updatedUserResponseState =
                        homeUseCase.updateUserDetailsOnServer(
                            fieldsToUpdate,
                            userDetails.firebaseUserId
                        )

                    if (updatedUserResponseState.status != RequestStatusEnum.EXCEPTION) {
                        homeUseCase.updateUserDetailsOnLocal(
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
            fieldsToUpdate[UserDetails::profilePhoto.name] = profilePhotoState.value?.uri.toString()
        }
        if (isCoverImageUpdated) {
            fieldsToUpdate[UserDetails::coverPhoto.name] = coverPhotoState.value?.uri.toString()
        }
        if (isUserNameUpdated) {
            fieldsToUpdate[UserDetails::name.name] = lowerCaseUserName
        }
        if (isBioUpdated) {
            fieldsToUpdate[UserDetails::bio.name] = userBioState.value
        }
        if (isGenderUpdated) {
            fieldsToUpdate[UserDetails::gender.name] = selectedGenderState.value
        }
        if (isDobUpdated) {
            fieldsToUpdate[UserDetails::dateOfBirth.name] =
                selectedDOBState.longValue
        }

        return fieldsToUpdate
    }
}