package com.example.connect.presentation.ui.home.user_profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.ErrorCodes
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.useCase.HomeUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class UserProfileViewModel @Inject constructor(private val homeUseCase: HomeUseCase) :
    BaseViewModel() {
    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<UserDetails>> =
        MutableStateFlow(ResponseState.none())

    val userDetailsStateFlow: StateFlow<ResponseState<UserDetails>> get() = _userDetailsStateFlow


    private val _friendsDetailsStateFlow: MutableStateFlow<ResponseState<List<UserDetails>>> =
        MutableStateFlow(ResponseState.none())

    val friendsDetailsStateFlow: StateFlow<ResponseState<List<UserDetails>>> get() = _friendsDetailsStateFlow

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostDetails>>> =
        MutableStateFlow(ResponseState.none())

    val postDetailsStateFlow: StateFlow<ResponseState<List<PostDetails>>> get() = _postDetailsStateFlow

    val snackBarMessageState = mutableStateOf("")

    fun getUserDetails() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _userDetailsStateFlow.value = ResponseState.loading()
                val fireBaseId = fireBaseAuth.currentUser?.uid
                if (fireBaseId != null) {
                    val userDetails =
                        homeUseCase.getUserDetailsFromLocal(fireBaseId)
                    if (userDetails != null) {
                        _userDetailsStateFlow.value = ResponseState.success(userDetails)
                        if (userDetails.friendList.isNotEmpty()) {
                            getFriendListFromIds(userDetails.friendList)
                        }
                    } else {
                        val userDetailsFromServerResponseState =
                            homeUseCase.getUserDetailsFromServer(fireBaseId)
                        if (userDetailsFromServerResponseState.status == RequestStatusEnum.SUCCESS) {
                            homeUseCase.addUserToLocalDb(userDetailsFromServerResponseState.data!!)
                            _userDetailsStateFlow.value =
                                ResponseState.success(userDetailsFromServerResponseState.data)
                            if (userDetailsFromServerResponseState.data.friendList.isNotEmpty()) {
                                getFriendListFromIds(userDetailsFromServerResponseState.data.friendList)
                            }
                        } else {
                            _userDetailsStateFlow.value = userDetailsFromServerResponseState
                        }
                    }
                } else {
                    _userDetailsStateFlow.value = ResponseState.error(ErrorCodes.NoUserFound)
                }
            }
        }
    }


    fun getPostDetails() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _postDetailsStateFlow.value = ResponseState.loading()
                val fireBaseId = fireBaseAuth.currentUser?.uid
                if (fireBaseId != null) {
                    val postDetails = homeUseCase.getPostDetailsFromLocale(fireBaseId)
                    if (!postDetails.isNullOrEmpty()) {
                        _postDetailsStateFlow.value = ResponseState.success(postDetails)
                    } else {
                        val postDetailsFromServerResponseState =
                            homeUseCase.getPostDetailsFromServer(fireBaseId)
                        if (postDetailsFromServerResponseState.status == RequestStatusEnum.SUCCESS) {
                            homeUseCase.addPostListToLocal(postDetailsFromServerResponseState.data!!)
                            _postDetailsStateFlow.value =
                                ResponseState.success(postDetailsFromServerResponseState.data)
                        } else {
                            _postDetailsStateFlow.value = postDetailsFromServerResponseState
                        }
                    }
                } else {
                    _postDetailsStateFlow.value = ResponseState.error(ErrorCodes.NoUserFound)
                }
            }
        }
    }

    fun getFriendListFromIds(friendIdList: List<String>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _friendsDetailsStateFlow.value = ResponseState.loading()
                _friendsDetailsStateFlow.value = homeUseCase.getUserDetailsFromIds(friendIdList)
            }
        }
    }

}