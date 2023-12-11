package com.example.connect.presentation.ui.home.user_profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.ErrorCodes
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.useCase.posts.AddPostListToDbUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromDbUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromIds
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getPostDetailsFromDbUseCase: GetPostDetailsFromDbUseCase,
    private val getPostDetailsFromRemoteUseCase: GetPostDetailsFromRemoteUseCase,
    private val addPostListToDbUseCase: AddPostListToDbUseCase,
    private val getUserDetailsFromIds: GetUserDetailsFromIds
) : BaseViewModel() {
    private val _friendsDetailsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())

    val friendsDetailsStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _friendsDetailsStateFlow

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostBean>>> =
        MutableStateFlow(ResponseState.none())

    val postDetailsStateFlow: StateFlow<ResponseState<List<PostBean>>> get() = _postDetailsStateFlow

    val snackBarMessageState = mutableStateOf("")


    fun getPostDetails() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _postDetailsStateFlow.value = ResponseState.loading()
                val fireBaseId = fireBaseAuth.currentUser?.uid
                if (fireBaseId != null) {
                    val postDetails = getPostDetailsFromDbUseCase.invoke(fireBaseId)
                    if (postDetails.isNotEmpty()) {
                        _postDetailsStateFlow.value = ResponseState.success(postDetails)
                    } else {
                        val postDetailsFromServerResponseState =
                            getPostDetailsFromRemoteUseCase.invoke(fireBaseId)
                        if (postDetailsFromServerResponseState.status == RequestStatusEnum.SUCCESS) {
                            addPostListToDbUseCase.invoke(postDetailsFromServerResponseState.data!!)
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
                if (friendIdList.isEmpty()) {
                    _friendsDetailsStateFlow.value = ResponseState.success(emptyList())
                } else {
                    _friendsDetailsStateFlow.value = ResponseState.loading()
                    _friendsDetailsStateFlow.value = getUserDetailsFromIds.invoke(friendIdList)
                }
            }
        }
    }
}