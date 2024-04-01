package com.teamproject2k.connect.domain.repository

import com.teamproject2k.connect.domain.models.CommentBean
import com.teamproject2k.connect.domain.models.CommentWithUserBean
import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.models.PostWithUserDetailsBean
import com.teamproject2k.connect.domain.network_utils.ResponseState

interface IPostRepository {
    /**
     * Gets post details from local database.
     *
     * @param fireBaseId The post's Firebase ID.
     * @return The post details, or null if the post is not found.
     */
    suspend fun getPostDetailsFromLocal(fireBaseId: String): List<PostBean>

    /**
     * Retrieves post details with associated user information from the local database.
     *
     * This function fetches post details along with user information from the local database,
     * excluding posts from users in the logged-in user's block list.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param loggedInUserBlockedList List of Firebase IDs representing users blocked by the logged-in user.
     * @return A [ResponseState] containing a list of [PostWithUserDetailsBean] objects if successful,
     *         or an error message if the operation fails.
     */
    suspend fun getPostDetailsWithUsersFromLocal(
        loggedInUserFirebaseId: String,
        loggedInUserBlockedList: List<String>
    ): ResponseState<List<PostWithUserDetailsBean>>

    /**
     * Gets post details from remote.
     *
     * @param userFirebaseId The post's Firebase ID.
     * @return A response state containing the post details, or an error if the request failed.
     */
    suspend fun getPostDetailsFromRemote(
        userFirebaseId: String,
        loggedInUserFirebaseId: String
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

    /**
     * Retrieves all posts with user details from the remote server.
     *
     * This function fetches all posts along with their associated user details from the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user, used for authentication and filtering posts.
     * @return A [ResponseState] representing the result of the operation. If successful, contains a list of [PostWithUserDetailsBean] objects;
     * otherwise, contains an error message.
     */
    suspend fun getAllPostsWithUserDetailsFromRemote(
        loggedInUserFirebaseId: String
    ): ResponseState<List<PostWithUserDetailsBean>>

    /**
     * Adds a like on a post on the remote server.
     *
     * This function sends a request to add a like on the specified post to the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user who is adding the like.
     * @param postFirebaseId The Firebase ID of the post to which the like is being added.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     * otherwise, contains an error message.
     */
    suspend fun addLikeOnPostOnRemote(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Removes a like from a post on the remote server.
     *
     * This function sends a request to remove a like from the specified post on the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user who is removing the like.
     * @param postFirebaseId The Firebase ID of the post from which the like is being removed.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     * otherwise, contains an error message.
     */
    suspend fun removeLikeOfPostFromRemote(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Retrieves saved posts with associated user details from the remote server.
     *
     * This function fetches posts saved by the logged-in user along with their associated user details from the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param savedPosts A list of Firebase IDs representing posts saved by the user.
     * @return A [ResponseState] containing a list of [PostWithUserDetailsBean] objects if successful,
     *         or an error message if the operation fails.
     */
    suspend fun getSavedPostsWithUsersFromRemote(
        loggedInUserFirebaseId: String,
        savedPosts: ArrayList<String>
    ): ResponseState<List<PostWithUserDetailsBean>>

    /**
     * Deletes a post from the remote server.
     *
     * This function sends a request to delete the post with the specified Firebase ID from the remote server.
     *
     * @param postFirebaseId The Firebase ID of the post to be deleted.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     * otherwise, contains an error message.
     */
    suspend fun deletePostFromRemote(postFirebaseId: String): ResponseState<Nothing>

    /**
     * Adds a comment on the remote server.
     *
     * This function sends a request to add the provided comment to the remote server.
     *
     * @param comment The comment to be added.
     * @return A [ResponseState] representing the result of the operation. If successful, returns the ID of the added comment;
     * otherwise, contains an error message.
     */
    suspend fun addCommentOnRemote(comment: CommentBean): ResponseState<String>

    /**
     * Deletes a comment on the remote server.
     *
     * This function sends a request to delete the comment with the specified ID from the remote server.
     *
     * @param commentId The ID of the comment to be deleted.
     * @param postFirebaseId The Firebase ID of the post to which the comment belongs.
     * @param deleteCount The number of times the comment has been deleted. This parameter might be used for tracking purposes.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     * otherwise, contains an error message.
     */
    suspend fun deleteCommentOnRemote(
        commentId: String,
        postFirebaseId: String,
        deleteCount: Int
    ): ResponseState<Nothing>

    /**
     * Retrieves all comments with associated user details for a post from the remote server.
     *
     * This function fetches all comments for the specified post along with their associated user details from the remote server.
     *
     * @param postFirebaseId The Firebase ID of the post for which comments are being fetched.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing a mutable map where the key is a [CommentWithUserBean] representing
     *         the main comment and the value is an array list of [CommentWithUserBean] representing its replies.
     *         If successful, returns the map of comments with associated user details; otherwise, contains an error message.
     */
    suspend fun getAllCommentsWithUsersFromRemote(
        postFirebaseId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<MutableMap<CommentWithUserBean, ArrayList<CommentWithUserBean>>>

    /**
     * Adds a like for a comment on the remote server.
     *
     * This function sends a request to add a like for the specified comment on the remote server.
     *
     * @param commentId The ID of the comment for which the like is being added.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user who is adding the like.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     * otherwise, contains an error message.
     */
    suspend fun addLikeForCommentOnRemote(
        commentId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Removes a like for a comment on the remote server.
     *
     * This function sends a request to remove a like for the specified comment on the remote server.
     *
     * @param commentId The ID of the comment for which the like is being removed.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user who is removing the like.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     * otherwise, contains an error message.
     */
    suspend fun removeLikeForCommentFromRemote(
        commentId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<Nothing>

    /**
     * Retrieves post details with associated user information from the local database.
     *
     * This function fetches post details along with user information from the local database,
     * excluding posts from users in the logged-in user's block list and filtered by a list of saved post Firebase IDs.
     *
     * @param savedPostFirebaseIds List of Firebase IDs representing posts saved by the user.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param loggedInUserBlockedList List of Firebase IDs representing users blocked by the logged-in user.
     * @return A [ResponseState] containing a list of [PostWithUserDetailsBean] objects if successful,
     *         or an error message if the operation fails.
     */
    suspend fun getPostDetailsWithUserFromLocal(
        savedPostFirebaseIds: List<String>, loggedInUserFirebaseId: String,
        loggedInUserBlockedList: List<String>,
    ): ResponseState<List<PostWithUserDetailsBean>>

    /**
     * Updates the details of a post in the local database.
     *
     * This function updates the details of a post in the local database with the provided post details.
     *
     * @param postDetails The details of the post to be updated.
     * @return The number of posts updated. Typically, this would be 1 if the post was successfully updated,
     *         and 0 if no post with the given details was found.
     */
    suspend fun updatePostDetailsOnLocal(postDetails: PostBean): Int

    /**
     * Updates the visibility of a post on the remote server.
     *
     * This function sends a request to update the visibility scope of the specified post on the remote server.
     *
     * @param postFirebaseId The Firebase ID of the post whose visibility is being updated.
     * @param postScopeName The new visibility scope for the post.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     * otherwise, contains an error message.
     */
    suspend fun updatePostVisibilityOnRemote(
        postFirebaseId: String,
        postScopeName: String
    ): ResponseState<Nothing>

    /**
     * Deletes a post from the local database.
     *
     * This function deletes a post identified by its Firebase ID from the local database.
     * Note: This function assumes that the post exists in the local database.
     *
     * @param postFirebaseId The Firebase ID of the post to be deleted.
     * @return The number of posts deleted. Typically, this would be 1 if the post was successfully deleted,
     *         and 0 if no post with the given Firebase ID was found.
     */
    suspend fun deletePostFromLocal(postFirebaseId: String): Int

    /**
     * Deletes all posts from the local database.
     *
     * This function deletes all posts stored in the local database.
     *
     * @return The number of posts deleted.
     */
    suspend fun deleteAllPostFomLocal(): Int

    /**
     * Deletes all posts of a user from the local database.
     *
     * This function deletes all posts associated with the specified user from the local database.
     *
     * @param userFirebaseId The Firebase ID of the user whose posts are to be deleted.
     * @return The number of posts deleted.
     */
    suspend fun deleteAllPostOfUserFromLocal(userFirebaseId: String): Int

    /**
     * Deletes all posts of a user with 'Friends Only' visibility from the local database.
     *
     * This function deletes all posts associated with the specified user that have 'Friends Only'
     * visibility from the local database.
     *
     * @param userFirebaseId The Firebase ID of the user whose posts with 'Friends Only' visibility
     *                       are to be deleted.
     * @return The number of posts deleted.
     */
    suspend fun deleteAllPostOfUserWithFriendsOnlyVisibilityFromLocal(userFirebaseId: String): Int
}