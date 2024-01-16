package com.example.connect.presentation.ui.home.other_user_profile

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.fcm.SendFCMUseCase
import com.example.connect.domain.useCase.posts.GetPostDetailsFromRemoteUseCase
import com.example.connect.domain.useCase.user.AcceptFriendRequestUseCase
import com.example.connect.domain.useCase.user.AddUserToDbUseCase
import com.example.connect.domain.useCase.user.BlockUserUseCase
import com.example.connect.domain.useCase.user.GetUserDetailsFromIdsFromRemoteUseCase
import com.example.connect.domain.useCase.user.LiveUserObserverFromRemoteUseCase
import com.example.connect.domain.useCase.user.RemoveFriendRequestUseCase
import com.example.connect.domain.useCase.user.SendFriendRequestUseCase
import com.example.connect.domain.useCase.user.UnBlockUserUseCase
import com.example.connect.domain.useCase.user.UnfriendAndBlockUserUseCase
import com.example.connect.domain.useCase.user.UnfriendUserUseCase
import com.example.connect.domain.useCase.user.UpdateOtherUserStatusOnDbUseCase
import com.example.connect.domain.useCase.user.WithdrawFriendRequestUseCase
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.services.fcm.NotificationTypesEnum
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.NotificationsConstantHelper
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OtherUserProfileViewModel @Inject constructor(
    private val getPostDetailsFromRemoteUseCase: GetPostDetailsFromRemoteUseCase,
    private val getUserDetailsFromIdsUseCase: GetUserDetailsFromIdsFromRemoteUseCase,
    private val sendFriendRequestUseCase: SendFriendRequestUseCase,
    private val withdrawFriendRequestUseCase: WithdrawFriendRequestUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val removeFriendRequestUseCase: RemoveFriendRequestUseCase,
    private val blockUserUseCase: BlockUserUseCase,
    private val unBlockUserUseCase: UnBlockUserUseCase,
    private val updateOtherUserStatusOnDbUseCase: UpdateOtherUserStatusOnDbUseCase,
    private val unfriendAndBlockUserUseCase: UnfriendAndBlockUserUseCase,
    private val unfriendUserUseCase: UnfriendUserUseCase,
    private val addUserToDbUseCase: AddUserToDbUseCase,
    private val liveUserObserverFromRemoteUseCase: LiveUserObserverFromRemoteUseCase,
    private val sendFCMUseCase: SendFCMUseCase
) : BaseViewModel() {
    var isDataInitialized = false
    val snackBarMessageState = mutableStateOf("")
    private val _friendsDetailsStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val friendsDetailsStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _friendsDetailsStateFlow

    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<List<PostBean>>> =
        MutableStateFlow(ResponseState.none())
    val postDetailsStateFlow: StateFlow<ResponseState<List<PostBean>>> get() = _postDetailsStateFlow

    private val _sendFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val sendFriendRequestStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _sendFriendRequestStateFlow

    private val _acceptFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val acceptFriendRequestStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _acceptFriendRequestStateFlow

    private val _withdrawFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val withdrawFriendRequestStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _withdrawFriendRequestStateFlow

    private val _removeFriendRequestStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val removeFriendRequestStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _removeFriendRequestStateFlow

    private val _unBlockUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val unBlockUserStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _unBlockUserStateFlow

    private val _blockUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val blockUserStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _blockUserStateFlow

    private val _unfriendUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val unfriendUserStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _unfriendUserStateFlow

    private val _unfriendAndBlockUserStateFlow: MutableStateFlow<ResponseState<List<Nothing>>> =
        MutableStateFlow(ResponseState.none())
    val unfriendAndBlockUserStateFlow: StateFlow<ResponseState<List<Nothing>>> get() = _unfriendAndBlockUserStateFlow

    private val _liveObserveRequiredUserDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean>> =
        MutableStateFlow(ResponseState.none())
    val liveObserveRequiredUserDetailsStateFlow: StateFlow<ResponseState<UsersBean>> get() = _liveObserveRequiredUserDetailsStateFlow

    private val _liveObserveCurrentUserDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean>> =
        MutableStateFlow(ResponseState.none())
    val liveObserveCurrentUserDetailsStateFlow: StateFlow<ResponseState<UsersBean>> get() = _liveObserveCurrentUserDetailsStateFlow

    private val _userDetailsStateFlow: MutableStateFlow<ResponseState<UsersBean>> =
        MutableStateFlow(ResponseState.none())
    val userDetailsStateFlow: StateFlow<ResponseState<UsersBean>> get() = _userDetailsStateFlow

    val statusWithCurrentUserState: MutableState<String> = mutableStateOf("")
    private lateinit var liveObserveRequiredUserListener: ListenerRegistration
    private lateinit var liveObserveCurrentUserListener: ListenerRegistration

    lateinit var requiredUserState: MutableState<UsersBean>
    lateinit var currentUserState: MutableState<UsersBean>

    /**
     * Initializes the data for the profile screen.
     *
     * @param currentUser The current user.
     * @param requestedUser The requested user.
     */
    fun initializeData(currentUser: UsersBean, requestedUser: UsersBean) {
        // Get the status with the current user.
        statusWithCurrentUserState.value =
            FunctionHelper.getStatusWithCurrentUser(currentUser, requestedUser)

        // Set the current user state.
        currentUserState = mutableStateOf(currentUser)

        // Set the requested user state.
        requiredUserState = mutableStateOf(requestedUser)

        // Set the data initialization flag to true.
        isDataInitialized = true
    }

    /**
     * Gets the details of a post.
     *
     * @param fireBaseId The fire base id of the post.
     */
    fun getPostDetails(fireBaseId: String) {
        // Launch a coroutine in the viewModelScope
        viewModelScope.launch {
            // Switch to the IO dispatcher to perform network operations
            withContext(Dispatchers.IO) {
                // Set the post details state to loading
                _postDetailsStateFlow.value = ResponseState.loading()

                // Get the post details from the remote use case
                _postDetailsStateFlow.value = getPostDetailsFromRemoteUseCase.invoke(
                    fireBaseId,
                    currentUserState.value.firebaseUserId
                )
            }
        }
    }

    /**
     * Gets the friend list from the given friend IDs.
     *
     * @param friendIdList The list of friend IDs.
     */
    fun getFriendListFromIds(friendIdList: List<String>) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Perform the network request in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Check if the friendIdList is empty.
                _friendsDetailsStateFlow.value = ResponseState.loading()
                if (friendIdList.isEmpty()) {
                    delay(500)
                    // If the friendIdList is empty, emit an empty list as the response.
                    _friendsDetailsStateFlow.value = ResponseState.success(emptyList())
                } else {
                    // If the friendIdList is not empty, emit a loading state.
                    // Get the user details from the userIds.
                    _friendsDetailsStateFlow.value =
                        getUserDetailsFromIdsUseCase.invoke(friendIdList)
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
                // Call the sendFriendRequestUseCase.
                val responseState =
                    sendFriendRequestUseCase.invoke(
                        // Get the current user's firebaseUserId from the currentUserState.
                        currentUserState.value.firebaseUserId,
                        // Get the required user's firebaseUserId from the requiredUserState.
                        requiredUserState.value.firebaseUserId
                    )
                // Check if the responseState is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Add the required user's firebaseUserId to the current user's requestedFriendRequestList.
                    currentUserState.value.requestedFriendRequestList.add(requiredUserState.value.firebaseUserId)
                    // Add the current user's firebaseUserId to the required user's receivedFriendRequestList.
                    requiredUserState.value.receivedFriendRequestList.add(currentUserState.value.firebaseUserId)
                    // Call the updateOtherUserStatusOnDbUseCase.
                    updateOtherUserStatusOnDbUseCase.invoke(
                        // Get the current user's firebaseUserId from the currentUserState.
                        currentUserState.value.firebaseUserId,
                        // Get the current user's otherUsersStatus from the currentUserState.
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
                    val data = hashMapOf(
                        Pair(
                            NotificationsConstantHelper.MESSAGE,
                            currentUserState.value.name
                        ),
                        Pair(
                            NotificationTypesEnum::name.name,
                            NotificationTypesEnum.FriendRequestReceived.name
                        )
                    )
                    sendFCMUseCase.invoke(
                        FunctionHelper.getAccessToken(context),
                        data,
                        requiredUserState.value.fcmToken
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

                // Call the withdrawFriendRequestUseCase.
                val responseState =
                    withdrawFriendRequestUseCase.invoke(
                        // Get the current user's firebase user ID.
                        currentUserState.value.firebaseUserId,
                        // Get the required user's firebase user ID.
                        requiredUserState.value.firebaseUserId
                    )

                // Check if the responseState is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the required user's firebase user ID from the current user's requestedFriendRequestList.
                    currentUserState.value.requestedFriendRequestList.remove(requiredUserState.value.firebaseUserId)

                    // Remove the current user's firebase user ID from the required user's receivedFriendRequestList.
                    requiredUserState.value.receivedFriendRequestList.remove(currentUserState.value.firebaseUserId)

                    // Call the updateOtherUserStatusOnDbUseCase.
                    updateOtherUserStatusOnDbUseCase.invoke(
                        // Get the current user's firebase user ID.
                        currentUserState.value.firebaseUserId,
                        // Get the current user's UserDbEntity.
                        currentUserState.value.toUserDbEntity().otherUsersStatus
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

                // Call the acceptFriendRequestUseCase to accept the friend request.
                val responseState =
                    acceptFriendRequestUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )

                // Check if the responseState is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the required user's firebaseUserId from the currentUserState's receivedFriendRequestList.
                    currentUserState.value.receivedFriendRequestList.remove(requiredUserState.value.firebaseUserId)

                    // Add the required user's firebaseUserId to the currentUserState's friendList.
                    currentUserState.value.friendList.add(requiredUserState.value.firebaseUserId)

                    // Remove the currentUserState's firebaseUserId from the requiredUserState's requestedFriendRequestList.
                    requiredUserState.value.requestedFriendRequestList.remove(currentUserState.value.firebaseUserId)

                    // Add the currentUserState's firebaseUserId to the requiredUserState's friendList.
                    requiredUserState.value.friendList.add(currentUserState.value.firebaseUserId)

                    // Call the updateOtherUserStatusOnDbUseCase to update the other user's status on the database.
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
                    val data = hashMapOf(
                        Pair(
                            NotificationsConstantHelper.MESSAGE,
                            currentUserState.value.name
                        ),
                        Pair(
                            NotificationTypesEnum::name.name,
                            NotificationTypesEnum.FriendRequestAccepted.name
                        )
                    )
                    sendFCMUseCase.invoke(
                        FunctionHelper.getAccessToken(context),
                        data,
                        requiredUserState.value.fcmToken
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

                // Call the removeFriendRequestUseCase.
                val responseState =
                    removeFriendRequestUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )

                // Check if the responseState is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the requiredUserState.value.firebaseUserId from the currentUserState.value.receivedFriendRequestList.
                    currentUserState.value.receivedFriendRequestList.remove(requiredUserState.value.firebaseUserId)

                    // Remove the currentUserState.value.firebaseUserId from the requiredUserState.value.requestedFriendRequestList.
                    requiredUserState.value.requestedFriendRequestList.remove(currentUserState.value.firebaseUserId)

                    // Call the updateOtherUserStatusOnDbUseCase.
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
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
                // Call the unBlockUserUseCase.
                val responseState =
                    unBlockUserUseCase.invoke(
                        // Get the current user's firebase user id.
                        currentUserState.value.firebaseUserId,
                        // Get the required user's firebase user id.
                        requiredUserState.value.firebaseUserId
                    )
                // Check if the responseState status is SUCCESS.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the required user's firebase user id from the current user's blockedUsersList.
                    currentUserState.value.blockedUsersList.remove(requiredUserState.value.firebaseUserId)
                    // Call the updateOtherUserStatusOnDbUseCase.
                    updateOtherUserStatusOnDbUseCase.invoke(
                        // Get the current user's firebase user id.
                        currentUserState.value.firebaseUserId,
                        // Get the current user's otherUsersStatus.
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
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
                // Call the blockUserUseCase.
                val responseState =
                    blockUserUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
                    )
                // Check if the responseState is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Add the requiredUserState.value.firebaseUserId to the currentUserState.value.blockedUsersList.
                    currentUserState.value.blockedUsersList.add(requiredUserState.value.firebaseUserId)
                    // Remove the requiredUserState.value.firebaseUserId from the currentUserState.value.friendList.
                    currentUserState.value.friendList.remove(requiredUserState.value.firebaseUserId)
                    // Remove the requiredUserState.value.firebaseUserId from the currentUserState.value.requestedFriendRequestList.
                    currentUserState.value.requestedFriendRequestList.remove(requiredUserState.value.firebaseUserId)
                    // Remove the requiredUserState.value.firebaseUserId from the currentUserState.value.receivedFriendRequestList.
                    currentUserState.value.receivedFriendRequestList.remove(requiredUserState.value.firebaseUserId)
                    // Call the updateOtherUserStatusOnDbUseCase.
                    updateOtherUserStatusOnDbUseCase.invoke(
                        currentUserState.value.firebaseUserId,
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
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
                // Call the unfriendUserUseCase to unfriend the user.
                val responseState =
                    unfriendUserUseCase.invoke(
                        // Get the current user's firebase user ID.
                        currentUserState.value.firebaseUserId,
                        // Get the required user's firebase user ID.
                        requiredUserState.value.firebaseUserId
                    )
                // Check if the response state is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the required user's firebase user ID from the current user's friend list.
                    currentUserState.value.friendList.remove(requiredUserState.value.firebaseUserId)
                    // Remove the current user's firebase user ID from the required user's friend list.
                    requiredUserState.value.friendList.remove(currentUserState.value.firebaseUserId)
                    // Call the updateOtherUserStatusOnDbUseCase to update the other user's status on the database.
                    updateOtherUserStatusOnDbUseCase.invoke(
                        // Get the current user's firebase user ID.
                        currentUserState.value.firebaseUserId,
                        // Get the current user's other users status.
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
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
                    unfriendAndBlockUserUseCase.invoke(
                        // Get the current user's firebase user ID.
                        currentUserState.value.firebaseUserId,
                        // Get the required user's firebase user ID.
                        requiredUserState.value.firebaseUserId
                    )
                // Check if the response state is successful.
                if (responseState.status == RequestStatusEnum.Success) {
                    // Remove the required user from the current user's friend list.
                    currentUserState.value.friendList.remove(requiredUserState.value.firebaseUserId)
                    // Add the required user to the current user's blocked users list.
                    currentUserState.value.blockedUsersList.add(requiredUserState.value.firebaseUserId)
                    // Remove the current user from the required user's friend list.
                    requiredUserState.value.friendList.remove(currentUserState.value.firebaseUserId)
                    // Update the other user's status on the database.
                    updateOtherUserStatusOnDbUseCase.invoke(
                        // Get the current user's firebase user ID.
                        currentUserState.value.firebaseUserId,
                        // Get the current user's other users status.
                        currentUserState.value.toUserDbEntity().otherUsersStatus
                    )
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
                val response = getUserDetailsFromIdsUseCase.invoke(
                    listOf(
                        currentUserState.value.firebaseUserId,
                        requiredUserState.value.firebaseUserId
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
                            userDetailList.find { it.firebaseUserId == currentUserState.value.firebaseUserId }
                        val requiredUser =
                            userDetailList.find { it.firebaseUserId == requiredUserState.value.firebaseUserId }

                        // Check if either the current user or required user is null
                        if (currentUser == null || requiredUser == null) {
                            // Set the state of the userDetailsStateFlow to error with the NO_USER_FOUND code
                            _userDetailsStateFlow.value =
                                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                        } else {
                            // Add the current user to the database
                            addUserToDbUseCase.invoke(currentUser)

                            // Get the post details of the required user
                            getPostDetails(requiredUser.firebaseUserId)

                            // Get the friend list of the required user
                            getFriendListFromIds(requiredUser.friendList)

                            // Update the currentUserState and requiredUserState with the new values
                            currentUserState.value = currentUser
                            requiredUserState.value = requiredUser

                            // Set the state of the userDetailsStateFlow to success with the current user
                            _userDetailsStateFlow.value = ResponseState.success(currentUser)
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
    fun liveObserveRequiredUsers() {
        // Launch a coroutine to observe the required users.
        viewModelScope.launch {
            // Get the live user observer from the remote use case.
            liveObserveRequiredUserListener = liveUserObserverFromRemoteUseCase.invoke(
                // The firebase user ID of the required user.
                requiredUserState.value.firebaseUserId,
                // The live data flow to emit the required user details.
                _liveObserveRequiredUserDetailsStateFlow
            )
        }
    }

    /**
     * Launches a coroutine in the viewModelScope to observe the current user.
     */
    fun liveObserveCurrentUsers() {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Get the live user observer from the remote use case.
            liveObserveCurrentUserListener = liveUserObserverFromRemoteUseCase.invoke(
                // Get the current user's firebase user id.
                currentUserState.value.firebaseUserId,
                // Get the live observe current user details state flow.
                _liveObserveCurrentUserDetailsStateFlow
            )
        }
    }

    /**
     * Updates the required user.
     *
     * @param updatedDetails The updated details of the required user.
     */
    fun updateRequiredUser(updatedDetails: UsersBean) {
        // Reset the live data state.
        _liveObserveRequiredUserDetailsStateFlow.value = ResponseState.none()

        // Update the required user state.
        requiredUserState.value = updatedDetails

        // Update the status with current user state.
        statusWithCurrentUserState.value =
            FunctionHelper.getStatusWithCurrentUser(currentUserState.value, requiredUserState.value)
    }

    /**
     * Updates the current user state with the updated details.
     *
     * @param updatedDetails The updated details of the current user.
     */
    fun updateCurrentUser(updatedDetails: UsersBean) {
        // Reset the live data value to none.
        _liveObserveCurrentUserDetailsStateFlow.value = ResponseState.none()

        // Update the current user state with the updated details.
        currentUserState.value = updatedDetails

        // Update the status with current user state.
        statusWithCurrentUserState.value =
            FunctionHelper.getStatusWithCurrentUser(currentUserState.value, requiredUserState.value)
    }

    /**
     * Cleans up any resources used by the view model.
     */
    override fun onCleared() {
        // Call the superclass 's onCleared() method.
        super.onCleared()

        // Remove the liveObserveRequiredUserListener listener.
        liveObserveRequiredUserListener.remove()

        // Remove the liveObserveCurrentUserListener listener.
        liveObserveCurrentUserListener.remove()
    }
}