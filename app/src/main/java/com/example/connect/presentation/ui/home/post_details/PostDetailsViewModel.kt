package com.example.connect.presentation.ui.home.post_details

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddLikeUseCase
import com.example.connect.domain.useCase.posts.DeletePostUseCase
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


@SuppressLint("StateNameRule")
@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val addLikeUseCase: AddLikeUseCase,
    private val removeLikeUseCase: RemoveLikeUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val unSavePostUseCase: UnSavePostUseCase,
    private val deletePostUseCase: DeletePostUseCase
) : BaseViewModel() {

    private val _deletePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val deletePostStateFlow: StateFlow<ResponseState<Nothing>> get() = _deletePostStateFlow

    var isDropdownMenuVisibleState = mutableStateOf(false)

    fun addLike(postId: String, currentUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                //_likeUnlikePostStateFlow.value = ResponseState.loading()
                val responseState = addLikeUseCase.invoke(
                    currentUserFirebaseId = currentUserFirebaseId,
                    postFirebaseId = postId
                )
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                //   _likeUnlikePostStateFlow.value = responseState
            }
        }
    }

    fun removeLike(postId: String, currentUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // _likeUnlikePostStateFlow.value = ResponseState.loading()
                val responseState = removeLikeUseCase.invoke(
                    currentUserFirebaseId = currentUserFirebaseId,
                    postFirebaseId = postId
                )
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                // _likeUnlikePostStateFlow.value = responseState
            }
        }
    }

    fun savePost(currentUserFirebaseId: String, postId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState = savePostUseCase.invoke(currentUserFirebaseId, postId)
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                // _saveUnSavePostStateFlow.value = responseState
            }
        }
    }

    fun unSavePost(currentUserFirebaseId: String, postId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState = unSavePostUseCase.invoke(currentUserFirebaseId, postId)
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                //  _saveUnSavePostStateFlow.value = responseState
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deletePostStateFlow.value = ResponseState.loading()
                _deletePostStateFlow.value = deletePostUseCase.invoke(postId)
            }
        }
    }
}