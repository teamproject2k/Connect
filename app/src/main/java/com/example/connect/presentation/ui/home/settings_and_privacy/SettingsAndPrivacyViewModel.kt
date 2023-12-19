package com.example.connect.presentation.ui.home.settings_and_privacy

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.common.VisibilityScopeEnum
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.data.models.user.UsersDbEntity
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.user.UpdateUserDetailsOnDbUseCase
import com.example.connect.domain.useCase.user.UpdateUserDetailsOnRemoteUseCase
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

    fun setUpData(userDetails: UsersBean, context: Context) {

        val defaultSelectedGenderVisibility =
            getDefaultSelectedVisibility(userDetails.genderVisibility)

        val defaultSelectedDobVisibility =
            getDefaultSelectedVisibility(userDetails.dobVisibility)

        val defaultSelectedFriendListVisibility =
            getDefaultSelectedVisibility(userDetails.friendListVisibility)

        genderVisibilityScopeList = FunctionHelper.getGenderVisibilityList(context)
        dobVisibilityScopeList = FunctionHelper.getDobVisibilityList(context)
        friendListVisibilityScopeList = FunctionHelper.getFriendListVisibilityList(context)

        genderVisibilityState =
            mutableStateOf(genderVisibilityScopeList[defaultSelectedGenderVisibility])
        dobVisibilityState = mutableStateOf(genderVisibilityScopeList[defaultSelectedDobVisibility])
        friendListVisibilityState =
            mutableStateOf(genderVisibilityScopeList[defaultSelectedFriendListVisibility])

        isFirstTimeSetup = false
    }

    private fun getDefaultSelectedVisibility(visibility: String): Int {
        return when (visibility) {
            VisibilityScopeEnum.Public.name -> 0
            VisibilityScopeEnum.FriendsOnly.name -> 1
            VisibilityScopeEnum.Private.name -> 2
            else -> -1
        }
    }

    fun updateGenderVisibility(firebaseUserId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _updateGenderVisibilityStateFlow.value = ResponseState.loading()

                val result = updateUserDetailsOnRemoteUseCase.invoke(
                    mutableMapOf(
                        UserRemoteEntity::genderVisibility.name to genderVisibilityState.value.scopeEnum.name
                    ),
                    firebaseUserId
                )

                if (result.status == RequestStatusEnum.SUCCESS) {
                    updateUserDetailsOnDbUseCase.invoke(
                        mutableMapOf(
                            UsersDbEntity::genderVisibility.name to genderVisibilityState.value.scopeEnum.name
                        ), firebaseUserId
                    )
                    _updateGenderVisibilityStateFlow.value = result
                }
            }
        }
    }

    fun updateDobVisibility(firebaseUserId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _updateDobVisibilityStateFlow.value = ResponseState.loading()
                val result = updateUserDetailsOnRemoteUseCase.invoke(
                    mutableMapOf(
                        UserRemoteEntity::dobVisibility.name to dobVisibilityState.value.scopeEnum.name
                    ),
                    firebaseUserId
                )
                if (result.status == RequestStatusEnum.SUCCESS) {
                    updateUserDetailsOnDbUseCase.invoke(
                        mutableMapOf(
                            UsersDbEntity::dobVisibility.name to dobVisibilityState.value.scopeEnum.name
                        ), firebaseUserId
                    )
                    _updateDobVisibilityStateFlow.value = result
                }
            }
        }
    }

    fun updateFriendListVisibility(firebaseUserId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _updateFriendListVisibilityStateFlow.value = ResponseState.loading()
                val result = updateUserDetailsOnRemoteUseCase.invoke(
                    mutableMapOf(
                        UserRemoteEntity::friendListVisibility.name to friendListVisibilityState.value.scopeEnum.name
                    ),
                    firebaseUserId
                )
                if (result.status == RequestStatusEnum.SUCCESS) {
                    updateUserDetailsOnDbUseCase.invoke(
                        mutableMapOf(
                            UsersDbEntity::friendListVisibility.name to friendListVisibilityState.value.scopeEnum.name
                        ), firebaseUserId
                    )
                    _updateFriendListVisibilityStateFlow.value = result
                }
            }
        }
    }
}