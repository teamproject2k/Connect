package com.example.connect.domain.repository

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.domain.models.UsersBean

interface IHomeRepository {

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
     * Gets post details from local database.
     *
     * @param fireBaseId The post's Firebase ID.
     * @return The post details, or null if the post is not found.
     */
    suspend fun getPostDetailsFromDb(fireBaseId: String): List<PostDetails>?

    /**
     * Gets post details from remote.
     *
     * @param fireBaseId The post's Firebase ID.
     * @return A response state containing the post details, or an error if the request failed.
     */
    suspend fun getPostDetailsFromRemote(fireBaseId: String): ResponseState<List<PostDetails>>

    /**
     * Adds a post to the local database.
     *
     * @param postDetails The post details to add.
     * @return The row ID of the newly added post.
     */
    suspend fun addPostToDb(postDetails: PostDetails): Long

    /**
     * Adds a list of posts to the local database.
     *
     * @param postDetailList The list of post details to add.
     * @return An array of row IDs of the newly added posts.
     */
    suspend fun addPostListToDb(postDetailList: List<PostDetails>): LongArray

    /**
     * Uploads a post to the remote.
     *
     * @param postDetails The post details to upload.
     * @param fireBaseId The user's Firebase ID.
     * @return A response state containing the post ID, or an error if the request failed.
     */
    suspend fun uploadPostToRemote(
        postDetails: PostDetails,
        fireBaseId: String
    ): ResponseState<String>

}