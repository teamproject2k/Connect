package com.example.connect.presentation.ui.home.edit_profile

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.example.connect.common.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.ButtonStateEnum
import com.example.connect.presentation.ui.models.PostMediaData
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class EditProfileViewModel @Inject constructor() :
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

    fun initializeStates(UsersBean: UsersBean) {
        this.userDetails = userDetails
        userNameState = mutableStateOf(userDetails.name)
        userBioState = mutableStateOf(userDetails.bio)
        selectedGenderState = mutableStateOf(userDetails.gender)
        selectedDOBState = mutableLongStateOf(userDetails.dateOfBirth)
        profilePhotoState =
            mutableStateOf(
                PostMediaData(
                    userDetails.profilePhoto?.toUri()?: Uri.EMPTY,
//                    ConstantsHelper.MediaTypeImage
                    ""
                )
            )
        coverPhotoState = mutableStateOf(
            PostMediaData(userDetails.coverPhoto?.toUri()?: Uri.EMPTY, ConstantsHelper.MediaTypeImage)
        )
        connectUserIdState = mutableStateOf(userDetails.connectUserId)
        isDataInitialized = true
    }

    fun updateUserProfile() {

    }

//    fun updateUserProfile() {
//
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                _updateUserStateFlow.value = ResponseState.loading()
//
//                val fieldsToUpdate = getFieldsToUpdate()
//                if (fieldsToUpdate.isEmpty()) {
//                    _updateUserStateFlow.value = ResponseState.success(null)
//                } else {
//                    var remoteProfilePhotoUrl: String? = null
//                    var remoteCoverPhotoUrl: String? = null
//
//                    if (fieldsToUpdate.containsKey(UsersBean::profilePhoto.name)) {
//                        val updateProfilePhotoResponseState =
////                            homeUseCase.updateImageOnRemoteStorage(
////                                profilePhotoState.value?.uri,
////                                userDetails.firebaseUserId,
////                                UsersBean::profilePhoto.name
////                            )
//                        if (updateProfilePhotoResponseState.status == RequestStatusEnum.EXCEPTION) {
//                            _updateUserStateFlow.value = updateProfilePhotoResponseState
//                            return@withContext
//                        } else {
//                            remoteProfilePhotoUrl = updateProfilePhotoResponseState.data
//                        }
//                    }
//                    if (fieldsToUpdate.containsKey(UsersBean::coverPhoto.name)) {
//                        val updateCoverPhotoResponseState =
//                            homeUseCase.updateImageOnRemoteStorage(
//                                coverPhotoState.value?.uri,
//                                userDetails.firebaseUserId,
//                                UsersBean::coverPhoto.name
//                            )
//                        if (updateCoverPhotoResponseState.status == RequestStatusEnum.EXCEPTION) {
//                            _updateUserStateFlow.value = updateCoverPhotoResponseState
//                            return@withContext
//                        } else {
//                            remoteCoverPhotoUrl = updateCoverPhotoResponseState.data
//                        }
//                    }
//                    if (!remoteProfilePhotoUrl.isNullOrEmpty()) {
//                        fieldsToUpdate[UsersBean::profilePhoto.name] = remoteProfilePhotoUrl
//                    }
//                    if (!remoteCoverPhotoUrl.isNullOrEmpty()) {
//                        fieldsToUpdate[UsersBean::coverPhoto.name] = remoteCoverPhotoUrl
//                    }
//                    if (fieldsToUpdate.containsKey(UsersBean::name.name)) {
//                        val userName = fieldsToUpdate[UsersBean::name.name]
//                        val currentUserByNameResponseState = userName?.let {
//                            homeUseCase.getUsersFromName(it)
//                        }
//                        if (userName == null || currentUserByNameResponseState?.status == RequestStatusEnum.EXCEPTION) {
//                            _updateUserStateFlow.value =
//                                ResponseState.error(currentUserByNameResponseState?.message ?: "")
//                            return@withContext
//                        } else {
//                            fieldsToUpdate[UsersBean::connectUserId.name] =
//                                FunctionHelper.getUserId(
//                                    userName.toString(),
//                                    currentUserByNameResponseState?.data ?: 0
//                                )
//                        }
//                    }
//                    val updatedUserResponseState =
//                        homeUseCase.updateUserDetailsOnServer(
//                            fieldsToUpdate,
//                            userDetails.firebaseUserId
//                        )
//
//                    if (updatedUserResponseState.status != RequestStatusEnum.EXCEPTION) {
//                        homeUseCase.updateUserDetailsOnLocal(
//                            fieldsToUpdate,
//                            userDetails.firebaseUserId
//                        )
//                    }
//                    _updateUserStateFlow.value = updatedUserResponseState
//                }
//            }
//        }
//    }

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