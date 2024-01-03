package com.example.connect.presentation.ui.home.saved_posts

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddLikeUseCase
import com.example.connect.domain.useCase.posts.DeletePostUseCase
import com.example.connect.domain.useCase.posts.GetSavedPostsWithUsersFromRemoteUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeUseCase
import com.example.connect.domain.useCase.posts.SavePostUseCase
import com.example.connect.domain.useCase.posts.UnSavePostUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SavedPostsViewModel @Inject constructor(
    private val getSavedPostsWithUsersFromRemoteUseCase: GetSavedPostsWithUsersFromRemoteUseCase,
    private val addLikeUseCase: AddLikeUseCase,
    private val removeLikeUseCase: RemoveLikeUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val unSavePostUseCase: UnSavePostUseCase
) :
    BaseViewModel() {

    private val _getSavedPostsWithUsersStateFlow: MutableStateFlow<ResponseState<Pair<ArrayList<PostBean>, ArrayList<UsersBean>>>> =
        MutableStateFlow(ResponseState.none())
    val getSavedPostsWithUsersStateFlow: StateFlow<ResponseState<Pair<ArrayList<PostBean>, ArrayList<UsersBean>>>> get() = _getSavedPostsWithUsersStateFlow

    private val _likeUnlikePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val likeUnlikePostStateFlow: StateFlow<ResponseState<Nothing>> get() = _likeUnlikePostStateFlow

    private val _saveUnSavePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val saveUnSavePostStateFlow: StateFlow<ResponseState<Nothing>> get() = _saveUnSavePostStateFlow

    val snackBarMessageState = mutableStateOf("")

    var isSavedListFetched = false

    fun getSavedPosts(loggedInUserFirebaseId: String, savedPosts: ArrayList<String>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getSavedPostsWithUsersStateFlow.value = ResponseState.loading()
                _getSavedPostsWithUsersStateFlow.value =
                    getSavedPostsWithUsersFromRemoteUseCase.invoke(
                        loggedInUserFirebaseId,
                        savedPosts
                    )
            }
        }
    }

    fun addLike(postId: String, currentUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _likeUnlikePostStateFlow.value = ResponseState.loading()
                val responseState = addLikeUseCase.invoke(
                    currentUserFirebaseId = currentUserFirebaseId,
                    postFirebaseId = postId
                )
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                _likeUnlikePostStateFlow.value = responseState
            }
        }
    }

    fun removeLike(postId: String, currentUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _likeUnlikePostStateFlow.value = ResponseState.loading()
                val responseState = removeLikeUseCase.invoke(
                    currentUserFirebaseId = currentUserFirebaseId,
                    postFirebaseId = postId
                )
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                _likeUnlikePostStateFlow.value = responseState
            }
        }
    }

    fun savePost(currentUserFirebaseId: String, postId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState = savePostUseCase.invoke(currentUserFirebaseId, postId)
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                _saveUnSavePostStateFlow.value = responseState
            }
        }
    }

    fun unSavePost(currentUserFirebaseId: String, postId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState = unSavePostUseCase.invoke(currentUserFirebaseId, postId)
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                _saveUnSavePostStateFlow.value = responseState
            }
        }
    }
}