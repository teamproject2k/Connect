package com.example.connect.presentation.ui.home.home

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.PostWithUserDetails
import com.example.connect.domain.models.StoriesWithUser
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddLikeOnRemoteUseCase
import com.example.connect.domain.useCase.posts.AddPostListToLocalUseCase
import com.example.connect.domain.useCase.posts.DeleteAllPostFromLocalUseCase
import com.example.connect.domain.useCase.posts.DeletePostFromLocalUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsWithUserDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsWithUsersFromLocalUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeOfPostFromRemoteUseCase
import com.example.connect.domain.useCase.posts.SavePostOnRemoteUseCase
import com.example.connect.domain.useCase.posts.UnSavePostFromRemoteUseCase
import com.example.connect.domain.useCase.posts.UpdatePostDetailsOnLocalUseCase
import com.example.connect.domain.useCase.story.AddAllStoriesToLocalUseCase
import com.example.connect.domain.useCase.story.DeleteAllStoriesFromLocalUseCase
import com.example.connect.domain.useCase.story.GetAllStoriesWithUserFormRemoteUseCase
import com.example.connect.domain.useCase.story.GetAllStoriesWithUserFromLocalUseCase
import com.example.connect.domain.useCase.user.AddUserListToLocalUseCase
import com.example.connect.domain.useCase.user.DeleteAllUsersExceptInListFromLocalUseCase
import com.example.connect.domain.useCase.user.UpdateUserOnLocalUseCase
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postDetailsWithUserDetailsUseCase: GetPostDetailsWithUserDetailsFromRemoteUseCase,
    private val storyDetailsWithUserDetailsUseCase: GetAllStoriesWithUserFormRemoteUseCase,
    private val addLikeOnRemoteUseCase: AddLikeOnRemoteUseCase,
    private val removeLikeOfPostFromRemoteUseCase: RemoveLikeOfPostFromRemoteUseCase,
    private val savePostOnRemoteUseCase: SavePostOnRemoteUseCase,
    private val unSavePostFromRemoteUseCase: UnSavePostFromRemoteUseCase,
    private val addPostListToLocalUseCase: AddPostListToLocalUseCase,
    private val addUserListToLocalUseCase: AddUserListToLocalUseCase,
    private val getPostDetailsWithUsersFromLocalUseCase: GetPostDetailsWithUsersFromLocalUseCase,
    private val updatePostDetailsOnLocalUseCase: UpdatePostDetailsOnLocalUseCase,
    private val deleteAllPostsFromLocal: DeleteAllPostFromLocalUseCase,
    private val deletePostFromLocalUseCase: DeletePostFromLocalUseCase,
    private val updateUserOnLocalUseCase: UpdateUserOnLocalUseCase,
    private val deleteAllUsersExceptInListFromLocalUseCase: DeleteAllUsersExceptInListFromLocalUseCase,
    private val getAllStoriesWithUserFromLocalUseCase: GetAllStoriesWithUserFromLocalUseCase,
    private val addAllStoriesToLocalUseCase: AddAllStoriesToLocalUseCase,
    private val deleteAllStoriesFromLocalUseCase: DeleteAllStoriesFromLocalUseCase
) : BaseViewModel() {

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostWithUserDetails>>> =
        MutableStateFlow(ResponseState.none())

    val postDetailsStateFlow = _postDetailsStateFlow.asStateFlow()

    private val _storyDetailsStateFlow: MutableStateFlow<ResponseState<ArrayList<StoriesWithUser>>> =
        MutableStateFlow(ResponseState.none())

    val storyDetailsStateFlow = _storyDetailsStateFlow.asStateFlow()


    private val _likeUnlikePostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())

    val likeUnlikePostStateFlow = _likeUnlikePostStateFlow.asStateFlow()

    private val _saveUnSavePostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())

    val saveUnSavePostStateFlow = _saveUnSavePostStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")

    private var isPostListFromRemoteFetched: Boolean = false

    private var isStoryListFetchedFromRemote: Boolean = false

    val postListWithUsersState = mutableStateListOf<PostWithUserDetails>()


    fun getStoryDetailsWithUserDetails(
        loggedInUserFirebaseId: String,
        isNetworkAvailable: Boolean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _storyDetailsStateFlow.value = ResponseState.loading()
                if (!isStoryListFetchedFromRemote && isNetworkAvailable) {
                    val response = storyDetailsWithUserDetailsUseCase.invoke(loggedInUserFirebaseId)
                    if (response.status == RequestStatusEnum.Success) {
                        val storyList = response.data?.flatMap { it.storiesList } ?: emptyList()
                        val usersList = response.data?.map { it.usersBean } ?: emptyList()
                        deleteAllStoriesFromLocalUseCase.invoke()
                        if (addAllStoriesToLocalUseCase.invoke(storyList).size == storyList.size) {
                            addUserListToLocalUseCase.invoke(usersList)
                            isStoryListFetchedFromRemote = true
                            _storyDetailsStateFlow.value =
                                ResponseState.success(
                                    getAllStoriesWithUserFromLocalUseCase.invoke(
                                        loggedInUserFirebaseId
                                    )
                                )
                        } else {
                            _storyDetailsStateFlow.value =
                                ResponseState.error(FirebaseErrorCodes.UNKNOWN_ERROR)
                        }
                    } else {
                        _storyDetailsStateFlow.value = ResponseState.error(response.message ?: "")
                    }
                } else {
                    _storyDetailsStateFlow.value =
                        ResponseState.success(
                            getAllStoriesWithUserFromLocalUseCase.invoke(
                                loggedInUserFirebaseId
                            )
                        )
                }
            }
        }
    }

    fun getPostDetailsWithUserDetails(loggedInUserFirebaseId: String, isNetworkAvailable: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _postDetailsStateFlow.value = ResponseState.loading()
                if (!isPostListFromRemoteFetched && isNetworkAvailable) {
                    val postListWithUserDetailsResponse =
                        postDetailsWithUserDetailsUseCase.invoke(loggedInUserFirebaseId)
                    if (postListWithUserDetailsResponse.status == RequestStatusEnum.Success) {
                        isPostListFromRemoteFetched = true
                        val postList = postListWithUserDetailsResponse.data?.map { it.postDetail }
                        val userList = postListWithUserDetailsResponse.data?.map { it.userDetail }
                        if (postList != null && userList != null) {
                            deleteAllUsersExceptInListFromLocalUseCase.invoke(listOf(loggedInUserFirebaseId))
                            deleteAllPostsFromLocal.invoke()
                            val addPostToLocalResult =
                                addPostListToLocalUseCase.invoke(postList)
                            if (addPostToLocalResult.size == postList.size) {
                                addUserListToLocalUseCase.invoke(userList)
                                _postDetailsStateFlow.value =
                                    getPostDetailsWithUsersFromLocalUseCase.invoke()
                            } else {
                                _postDetailsStateFlow.value =
                                    ResponseState.error(FirebaseErrorCodes.UNKNOWN_ERROR)
                            }
                        } else {
                            _postDetailsStateFlow.value =
                                ResponseState.error(FirebaseErrorCodes.UNKNOWN_ERROR)
                        }
                    } else {
                        _postDetailsStateFlow.value = postListWithUserDetailsResponse
                    }
                } else {
                    _postDetailsStateFlow.value =
                        getPostDetailsWithUsersFromLocalUseCase.invoke()
                }
            }
        }
    }

    fun addLikeOnPost(postDetails: PostBean, loggedInUserFirebaseId: String, onUpdate: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _likeUnlikePostStateFlow.value = ResponseState.loading()
                val addLikeResponse = addLikeOnRemoteUseCase.invoke(
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    postFirebaseId = postDetails.postFirebaseId
                )
                if (addLikeResponse.status == RequestStatusEnum.Success) {
                    postDetails.likedBy.add(loggedInUserFirebaseId)
                    updatePostDetailsOnLocalUseCase.invoke(postDetails)
                    onUpdate()
                    _likeUnlikePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (addLikeResponse.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase.invoke(postDetails.postFirebaseId)
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
                val removeLikeResponse = removeLikeOfPostFromRemoteUseCase.invoke(
                    loggedInUserFirebaseId = loggedInUserFirebaseId,
                    postFirebaseId = postDetails.postFirebaseId
                )
                if (removeLikeResponse.status == RequestStatusEnum.Success) {
                    postDetails.likedBy.remove(loggedInUserFirebaseId)
                    updatePostDetailsOnLocalUseCase.invoke(postDetails)
                    onUpdate()
                    _likeUnlikePostStateFlow.value = ResponseState.success(null)
                }
                if (removeLikeResponse.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                    deletePostFromLocalUseCase.invoke(postDetails.postFirebaseId)
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
                    savePostOnRemoteUseCase.invoke(loggedInUsersBean.firebaseUserId, postFirebaseId)
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.add(postFirebaseId)
                    updateUserOnLocalUseCase.invoke(loggedInUsersBean)
                    onUpdate()
                    _saveUnSavePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (responseState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase.invoke(postFirebaseId)
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
                    unSavePostFromRemoteUseCase.invoke(
                        loggedInUsersBean.firebaseUserId,
                        postFirebaseId
                    )
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.remove(postFirebaseId)
                    updateUserOnLocalUseCase.invoke(loggedInUsersBean)
                    onUpdate()
                    _saveUnSavePostStateFlow.value = ResponseState.success(null)
                } else {
                    if (responseState.message == FirebaseErrorCodes.POST_NOT_FOUND) {
                        deletePostFromLocalUseCase.invoke(postFirebaseId)
                    }
                    _saveUnSavePostStateFlow.value =
                        ResponseState.error(responseState.message ?: "", postFirebaseId)
                }
            }
        }
    }
}