package com.example.connect.presentation.ui.home.saved_posts

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.PostWithUserDetails
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddLikeUseCase
import com.example.connect.domain.useCase.posts.AddPostListToLocalUseCase
import com.example.connect.domain.useCase.posts.GetSavedPostDetailsWithUserFromLocal
import com.example.connect.domain.useCase.posts.GetSavedPostsWithUsersFromRemoteUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeUseCase
import com.example.connect.domain.useCase.posts.SavePostUseCase
import com.example.connect.domain.useCase.posts.UnSavePostUseCase
import com.example.connect.domain.useCase.posts.UpdatePostDetailsOnLocalUseCase
import com.example.connect.domain.useCase.user.AddUserListToLocalUseCase
import com.example.connect.domain.useCase.user.UpdateUserDetailsOnLocal
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SavedPostsViewModel @Inject constructor(
    private val getSavedPostsWithUsersFromRemoteUseCase: GetSavedPostsWithUsersFromRemoteUseCase,
    private val addLikeUseCase: AddLikeUseCase,
    private val removeLikeUseCase: RemoveLikeUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val unSavePostUseCase: UnSavePostUseCase,
    private val updateUserDetailsOnLocal: UpdateUserDetailsOnLocal,
    private val getSavedPostDetailsWithUserFromLocal: GetSavedPostDetailsWithUserFromLocal,
    private val addPostListToLocalUseCase: AddPostListToLocalUseCase,
    private val addUserListToLocalUseCase: AddUserListToLocalUseCase,
    private val updatePostDetailsOnLocalUseCase: UpdatePostDetailsOnLocalUseCase
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

    var isSavedPostListFetched = false

    var postListWithUserDetailsListState = mutableStateListOf<PostWithUserDetails>()

    fun getSavedPosts(
        loggedInUserFirebaseId: String,
        savedPosts: ArrayList<String>,
        whetherGetDataFromRemote: Boolean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getSavedPostsWithUsersStateFlow.value = ResponseState.loading()
                if (whetherGetDataFromRemote) {
                    if (savedPosts.isEmpty()) {
                        _getSavedPostsWithUsersStateFlow.value = ResponseState.success(emptyList())
                    } else {
                        val savedPostResponse = getSavedPostsWithUsersFromRemoteUseCase.invoke(
                            loggedInUserFirebaseId,
                            savedPosts
                        )
                        if (savedPostResponse.status == RequestStatusEnum.Success) {
                            val postList = savedPostResponse.data?.map { it.postDetail }
                            val userList = savedPostResponse.data?.map { it.userDetail }
                            if (postList != null && userList != null) {
                                val addPostToLocalResult =
                                    addPostListToLocalUseCase.invoke(postList)
                                if (addPostToLocalResult.size == postList.size) {
                                    addUserListToLocalUseCase.invoke(userList)
                                }
                            }
                        }
                        _getSavedPostsWithUsersStateFlow.value = savedPostResponse
                    }
                } else {
                    _getSavedPostsWithUsersStateFlow.value =
                        getSavedPostDetailsWithUserFromLocal.invoke(savedPosts)
                }
            }
        }
    }

    fun addLike(postDetails: PostBean, loggedInUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _likeUnlikePostStateFlow.value = ResponseState.loading()
                val responseState = addLikeUseCase.invoke(
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    postFirebaseId = postDetails.postFirebaseId
                )
                if (responseState.status == RequestStatusEnum.Success) {
                    postDetails.likedBy.add(loggedInUserFirebaseId)
                    updatePostDetailsOnLocalUseCase.invoke(postDetails)
                    onUpdate()
                    _likeUnlikePostStateFlow.value = ResponseState.success(null)
                } else {
                    _likeUnlikePostStateFlow.value =
                        ResponseState.error(responseState.message ?: "", postDetails.postFirebaseId)
                }
            }
        }
    }

    fun removeLike(postDetails: PostBean, loggedInUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _likeUnlikePostStateFlow.value = ResponseState.loading()
                val responseState = removeLikeUseCase.invoke(
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    postFirebaseId = postDetails.postFirebaseId
                )
                if (responseState.status == RequestStatusEnum.Success) {
                    postDetails.likedBy.remove(loggedInUserFirebaseId)
                    updatePostDetailsOnLocalUseCase.invoke(postDetails)
                    onUpdate()
                    _likeUnlikePostStateFlow.value = ResponseState.success(null)
                }
                _likeUnlikePostStateFlow.value =
                    ResponseState.error(responseState.message ?: "", postDetails.postFirebaseId)
            }
        }
    }

    fun savePost(loggedInUsersBean: UsersBean, postId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState = savePostUseCase.invoke(loggedInUsersBean.firebaseUserId, postId)
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.add(postId)
                    updateUserDetailsOnLocal.invoke(loggedInUsersBean)
                    onUpdate()
                    _saveUnSavePostStateFlow.value = ResponseState.success(null)
                } else {
                    _saveUnSavePostStateFlow.value =
                        ResponseState.error(responseState.message ?: "", postId)
                }
            }
        }
    }

    fun unSavePost(loggedInUsersBean: UsersBean, postId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _saveUnSavePostStateFlow.value = ResponseState.loading()
                val responseState =
                    unSavePostUseCase.invoke(loggedInUsersBean.firebaseUserId, postId)
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.remove(postId)
                    updateUserDetailsOnLocal.invoke(loggedInUsersBean)
                    onUpdate()
                    _saveUnSavePostStateFlow.value = ResponseState.success(null)
                } else {
                    _saveUnSavePostStateFlow.value =
                        ResponseState.error(responseState.message ?: "", postId)
                }
            }
        }
    }
}