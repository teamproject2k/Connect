package com.example.connect.presentation.ui.home.saved_posts

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.PostWithUserDetails
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddLikeOnRemoteUseCase
import com.example.connect.domain.useCase.posts.AddPostListToLocalUseCase
import com.example.connect.domain.useCase.posts.DeleteAllPostFromLocalUseCase
import com.example.connect.domain.useCase.posts.DeletePostFromLocalUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsWithUserDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.posts.GetSavedPostDetailsWithUserFromLocalUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeOfPostFromRemoteUseCase
import com.example.connect.domain.useCase.posts.SavePostOnRemoteUseCase
import com.example.connect.domain.useCase.posts.UnSavePostFromRemoteUseCase
import com.example.connect.domain.useCase.posts.UpdatePostDetailsOnLocalUseCase
import com.example.connect.domain.useCase.user.AddUserListToLocalUseCase
import com.example.connect.domain.useCase.user.DeleteAllUsersExceptInListFromLocalUseCase
import com.example.connect.domain.useCase.user.UpdateUserOnLocalUseCase
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseViewModel
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

    private val _getSavedPostsWithUsersStateFlow: MutableStateFlow<ResponseState<List<PostWithUserDetails>>> =
        MutableStateFlow(ResponseState.none())

    val getSavedPostsWithUsersStateFlow = _getSavedPostsWithUsersStateFlow.asStateFlow()

    private val _likeUnlikePostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())

    val likeUnlikePostStateFlow = _likeUnlikePostStateFlow.asStateFlow()

    private val _saveUnSavePostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())

    val saveUnSavePostStateFlow = _saveUnSavePostStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")

    var postListWithUserDetailsListState = mutableStateListOf<PostWithUserDetails>()

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

    fun savePost(loggedInUsersBean: UsersBean, postFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState =
                    savePostOnRemoteUseCase(loggedInUsersBean.firebaseUserId, postFirebaseId)
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.add(postFirebaseId)
                    updateUserOnLocalUseCase(loggedInUsersBean)
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

    fun unSavePost(loggedInUsersBean: UsersBean, postFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState =
                    unSavePostFromRemoteUseCase(
                        loggedInUsersBean.firebaseUserId,
                        postFirebaseId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.remove(postFirebaseId)
                    updateUserOnLocalUseCase(loggedInUsersBean)
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