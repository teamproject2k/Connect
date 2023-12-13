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

    suspend fun updateUserDetailsOnRemote(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): ResponseState<Nothing?>

    suspend fun updateUserDetailsOnDb(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): Long

}