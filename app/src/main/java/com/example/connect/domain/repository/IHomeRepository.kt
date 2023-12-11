package com.example.connect.domain.repository

import android.net.Uri
import com.example.connect.common.ResponseState
import com.example.connect.domain.models.PostBean
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
    suspend fun getPostDetailsFromDb(fireBaseId: String): List<PostBean>

    /**
     * Gets post details from remote.
     *
     * @param fireBaseId The post's Firebase ID.
     * @return A response state containing the post details, or an error if the request failed.
     */
    suspend fun getPostDetailsFromRemote(fireBaseId: String): ResponseState<List<PostBean>>

    /**
     * Adds a post to the local database.
     *
     * @param postDetails The post details to add.
     * @return The row ID of the newly added post.
     */
    suspend fun addPostToDb(postDetails: PostBean): Long

    /**
     * Adds a list of posts to the local database.
     *
     * @param postDetailList The list of post details to add.
     * @return An array of row IDs of the newly added posts.
     */
    suspend fun addPostListToDb(postDetailList: List<PostBean>): LongArray

    /**
     * Uploads a post to the remote.
     *
     * @param postDetails The post details to upload.
     * @param fireBaseId The user's Firebase ID.
     * @return A response state containing the post ID, or an error if the request failed.
     */
    suspend fun uploadPostToRemote(
        postDetails: PostBean,
        fireBaseId: String
    ): ResponseState<String>


    /**
     * Uploads a file to a remote server.
     *
     * @param url The URL of the remote server.
     * @param path The path to the file to be uploaded.
     * @return A [ResponseState] containing the result of the upload.
     */
    suspend fun uploadFileToRemote(url: Uri, path: String): ResponseState<String>

    suspend fun updateUserDetailsOnServer(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): ResponseState<Nothing?>

    suspend fun getUsersFromName(name: Any): ResponseState<Int>

    suspend fun updateImageOnRemoteStorage(
        imageUri: Uri?,
        firebaseUserId: String,
        parameterToUpdate: String
    ): ResponseState<String>

    suspend fun updateUserDetailsOnLocal(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): Long


    suspend fun getDeviceIdFromRemote(firebaseUserId: String): ResponseState<String>
}