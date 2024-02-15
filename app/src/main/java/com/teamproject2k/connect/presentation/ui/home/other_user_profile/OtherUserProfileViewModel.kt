package com.teamproject2k.connect.presentation.ui.home.other_user_profile

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.use_case.fcm.SendFCMUseCase
import com.teamproject2k.connect.domain.use_case.posts.DeleteAllPostOfUserFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.DeleteOnlyFriendsOnlyPostOfUserFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.posts.GetPostDetailsFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.AcceptFriendRequestOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.AddUserToLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.BlockUserOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.LiveUserObserverFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.RemoveFriendRequestOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.SendFriendRequestOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.UnBlockUserOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.UnfriendAndBlockUserOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.UnfriendUserOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateUserOnLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.UpdateUsersStatusOnLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.WithdrawFriendRequestOnRemoteUseCase
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.services.fcm.NotificationTypesEnum
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import com.teamproject2k.connect.presentation.utils.NotificationsConstantHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OtherUserProfileViewModel @Inject constructor(
    private val getPostDetailsFromRemoteUseCase: GetPostDetailsFromRemoteUseCase,
    private val getUserDetailsFromIdsUseCase: GetUserDetailsFromIdsFromRemoteUseCase,
    private val sendFriendRequestOnRemoteUseCase: SendFriendRequestOnRemoteUseCase,
    private val withdrawFriendRequestOnRemoteUseCase: WithdrawFriendRequestOnRemoteUseCase,
    private val acceptFriendRequestOnRemoteUseCase: AcceptFriendRequestOnRemoteUseCase,
    private val removeFriendRequestOnRemoteUseCase: RemoveFriendRequestOnRemoteUseCase,
    private val blockUserOnRemoteUseCase: BlockUserOnRemoteUseCase,
    private val unBlockUserOnRemoteUseCase: UnBlockUserOnRemoteUseCase,
    private val updateUsersStatusOnLocalUseCase: UpdateUsersStatusOnLocalUseCase,
    private val unfriendAndBlockUserOnRemoteUseCase: UnfriendAndBlockUserOnRemoteUseCase,
    private val unfriendUserOnRemoteUseCase: UnfriendUserOnRemoteUseCase,
    private val addUserToLocalUseCase: AddUserToLocalUseCase,
    private val liveUserObserverFromRemoteUseCase: LiveUserObserverFromRemoteUseCase,
    private val sendFCMUseCase: SendFCMUseCase,
    private val deleteOnlyFriendsOnlyPostOfUserFromLocalUseCase: DeleteOnlyFriendsOnlyPostOfUserFromLocalUseCase,
    private val deleteAllPostOfUserFromLocalUseCase: DeleteAllPostOfUserFromLocalUseCase,
    private val updateUserOnLocalUseCase: UpdateUserOnLocalUseCase
) : BaseViewModel() {

    private lateinit var liveObserveOtherUserListener: ListenerRegistration
    private lateinit var liveObserveLoggedInUserListener: ListenerRegistration
    lateinit var otherUserState: MutableState<UsersBean>
    lateinit var loggedInUserState: MutableState<UsersBean>

    var isDataInitialized = false

    val snackBarMessageState = mutableStateOf("")
    val statusWithCurrentUserState: MutableState<String> = mutableStateOf("")

    private val _friendsDetailsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val friendsDetailsStateFlow = _friendsDetailsStateFlow.asStateFlow()

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostBean>>> =
        MutableStateFlow(ResponseState.none())
    val postDetailsStateFlow = _postDetailsStateFlow.asStateFlow()

    private val _sendFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val sendFriendRequestStateFlow = _sendFriendRequestStateFlow.asStateFlow()

    private val _acceptFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val acceptFriendRequestStateFlow = _acceptFriendRequestStateFlow.asStateFlow()

    private val _withdrawFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val withdrawFriendRequestStateFlow = _withdrawFriendRequestStateFlow.asStateFlow()

    private val _removeFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val removeFriendRequestStateFlow = _removeFriendRequestStateFlow.asStateFlow()

    private val _unBlockUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val unBlockUserStateFlow = _unBlockUserStateFlow.asStateFlow()

    private val _blockUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val blockUserStateFlow = _blockUserStateFlow.asStateFlow()

    private val _unfriendUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val unfriendUserStateFlow = _unfriendUserStateFlow.asStateFlow()

    private val _unfriendAndBlockUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val unfriendAndBlockUserStateFlow = _unfriendAndBlockUserStateFlow.asStateFlow()

    private val _liveObserveOtherUserDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean>> =
        MutableStateFlow(ResponseState.none())
    val liveObserveRequiredUserDetailsStateFlow =
        _liveObserveOtherUserDetailsStateFlow.asStateFlow()

    private val _liveObserveLoggedInUserDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean>> =
        MutableStateFlow(ResponseState.none())
    val liveObserveLoggedInUserDetailsStateFlow =
        _liveObserveLoggedInUserDetailsStateFlow.asStateFlow()

    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<Pair<UsersBean, UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val userDetailsStateFlow = _userDetailsStateFlow.asStateFlow()

    /**
     * Initializes the data for the profile screen.
     *
     * @param currentUser The current user.
     * @param requestedUser The requested user.
     */
    fun initializeData(currentUser: UsersBean, requestedUser: UsersBean) {
        // Set the current user state.
        loggedInUserState = mutableStateOf(currentUser)

        // Set the requested user state.
        otherUserState = mutableStateOf(requestedUser)

        // Get the status with the current user.
        statusWithCurrentUserState.value =
            FunctionHelper.getStatusWithCurrentUser(loggedInUserState.value, otherUserState.value)

        // Set the data initialization flag to true.
        isDataInitialized = true
    }

    /**
     * Gets the details of a post.
     *
     */
    fun getPostDetails() {
        // Launch a coroutine in the viewModelScope
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations
            withContext(Dispatchers.IO) {
                // Set the post details state to loading
                _postDetailsStateFlow.value = ResponseState.loading()
                // Get the post details from the remote use case
                _postDetailsStateFlow.value = getPostDetailsFromRemoteUseCase(
                    otherUserState.value.firebaseUserId,
                    loggedInUserState.value.firebaseUserId
                )
            }
        }
    }

    /**
     * Gets the friend list from the given friend IDs.
     *
     */
    fun getFriendListFromIds() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Perform the network request in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Check if the friendIdList is empty.
                _friendsDetailsStateFlow.value = ResponseState.loading()
                if (otherUserState.value.friendList.isEmpty()) {
                    delay(500)
                    // If the friendIdList is empty, emit an empty list as the response.
                    _friendsDetailsStateFlow.value = ResponseState.success(emptyList())
                } else {
                    // If the friendIdList is not empty, emit a loading state.
                    // Get the user details from the userIds.
                    _friendsDetailsStateFlow.value =
                        getUserDetailsFromIdsUseCase(otherUserState.value.friendList)
                }
            }
        }
    }

    /**
     * Sends a friend request to the required user.
     */
    fun sendFriendRequest(context: Context) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the sendFriendRequestStateFlow to loading.
                _sendFriendRequestStateFlow.value = ResponseState.loading()
                // Call the sendFriendRequestOnRemoteUseCase.
                val responseState =
                    sendFriendRequestOnRemoteUseCase(
                        // Get the current user's firebaseUserId from the currentUserState.
                        loggedInUserState.value.firebaseUserId,
                        // Get the required user's firebaseUserId from the requiredUserState.
                        otherUserState.value.firebaseUserId
                    )
                // Check if the responseState is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Add the required user's firebaseUserId to the current user's requestedFriendRequestList.
                    if (!loggedInUserState.value.requestedFriendRequestList.contains(otherUserState.value.firebaseUserId)) {
                        loggedInUserState.value.requestedFriendRequestList.add(otherUserState.value.firebaseUserId)
                    }
                    // Add the current user's firebaseUserId to the required user's receivedFriendRequestList.
                    if (!otherUserState.value.receivedFriendRequestList.contains(loggedInUserState.value.firebaseUserId)) {
                        otherUserState.value.receivedFriendRequestList.add(loggedInUserState.value.firebaseUserId)
                    }
                    // Call the updateOtherUserStatusOnDbUseCase.
                    updateUsersStatusOnLocalUseCase(
                        // Get the current user's firebaseUserId from the currentUserState.
                        loggedInUserState.value.firebaseUserId,
                        // Get the current user's otherUsersStatus from the currentUserState.
                        loggedInUserState.value.toUserLocalEntity().otherUsersStatus
                    )
                    val data = hashMapOf(
                        Pair(
                            NotificationsConstantHelper.MESSAGE,
                            loggedInUserState.value.name
                        ),
                        Pair(
                            NotificationTypesEnum::name.name,
                            NotificationTypesEnum.FriendRequestReceived.name
                        )
                    )
                    sendFCMUseCase(
                        FunctionHelper.getAccessToken(context),
                        data,
                        otherUserState.value.fcmToken
                    )
                    // Set the sendFriendRequestStateFlow to the responseState.
                    _sendFriendRequestStateFlow.value = responseState
                } else {
                    // Set the sendFriendRequestStateFlow to the responseState.
                    _sendFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    /**
     * Withdraws a friend request.
     */
    fun withdrawFriendRequest() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the withdrawFriendRequestStateFlow to loading.
                _withdrawFriendRequestStateFlow.value = ResponseState.loading()

                // Call the withdrawFriendRequestOnRemoteUseCase.
                val responseState =
                    withdrawFriendRequestOnRemoteUseCase(
                        // Get the current user's firebase user ID.
                        loggedInUserState.value.firebaseUserId,
                        // Get the required user's firebase user ID.
                        otherUserState.value.firebaseUserId
                    )

                // Check if the responseState is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the required user's firebase user ID from the current user's requestedFriendRequestList.
                    loggedInUserState.value.requestedFriendRequestList.remove(otherUserState.value.firebaseUserId)

                    // Remove the current user's firebase user ID from the required user's receivedFriendRequestList.
                    otherUserState.value.receivedFriendRequestList.remove(loggedInUserState.value.firebaseUserId)

                    // Call the updateOtherUserStatusOnDbUseCase.
                    updateUsersStatusOnLocalUseCase(
                        // Get the current user's firebase user ID.
                        loggedInUserState.value.firebaseUserId,
                        // Get the current user's UserDbEntity.
                        loggedInUserState.value.toUserLocalEntity().otherUsersStatus
                    )

                    // Set the withdrawFriendRequestStateFlow to the responseState.
                    _withdrawFriendRequestStateFlow.value = responseState
                } else {
                    // Set the withdrawFriendRequestStateFlow to the responseState.
                    _withdrawFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    /**
     * Accepts a friend request.
     */
    fun acceptFriendRequest(context: Context) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations.
            withContext(Dispatchers.IO) {
                // Set the acceptFriendRequestStateFlow to loading state.
                _acceptFriendRequestStateFlow.value = ResponseState.loading()

                // Call the acceptFriendRequestOnRemoteUseCase to accept the friend request.
                val responseState =
                    acceptFriendRequestOnRemoteUseCase(
                        loggedInUserState.value.firebaseUserId,
                        otherUserState.value.firebaseUserId
                    )

                // Check if the responseState is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the required user's firebaseUserId from the currentUserState's receivedFriendRequestList.
                    loggedInUserState.value.receivedFriendRequestList.remove(otherUserState.value.firebaseUserId)

                    // Add the required user's firebaseUserId to the currentUserState's friendList.
                    if (!loggedInUserState.value.friendList.contains(otherUserState.value.firebaseUserId)) {
                        loggedInUserState.value.friendList.add(otherUserState.value.firebaseUserId)
                    }

                    // Remove the currentUserState's firebaseUserId from the requiredUserState's requestedFriendRequestList.
                    otherUserState.value.requestedFriendRequestList.remove(loggedInUserState.value.firebaseUserId)

                    // Add the currentUserState's firebaseUserId to the requiredUserState's friendList.
                    if (!otherUserState.value.friendList.contains(loggedInUserState.value.firebaseUserId)) {
                        otherUserState.value.friendList.add(loggedInUserState.value.firebaseUserId)
                    }

                    // Call the updateOtherUserStatusOnDbUseCase to update the other user's status on the database.
                    updateUsersStatusOnLocalUseCase(
                        loggedInUserState.value.firebaseUserId,
                        loggedInUserState.value.toUserLocalEntity().otherUsersStatus
                    )
                    val data = hashMapOf(
                        Pair(
                            NotificationsConstantHelper.MESSAGE,
                            loggedInUserState.value.name
                        ),
                        Pair(
                            NotificationTypesEnum::name.name,
                            NotificationTypesEnum.FriendRequestAccepted.name
                        )
                    )
                    sendFCMUseCase(
                        FunctionHelper.getAccessToken(context),
                        data,
                        otherUserState.value.fcmToken
                    )
                    // Set the acceptFriendRequestStateFlow to the responseState.
                    _acceptFriendRequestStateFlow.value = responseState
                } else {
                    // Set the acceptFriendRequestStateFlow to the responseState.
                    _acceptFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    /**
     * Removes a friend request.
     */
    fun removeFriendRequest() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the removeFriendRequestStateFlow to loading.
                _removeFriendRequestStateFlow.value = ResponseState.loading()

                // Call the removeFriendRequestOnRemoteUseCase.
                val responseState =
                    removeFriendRequestOnRemoteUseCase(
                        loggedInUserState.value.firebaseUserId,
                        otherUserState.value.firebaseUserId
                    )

                // Check if the responseState is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the requiredUserState.value.firebaseUserId from the currentUserState.value.receivedFriendRequestList.
                    loggedInUserState.value.receivedFriendRequestList.remove(otherUserState.value.firebaseUserId)

                    // Remove the currentUserState.value.firebaseUserId from the requiredUserState.value.requestedFriendRequestList.
                    otherUserState.value.requestedFriendRequestList.remove(loggedInUserState.value.firebaseUserId)

                    // Call the updateOtherUserStatusOnDbUseCase.
                    updateUsersStatusOnLocalUseCase(
                        loggedInUserState.value.firebaseUserId,
                        loggedInUserState.value.toUserLocalEntity().otherUsersStatus
                    )

                    // Set the removeFriendRequestStateFlow to the responseState.
                    _removeFriendRequestStateFlow.value = responseState
                } else {
                    // Set the removeFriendRequestStateFlow to the responseState.
                    _removeFriendRequestStateFlow.value = responseState
                }
            }
        }
    }

    /**
     * Unblocks the required user.
     */
    fun unBlockUser() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the unBlockUserStateFlow to loading state.
                _unBlockUserStateFlow.value = ResponseState.loading()
                // Call the unBlockUserOnRemoteUseCase.
                val responseState =
                    unBlockUserOnRemoteUseCase(
                        // Get the current user's firebase user id.
                        loggedInUserState.value.firebaseUserId,
                        // Get the required user's firebase user id.
                        otherUserState.value.firebaseUserId
                    )
                // Check if the responseState status is SUCCESS.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the required user's firebase user id from the current user's blockedUsersList.
                    loggedInUserState.value.blockedUsersList.remove(otherUserState.value.firebaseUserId)
                    // Call the updateOtherUserStatusOnDbUseCase.
                    updateUsersStatusOnLocalUseCase(
                        // Get the current user's firebase user id.
                        loggedInUserState.value.firebaseUserId,
                        // Get the current user's otherUsersStatus.
                        loggedInUserState.value.toUserLocalEntity().otherUsersStatus
                    )
                    getPostDetails()
                    // Set the unBlockUserStateFlow to the responseState.
                    _unBlockUserStateFlow.value = responseState
                } else {
                    // Set the unBlockUserStateFlow to the responseState.
                    _unBlockUserStateFlow.value = responseState
                }
            }
        }
    }

    /**
     * Blocks a user.
     */
    fun blockUser() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Switch to the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the blockUserStateFlow to loading.
                _blockUserStateFlow.value = ResponseState.loading()
                // Call the blockUserOnRemoteUseCase.
                val responseState =
                    blockUserOnRemoteUseCase(
                        loggedInUserState.value.firebaseUserId,
                        otherUserState.value.firebaseUserId
                    )
                // Check if the responseState is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Add the requiredUserState.value.firebaseUserId to the currentUserState.value.blockedUsersList.
                    loggedInUserState.value.blockedUsersList.add(otherUserState.value.firebaseUserId)
                    // Remove the requiredUserState.value.firebaseUserId from the currentUserState.value.friendList.
                    loggedInUserState.value.friendList.remove(otherUserState.value.firebaseUserId)
                    // Remove the requiredUserState.value.firebaseUserId from the currentUserState.value.requestedFriendRequestList.
                    loggedInUserState.value.requestedFriendRequestList.remove(otherUserState.value.firebaseUserId)
                    // Remove the requiredUserState.value.firebaseUserId from the currentUserState.value.receivedFriendRequestList.
                    loggedInUserState.value.receivedFriendRequestList.remove(otherUserState.value.firebaseUserId)
                    // Call the updateOtherUserStatusOnDbUseCase.
                    updateUsersStatusOnLocalUseCase(
                        loggedInUserState.value.firebaseUserId,
                        loggedInUserState.value.toUserLocalEntity().otherUsersStatus
                    )
                    deleteAllPostOfUserFromLocalUseCase(otherUserState.value.firebaseUserId)
                    getPostDetails()
                    // Set the blockUserStateFlow to the responseState.
                    _blockUserStateFlow.value = responseState
                } else {
                    // Set the blockUserStateFlow to the responseState.
                    _blockUserStateFlow.value = responseState
                }
            }
        }
    }

    /**
     * Unfriends the required user.
     */
    fun unfriendUser() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Perform the unfriend operation in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the unfriendUserStateFlow to loading state.
                _unfriendUserStateFlow.value = ResponseState.loading()
                // Call the unfriendUserOnRemoteUseCase to unfriend the user.
                val responseState =
                    unfriendUserOnRemoteUseCase(
                        // Get the current user's firebase user ID.
                        loggedInUserState.value.firebaseUserId,
                        // Get the required user's firebase user ID.
                        otherUserState.value.firebaseUserId
                    )
                // Check if the response state is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the required user's firebase user ID from the current user's friend list.
                    loggedInUserState.value.friendList.remove(otherUserState.value.firebaseUserId)
                    // Remove the current user's firebase user ID from the required user's friend list.
                    otherUserState.value.friendList.remove(loggedInUserState.value.firebaseUserId)
                    // Call the updateOtherUserStatusOnDbUseCase to update the other user's status on the database.
                    updateUsersStatusOnLocalUseCase(
                        // Get the current user's firebase user ID.
                        loggedInUserState.value.firebaseUserId,
                        // Get the current user's other users status.
                        loggedInUserState.value.toUserLocalEntity().otherUsersStatus
                    )
                    deleteOnlyFriendsOnlyPostOfUserFromLocalUseCase(otherUserState.value.firebaseUserId)

                    // Set the unfriendUserStateFlow to the response state.
                    _unfriendUserStateFlow.value = responseState
                } else {
                    // Set the unfriendUserStateFlow to the response state.
                    _unfriendUserStateFlow.value = responseState
                }
            }
        }
    }

    /**
     * Unfriends and blocks the required user.
     */
    fun unfriendAndBlockUser() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Perform the unfriend and block user operation in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the unfriend and block user state flow to loading.
                _unfriendAndBlockUserStateFlow.value = ResponseState.loading()
                // Call the unfriend and block user use case.
                val responseState =
                    unfriendAndBlockUserOnRemoteUseCase(
                        // Get the current user's firebase user ID.
                        loggedInUserState.value.firebaseUserId,
                        // Get the required user's firebase user ID.
                        otherUserState.value.firebaseUserId
                    )
                // Check if the response state is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the required user from the current user's friend list.
                    loggedInUserState.value.friendList.remove(otherUserState.value.firebaseUserId)
                    // Add the required user to the current user's blocked users list.
                    loggedInUserState.value.blockedUsersList.add(otherUserState.value.firebaseUserId)
                    // Remove the current user from the required user's friend list.
                    otherUserState.value.friendList.remove(loggedInUserState.value.firebaseUserId)
                    // Update the other user's status on the database.
                    updateUsersStatusOnLocalUseCase(
                        // Get the current user's firebase user ID.
                        loggedInUserState.value.firebaseUserId,
                        // Get the current user's other users status.
                        loggedInUserState.value.toUserLocalEntity().otherUsersStatus
                    )
                    deleteAllPostOfUserFromLocalUseCase(otherUserState.value.firebaseUserId)
                    // Set the unfriend and block user state flow to the response state.
                    _unfriendAndBlockUserStateFlow.value = responseState
                } else {
                    // Set the unfriend and block user state flow to the response state.
                    _unfriendAndBlockUserStateFlow.value = responseState
                }
            }
        }
    }

    /**
     * Gets the user details of the current user and the required user.
     */
    fun getUserDetails() {
        // Launch a coroutine in the viewModelScope
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations
            withContext(Dispatchers.IO) {
                // Set the state of the userDetailsStateFlow to loading
                _userDetailsStateFlow.value = ResponseState.loading()

                // Call the getUserDetailsFromIdsUseCase to get the user details
                val response = getUserDetailsFromIdsUseCase(
                    listOf(
                        loggedInUserState.value.firebaseUserId,
                        otherUserState.value.firebaseUserId
                    )
                )

                // Check if the response status is SUCCESS
                if (response.status == RequestStatusEnum.Success) {
                    // Get the list of user details from the response
                    val userDetailList = response.data ?: emptyList()

                    // Check if the list size is 2, which means both users were found
                    if (userDetailList.size == 2) {
                        // Find the current user and required user from the list
                        val currentUser =
                            userDetailList.find { it.firebaseUserId == loggedInUserState.value.firebaseUserId }
                        val otherUser =
                            userDetailList.find { it.firebaseUserId == otherUserState.value.firebaseUserId }

                        // Check if either the current user or required user is null
                        if (currentUser == null || otherUser == null) {
                            // Set the state of the userDetailsStateFlow to error with the NO_USER_FOUND code
                            _userDetailsStateFlow.value =
                                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                        } else {
                            // Add the current user to the database
                            addUserToLocalUseCase(currentUser)
                            // Set the state of the userDetailsStateFlow to success with the current user
                            _userDetailsStateFlow.value =
                                ResponseState.success(Pair(currentUser, otherUser))
                        }
                    } else {
                        // Set the state of the userDetailsStateFlow to error with the NO_USER_FOUND code
                        _userDetailsStateFlow.value =
                            ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                    }
                } else {
                    // Set the state of the userDetailsStateFlow to error with the response message
                    _userDetailsStateFlow.value = ResponseState.error(response.message ?: "")
                }
            }
        }
    }

    /**
     * Launches a coroutine to observe the required users.
     */
    fun liveObserveOtherUsers() {
        // Launch a coroutine to observe the required users.
        viewModelScope.launch {
            // Get the live user observer from the remote use case.
            liveObserveOtherUserListener = liveUserObserverFromRemoteUseCase(
                // The firebase user ID of the required user.
                otherUserState.value.firebaseUserId,
                // The live data flow to emit the required user details.
                _liveObserveOtherUserDetailsStateFlow
            )
        }
    }

    /**
     * Launches a coroutine in the viewModelScope to observe the current user.
     */
    fun liveObserveLoggedInUsers() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Get the live user observer from the remote use case.
            liveObserveLoggedInUserListener = liveUserObserverFromRemoteUseCase(
                // Get the current user's firebase user id.
                loggedInUserState.value.firebaseUserId,
                // Get the live observe current user details state flow.
                _liveObserveLoggedInUserDetailsStateFlow
            )
        }
    }

    /**
     * Updates the required user.
     *
     * @param updatedDetails The updated details of the required user.
     */
    fun updateOtherUser(updatedDetails: UsersBean) {
        // Reset the live data state.
        _liveObserveOtherUserDetailsStateFlow.value = ResponseState.none()

        // Update the required user state.
        otherUserState.value = updatedDetails

        // Update the status with current user state.
        statusWithCurrentUserState.value =
            FunctionHelper.getStatusWithCurrentUser(loggedInUserState.value, otherUserState.value)
    }

    /**
     * Updates the current user state with the updated details.
     *
     * @param updatedDetails The updated details of the current user.
     */
    fun updateLoggedInUser(updatedDetails: UsersBean) {
        // Reset the live data value to none.
        _liveObserveLoggedInUserDetailsStateFlow.value = ResponseState.none()
        // Update the current user state with the updated details.
        loggedInUserState.value = updatedDetails
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                updateUserOnLocalUseCase(updatedDetails)
            }
        }
        // Update the status with current user state.
        statusWithCurrentUserState.value =
            FunctionHelper.getStatusWithCurrentUser(loggedInUserState.value, otherUserState.value)
    }

    /**
     * Cleans up any resources used by the view model.
     */
    override fun onCleared() {
        // Call the superclass 's onCleared() method.
        super.onCleared()

        // Remove the liveObserveRequiredUserListener listener.
        liveObserveOtherUserListener.remove()

        // Remove the liveObserveCurrentUserListener listener.
        liveObserveLoggedInUserListener.remove()
    }
}