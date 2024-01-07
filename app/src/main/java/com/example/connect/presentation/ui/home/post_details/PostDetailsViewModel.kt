package com.example.connect.presentation.ui.home.post_details

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddCommentUseCase
import com.example.connect.domain.useCase.posts.AddLikeUseCase
import com.example.connect.domain.useCase.posts.GetAllCommentsWithUsersUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeUseCase
import com.example.connect.domain.useCase.posts.SavePostUseCase
import com.example.connect.domain.useCase.posts.UnSavePostUseCase
import com.example.connect.presentation.base.BaseViewModel
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
class PostDetailsViewModel @Inject constructor(
    private val addLikeUseCase: AddLikeUseCase,
    private val removeLikeUseCase: RemoveLikeUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val unSavePostUseCase: UnSavePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getAllCommentsWithUsersUseCase: GetAllCommentsWithUsersUseCase
) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _getAllCommentsStateFlow: MutableStateFlow<ResponseState<Pair<MutableMap<CommentBean, ArrayList<CommentBean>>, List<UsersBean>>>> =
        MutableStateFlow(ResponseState.none())
    val getAllCommentsStateFlow: StateFlow<ResponseState<Pair<MutableMap<CommentBean, ArrayList<CommentBean>>, List<UsersBean>>>> get() = _getAllCommentsStateFlow

    lateinit var commentsStateMap: SnapshotStateMap<CommentBean, ArrayList<CommentBean>>

    private val _addCommentStateFlow: MutableStateFlow<ResponseState<CommentBean>> =
        MutableStateFlow(ResponseState.none())
    val addCommentStateFlow: StateFlow<ResponseState<CommentBean>> get() = _addCommentStateFlow

    val commentText = mutableStateOf("")
    var commentedOn: MutableState<CommentBean?> = mutableStateOf(null)

    var repliedCommentPosterConnectId = mutableStateOf("")
    var isInitialized = false

    lateinit var postId: String

    val isSendingComment = mutableStateOf(false)

    fun initialize(postId: String) {
        if (!isInitialized) {
            this.postId = postId
            commentsStateMap = mutableStateMapOf()
            isInitialized = true
        }
    }

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

    fun addComment(loggedInUserFirebaseId: String) {
        lateinit var comment: CommentBean
        if (commentedOn.value == null) {  // Parent comment
            comment = CommentBean(
                commentFirebaseId = "",
                createdAt = FunctionHelper.getCurrentTimeInMillis(),
                commentedBy = loggedInUserFirebaseId,
                parentCommentId = null,
                repliedOnCommentId = null,
                repliedOnUserId = null,
                postId = postId,
                comment = commentText.value
            )
        } else {  // Child comment
            comment = CommentBean(
                commentFirebaseId = "",
                createdAt = FunctionHelper.getCurrentTimeInMillis(),
                commentedBy = loggedInUserFirebaseId,
                parentCommentId = commentedOn.value!!.parentCommentId
                    ?: commentedOn.value!!.commentFirebaseId,
                repliedOnCommentId = commentedOn.value!!.commentFirebaseId,
                repliedOnUserId = commentedOn.value!!.commentedBy,
                postId = postId,
                comment = commentText.value
            )
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _addCommentStateFlow.value = ResponseState.loading()
                val response = addCommentUseCase.invoke(comment)
                if (response.status == RequestStatusEnum.Success) {
                    comment.commentFirebaseId = response.data ?: ""
                    if (comment.commentFirebaseId.isNotBlank()) {
                        commentedOn.value = null
                        commentText.value = ""
                        _addCommentStateFlow.value = ResponseState.success(comment)
                    } else {
                        _addCommentStateFlow.value = ResponseState.error("")
                    }
                } else {
                    _addCommentStateFlow.value = ResponseState.error(response.message ?: "")
                }
            }
        }
    }

    fun getAllCommentsWithUsers(postId: String, loggedInUserFireId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getAllCommentsStateFlow.value = ResponseState.loading()
                val getAllCommentResponse =
                    getAllCommentsWithUsersUseCase.invoke(postId, loggedInUserFireId)
                if (getAllCommentResponse.status == RequestStatusEnum.Success) {
                    val commentMap = getAllCommentResponse.data?.first
                    if (!commentMap.isNullOrEmpty()) {
                        commentsStateMap.putAll(commentMap)
                    }
                    _getAllCommentsStateFlow.value = getAllCommentResponse
                } else {
                    _getAllCommentsStateFlow.value = getAllCommentResponse
                }
            }
        }
    }
}