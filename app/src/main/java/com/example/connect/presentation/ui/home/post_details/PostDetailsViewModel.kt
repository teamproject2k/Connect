package com.example.connect.presentation.ui.home.post_details

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.models.CommentWithUser
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddCommentOnRemoteUseCase
import com.example.connect.domain.useCase.posts.AddLikeForCommentOnRemoteUseCase
import com.example.connect.domain.useCase.posts.AddLikeOnRemoteUseCase
import com.example.connect.domain.useCase.posts.DeleteCommentOnRemoteUseCase
import com.example.connect.domain.useCase.posts.DeletePostFromLocalUseCase
import com.example.connect.domain.useCase.posts.DeletePostFromRemoteUseCase
import com.example.connect.domain.useCase.posts.GetAllCommentsWithUsersFromRemoteUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeForCommentFromRemoteUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeOfPostFromRemoteUseCase
import com.example.connect.domain.useCase.posts.SavePostOnRemoteUseCase
import com.example.connect.domain.useCase.posts.UnSavePostFromRemoteUseCase
import com.example.connect.domain.useCase.posts.UpdatePostDetailsOnLocalUseCase
import com.example.connect.domain.useCase.posts.UpdatePostVisibilityOnRemoteUseCase
import com.example.connect.domain.useCase.user.UpdateUserOnLocalUseCase
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
    private val addLikeOnRemoteUseCase: AddLikeOnRemoteUseCase,
    private val removeLikeOfPostFromRemoteUseCase: RemoveLikeOfPostFromRemoteUseCase,
    private val savePostOnRemoteUseCase: SavePostOnRemoteUseCase,
    private val unSavePostFromRemoteUseCase: UnSavePostFromRemoteUseCase,
    private val addCommentOnRemoteUseCase: AddCommentOnRemoteUseCase,
    private val getAllCommentsWithUsersFromRemoteUseCase: GetAllCommentsWithUsersFromRemoteUseCase,
    private val deleteCommentOnRemoteUseCase: DeleteCommentOnRemoteUseCase,
    private val addLikeForCommentOnRemoteUseCase: AddLikeForCommentOnRemoteUseCase,
    private val removeLikeForCommentFromRemoteUseCase: RemoveLikeForCommentFromRemoteUseCase,
    private val deletePostFromRemoteUseCase: DeletePostFromRemoteUseCase,
    private val updatePostVisibilityOnRemoteUseCase: UpdatePostVisibilityOnRemoteUseCase,
    private val updatePostDetailsOnLocalUseCase: UpdatePostDetailsOnLocalUseCase,
    private val updateUserOnLocalUseCase: UpdateUserOnLocalUseCase,
    private val deletePostFromLocalUseCase: DeletePostFromLocalUseCase
) : BaseViewModel() {

    private val _likeUnlikePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val likeUnlikePostStateFlow = _likeUnlikePostStateFlow.asStateFlow()

    private val _saveUnSavePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val saveUnSavePostStateFlow = _saveUnSavePostStateFlow.asStateFlow()

    var commentDataMap = mutableMapOf<CommentWithUser, ArrayList<CommentWithUser>>()


    val snackBarMessageState = mutableStateOf("")

    private val _deletePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val deletePostStateFlow = _deletePostStateFlow.asStateFlow()

    private val _getAllCommentsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val getAllCommentsStateFlow = _getAllCommentsStateFlow.asStateFlow()

    private val _addCommentStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val addCommentStateFlow = _addCommentStateFlow.asStateFlow()

    private val _deleteCommentStateFlow: MutableStateFlow<ResponseState<Nothing>> =
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

    lateinit var isPostLikedByLoggedInUserState: MutableState<Boolean>

    lateinit var isPostSavedByLoggedInUserState: MutableState<Boolean>

    val getCommentListState = mutableIntStateOf(-1)

    var isCommentDataFetched: Boolean = false

    var showDeletePostAlertDialogState = mutableStateOf(false)

    fun initialize(context: Context, post: PostBean, loggedInUsersBean: UsersBean) {
        this.post = post
        postVisibilityScopeList = FunctionHelper.getPostVisibilityList(context)
        val postVisibility =
            postVisibilityScopeList.find { it.scopeEnum.name == post.postVisibilityScope }
        if (postVisibility != null) {
            currentPostVisibilityState = mutableStateOf(postVisibility)
        }
        isPostLikedByLoggedInUserState =
            mutableStateOf(post.likedBy.contains(loggedInUsersBean.firebaseUserId))
        isPostSavedByLoggedInUserState =
            mutableStateOf(loggedInUsersBean.savedPosts.contains(post.postFirebaseId))
        isInitialized = true
    }

    fun addLikeOnPost(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _likeUnlikePostStateFlow.value = ResponseState.loading()
                val addLikeResponse = addLikeOnRemoteUseCase(
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    postFirebaseId = post.postFirebaseId
                )
                if (addLikeResponse.status == RequestStatusEnum.Success) {
                    post.likedBy.add(loggedInUserFirebaseId)
                    updatePostDetailsOnLocalUseCase(post)
                    withContext(Dispatchers.Main) {
                        isPostLikedByLoggedInUserState.value = true
                    }
                    _likeUnlikePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (addLikeResponse.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase(post.postFirebaseId)
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
                val removeLikeResponse = removeLikeOfPostFromRemoteUseCase(
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    postFirebaseId = post.postFirebaseId
                )
                if (removeLikeResponse.status == RequestStatusEnum.Success) {
                    post.likedBy.remove(loggedInUserFirebaseId)
                    updatePostDetailsOnLocalUseCase(post)
                    withContext(Dispatchers.Main) {
                        isPostLikedByLoggedInUserState.value = false
                    }
                    _likeUnlikePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (removeLikeResponse.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase(post.postFirebaseId)
                    }
                    _likeUnlikePostStateFlow.value =
                        ResponseState.error(removeLikeResponse.message ?: "")
                }

            }
        }
    }

    fun savePost(loggedInUsersBean: UsersBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState =
                    savePostOnRemoteUseCase(
                        loggedInUsersBean.firebaseUserId,
                        post.postFirebaseId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.add(post.postFirebaseId)
                    updateUserOnLocalUseCase(loggedInUsersBean)
                    withContext(Dispatchers.Main) {
                        isPostSavedByLoggedInUserState.value = true
                    }
                    _saveUnSavePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (responseState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase(post.postFirebaseId)
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
                    unSavePostFromRemoteUseCase(
                        loggedInUsersBean.firebaseUserId,
                        post.postFirebaseId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.remove(post.postFirebaseId)
                    updateUserOnLocalUseCase(loggedInUsersBean)
                    withContext(Dispatchers.Main) {
                        isPostSavedByLoggedInUserState.value = false
                    }
                    _saveUnSavePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (responseState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase(post.postFirebaseId)
                    }
                    _saveUnSavePostStateFlow.value =
                        ResponseState.error(responseState.message ?: "")
                }
            }
        }
    }

    fun deletePost(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deletePostStateFlow.value = ResponseState.loading()
                if (loggedInUserFirebaseId == post.createdByUserFirebaseId) {
                    val deletePostResponse = deletePostFromRemoteUseCase(post.postFirebaseId)
                    if (deletePostResponse.status == RequestStatusEnum.Success) {
                        deletePostFromLocalUseCase(post.postFirebaseId)
                    }
                    _deletePostStateFlow.value = deletePostResponse
                } else {
                    _deletePostStateFlow.value =
                        ResponseState.error(FirebaseErrorCodes.UNAUTHORIZED_ACCESS)
                }
            }
        }
    }

    fun addComment(loggedInUser: UsersBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _addCommentStateFlow.value = ResponseState.loading()
                val comment: CommentBean
                val commentedOn = commentedOnState.value
                if (commentedOn == null) {  // Parent comment
                    comment = CommentBean(
                        commentFirebaseId = "",
                        createdAt = FunctionHelper.getCurrentTimeInMillis(),
                        commentedBy = loggedInUser.firebaseUserId,
                        parentCommentId = null,
                        repliedOnCommentId = null,
                        repliedOnUserId = null,
                        postFirebaseId = post.postFirebaseId,
                        commentMessage = commentTextState.value,
                        whetherDeleted = false,
                        likedBy = arrayListOf()
                    )
                } else {  // Child comment
                    comment = CommentBean(
                        commentFirebaseId = "",
                        createdAt = FunctionHelper.getCurrentTimeInMillis(),
                        commentedBy = loggedInUser.firebaseUserId,
                        parentCommentId = commentedOn.parentCommentId
                            ?: commentedOn.commentFirebaseId,
                        repliedOnCommentId = commentedOn.commentFirebaseId,
                        repliedOnUserId = commentedOn.commentedBy,
                        postFirebaseId = post.postFirebaseId,
                        commentMessage = commentTextState.value,
                        whetherDeleted = false,
                        likedBy = arrayListOf()
                    )
                }
                val addCommentResponseState = addCommentOnRemoteUseCase(comment)
                if (addCommentResponseState.status == RequestStatusEnum.Success) {
                    comment.commentFirebaseId = addCommentResponseState.data ?: ""
                    if (comment.commentFirebaseId.isNotBlank()) {
                        post.commentCount++
                        updatePostDetailsOnLocalUseCase(post)
                        if (comment.parentCommentId == null) {
                            val updatedMap =
                                mutableMapOf<CommentWithUser, ArrayList<CommentWithUser>>()
                            updatedMap[CommentWithUser(comment, loggedInUser)] = arrayListOf()
                            updatedMap.putAll(commentDataMap)
                            commentDataMap = updatedMap
                        } else {
                            val parent =
                                commentDataMap.keys.find { it.comment.commentFirebaseId == comment.parentCommentId }
                            if (parent != null) {
                                val updatedChildList = arrayListOf<CommentWithUser>()
                                val currentChildList = commentDataMap[parent]
                                if (currentChildList != null) {
                                    updatedChildList.addAll(currentChildList)
                                    updatedChildList.add(
                                        CommentWithUser(
                                            comment,
                                            loggedInUser,
                                            repliedCommentPosterConnectIdState.value
                                        )
                                    )
                                    commentDataMap[parent] = updatedChildList
                                }
                            }
                        }
                        _addCommentStateFlow.value = ResponseState.success(null)
                    } else {
                        _addCommentStateFlow.value = ResponseState.error("")
                    }
                } else {
                    if (addCommentResponseState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase(post.postFirebaseId)
                    }
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
                    deleteCommentOnRemoteUseCase(
                        comment.commentFirebaseId,
                        post.postFirebaseId,
                        deleteCount
                    )
                if (deleteCommentResponseState.status == RequestStatusEnum.Success) {
                    comment.whetherDeleted = true
                    post.commentCount -= deleteCount
                    updatePostDetailsOnLocalUseCase(post)
                    if (comment.parentCommentId == null) {
                        //parent comment
                        commentDataMap.keys.removeIf { it.comment.commentFirebaseId == comment.commentFirebaseId }

                    } else {
                        val parent =
                            commentDataMap.keys.find { it.comment.commentFirebaseId == comment.parentCommentId }
                        commentDataMap[parent]?.removeIf { it.comment.commentFirebaseId == comment.commentFirebaseId }
                    }
                    _deleteCommentStateFlow.value =
                        ResponseState.success(null)
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
                val getAllCommentsResponse =
                    getAllCommentsWithUsersFromRemoteUseCase(
                        post.postFirebaseId,
                        loggedInUserFireId
                    )
                if (getAllCommentsResponse.status == RequestStatusEnum.Success && getAllCommentsResponse.data != null) {
                    commentDataMap = getAllCommentsResponse.data
                    isCommentDataFetched = true
                    _getAllCommentsStateFlow.value = ResponseState.success(null)
                } else {
                    _getAllCommentsStateFlow.value =
                        ResponseState.error(getAllCommentsResponse.message ?: "")
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
                    addLikeForCommentOnRemoteUseCase(
                        comment.commentFirebaseId,
                        loggedInUserFireId
                    )
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
                    removeLikeForCommentFromRemoteUseCase(
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

    fun updatePostVisibility(postScope: VisibilityScope, loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _updatePostVisibilityStateFlow.value = ResponseState.loading()
                if (post.createdByUserFirebaseId == loggedInUserFirebaseId) {
                    val response =
                        updatePostVisibilityOnRemoteUseCase(
                            post.postFirebaseId,
                            postScope.scopeEnum.name
                        )
                    if (response.status == RequestStatusEnum.Success) {
                        post.postVisibilityScope = postScope.scopeEnum.name
                        updatePostDetailsOnLocalUseCase(post)
                        _updatePostVisibilityStateFlow.value = ResponseState.success(postScope)
                    } else {
                        _updatePostVisibilityStateFlow.value =
                            ResponseState.error(response.message ?: "")
                    }
                } else {
                    _updatePostVisibilityStateFlow.value =
                        ResponseState.error(FirebaseErrorCodes.UNAUTHORIZED_ACCESS)
                }
            }
        }
    }
}