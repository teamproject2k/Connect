package com.example.connect.data.repository

import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.comment.CommentRemoteEntity
import com.example.connect.data.models.post.PostRemoteEntity
import com.example.connect.data.models.post.PostWithUserDetailsFromLocalEntity
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.enums.StatusWithCurrentUserRemoteEnum
import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.models.CommentWithUser
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.PostWithUserDetails
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.domain.utils.VisibilityScopeEnum
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IPostRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val appDatabase: AppDatabase
) : IPostRepository {
    override suspend fun getPostDetailsFromLocal(fireBaseId: String): List<PostBean> {
        // Get the post details from the local database.
        return appDatabase.getPostDao().getPostList(fireBaseId).map { it.toPostBean() }
    }

    override suspend fun getPostDetailsWithUsersFromLocal(
        loggedInUserFirebaseId: String,
        loggedInUserBlockedList: List<String>
    ): ResponseState<List<PostWithUserDetails>> {
        return try {
            ResponseState.success(
                getPostDetailsWithUsers(
                    appDatabase.getPostDao().getPostDetailsWithUsers(),
                    loggedInUserFirebaseId,
                    loggedInUserBlockedList
                )
            )
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getPostDetailsFromRemote(
        userFirebaseId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<List<PostBean>> {
        // Get the post details from the server.
        val postList = arrayListOf<PostBean>()
        try {
            if (userFirebaseId == loggedInUserFirebaseId) {
                val postListDocument = fireStore.collection(FirebaseConstants.POST_KEY)
                    .whereEqualTo(PostRemoteEntity::createdByUserFirebaseId.name, userFirebaseId)
                    .get()
                    .await()
                val loggedInUserPostList = arrayListOf<PostBean>()
                postListDocument.forEach { postDocument ->
                    if (postDocument != null && postDocument.exists()) {
                        val postBean = postDocument.toObject(PostRemoteEntity::class.java)
                            .toPostBean(postDocument.id)
                        if (!postBean.whetherDeleted) {
                            loggedInUserPostList.add(postBean)
                        }
                    }
                }
                return ResponseState.success(postList)
            } else {
                val loggedInUserAndOtherUserDocument =
                    fireStore.collection(FirebaseConstants.USER_KEY).whereIn(
                        UserRemoteEntity::firebaseUserId.name,
                        listOf(userFirebaseId, loggedInUserFirebaseId)
                    ).get().await().map { userDocument ->
                        userDocument.toObject(UserRemoteEntity::class.java)
                    }

                if (loggedInUserAndOtherUserDocument.size == 2) {
                    val otherUserPostList = arrayListOf<PostBean>()
                    val postListDocument = fireStore.collection(FirebaseConstants.POST_KEY)
                        .whereEqualTo(
                            PostRemoteEntity::createdByUserFirebaseId.name,
                            userFirebaseId
                        )
                        .get()
                        .await()
                    postListDocument.forEach { postDocument ->
                        if (postDocument != null && postDocument.exists()) {
                            val postBean = postDocument.toObject(PostRemoteEntity::class.java)
                                .toPostBean(postDocument.id)
                            if (
                                !postBean.whetherDeleted &&
                                loggedInUserAndOtherUserDocument[0].otherUsersStatus[loggedInUserAndOtherUserDocument[1].firebaseUserId] != StatusWithCurrentUserRemoteEnum.Blocked.name &&
                                loggedInUserAndOtherUserDocument[1].otherUsersStatus[loggedInUserAndOtherUserDocument[0].firebaseUserId] != StatusWithCurrentUserRemoteEnum.Blocked.name
                            ) {
                                otherUserPostList.add(postBean)
                            }
                        }
                    }
                    return ResponseState.success(otherUserPostList)
                } else {
                    return ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }
        } catch (exception: Exception) {
            return ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addPostToLocal(postDetails: PostBean): Long {
        // Add the post details to the local database.
        return appDatabase.getPostDao().insertPost(postDetails.toPostDbEntity())
    }

    override suspend fun addPostListToLocal(postDetailList: List<PostBean>): LongArray {
        // Add the post details to the local database.
        return appDatabase.getPostDao().insertPostList(postDetailList.map { it.toPostDbEntity() })
    }

    override suspend fun uploadPostToRemote(
        postDetails: PostBean,
        fireBaseId: String
    ): ResponseState<String> {
        // Upload the post details to the server.
        return try {
            val response = fireStore.collection(FirebaseConstants.POST_KEY)
                .add(postDetails.toPostRemoteEntity())
                .await()
            ResponseState.success(response.id)
        } catch (exception: Exception) {
            // An error occurred while uploading the post details to the server.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }


    private suspend fun getPostDetailsWithUserDetailsFromRemote(
        loggedInUserFirebaseId: String,
        postListToFetch: List<String>?
    ): ResponseState<List<PostWithUserDetails>> {
        val postsWithUsersList = arrayListOf<PostWithUserDetails>()
        val postList = arrayListOf<PostBean>()
        val usersList = arrayListOf<UsersBean>()
        val loggedInUser: UserRemoteEntity?
        return try {
            val loggedInUserDocument =
                fireStore.collection(FirebaseConstants.USER_KEY).document(loggedInUserFirebaseId)
                    .get().await()
            loggedInUser =
                loggedInUserDocument.toObject(UserRemoteEntity::class.java)
            if (loggedInUser != null) {
                usersList.add(loggedInUser.toUserBean())
            }
            val baseCondition = fireStore.collection(FirebaseConstants.POST_KEY)
                .whereEqualTo(PostRemoteEntity::whetherDeleted.name, false)
            val postListQuery = if (postListToFetch == null) {
                baseCondition
            } else {
                baseCondition
                    .whereIn(FieldPath.documentId(), postListToFetch)
            }
            val postListResponse = postListQuery.get().await()
            postListResponse.forEach { postDocument ->
                val post = postDocument.toObject(PostRemoteEntity::class.java)
                if (postDocument != null && postDocument.exists() && loggedInUser?.otherUsersStatus?.get(
                        post.createdByUserFirebaseId
                    ) != StatusWithCurrentUserRemoteEnum.Blocked.name
                ) {
                    postList.add(post.toPostBean(postDocument.id))
                }
            }
            postList.sortByDescending { it.createdAt }
            postList.forEach { post ->
                var postedByUser =
                    usersList.find { it.firebaseUserId == post.createdByUserFirebaseId }
                if (postedByUser == null) {
                    val postedByUserDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                        .document(post.createdByUserFirebaseId)
                        .get().await()
                    postedByUser =
                        postedByUserDocument.toObject(UserRemoteEntity::class.java)?.toUserBean()
                    if (postedByUser != null) {
                        usersList.add(postedByUser)
                    }
                }
                if (postedByUser != null && !postedByUser.blockedUsersList.contains(
                        loggedInUserFirebaseId
                    )
                ) {
                    val whetherPostVisibleToLoggedInUser =
                        post.createdByUserFirebaseId == loggedInUser?.firebaseUserId ||
                                post.postVisibilityScope == VisibilityScopeEnum.Public.name ||
                                postedByUser.friendList.contains(loggedInUserFirebaseId)
                    if (whetherPostVisibleToLoggedInUser) {
                        postsWithUsersList.add(PostWithUserDetails(post, postedByUser))
                    }
                }
            }
            ResponseState.success(postsWithUsersList)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getAllPostsWithUserDetailsFromRemote(loggedInUserFirebaseId: String): ResponseState<List<PostWithUserDetails>> {
        return getPostDetailsWithUserDetailsFromRemote(loggedInUserFirebaseId, null)
    }


    private suspend fun addOrRemoveLikeOnPost(
        loggedInUserFirebaseId: String,
        postFirebaseId: String,
        whetherAddLike: Boolean
    ): ResponseState<Nothing> {
        try {
            val currentPostDocument =
                fireStore.collection(FirebaseConstants.POST_KEY).document(postFirebaseId).get()
                    .await()
            val postEntity = currentPostDocument.toObject(PostRemoteEntity::class.java)
            if (postEntity != null && !postEntity.whetherDeleted) {
                val postedByUserDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                    .document(postEntity.createdByUserFirebaseId).get().await()
                val postedByUserEntity =
                    postedByUserDocument.toObject(UserRemoteEntity::class.java)
                return if (postedByUserEntity?.otherUsersStatus?.get(loggedInUserFirebaseId) != StatusWithCurrentUserRemoteEnum.Blocked.name) {
                    fireStore.collection(FirebaseConstants.POST_KEY)
                        .document(postFirebaseId)
                        .update(
                            PostRemoteEntity::likedBy.name,
                            if (whetherAddLike) FieldValue.arrayUnion(loggedInUserFirebaseId)
                            else FieldValue.arrayRemove(loggedInUserFirebaseId)
                        )
                        .await()
                    ResponseState.success(null)
                } else {
                    return ResponseState.error(FirebaseErrorCodes.POST_NOT_FOUND)
                }
            } else {
                return ResponseState.error(FirebaseErrorCodes.POST_NOT_FOUND)
            }
        } catch (exception: Exception) {
            return ResponseState.error(exception.localizedMessage ?: "")
        }
    }


    /**
     * Adds a like to a post on the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param postFirebaseId The Firebase ID of the post.
     * @return A [ResponseState] object indicating the success or failure of the operation.
     * Errors :-
     * gives POST_NOT_FOUND error if the post is deleted or posted by user blocked the logged in user
     * and other firebase errors if any
     */
    override suspend fun addLikeOnPostOnRemote(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return addOrRemoveLikeOnPost(
            loggedInUserFirebaseId = loggedInUserFirebaseId,
            postFirebaseId = postFirebaseId,
            whetherAddLike = true
        )
    }

    /**
     * removes  like from the post on the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param postFirebaseId The Firebase ID of the post.
     * @return A [ResponseState] object indicating the success or failure of the operation.
     * Errors :-
     * gives POST_NOT_FOUND error if the post is deleted or posted by user blocked the logged in user
     * and other firebase errors if any
     */
    override suspend fun removeLikeOfPostFromRemote(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return addOrRemoveLikeOnPost(
            loggedInUserFirebaseId = loggedInUserFirebaseId,
            postFirebaseId = postFirebaseId,
            whetherAddLike = false
        )
    }

    override suspend fun getSavedPostsWithUsersFromRemote(
        loggedInUserFirebaseId: String,
        savedPosts: ArrayList<String>
    ): ResponseState<List<PostWithUserDetails>> {
        return getPostDetailsWithUserDetailsFromRemote(loggedInUserFirebaseId, savedPosts)
    }

    override suspend fun deletePostFromRemote(postFirebaseId: String): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.POST_KEY).document(postFirebaseId)
                .update(PostRemoteEntity::whetherDeleted.name, true).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            // An error occurred while deleting the post from remote.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addCommentOnRemote(comment: CommentBean): ResponseState<String> {
        val currentPostDocument =
            fireStore.collection(FirebaseConstants.POST_KEY).document(comment.postFirebaseId).get()
                .await()
        if (currentPostDocument.exists()) {
            val postEntity = currentPostDocument.toObject(PostRemoteEntity::class.java)
            if (postEntity != null && !postEntity.whetherDeleted) {
                val postedByUserDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                    .document(postEntity.createdByUserFirebaseId).get().await()
                if (postedByUserDocument.exists()) {
                    val postedByUserEntity =
                        postedByUserDocument.toObject(UserRemoteEntity::class.java)
                    return if (postedByUserEntity?.otherUsersStatus?.get(comment.commentedBy) != StatusWithCurrentUserRemoteEnum.Blocked.name) {
                        fireStore.runTransaction { transaction ->
                            val postDocument =
                                fireStore.collection(FirebaseConstants.POST_KEY)
                                    .document(comment.postFirebaseId)
                            transaction.update(
                                postDocument,
                                PostRemoteEntity::commentCount.name,
                                FieldValue.increment(1)
                            )
                            val addCommentDocumentRef =
                                fireStore.collection(FirebaseConstants.COMMENT_KEY).document()
                            transaction.set(
                                addCommentDocumentRef,
                                comment.toCommentRemoteEntity()
                            )
                            ResponseState.success(addCommentDocumentRef.id)
                        }.await()
                    } else {
                        ResponseState.error(FirebaseErrorCodes.POST_NOT_FOUND)
                    }
                } else {
                    return ResponseState.error(FirebaseErrorCodes.POST_NOT_FOUND)
                }
            } else {
                return ResponseState.error(FirebaseErrorCodes.POST_NOT_FOUND)
            }
        } else {
            return ResponseState.error(FirebaseErrorCodes.POST_NOT_FOUND)
        }
    }

    override suspend fun deleteCommentOnRemote(
        commentId: String,
        postFirebaseId: String,
        deleteCount: Int
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                val postDocument =
                    fireStore.collection(FirebaseConstants.POST_KEY).document(postFirebaseId)
                transaction.update(
                    postDocument,
                    PostRemoteEntity::commentCount.name,
                    FieldValue.increment(-(deleteCount).toLong())
                )
                val deleteCommentDocumentRef =
                    fireStore.collection(FirebaseConstants.COMMENT_KEY).document(commentId)
                transaction.update(
                    deleteCommentDocumentRef,
                    CommentRemoteEntity::whetherDeleted.name,
                    true
                )
                ResponseState.success(null)
            }.await()
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getAllCommentsWithUsersFromRemote(
        postFirebaseId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<MutableMap<CommentWithUser, ArrayList<CommentWithUser>>> {
        return try {
            val commentListResponse = fireStore.collection(FirebaseConstants.COMMENT_KEY)
                .whereEqualTo(CommentRemoteEntity::postFirebaseId.name, postFirebaseId)
                .whereEqualTo(CommentRemoteEntity::whetherDeleted.name, false)
                .get()
                .await()
            val parentChildMap = mutableMapOf<CommentWithUser, ArrayList<CommentWithUser>>()
            val commentList = arrayListOf<CommentBean>()
            val userList = arrayListOf<UsersBean>()
            val loggedInUserDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                .document(loggedInUserFirebaseId).get().await()
            val loggedInUser = loggedInUserDocument.toObject(UserRemoteEntity::class.java)
            if (loggedInUserDocument != null && loggedInUserDocument.exists() && loggedInUser != null) {
                userList.add(loggedInUser.toUserBean())
                commentListResponse.documents.forEach { document ->
                    if (document.exists()) {
                        val comment = document.toObject(CommentRemoteEntity::class.java)
                        if (comment != null && loggedInUser.otherUsersStatus[comment.commentedBy] != StatusWithCurrentUserRemoteEnum.Blocked.name) {
                            val commentedByUserDetails =
                                userList.find { user -> user.firebaseUserId == comment.commentedBy }
                            if (commentedByUserDetails == null) {
                                val userDocument =
                                    fireStore.collection(FirebaseConstants.USER_KEY)
                                        .document(comment.commentedBy).get().await()
                                val user = userDocument.toObject(UserRemoteEntity::class.java)
                                if (user != null) {
                                    userList.add(user.toUserBean())
                                    if (user.otherUsersStatus[loggedInUserFirebaseId] != StatusWithCurrentUserRemoteEnum.Blocked.name) {
                                        commentList.add(comment.toCommentBean(document.id))
                                    }
                                }
                            } else {
                                val user =
                                    userList.find { user -> user.firebaseUserId == comment.commentedBy }
                                if (user != null) {
                                    if (!user.blockedUsersList.contains(loggedInUserFirebaseId)) {
                                        commentList.add(comment.toCommentBean(document.id))
                                    }
                                }
                            }
                        }
                    }
                }
                commentList.sortBy { it.createdAt }
                val parentCommentList =
                    commentList.filter { comment -> comment.parentCommentId == null }
                        .sortedByDescending { it.createdAt }
                parentCommentList.forEach { parentComment ->
                    val parentUser =
                        userList.find { user -> user.firebaseUserId == parentComment.commentedBy }
                    val childCommentsList =
                        commentList.filter { comment -> comment.parentCommentId == parentComment.commentFirebaseId } as ArrayList
                    if (parentUser != null) {
                        val childCommentListWithUsers = arrayListOf<CommentWithUser>()
                        childCommentsList.forEach { comment ->
                            val childCommentUser =
                                userList.find { user -> user.firebaseUserId == comment.commentedBy }
                            val commentedOnConnectId =
                                userList.find { user -> user.firebaseUserId == comment.repliedOnUserId }?.connectUserId
                            if (childCommentUser != null) {
                                childCommentListWithUsers.add(
                                    CommentWithUser(
                                        comment,
                                        childCommentUser,
                                        commentedOnConnectId
                                    )
                                )
                            }
                        }
                        parentChildMap[CommentWithUser(parentComment, parentUser)] =
                            childCommentListWithUsers
                    }
                }
                ResponseState.success(parentChildMap)
            } else {
                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }


    override suspend fun addLikeForCommentOnRemote(
        commentId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.COMMENT_KEY).document(commentId).update(
                CommentRemoteEntity::likedBy.name,
                FieldValue.arrayUnion(loggedInUserFirebaseId)
            ).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun removeLikeForCommentFromRemote(
        commentId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.COMMENT_KEY).document(commentId).update(
                CommentRemoteEntity::likedBy.name,
                FieldValue.arrayRemove(loggedInUserFirebaseId)
            ).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getPostDetailsWithUserFromLocal(
        savedPostFirebaseIds: List<String>,
        loggedInUserFirebaseId: String,
        loggedInUserBlockedList: List<String>,
    ): ResponseState<List<PostWithUserDetails>> {
        return try {
            ResponseState.success(
                getPostDetailsWithUsers(
                    appDatabase.getPostDao().getSavedPostsAndUsers(savedPostFirebaseIds),
                    loggedInUserFirebaseId,
                    loggedInUserBlockedList
                )
            )
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    private fun getPostDetailsWithUsers(
        postWithUsersList: List<PostWithUserDetailsFromLocalEntity>,
        loggedInUserFirebaseId: String,
        loggedInUserBlockedList: List<String>
    ): ArrayList<PostWithUserDetails> {
        val postWithUsersDetailList = arrayListOf<PostWithUserDetails>()
        postWithUsersList.forEach { postWithUser ->
            val postedBy = postWithUser.userDetail?.toUserBean()
            if (postedBy != null && !loggedInUserBlockedList.contains(postedBy.firebaseUserId) && !postedBy.blockedUsersList.contains(
                    loggedInUserFirebaseId
                )
            ) {
                val whetherShowPostToLoggedInUser =
                    postWithUser.postDetail.createdByUserFirebaseId == loggedInUserFirebaseId
                            || postWithUser.postDetail.postVisibilityScope == VisibilityScopeEnum.Public.name
                            || (postedBy.friendList.contains(loggedInUserFirebaseId))
                if (whetherShowPostToLoggedInUser) {
                    postWithUsersDetailList.add(
                        PostWithUserDetails(
                            postWithUser.postDetail.toPostBean(),
                            postedBy
                        )
                    )
                }
            }
        }
        return postWithUsersDetailList
    }

    override suspend fun updatePostDetailsOnLocal(postDetails: PostBean): Int {
        return appDatabase.getPostDao().updatePostDetails(postDetails.toPostDbEntity())
    }

    override suspend fun updatePostVisibilityOnRemote(
        postFirebaseId: String,
        postScopeName: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.POST_KEY).document(postFirebaseId).update(
                PostRemoteEntity::postVisibilityScope.name, postScopeName
            ).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun deletePostFromLocal(postFirebaseId: String): Int {
        return appDatabase.getPostDao().deletePost(postFirebaseId)
    }

    override suspend fun deleteAllPostFomLocal(): Int {
        return appDatabase.getPostDao().deleteAllPosts()
    }

    override suspend fun deleteAllPostOfUserFromLocal(userFirebaseId: String): Int {
        return appDatabase.getPostDao().deleteAllPostOfUser(userFirebaseId)
    }

    override suspend fun deleteAllPostOfUserWithFriendsOnlyVisibilityFromLocal(userFirebaseId: String): Int {
        return appDatabase.getPostDao().deleteOnlyFriendsOnlyPostOfUser(userFirebaseId)
    }
}