package com.teamproject2k.connect.presentation.ui.home.post_details

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.CommentBean
import com.teamproject2k.connect.domain.models.CommentWithUserBean
import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.posts.AddCommentOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.AddLikeForCommentOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.AddLikeOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.DeleteCommentOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.DeletePostFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.DeletePostFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.GetAllCommentsWithUsersFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.RemoveLikeForCommentFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.RemoveLikeOfPostFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.SavePostOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.UnSavePostFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.UpdatePostDetailsOnLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.UpdatePostVisibilityOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateUserOnLocalUseCase
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.ui.models.VisibilityScope
import com.teamproject2k.connect.presentation.utils.FunctionHelper
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

    lateinit var post: PostBean
    lateinit var postVisibilityScopeList: List<VisibilityScope>
    lateinit var currentPostVisibilityState: MutableState<VisibilityScope>
    lateinit var isPostLikedByLoggedInUserState: MutableState<Boolean>
    lateinit var isPostSavedByLoggedInUserState: MutableState<Boolean>

    var isDataInitialized = false
    var isCommentDataFetched: Boolean = false

    val forceRecomposeState = mutableIntStateOf(0)
    val getCommentListState = mutableIntStateOf(-1)
    val commentTextState = mutableStateOf("")
    val repliedCommentPosterConnectIdState = mutableStateOf("")
    val isSendingCommentState = mutableStateOf(false)
    var showDeletePostAlertDialogState = mutableStateOf(false)
    val commentedOnState: MutableState<CommentBean?> = mutableStateOf(null)

    private val _likeUnlikePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val likeUnlikePostStateFlow = _likeUnlikePostStateFlow.asStateFlow()

    private val _saveUnSavePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val saveUnSavePostStateFlow = _saveUnSavePostStateFlow.asStateFlow()

    var commentDataMap = mutableMapOf<CommentWithUserBean, ArrayList<CommentWithUserBean>>()
    val snackBarMessageState = mutableStateOf("")

    private val _deletePostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val deletePostStateFlow = _deletePostStateFlow.asStateFlow()

    private val _getAllCommentsStateFlow: MutableStateFlow<ResponseState<List<UserBean>>> =
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

    /**
     * Initializes the ViewModel with the provided post details and logged-in user information.
     * This function sets up the ViewModel with the given post details, including its visibility scope,
     * whether it is liked and saved by the logged-in user.
     *
     * @param context The application context.
     * @param post The post details to be initialized with.
     * @param loggedInUserBean The details of the logged-in user.
     */
    fun initialize(context: Context, post: PostBean, loggedInUserBean: UserBean) {
        this.post = post
        postVisibilityScopeList = FunctionHelper.getPostVisibilityList(context)
        val postVisibility =
            postVisibilityScopeList.find { it.scopeEnum.name == post.postVisibilityScope }
        if (postVisibility != null) {
            currentPostVisibilityState = mutableStateOf(postVisibility)
        }
        isPostLikedByLoggedInUserState =
            mutableStateOf(post.likedBy.contains(loggedInUserBean.firebaseUserId))
        isPostSavedByLoggedInUserState =
            mutableStateOf(loggedInUserBean.savedPosts.contains(post.postFirebaseId))
        isDataInitialized = true
    }

    /**
     * Adds a like to the post.
     * This function adds a like to the specified post by the logged-in user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     */
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

    /**
     * Removes the like for the post.
     * This function removes the like for the specified post made by the logged-in user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     */
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

    /**
     * Saves the post.
     * This function saves the specified post to the saved posts list of the logged-in user.
     *
     * @param loggedInUserBean The user object representing the logged-in user.
     */
    fun savePost(loggedInUserBean: UserBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState =
                    savePostOnRemoteUseCase(
                        loggedInUserBean.firebaseUserId,
                        post.postFirebaseId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUserBean.savedPosts.add(post.postFirebaseId)
                    updateUserOnLocalUseCase(loggedInUserBean)
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

    /**
     * Un-saves the post.
     * This function removes the specified post from the saved posts list of the logged-in user.
     *
     * @param loggedInUserBean The user object representing the logged-in user.
     */
    fun unSavePost(loggedInUserBean: UserBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState =
                    unSavePostFromRemoteUseCase(
                        loggedInUserBean.firebaseUserId,
                        post.postFirebaseId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUserBean.savedPosts.remove(post.postFirebaseId)
                    updateUserOnLocalUseCase(loggedInUserBean)
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

    /**
     * Deletes the post.
     * This function deletes the specified post both remotely and locally if the logged-in user has the authority to do so.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     */
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

    /**
     * Adds a comment to the post.
     * This function allows the logged-in user to add a comment to the specified post.
     *
     * @param loggedInUser The user object representing the logged-in user.
     */
    fun addComment(loggedInUser: UserBean) {
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
                                mutableMapOf<CommentWithUserBean, ArrayList<CommentWithUserBean>>()
                            updatedMap[CommentWithUserBean(comment, loggedInUser)] = arrayListOf()
                            updatedMap.putAll(commentDataMap)
                            commentDataMap = updatedMap
                        } else {
                            val parent =
                                commentDataMap.keys.find { it.comment.commentFirebaseId == comment.parentCommentId }
                            if (parent != null) {
                                val updatedChildList = arrayListOf<CommentWithUserBean>()
                                val currentChildList = commentDataMap[parent]
                                if (currentChildList != null) {
                                    updatedChildList.addAll(currentChildList)
                                    updatedChildList.add(
                                        CommentWithUserBean(
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

    /**
     * Deletes a comment from the post.
     * This function deletes the specified comment both remotely and locally, and updates the associated post details.
     *
     * @param comment The comment to be deleted.
     * @param deleteCount The number of comments to be deleted (including the specified comment).
     */
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

    /**
     * Retrieves all comments along with associated user details for a particular post from the remote server.
     * This function fetches comments and user details asynchronously and updates the local state accordingly.
     *
     * @param loggedInUserFireId The Firebase ID of the logged-in user.
     */
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

    /**
     * Adds a like for a comment given by the logged-in user.
     * This function adds the like for the specified comment both remotely and locally.
     *
     * @param comment The comment for which the like is to be added.
     * @param loggedInUserFireId The Firebase ID of the logged-in user.
     * @param onSuccess Callback function to be called upon successful addition of the like.
     * @param onError Callback function to be called upon encountering an error during the addition process. Receives an optional error message.
     */
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

    /**
     * Removes a like for a comment given by the logged-in user.
     * This function removes the like for the specified comment both remotely and locally.
     *
     * @param comment The comment for which the like is to be removed.
     * @param loggedInUserFireId The Firebase ID of the logged-in user.
     * @param onSuccess Callback function to be called upon successful removal of the like.
     * @param onError Callback function to be called upon encountering an error during the removal process. Receives an optional error message.
     */
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

    /**
     * Updates the visibility scope of a post.
     * This function updates the visibility scope of the specified post both remotely and locally.
     *
     * @param postScope The new visibility scope for the post.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     */
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