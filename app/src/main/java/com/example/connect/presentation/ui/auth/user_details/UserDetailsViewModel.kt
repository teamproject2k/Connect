package com.example.connect.presentation.ui.auth.user_details

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.common.VisibilityScopeEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.AddUserToRemoteUseCase
import com.example.connect.domain.useCase.user.GetUsersFromNameUseCaseFromRemote
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.ButtonStateEnum
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.getUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val addUserToRemoteUseCase: AddUserToRemoteUseCase,
    private val addUserToDbUseCase: AddUserToDbUseCase,
    private val getUsersFromNameUseCase: GetUsersFromNameUseCaseFromRemote
) :
    BaseViewModel() {
    val snackBarMessageState = mutableStateOf("")
    val currentButtonLoadingState = mutableStateOf(ButtonStateEnum.NotLoading)
    val userNameState = mutableStateOf("")
    val selectedDOBState = mutableLongStateOf(-1)
    val selectedGenderState = mutableStateOf("")
    private val _addUserStateFlow: MutableStateFlow<ResponseState<Int>> =
        MutableStateFlow(ResponseState.none())
    val addUserStateFlow: StateFlow<ResponseState<Int>> get() = _addUserStateFlow


    /**
     * Creates a user profile in the database.
     */
    fun createUserProfile() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _addUserStateFlow.value = ResponseState.loading()
                val formattedUserName =
                    FunctionHelper.getLowerCaseTextWithOutExtraSpace(userNameState.value)
                //get no of users with name to set user id
                val currentUserByNameResponseState =
                    getUsersFromNameUseCase.invoke(formattedUserName)
                if (currentUserByNameResponseState.status != RequestStatusEnum.EXCEPTION && sharedPreference.deviceId != null) {
                    val createdDate = FunctionHelper.getCurrentTimeInMillis()
                    val user = UsersBean(
                        fireBaseAuth.currentUser!!.uid,
                        getUserId(formattedUserName, currentUserByNameResponseState.data ?: 0),
                        formattedUserName,
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
                    val userDetailsResponseState = addUserToRemoteUseCase.invoke(user)
                    if (userDetailsResponseState.status == RequestStatusEnum.SUCCESS) {
                        addUserToDbUseCase.invoke(user)
                    }
                    _addUserStateFlow.value = userDetailsResponseState
                } else {
                    _addUserStateFlow.value =
                        ResponseState.error(currentUserByNameResponseState.message ?: "")
                }
            }
        }
    }

}