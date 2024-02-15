package com.teamproject2k.connect.presentation.ui.home.saved_posts

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.models.PostWithUserDetailsBean
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.posts.AddLikeOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.AddPostListToLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.DeleteAllPostFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.DeletePostFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.GetPostDetailsWithUserDetailsFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.GetSavedPostDetailsWithUserFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.RemoveLikeOfPostFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.SavePostOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.UnSavePostFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.UpdatePostDetailsOnLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.AddUserListToLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.DeleteAllUsersExceptInListFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateUserOnLocalUseCase
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SavedPostsViewModel @Inject constructor(
    private val postDetailsWithUserDetailsUseCase: GetPostDetailsWithUserDetailsFromRemoteUseCase,
    private val addLikeOnRemoteUseCase: AddLikeOnRemoteUseCase,
    private val removeLikeOfPostFromRemoteUseCase: RemoveLikeOfPostFromRemoteUseCase,
    private val savePostOnRemoteUseCase: SavePostOnRemoteUseCase,
    private val unSavePostFromRemoteUseCase: UnSavePostFromRemoteUseCase,
    private val updateUserOnLocalUseCase: UpdateUserOnLocalUseCase,
    private val getSavedPostDetailsWithUserFromLocalUseCase: GetSavedPostDetailsWithUserFromLocalUseCase,
    private val addPostListToLocalUseCase: AddPostListToLocalUseCase,
    private val addUserListToLocalUseCase: AddUserListToLocalUseCase,
    private val updatePostDetailsOnLocalUseCase: UpdatePostDetailsOnLocalUseCase,
    private val deletePostFromLocalUseCase: DeletePostFromLocalUseCase,
    private val deleteAllUsersExceptInListFromLocalUseCase: DeleteAllUsersExceptInListFromLocalUseCase,
    private val deleteAllPostsFromLocal: DeleteAllPostFromLocalUseCase,

    ) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")
    var postListWithUserDetailsListState = mutableStateListOf<PostWithUserDetailsBean>()

    private val _getSavedPostsWithUsersStateFlow: MutableStateFlow<ResponseState<List<PostWithUserDetailsBean>>> =
        MutableStateFlow(ResponseState.none())
    val getSavedPostsWithUsersStateFlow = _getSavedPostsWithUsersStateFlow.asStateFlow()

    private val _likeUnlikePostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())
    val likeUnlikePostStateFlow = _likeUnlikePostStateFlow.asStateFlow()

    private val _saveUnSavePostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())
    val saveUnSavePostStateFlow = _saveUnSavePostStateFlow.asStateFlow()

    fun getSavedPosts(
        loggedInUserFirebaseId: String,
        loggedInUserBlockedList: List<String>,
        savedPosts: ArrayList<String>,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getSavedPostsWithUsersStateFlow.value = ResponseState.loading()
                if (savedPosts.isEmpty()) {
                    delay(500)
                    _getSavedPostsWithUsersStateFlow.value = ResponseState.success(emptyList())
                } else {
                    val postListWithUserDetailsResponse =
                        postDetailsWithUserDetailsUseCase(loggedInUserFirebaseId)
                    if (postListWithUserDetailsResponse.status == RequestStatusEnum.Success) {
                        val postList = postListWithUserDetailsResponse.data?.map { it.postDetail }
                        val userList = postListWithUserDetailsResponse.data?.map { it.userDetail }
                        if (postList != null && userList != null) {
                            deleteAllUsersExceptInListFromLocalUseCase(listOf(loggedInUserFirebaseId))
                            deleteAllPostsFromLocal()
                            val addPostToLocalResult =
                                addPostListToLocalUseCase(postList)
                            if (addPostToLocalResult.size == postList.size) {
                                addUserListToLocalUseCase(userList)
                                _getSavedPostsWithUsersStateFlow.value =
                                    getSavedPostDetailsWithUserFromLocalUseCase(
                                        savedPosts,
                                        loggedInUserFirebaseId,
                                        loggedInUserBlockedList
                                    )
                            } else {
                                _getSavedPostsWithUsersStateFlow.value =
                                    ResponseState.error(FirebaseErrorCodes.UNKNOWN_ERROR)
                                return@withContext
                            }
                        } else {
                            _getSavedPostsWithUsersStateFlow.value =
                                ResponseState.error(FirebaseErrorCodes.UNKNOWN_ERROR)
                            return@withContext
                        }
                    } else {
                        _getSavedPostsWithUsersStateFlow.value =
                            ResponseState.error(postListWithUserDetailsResponse.message ?: "")
                    }
                }
            }
        }
    }

    fun addLikeOnPost(postDetails: PostBean, loggedInUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _likeUnlikePostStateFlow.value = ResponseState.loading()
                val addLikeResponse = addLikeOnRemoteUseCase(
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    postFirebaseId = postDetails.postFirebaseId
                )
                if (addLikeResponse.status == RequestStatusEnum.Success) {
                    postDetails.likedBy.add(loggedInUserFirebaseId)
                    updatePostDetailsOnLocalUseCase(postDetails)
                    onUpdate()
                    _likeUnlikePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (addLikeResponse.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase(postDetails.postFirebaseId)
                    }
                    _likeUnlikePostStateFlow.value =
                        ResponseState.error(
                            addLikeResponse.message ?: "",
                            postDetails.postFirebaseId
                        )
                }
            }
        }
    }

    fun removeLikeForPost(
        postDetails: PostBean,
        loggedInUserFirebaseId: String,
        onUpdate: () -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _likeUnlikePostStateFlow.value = ResponseState.loading()
                val removeLikeResponse = removeLikeOfPostFromRemoteUseCase(
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    postFirebaseId = postDetails.postFirebaseId
                )
                if (removeLikeResponse.status == RequestStatusEnum.Success) {
                    postDetails.likedBy.remove(loggedInUserFirebaseId)
                    updatePostDetailsOnLocalUseCase(postDetails)
                    onUpdate()
                    _likeUnlikePostStateFlow.value = ResponseState.success(null)
                }
                if (removeLikeResponse.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                    deletePostFromLocalUseCase(postDetails.postFirebaseId)
                }
                _likeUnlikePostStateFlow.value =
                    ResponseState.error(
                        removeLikeResponse.message ?: "",
                        postDetails.postFirebaseId
                    )
            }
        }
    }

    fun savePost(loggedInUserBean: UserBean, postFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState =
                    savePostOnRemoteUseCase(loggedInUserBean.firebaseUserId, postFirebaseId)
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUserBean.savedPosts.add(postFirebaseId)
                    updateUserOnLocalUseCase(loggedInUserBean)
                    onUpdate()
                    _saveUnSavePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (responseState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase(postFirebaseId)
                    }
                    _saveUnSavePostStateFlow.value =
                        ResponseState.error(responseState.message ?: "", postFirebaseId)
                }
            }
        }
    }

    fun unSavePost(loggedInUserBean: UserBean, postFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState =
                    unSavePostFromRemoteUseCase(
                        loggedInUserBean.firebaseUserId,
                        postFirebaseId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUserBean.savedPosts.remove(postFirebaseId)
                    updateUserOnLocalUseCase(loggedInUserBean)
                    onUpdate()
                    _saveUnSavePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (responseState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase(postFirebaseId)
                    }
                    _saveUnSavePostStateFlow.value =
                        ResponseState.error(responseState.message ?: "", postFirebaseId)
                }
            }
        }
    }
}