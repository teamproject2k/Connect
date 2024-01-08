package com.example.connect.presentation.ui.home.post_details

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddCommentUseCase
import com.example.connect.domain.useCase.posts.AddLikeForCommentUseCase
import com.example.connect.domain.useCase.posts.AddLikeUseCase
import com.example.connect.domain.useCase.posts.DeleteCommentUseCase
import com.example.connect.domain.useCase.posts.GetAllCommentsWithUsersUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeForCommentUseCase
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

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val addLikeUseCase: AddLikeUseCase,
    private val removeLikeUseCase: RemoveLikeUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val unSavePostUseCase: UnSavePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getAllCommentsWithUsersUseCase: GetAllCommentsWithUsersUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val addLikeForCommentUseCase: AddLikeForCommentUseCase,
    private val removeLikeForCommentUseCase: RemoveLikeForCommentUseCase
) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _getAllCommentsStateFlow: MutableStateFlow<ResponseState<Pair<MutableMap<CommentBean, ArrayList<CommentBean>>, List<UsersBean>>>> =
        MutableStateFlow(ResponseState.none())
    val getAllCommentsStateFlow: StateFlow<ResponseState<Pair<MutableMap<CommentBean, ArrayList<CommentBean>>, List<UsersBean>>>> get() = _getAllCommentsStateFlow

    @SuppressLint("StateNameRule")
    lateinit var commentsMapState: SnapshotStateMap<CommentBean, ArrayList<CommentBean>>

    private val _addCommentStateFlow: MutableStateFlow<ResponseState<CommentBean>> =
        MutableStateFlow(ResponseState.none())
    val addCommentStateFlow: StateFlow<ResponseState<CommentBean>> get() = _addCommentStateFlow

    private val _deleteCommentStateFlow: MutableStateFlow<ResponseState<Pair<String, String?>>> =
        MutableStateFlow(ResponseState.none())
    val deleteCommentStateFlow: StateFlow<ResponseState<Pair<String, String?>>> get() = _deleteCommentStateFlow

    val commentTextState = mutableStateOf("")
    val commentedOnState: MutableState<CommentBean?> = mutableStateOf(null)

    val repliedCommentPosterConnectIdState = mutableStateOf("")
    var isInitialized = false

    lateinit var post: PostBean

    val isSendingCommentState = mutableStateOf(false)

    fun initialize(post: PostBean) {
        if (!isInitialized) {
            this.post = post
            commentsMapState = mutableStateMapOf()
            isInitialized = true
        }
    }

    fun addLike(currentUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                //_likeUnlikePostStateFlow.value = ResponseState.loading()
                val responseState = addLikeUseCase.invoke(
                    currentUserFirebaseId = currentUserFirebaseId,
                    postFirebaseId = post.id
                )
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                //   _likeUnlikePostStateFlow.value = responseState
            }
        }
    }

    fun removeLike(currentUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // _likeUnlikePostStateFlow.value = ResponseState.loading()
                val responseState = removeLikeUseCase.invoke(
                    currentUserFirebaseId = currentUserFirebaseId,
                    postFirebaseId = post.id
                )
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                // _likeUnlikePostStateFlow.value = responseState
            }
        }
    }

    fun savePost(currentUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState = savePostUseCase.invoke(currentUserFirebaseId, post.id)
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                // _saveUnSavePostStateFlow.value = responseState
            }
        }
    }

    fun unSavePost(currentUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState = unSavePostUseCase.invoke(currentUserFirebaseId, post.id)
                if (responseState.status == RequestStatusEnum.Success) {
                    onUpdate()
                }
                //  _saveUnSavePostStateFlow.value = responseState
            }
        }
    }

    fun addComment(loggedInUserFirebaseId: String) {
        val comment: CommentBean
        val commentedOn = commentedOnState.value
        if (commentedOn == null) {  // Parent comment
            comment = CommentBean(
                commentFirebaseId = "",
                createdAt = FunctionHelper.getCurrentTimeInMillis(),
                commentedBy = loggedInUserFirebaseId,
                parentCommentId = null,
                repliedOnCommentId = null,
                repliedOnUserId = null,
                postId = post.id,
                commentMessage = commentTextState.value,
                whetherDeleted = false,
                arrayListOf()
            )
        } else {  // Child comment
            comment = CommentBean(
                commentFirebaseId = "",
                createdAt = FunctionHelper.getCurrentTimeInMillis(),
                commentedBy = loggedInUserFirebaseId,
                parentCommentId = commentedOn.parentCommentId ?: commentedOn.commentFirebaseId,
                repliedOnCommentId = commentedOn.commentFirebaseId,
                repliedOnUserId = commentedOn.commentedBy,
                postId = post.id,
                commentMessage = commentTextState.value,
                whetherDeleted = false,
                arrayListOf()
            )
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _addCommentStateFlow.value = ResponseState.loading()
                val addCommentResponseState = addCommentUseCase.invoke(comment)
                if (addCommentResponseState.status == RequestStatusEnum.Success) {
                    comment.commentFirebaseId = addCommentResponseState.data ?: ""
                    if (comment.commentFirebaseId.isNotBlank()) {
                        _addCommentStateFlow.value = ResponseState.success(comment)
                    } else {
                        _addCommentStateFlow.value = ResponseState.error("")
                    }
                } else {
                    _addCommentStateFlow.value =
                        ResponseState.error(addCommentResponseState.message ?: "")
                }
            }
        }
    }

    fun deleteComment(comment: CommentBean, deleteCount: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deleteCommentStateFlow.value = ResponseState.loading()
                val deleteCommentResponseState =
                    deleteCommentUseCase.invoke(comment.commentFirebaseId, post.id, deleteCount)
                if (deleteCommentResponseState.status == RequestStatusEnum.Success) {
                    comment.whetherDeleted = true
                    _deleteCommentStateFlow.value =
                        ResponseState.success(
                            Pair(
                                comment.commentFirebaseId,
                                comment.parentCommentId
                            )
                        )
                } else {
                    _deleteCommentStateFlow.value =
                        ResponseState.error(deleteCommentResponseState.message ?: "")
                }
            }
        }
    }

    fun getAllCommentsWithUsers(loggedInUserFireId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getAllCommentsStateFlow.value = ResponseState.loading()
                val getAllCommentResponseState =
                    getAllCommentsWithUsersUseCase.invoke(post.id, loggedInUserFireId)
                if (getAllCommentResponseState.status == RequestStatusEnum.Success) {
                    val commentMap = getAllCommentResponseState.data?.first
                    if (!commentMap.isNullOrEmpty()) {
                        commentsMapState.putAll(commentMap)
                    }
                    _getAllCommentsStateFlow.value = getAllCommentResponseState
                } else {
                    _getAllCommentsStateFlow.value = getAllCommentResponseState
                }
            }
        }
    }

    fun addLikeForComment(
        comment: CommentBean,
        loggedInUserFireId: String,
        onSuccess: () -> Unit,
        onError: (errorMessage: String?) -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val addLikeForCommentResponseState =
                    addLikeForCommentUseCase.invoke(comment.commentFirebaseId, loggedInUserFireId)
                if (addLikeForCommentResponseState.status == RequestStatusEnum.Success) {
                    if (!comment.likedBy.contains(loggedInUserFireId)) {
                        comment.likedBy.add(loggedInUserFireId)
                    }
                    onSuccess()
                } else {
                    onError(addLikeForCommentResponseState.message)
                }
            }
        }
    }

    fun removeLikeForComment(
        comment: CommentBean,
        loggedInUserFireId: String,
        onSuccess: () -> Unit,
        onError: (errorMessage: String?) -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val removeLikeForCommentResponseState =
                    removeLikeForCommentUseCase.invoke(
                        comment.commentFirebaseId,
                        loggedInUserFireId
                    )
                if (removeLikeForCommentResponseState.status == RequestStatusEnum.Success) {
                    comment.likedBy.remove(loggedInUserFireId)
                    onSuccess()
                } else {
                    onError(removeLikeForCommentResponseState.message)
                }
            }
        }
    }
}