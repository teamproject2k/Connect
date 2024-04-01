package com.teamproject2k.connect.domain.repository

import com.google.firebase.firestore.ListenerRegistration
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import kotlinx.coroutines.flow.MutableStateFlow

interface IUserRepository {
    /**
     * Gets the user details from the remote database.
     *
     * @param userId The user ID.
     * @return The response state.
     */
    suspend fun getUserDetailsFromRemote(userId: String): ResponseState<UserBean?>

    /**
     * Gets the number of users with the given name from remote.
     *
     * @param connectIdFirstPart The name.
     * @return The response state.
     */
    suspend fun getUsersCountFromNameInitialsFromRemote(connectIdFirstPart: String): ResponseState<Int>

    /**
     * Adds the user to the remote database.
     *
     * @param userDetails The user details.
     * @return The response state.
     */
    suspend fun addUserToRemote(userDetails: UserBean): ResponseState<Nothing>

    /**
     * Adds the user to the local database.
     *
     * @param userDetails The user details.
     * @return The row ID of the inserted row.
     */
    suspend fun addUserToLocal(userDetails: UserBean): Long

    /**
     * Gets user details from local database.
     *
     * @param fireBaseId The user's Firebase ID.
     * @return The user details, or null if the user is not found.
     */
    suspend fun getUserDetailsFromLocal(fireBaseId: String): UserBean?

    /**
     * Gets user details from local database by ids from remote.
     *
     * @param idList The list of user ids.
     * @return A response state containing the list of user details, or an error if the request failed.
     */
    suspend fun getUserDetailsFromIdsFromRemote(idList: List<String>): ResponseState<List<UserBean>>

    /**
     * Updates the user details on the remote server.
     *
     * @param fieldsToUpdate A map of fields to update.
     * @param firebaseUserId The user's Firebase ID.
     * @return A [ResponseState] containing the result of the update.
     */
    suspend fun updateUserDetailsOnRemote(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): ResponseState<Nothing?>

    /**
     * Updates the user details on the database.
     *
     * @param fieldsToUpdate A map of fields to update.
     * @param firebaseUserId The user's Firebase ID.
     * @return The number of rows affected.
     */
    suspend fun updateUserDetailsOnLocal(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): Long

