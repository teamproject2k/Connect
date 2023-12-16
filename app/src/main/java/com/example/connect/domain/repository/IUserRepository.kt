package com.example.connect.domain.repository

import com.example.connect.common.ResponseState
import com.example.connect.domain.models.UsersBean

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


    suspend fun sendFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>


    suspend fun acceptFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>


    suspend fun denyFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing>


    suspend fun blockUser(currentUserFirebaseId: String, requestedUserFirebaseId: String)


    suspend fun removeFriend(currentUserFirebaseId: String, requestedUserFirebaseId: String)


    suspend fun updateOtherUsersStatusOnDb(
        currentUserFirebaseId: String,
        otherUsersStatus: MutableMap<String, String>
    ): Int

}