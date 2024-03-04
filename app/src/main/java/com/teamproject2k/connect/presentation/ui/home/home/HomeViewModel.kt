package com.teamproject2k.connect.presentation.ui.home.home

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.models.PostWithUserDetailsBean
import com.teamproject2k.connect.domain.models.StoriesWithUserBean
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.posts.AddLikeOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.AddPostListToLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.DeleteAllPostFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.DeletePostFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.GetPostDetailsWithUserDetailsFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.GetPostDetailsWithUsersFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.RemoveLikeOfPostFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.SavePostOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.UnSavePostFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.posts.UpdatePostDetailsOnLocalUseCase
import com.teamproject2k.connect.domain.use_case.story.AddAllStoriesToLocalUseCase
import com.teamproject2k.connect.domain.use_case.story.DeleteAllStoriesFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.story.GetAllStoriesWithUserFormRemoteUseCase
import com.teamproject2k.connect.domain.use_case.story.GetAllStoriesWithUserFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.AddUserListToLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.DeleteAllUsersExceptInListFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateUserOnLocalUseCase
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.base.BaseViewModel
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

    private var isPostListFromRemoteFetched: Boolean = false
    private var isStoryListFetchedFromRemote: Boolean = false

    val snackBarMessageState = mutableStateOf("")
    val postListWithUsersState = mutableStateListOf<PostWithUserDetailsBean>()

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostWithUserDetailsBean>>> =
        MutableStateFlow(ResponseState.none())
    val postDetailsStateFlow = _postDetailsStateFlow.asStateFlow()

    private val _storyDetailsStateFlow: MutableStateFlow<ResponseState<ArrayList<StoriesWithUserBean>>> =
        MutableStateFlow(ResponseState.none())
    val storyDetailsStateFlow = _storyDetailsStateFlow.asStateFlow()

    private val _likeUnlikePostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())
    val likeUnlikePostStateFlow = _likeUnlikePostStateFlow.asStateFlow()

    private val _saveUnSavePostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())
    val saveUnSavePostStateFlow = _saveUnSavePostStateFlow.asStateFlow()

    /**
     * Retrieves story details along with user details, considering the given parameters.
     * This function fetches data both remotely and locally and updates the state flow accordingly.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param isForceRefresh Boolean indicating whether to force refresh the data from the remote source.
     */
    fun getStoryDetailsWithUserDetails(
        loggedInUserFirebaseId: String,
        isForceRefresh: Boolean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _storyDetailsStateFlow.value = ResponseState.loading()
                if (isForceRefresh || !isStoryListFetchedFromRemote) {
                    val response = storyDetailsWithUserDetailsUseCase(loggedInUserFirebaseId)
                    if (response.status == RequestStatusEnum.Success) {
                        val storyList = response.data?.flatMap { it.storiesList } ?: emptyList()
                        val usersList = response.data?.map { it.userBean } ?: emptyList()
                        deleteAllStoriesFromLocalUseCase()
                        if (addAllStoriesToLocalUseCase(storyList).size == storyList.size) {
                            addUserListToLocalUseCase(usersList)
                            isStoryListFetchedFromRemote = true
                            _storyDetailsStateFlow.value =
                                ResponseState.success(
                                    getAllStoriesWithUserFromLocalUseCase(
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
                            getAllStoriesWithUserFromLocalUseCase(
                                loggedInUserFirebaseId
                            )
                        )
                }
            }
        }
    }

    /**
     * Retrieves post details along with user details, considering the given parameters.
     * This function fetches data both remotely and locally and updates the state flow accordingly.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param loggedInUserBlockedList List of Firebase IDs representing users blocked by the logged-in user.
     * @param isForceRefresh Boolean indicating whether to force refresh the data from the remote source.
     */
    fun getPostDetailsWithUserDetails(
        loggedInUserFirebaseId: String,
        loggedInUserBlockedList: List<String>,
        isForceRefresh: Boolean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _postDetailsStateFlow.value = ResponseState.loading()
                if (!isPostListFromRemoteFetched || isForceRefresh) {
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
                                isPostListFromRemoteFetched = true
                                _postDetailsStateFlow.value =
                                    getPostDetailsWithUsersFromLocalUseCase(
                                        loggedInUserFirebaseId,
                                        loggedInUserBlockedList
                                    )
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
                        getPostDetailsWithUsersFromLocalUseCase(
                            loggedInUserFirebaseId,
                            loggedInUserBlockedList
                        )
                }
            }
        }
    }

    /**
     * Adds a like from the logged-in user for a specific post.
     * This function adds the like both remotely and locally.
     *
     * @param postDetails The details of the post to which the like is to be added.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param onUpdate Callback function to be executed after the like addition is completed.
     */
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

    /**
     * Removes the like of the logged-in user for a specific post.
     * This function removes the like both remotely and locally.
     *
     * @param postDetails The details of the post from which the like is to be removed.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param onUpdate Callback function to be executed after the like removal is completed.
     */
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

    /**
     * Saves a post to the saved posts list of the logged-in user.
     * This function performs the saving both remotely and locally.
     *
     * @param loggedInUserBean The user currently logged in.
     * @param postFirebaseId The Firebase ID of the post to be saved.
     * @param onUpdate Callback function to be executed after the saving is completed.
     */
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

    /**
    * Removes a post from the saved posts list of the logged-in user.
    * This function performs the removal both remotely and locally.
    *
    * @param loggedInUserBean The user currently logged in.
    * @param postFirebaseId The Firebase ID of the post to be removed.
    * @param onUpdate Callback function to be executed after the removal is completed.
    */
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