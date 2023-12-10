package com.example.connect.domain.repository

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails

interface IHomeRepository {

    /**
     * Gets user details from local database.
     *
     * @param fireBaseId The user's Firebase ID.
     * @return The user details, or null if the user is not found.
     */
    suspend fun getUserDetailsFromLocal(fireBaseId: String): UserDetails?

    /**
     * Gets user details from server.
     *
     * @param fireBaseId The user's Firebase ID.
     * @return A response state containing the user details, or an error if the request failed.
     */
    suspend fun getUserDetailsFromServer(fireBaseId: String): ResponseState<UserDetails>

    /**
     * Adds a user to the local database.
     *
     * @param userDetails The user details to add.
     * @return The row ID of the newly added user.
     */
    suspend fun addUserToLocalDb(userDetails: UserDetails): Long

    /**
     * Gets user details from local database by ids.
     *
     * @param idList The list of user ids.
     * @return A response state containing the list of user details, or an error if the request failed.
     */
    suspend fun getUserDetailsFromIds(idList: List<String>): ResponseState<List<UserDetails>>

    /**
     * Gets post details from local database.
     *
     * @param fireBaseId The post's Firebase ID.
     * @return The post details, or null if the post is not found.
     */
    suspend fun getPostDetailsFromLocal(fireBaseId: String): List<PostDetails>?

    /**
     * Gets post details from server.
     *
     * @param fireBaseId The post's Firebase ID.
     * @return A response state containing the post details, or an error if the request failed.
     */
    suspend fun getPostDetailsFromServer(fireBaseId: String): ResponseState<List<PostDetails>>

    /**
     * Adds a post to the local database.
     *
     * @param postDetails The post details to add.
     * @return The row ID of the newly added post.
     */
    suspend fun addPostToLocal(postDetails: PostDetails): Long

    /**
     * Adds a list of posts to the local database.
     *
     * @param postDetailList The list of post details to add.
     * @return An array of row IDs of the newly added posts.
     */
    suspend fun addPostListToLocal(postDetailList: List<PostDetails>): LongArray

    /**
     * Uploads a post to the server.
     *
     * @param postDetails The post details to upload.
     * @param fireBaseId The user's Firebase ID.
     * @return A response state containing the post ID, or an error if the request failed.
     */
    suspend fun uploadPostToServer(
        postDetails: PostDetails,
        fireBaseId: String
    ): ResponseState<String>

}