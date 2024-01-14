package com.example.connect.domain.repository

import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.PostWithUserDetails
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState

interface IPostRepository {
    /**
     * Gets post details from local database.
     *
     * @param fireBaseId The post's Firebase ID.
     * @return The post details, or null if the post is not found.
     */
    suspend fun getPostDetailsFromLocal(fireBaseId: String): List<PostBean>

    /**
     * Gets post details from remote.
     *
     * @param fireBaseId The post's Firebase ID.
     * @return A response state containing the post details, or an error if the request failed.
     */
    suspend fun getPostDetailsFromRemote(
        fireBaseId: String,
        currentUserFirebaseId: String
    ): ResponseState<List<PostBean>>

    /**
     * Adds a post to the local database.
     *
     * @param postDetails The post details to add.
     * @return The row ID of the newly added post.
     */
    suspend fun addPostToLocal(postDetails: PostBean): Long

    /**
     * Adds a list of posts to the local database.
     *
     * @param postDetailList The list of post details to add.
     * @return An array of row IDs of the newly added posts.
     */
    suspend fun addPostListToLocal(postDetailList: List<PostBean>): LongArray

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

    suspend fun getAllPostsWithUserDetailsFromRemote(
        currentUserFirebaseId: String
    ): ResponseState<Pair<List<PostBean>, List<UsersBean>>>

    suspend fun addLikeOf(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing>

    suspend fun removeLikeOf(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing>

    suspend fun getSavedPostsWithUsersFromRemote(
        loggedInUserFirebaseId: String,
        savedPosts: ArrayList<String>
    ): ResponseState<List<PostWithUserDetails>>

    suspend fun deletePostFromRemote(postId: String): ResponseState<Nothing>
    suspend fun addCommentOnRemote(comment: CommentBean): ResponseState<String>

    suspend fun deleteCommentOnRemote(
        commentId: String,
        postId: String,
        deleteCount: Int
    ): ResponseState<Nothing>

    suspend fun getAllCommentsWithUsersFromRemote(
        postId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<Pair<MutableMap<CommentBean, ArrayList<CommentBean>>, List<UsersBean>>>


    suspend fun addLikeForComment(
        commentId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<Nothing>


    suspend fun removeLikeForComment(
        commentId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<Nothing>

    suspend fun getPostWithUserFromLocal(savedPostIds: List<String>): ResponseState<List<PostWithUserDetails>>

    suspend fun updatePostDetailsOnLocal(postDetails: PostBean): Int
}