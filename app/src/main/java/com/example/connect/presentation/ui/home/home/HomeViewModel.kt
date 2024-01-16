package com.example.connect.presentation.ui.home.home

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.PostWithUserDetails
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddLikeUseCase
import com.example.connect.domain.useCase.posts.AddPostListToLocalUseCase
import com.example.connect.domain.useCase.posts.DeleteAllPostFromLocal
import com.example.connect.domain.useCase.posts.DeletePostFromLocalUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsWithUserDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsWithUsersFromLocalUseCase
import com.example.connect.domain.useCase.posts.RemoveLikeUseCase
import com.example.connect.domain.useCase.posts.SavePostUseCase
import com.example.connect.domain.useCase.posts.UnSavePostUseCase
import com.example.connect.domain.useCase.posts.UpdatePostDetailsOnLocalUseCase
import com.example.connect.domain.useCase.story.GetStoryDetailsWithUserDetailsUseCase
import com.example.connect.domain.useCase.user.AddUserListToLocalUseCase
import com.example.connect.domain.useCase.user.DeleteAllUserFromLocalExceptInList
import com.example.connect.domain.useCase.user.UpdateUserDetailsOnLocal
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postDetailsWithUserDetailsUseCase: GetPostDetailsWithUserDetailsFromRemoteUseCase,
    private val storyDetailsWithUserDetailsUseCase: GetStoryDetailsWithUserDetailsUseCase,
    private val addLikeUseCase: AddLikeUseCase,
    private val removeLikeUseCase: RemoveLikeUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val unSavePostUseCase: UnSavePostUseCase,
    private val addPostListToLocalUseCase: AddPostListToLocalUseCase,
    private val addUserListToLocalUseCase: AddUserListToLocalUseCase,
    private val getPostDetailsWithUsersFromLocalUseCase: GetPostDetailsWithUsersFromLocalUseCase,
    private val updatePostDetailsOnLocalUseCase: UpdatePostDetailsOnLocalUseCase,
    private val deleteAllPostsFromLocal: DeleteAllPostFromLocal,
    private val deletePostFromLocalUseCase: DeletePostFromLocalUseCase,
    private val updateUserDetailsOnLocal: UpdateUserDetailsOnLocal,
    private val deleteAllUserFromLocalExceptInList: DeleteAllUserFromLocalExceptInList
) : BaseViewModel() {

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostWithUserDetails>>> =
        MutableStateFlow(ResponseState.none())

    val postDetailsStateFlow = _postDetailsStateFlow.asStateFlow()

    private val _storyDetailsStateFlow: MutableStateFlow<ResponseState<Pair<MutableMap<String, ArrayList<StoryBean>>, ArrayList<UsersBean>>>> =
        MutableStateFlow(ResponseState.none())


    private val _likeUnlikePostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())

    val likeUnlikePostStateFlow = _likeUnlikePostStateFlow.asStateFlow()

    private val _saveUnSavePostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())

    val saveUnSavePostStateFlow = _saveUnSavePostStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")

    private var isPostListFromRemoteFetched: Boolean = false

    val postListWithUsersState = mutableStateListOf<PostWithUserDetails>()
    val storyDetailsStateFlow: StateFlow<ResponseState<Pair<MutableMap<String, ArrayList<StoryBean>>, ArrayList<UsersBean>>>> get() = _storyDetailsStateFlow


    fun getStoryDetailsWithUserDetails(loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _storyDetailsStateFlow.value = ResponseState.loading()
                _storyDetailsStateFlow.value =
                    storyDetailsWithUserDetailsUseCase.invoke(loggedInUserFirebaseId)
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
                            deleteAllUserFromLocalExceptInList.invoke(listOf(loggedInUserFirebaseId))
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
                val addLikeResponse = addLikeUseCase.invoke(
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
                val removeLikeResponse = removeLikeUseCase.invoke(
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
                    savePostUseCase.invoke(loggedInUsersBean.firebaseUserId, postFirebaseId)
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.add(postFirebaseId)
                    updateUserDetailsOnLocal.invoke(loggedInUsersBean)
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
                    unSavePostUseCase.invoke(loggedInUsersBean.firebaseUserId, postFirebaseId)
                if (responseState.status == RequestStatusEnum.Success) {
                    loggedInUsersBean.savedPosts.remove(postFirebaseId)
                    updateUserDetailsOnLocal.invoke(loggedInUsersBean)
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