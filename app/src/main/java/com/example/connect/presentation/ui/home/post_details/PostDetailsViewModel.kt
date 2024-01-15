package com.example.connect.presentation.ui.home.post_details

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.connect.domain.useCase.posts.DeletePostFromLocalUseCase
import com.example.connect.domain.useCase.posts.DeletePostFromRemoteUseCase
import com.example.connect.domain.useCase.posts.GetAllCommentsWithUsersUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeForCommentUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeUseCase
import com.example.connect.domain.useCase.posts.SavePostUseCase
import com.example.connect.domain.useCase.posts.UnSavePostUseCase
import com.example.connect.domain.useCase.posts.UpdatePostDetailsOnLocalUseCase
import com.example.connect.domain.useCase.posts.UpdatePostVisibilityOnRemoteUseCase
import com.example.connect.domain.useCase.user.UpdateUserDetailsOnLocal
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.models.VisibilityScope
import com.example.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val removeLikeForCommentUseCase: RemoveLikeForCommentUseCase,
    private val deletePostFromRemoteUseCase: DeletePostFromRemoteUseCase,
    private val updatePostVisibilityOnRemoteUseCase: UpdatePostVisibilityOnRemoteUseCase,
    private val updatePostDetailsOnLocalUseCase: UpdatePostDetailsOnLocalUseCase,
    private val updateUserDetailsOnLocal: UpdateUserDetailsOnLocal,
    private val deletePostFromLocalUseCase: DeletePostFromLocalUseCase
) : BaseViewModel() {

    private val _likeUnlikePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val likeUnlikePostStateFlow = _likeUnlikePostStateFlow.asStateFlow()

    private val _saveUnSavePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val saveUnSavePostStateFlow = _saveUnSavePostStateFlow.asStateFlow()

    var commentDataMap = mutableMapOf<CommentBean, ArrayList<CommentBean>>()

    val isDropdownMenuVisibleState = mutableStateOf(false)

    val snackBarMessageState = mutableStateOf("")

    private val _deletePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val deletePostStateFlow = _deletePostStateFlow.asStateFlow()

    private val _getAllCommentsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val getAllCommentsStateFlow = _getAllCommentsStateFlow.asStateFlow()

    private val _addCommentStateFlow: MutableStateFlow<ResponseState<CommentBean>> =
        MutableStateFlow(ResponseState.none())
    val addCommentStateFlow = _addCommentStateFlow.asStateFlow()

    private val _deleteCommentStateFlow: MutableStateFlow<ResponseState<Triple<String, String?, Int>>> =
        MutableStateFlow(ResponseState.none())
    val deleteCommentStateFlow = _deleteCommentStateFlow.asStateFlow()

    private val _updatePostVisibilityStateFlow: MutableStateFlow<ResponseState<VisibilityScope>> =
        MutableStateFlow(ResponseState.none())
    val updatePostVisibilityStateFlow = _updatePostVisibilityStateFlow.asStateFlow()

    val commentTextState = mutableStateOf("")
    val commentedOnState: MutableState<CommentBean?> = mutableStateOf(null)

    val forceRecomposeState = mutableIntStateOf(0)
    val repliedCommentPosterConnectIdState = mutableStateOf("")
    var isInitialized = false

    lateinit var post: PostBean

    val isSendingCommentState = mutableStateOf(false)

    lateinit var postVisibilityScopeList: List<VisibilityScope>

    lateinit var currentPostVisibilityState: MutableState<VisibilityScope>

    lateinit var isPostLikedByLoggedInUser: MutableState<Boolean>

    lateinit var isPostSavedByLoggedInUser: MutableState<Boolean>

    fun initialize(context: Context, post: PostBean, loggedInUsersBean: UsersBean) {
        this.post = post
        postVisibilityScopeList = FunctionHelper.getPostVisibilityList(context)
        val postVisibility =
            postVisibilityScopeList.find { it.scopeEnum.name == post.postVisibilityScope }
        if (postVisibility != null) {
            currentPostVisibilityState = mutableStateOf(postVisibility)
        }
        isPostLikedByLoggedInUser =
            mutableStateOf(post.likedBy.contains(loggedInUsersBean.firebaseUserId))
        isPostSavedByLoggedInUser =
            mutableStateOf(loggedInUsersBean.savedPosts.contains(post.postFirebaseId))
        isInitialized = true
    }

    fun addLikeOnPost(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _likeUnlikePostStateFlow.value = ResponseState.loading()
                val addLikeResponse = addLikeUseCase.invoke(
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    postFirebaseId = post.postFirebaseId
                )
                if (addLikeResponse.status == RequestStatusEnum.Success) {
                    post.likedBy.add(loggedInUserFirebaseId)
                    updatePostDetailsOnLocalUseCase.invoke(post)
                    withContext(Dispatchers.Main) {
                        isPostLikedByLoggedInUser.value = true
                    }
                    _likeUnlikePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (addLikeResponse.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase.invoke(post.postFirebaseId)
                    }
                    _likeUnlikePostStateFlow.value =
                        ResponseState.error(addLikeResponse.message ?: "")
                }
            }
        }
    }

    fun removeLikeForPost(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _likeUnlikePostStateFlow.value = ResponseState.loading()
                val removeLikeResponse = removeLikeUseCase.invoke(
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    postFirebaseId = post.postFirebaseId
                )
                if (removeLikeResponse.status == RequestStatusEnum.Success) {
                    post.likedBy.remove(loggedInUserFirebaseId)
                    updatePostDetailsOnLocalUseCase.invoke(post)
                    withContext(Dispatchers.Main) {
                        isPostLikedByLoggedInUser.value = false
                    }
                    _likeUnlikePostStateFlow.value = ResponseState.success(null)
                }
                if (removeLikeResponse.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                    deletePostFromLocalUseCase.invoke(post.postFirebaseId)
                }
                _likeUnlikePostStateFlow.value =
                    ResponseState.error(removeLikeResponse.message ?: "")
            }
        }
    }

    fun savePost(loggedInUsersBean: UsersBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState =
                    savePostUseCase.invoke(loggedInUsersBean.firebaseUserId, post.postFirebaseId)
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.add(post.postFirebaseId)
                    updateUserDetailsOnLocal.invoke(loggedInUsersBean)
                    withContext(Dispatchers.Main) {
                        isPostSavedByLoggedInUser.value = true
                    }
                    _saveUnSavePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (responseState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase.invoke(post.postFirebaseId)
                    }
                    _saveUnSavePostStateFlow.value =
                        ResponseState.error(responseState.message ?: "")
                }
            }
        }
    }

    fun unSavePost(loggedInUsersBean: UsersBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState =
                    unSavePostUseCase.invoke(loggedInUsersBean.firebaseUserId, post.postFirebaseId)
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.remove(post.postFirebaseId)
                    updateUserDetailsOnLocal.invoke(loggedInUsersBean)
                    withContext(Dispatchers.Main) {
                        isPostSavedByLoggedInUser.value = false
                    }
                    _saveUnSavePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (responseState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase.invoke(post.postFirebaseId)
                    }
                    _saveUnSavePostStateFlow.value =
                        ResponseState.error(responseState.message ?: "")
                }
            }
        }
    }

    fun deletePost() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deletePostStateFlow.value = ResponseState.loading()
                val deletePostResponse = deletePostFromRemoteUseCase.invoke(post.postFirebaseId)
                if (deletePostResponse.status == RequestStatusEnum.Success) {
                    deletePostFromLocalUseCase.invoke(post.postFirebaseId)
                }
                _deletePostStateFlow.value = deletePostResponse
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
                postId = post.postFirebaseId,
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
                postId = post.postFirebaseId,
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
                    deleteCommentUseCase.invoke(
                        comment.commentFirebaseId,
                        post.postFirebaseId,
                        deleteCount
                    )
                if (deleteCommentResponseState.status == RequestStatusEnum.Success) {
                    comment.whetherDeleted = true
                    _deleteCommentStateFlow.value =
                        ResponseState.success(
                            Triple(
                                comment.commentFirebaseId,
                                comment.parentCommentId,
                                deleteCount
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
                    getAllCommentsWithUsersUseCase.invoke(post.postFirebaseId, loggedInUserFireId)
                if (getAllCommentResponseState.status == RequestStatusEnum.Success) {
                    val commentMap = getAllCommentResponseState.data?.first
                    if (!commentMap.isNullOrEmpty()) {
                        commentDataMap = commentMap
                    }
                    _getAllCommentsStateFlow.value =
                        ResponseState.success(getAllCommentResponseState.data?.second)
                } else {
                    _getAllCommentsStateFlow.value =
                        ResponseState.error(getAllCommentResponseState.message ?: "")
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

    fun updatePostVisibility(postScope: VisibilityScope) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _updatePostVisibilityStateFlow.value = ResponseState.loading()
                val response =
                    updatePostVisibilityOnRemoteUseCase.invoke(
                        post.postFirebaseId,
                        postScope.scopeEnum.name
                    )
                if (response.status == RequestStatusEnum.Success) {
                    _updatePostVisibilityStateFlow.value = ResponseState.success(postScope)
                } else {
                    _updatePostVisibilityStateFlow.value =
                        ResponseState.error(response.message ?: "")
                }
            }
        }
    }
}