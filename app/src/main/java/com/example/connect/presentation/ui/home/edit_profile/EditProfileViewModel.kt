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

    private val _updateProfileImageStateFlow: MutableStateFlow<ResponseState<String?>> =
        MutableStateFlow(ResponseState.none())

    private val _updateCoverImageStateFlow: MutableStateFlow<ResponseState<String?>> =
        MutableStateFlow(ResponseState.none())

    lateinit var userNameState: MutableState<String>
    lateinit var connectUserIdState: MutableState<String>
    lateinit var userBioState: MutableState<String>
    lateinit var selectedDOBState: MutableLongState
    lateinit var selectedGenderState: MutableState<String>
    lateinit var profilePhotoState: MutableState<PostMediaData?>
    lateinit var coverPhotoState: MutableState<PostMediaData?>
    var isProfileUri = true
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonLoadingEnum.NotLoading)
    var isDataInitialized = false
    private var isUserNameUpdated = false
    private var isProfileImageUpdated = false
    private var isCoverImageUpdated = false

    fun initializeStates(userDetails: UserDetails) {
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
        isDataInitialized = true
        connectUserIdState = mutableStateOf(userDetails.connectUserId)
    }

    fun getFieldsToUpdate(userDetails: UserDetails): MutableMap<String, String> {

        val fieldsToUpdate: MutableMap<String, String> = mutableMapOf()

        isCoverImageUpdated =
            coverPhotoState.value?.uri.toString() != userDetails.coverPhoto
        isProfileImageUpdated = profilePhotoState.value?.uri.toString() != userDetails.profilePhoto

        val lowerCaseUserName = FunctionHelper.getLowerCaseUserName(userNameState.value)
        isUserNameUpdated = userDetails.name != lowerCaseUserName
        val isBioUpdated = userDetails.bio != userBioState.value
        val isGenderUpdated = userDetails.gender != selectedGenderState.value
        val isDobUpdated = userDetails.dateOfBirth != selectedDOBState.longValue

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
                selectedDOBState.longValue.toString()
        }

        return fieldsToUpdate
    }

    fun updateUserProfile(fieldsToUpdate: MutableMap<String, String>, firebaseUserId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _updateUserStateFlow.value = ResponseState.loading()

                if (isUserNameUpdated) {
                    val userName = fieldsToUpdate[UserDetails::name.name]
                    val currentUserByNameResponseState = userName?.let {
                        homeUseCase.getUsersFromName(it)
                    }
                    if (userName != null && currentUserByNameResponseState?.status != RequestStatusEnum.EXCEPTION) {
                        updateProfile(
                            fieldsToUpdate,
                            firebaseUserId,
                            userName,
                            currentUserByNameResponseState
                        )
                    }
                } else {
                    updateProfile(
                        fieldsToUpdate,
                        firebaseUserId
                    )
                }
            }
        }
    }

    private fun updateProfile(
        fieldsToUpdate: MutableMap<String, String>,
        firebaseUserId: String,
        userName: String = "",
        currentUserByNameResponseState: ResponseState<Int>? = ResponseState.none()
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                if (isProfileImageUpdated) {
                    updateProfileImageOnRemoteStorage(firebaseUserId)
                }

                if (isCoverImageUpdated) {
                    updateCoverImageOnRemoteStorage(firebaseUserId)
                }

                if ((!isProfileImageUpdated || _updateProfileImageStateFlow.value.status != RequestStatusEnum.EXCEPTION) &&
                    (!isCoverImageUpdated || _updateCoverImageStateFlow.value.status != RequestStatusEnum.EXCEPTION)
                ) {

                    if (isCoverImageUpdated) {
                        fieldsToUpdate[UserDetails::coverPhoto.name] =
                            _updateCoverImageStateFlow.value.data.toString()
                    }

                    if (isProfileImageUpdated) {
                        fieldsToUpdate[UserDetails::profilePhoto.name] =
                            _updateProfileImageStateFlow.value.data.toString()
                    }

                    if (isUserNameUpdated) {
                        fieldsToUpdate[UserDetails::connectUserId.name] =
                            FunctionHelper.getUserId(
                                userName,
                                currentUserByNameResponseState?.data ?: 0
                            )
                    }
                    _updateUserStateFlow.value =
                        homeUseCase.updateUserDetails(fieldsToUpdate)
                }
            }
        }
    }

    private fun updateProfileImageOnRemoteStorage(firebaseUserId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _updateProfileImageStateFlow.value = ResponseState.loading()
                _updateProfileImageStateFlow.value = homeUseCase.updateProfileImageOnRemoteStorage(
                    profilePhotoState.value?.uri,
                    firebaseUserId
                )
            }
        }
    }

    private fun updateCoverImageOnRemoteStorage(firebaseUserId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _updateCoverImageStateFlow.value = ResponseState.loading()
                _updateCoverImageStateFlow.value = homeUseCase.updateCoverImageOnRemoteStorage(
                    coverPhotoState.value?.uri,
                    firebaseUserId
                )
            }
        }
    }

}