package com.example.connect.domain.repository

import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow

interface IUserRepository {
    /**
     * Gets the user details from the remote database.
     *
     * @param userId The user ID.
     * @return The response state.
     */
    suspend fun getUserDetailsFromRemote(userId: String): ResponseState<UsersBean?>

    /**
     * Gets the number of users with the given name from remote.
     *
     * @param name The name.
     * @return The response state.
     */
    suspend fun getUsersCountFromNameFromRemote(name: String): ResponseState<Int>

    /**
     * Adds the user to the remote database.
     *
     * @param userDetails The user details.
     * @return The response state.
     */
    suspend fun addUserToRemote(userDetails: UsersBean): ResponseState<Nothing>

    /**
     * Adds the user to the local database.
     *
     * @param userDetails The user details.
     * @return The row ID of the inserted row.
     */
    suspend fun addUserToDb(userDetails: UsersBean): Long


    /**
     * Gets user details from local database.
     *
     * @param fireBaseId The user's Firebase ID.
     * @return The user details, or null if the user is not found.
     */
    suspend fun getUserDetailsFromDb(fireBaseId: String): UsersBean?

    /**
     * Gets user details from local database by ids from remote.
     *
     * @param idList The list of user ids.
     * @return A response state containing the list of user details, or an error if the request failed.
     */
    suspend fun getUserDetailsFromIdsFromRemote(idList: List<String>): ResponseState<List<UsersBean>>

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
    suspend fun updateUserDetailsOnDb(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): Long

    /**
     * Get all users not in the given list.
     *
     * @param excludeUserIdList The list of user IDs to exclude.
     * @return A response state containing the list of users not in the given list.
     */
    suspend fun getAllUsersNotInList(
        excludeUserIdList: List<String>,
        currentUserFirebaseId: String
    ): ResponseState<ArrayList<UsersBean>>

    /**
     * Sends a friend request to the specified user.
     *
     * @param currentUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to send the request to.
     *
     * @return A [ResponseState] containing either a success or failure message.
     */
    suspend fun sendFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Withdraws a friend request from the current user to the requested user.
     *
     * @param currentUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the requested user.
     * @return A [ResponseState] containing either a success or failure message.
     */
    suspend fun withdrawFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Accepts a friend request.
     *
     * @param currentUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user who sent the friend request.
     * @return A [ResponseState] containing either a success or error message.
     */
    suspend fun acceptFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Removes a friend request from the database.
     *
     * @param currentUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user who sent the friend request.
     * @return A [ResponseState] containing either a success message or an error message.
     */
    suspend fun removeFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Blocks a user.
     *
     * @param currentUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to be blocked.
     * @return A [ResponseState] containing either a success or error message.
     */
    suspend fun blockUser(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Unblocks a user.
     *
     * @param currentUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to unblock.
     * @return A [ResponseState] containing either a success or failure message.
     */
    suspend fun unBlockUser(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Unfriends the requested user from the current user.
     *
     * @param currentUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the requested user.
     * @return A [ResponseState] containing either a success or error message.
     */
    suspend fun unFriendUser(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Unfriends and blocks a user.
     *
     * @param currentUserFirebaseId The current user's Firebase ID.
     * @param requestedUserFirebaseId The requested user's Firebase ID.
     * @return A [ResponseState] containing either a success or failure message.
     */
    suspend fun unFriendAndBlockUser(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Updates the status with other users on the database.
     *
     * @param currentUserFirebaseId The Firebase ID of the current user.
     * @param otherUsersStatus A map of other users' Firebase IDs to their statuses.
     * @return The number of rows affected.
     */
    suspend fun updateOtherUsersStatusOnDb(
        currentUserFirebaseId: String,
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
        firebaseUserId: String, userObserverStateFlow: MutableStateFlow<ResponseState<UsersBean>>
    ): ListenerRegistration


    suspend fun savePost(currentUserFirebaseId: String, postId: String): ResponseState<Nothing>

    suspend fun unSavePost(currentUserFirebaseId: String, postId: String): ResponseState<Nothing>


    suspend fun updateFCMTokenOnRemote(
        currentUserFirebaseId: String,
        fcmToken: String
    ): ResponseState<Nothing>

    suspend fun updateFCMTokenOnLocal(currentUserFirebaseId: String, updatedToken: String): Int


    suspend fun updateSavedPost(loggedInUserFirebaseId: String, savedPost: List<String>): Int
}