    /**
     * Get all users not in the given list.
     *
     * @param excludeUserIdList The list of user IDs to exclude.
     * @return A response state containing the list of users not in the given list.
     */
    suspend fun getAllUsersNotInListFromRemote(
        excludeUserIdList: List<String>,
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<UserBean>>

    /**
     * Sends a friend request to the specified user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to send the request to.
     *
     * @return A [ResponseState] containing either a success or failure message.
     */
    suspend fun sendFriendRequestOnRemote(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Withdraws a friend request from the current user to the requested user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the requested user.
     * @return A [ResponseState] containing either a success or failure message.
     */
    suspend fun withdrawFriendRequestOnRemote(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Accepts a friend request.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user who sent the friend request.
     * @return A [ResponseState] containing either a success or error message.
     */
    suspend fun acceptFriendRequestOnRemote(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Removes a friend request from the database.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user who sent the friend request.
     * @return A [ResponseState] containing either a success message or an error message.
     */
    suspend fun removeFriendRequestOnRemote(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Blocks a user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to be blocked.
     * @return A [ResponseState] containing either a success or error message.
     */
    suspend fun blockUserOnRemote(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Unblocks a user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to unblock.
     * @return A [ResponseState] containing either a success or failure message.
     */
    suspend fun unBlockUserOnRemote(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Unfriends the requested user from the current user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the requested user.
     * @return A [ResponseState] containing either a success or error message.
     */
    suspend fun unFriendUserOnRemote(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Unfriends and blocks a user.
     *
     * @param loggedInUserFirebaseId The current user's Firebase ID.
     * @param requestedUserFirebaseId The requested user's Firebase ID.
     * @return A [ResponseState] containing either a success or failure message.
     */
    suspend fun unFriendAndBlockUserOnRemote(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Updates the status with other users on the database.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param otherUsersStatus A map of other users' Firebase IDs to their statuses.
     * @return The number of rows affected.
     */
    suspend fun updateUsersStatusOnLocal(
        loggedInUserFirebaseId: String,
        otherUsersStatus: MutableMap<String, String>
    ): Int

    /**
     * Live observe user from remote.
     *
     * @param firebaseUserId The firebase user id.
     * @param userObserverStateFlow The user observer state flow.
     * @return The listener registration.
     */
    suspend fun liveObserveUserFromRemote(
        firebaseUserId: String, userObserverStateFlow: MutableStateFlow<ResponseState<UserBean>>
    ): ListenerRegistration

    /**
     * Saves a post on the remote server for a logged-in user.
     *
     * This function sends a request to save the post with the specified Firebase ID for the logged-in user on the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user who is saving the post.
     * @param postFirebaseId The Firebase ID of the post to be saved.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     * otherwise, contains an error message.
     */
    suspend fun savePostOnRemote(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Un-saves a post from the remote server for a logged-in user.
     *
     * This function sends a request to un-save the post with the specified Firebase ID for the logged-in user on the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user who is un-saving the post.
     * @param postFirebaseId The Firebase ID of the post to be un-saved.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     * otherwise, contains an error message.
     */
    suspend fun unSavePostFromRemote(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Updates the FCM token for a logged-in user on the remote server.
     *
     * This function sends a request to update the FCM token for the specified logged-in user on the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user whose FCM token is being updated.
     * @param fcmToken The new FCM token to be set for the logged-in user.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     * otherwise, contains an error message.
     */
    suspend fun updateFCMTokenOnRemote(
        loggedInUserFirebaseId: String,
        fcmToken: String
    ): ResponseState<Nothing>

    /**
     * Updates the FCM token for a logged-in user in the local database.
     *
     * This function updates the FCM token for the specified logged-in user in the local database.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user whose FCM token is being updated.
     * @param updatedToken The new FCM token to be set for the logged-in user.
     * @return The number of users whose FCM token was updated. Typically, this would be 1 if the update was successful,
     *         and 0 if no user with the given Firebase ID was found.
     */
    suspend fun updateFCMTokenOnLocal(loggedInUserFirebaseId: String, updatedToken: String): Int

    /**
     * Updates the list of saved posts for the logged-in user in the local database.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param savedPost The updated list of post Firebase IDs to be saved for the logged-in user.
     * @return The number of saved posts updated in the local database.
     */
    suspend fun updateSavedPostOnLocal(loggedInUserFirebaseId: String, savedPost: List<String>): Int

    /**
     * Updates user details in the local database.
     *
     * This function updates the details of a user in the local database with the provided user details.
     *
     * @param userDetails The details of the user to be updated.
     * @return The number of users updated. Typically, this would be 1 if the user was successfully updated,
     *         and 0 if no user with the given details was found.
     */
    suspend fun updateUserOnLocal(userDetails: UserBean): Int

    /**
     * Adds a list of users to the local database.
     *
     * @param userList The list of users to be added to the local database.
     * @return An array of long values representing the IDs of the inserted users in the local database.
     *         The order of IDs corresponds to the order of users in the input list.
     */
    suspend fun addUserListToLocal(userList: List<UserBean>): LongArray

    /**
     * Retrieves user details from the local database based on a list of user IDs.
     *
     * @param userIdList The list of Firebase IDs of users.
     * @return A list of [UserBean] objects containing the details of users retrieved from the local database.
     */
    suspend fun getAllUsersFromIdsFromLocal(userIdList: List<String>): List<UserBean>

    /**
     * Deletes all users from the local database except those specified in the given list.
     *
     * @param exceptList The list of Firebase IDs of users to be excluded from deletion.
     * @return The number of users deleted from the local database.
     */
    suspend fun deleteAllUsersFromLocalExceptInList(exceptList: List<String>): Int

    /**
     * Retrieves the list of received friend requests for the logged-in user from the remote server.
     *
     * @param userFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing a pair where the first element is the details of the logged-in user
     *         and the second element is an array list of [UserBean] representing users who sent friend requests to the logged-in user.
     *         If successful, returns the pair; otherwise, contains an error message.
     */
    suspend fun getLoggedInUserReceivedFriendRequestListFromRemote(userFirebaseId: String): ResponseState<Pair<UserBean, ArrayList<UserBean>>>

    /**
     * Retrieves the friend list of the logged-in user from the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing a pair where the first element is the details of the logged-in user
     *         and the second element is an array list of [UserBean] representing users who are friends with the logged-in user.
     *         If successful, returns the pair; otherwise, contains an error message.
     */
    suspend fun getLoggedInUserFriendListFromRemote(loggedInUserFirebaseId: String): ResponseState<Pair<UserBean, ArrayList<UserBean>>>

    /**
     * Retrieves the block list of the logged-in user from the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing a pair where the first element is the details of the logged-in user
     *         and the second element is an array list of [UserBean] representing users blocked by the logged-in user.
     *         If successful, returns the pair; otherwise, contains an error message.
     */
    suspend fun getLoggedInUserBlockedListFromRemote(loggedInUserFirebaseId: String): ResponseState<Pair<UserBean, ArrayList<UserBean>>>

    /**
     * Retrieves the list of users friend-requested by the logged-in user from the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing a pair where the first element is the details of the logged-in user
     *         and the second element is an array list of [UserBean] representing users requested by the logged-in user.
     *         If successful, returns the pair; otherwise, contains an error message.
     */
    suspend fun getRequestedByLoggedInUserListFromRemoteFromRemote(loggedInUserFirebaseId: String): ResponseState<Pair<UserBean, ArrayList<UserBean>>>
}