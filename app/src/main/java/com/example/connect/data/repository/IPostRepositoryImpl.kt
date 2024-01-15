package com.example.connect.data.repository

import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.comment.CommentRemoteEntity
import com.example.connect.data.models.post.PostRemoteEntity
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.enums.StatusWithCurrentUserRemoteEnum
import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.PostWithUserDetails
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.domain.utils.FirebaseErrorCodes
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

    override suspend fun getPostDetailsWithUsersFromLocal(): ResponseState<List<PostWithUserDetails>> {
        val postWithUsersDetailList = arrayListOf<PostWithUserDetails>()
        return try {
            val postWithUsersList = appDatabase.getPostDao().getPostDetailsWithUsers()
            postWithUsersList.forEach { postWithUser ->
                if (postWithUser.userDetail != null) {
                    postWithUsersDetailList.add(
                        PostWithUserDetails(
                            postWithUser.postDetail.toPostBean(),
                            postWithUser.userDetail.toUserBean()
                        )
                    )
                }
            }
            ResponseState.success(postWithUsersDetailList)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getPostDetailsFromRemote(
        fireBaseId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<List<PostBean>> {
        // Get the post details from the server.
        return try {
            val response = fireStore.collection(FirebaseConstants.POST_KEY)
                .whereEqualTo(PostRemoteEntity::createdByUserFirebaseId.name, fireBaseId).get()
                .await()
            val postList = arrayListOf<PostBean>()
            val currentUserDocument =
                fireStore.collection(FirebaseConstants.USER_KEY).document(loggedInUserFirebaseId)
                    .get().await()
            val currentUser = if (currentUserDocument != null && currentUserDocument.exists()) {
                currentUserDocument.toObject(UserRemoteEntity::class.java)
            } else {
                null
            }
            response.documents.forEach { document ->
                val post = document.toObject(PostRemoteEntity::class.java)
                if (post != null && !post.whetherDeleted) {
                    postList.add(
                        post.toPostBean(
                            document.id,
                            currentUser?.savedPosts?.contains(document.id) ?: false
                        )
                    )
                }
            }
            postList.sortByDescending { it.createdAt }
            ResponseState.success(postList)
        } catch (exception: Exception) {
            // An error occurred while getting the post details from the server.
            ResponseState.error(exception.localizedMessage ?: "")
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
        var currentUser: UserRemoteEntity? = null
        return try {
            val currentUserDocument =
                fireStore.collection(FirebaseConstants.USER_KEY).document(loggedInUserFirebaseId)
                    .get().await()
            if (currentUserDocument != null && currentUserDocument.exists()) {
                currentUser =
                    currentUserDocument.toObject(UserRemoteEntity::class.java)
                if (currentUser != null) {
                    usersList.add(currentUser.toUserBean())
                }
            }
            val postListQuery = if (postListToFetch == null) {
                fireStore.collection(FirebaseConstants.POST_KEY)
            } else {
                fireStore.collection(FirebaseConstants.POST_KEY).whereIn(
                    FieldPath.documentId(), postListToFetch
                )
            }
            val postListResponse = postListQuery.get().await()
            postListResponse.forEach { postDocument ->
                if (postDocument != null && postDocument.exists()) {
                    val post = postDocument.toObject(PostRemoteEntity::class.java)
                    if (!post.whetherDeleted && currentUser?.otherUsersStatus?.get(post.createdByUserFirebaseId) != StatusWithCurrentUserRemoteEnum.Blocked.name) {
                        postList.add(
                            post.toPostBean(
                                postDocument.id,
                                currentUser?.savedPosts?.contains(postDocument.id) ?: false
                            )
                        )
                    }
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
                    if (postedByUserDocument != null && postedByUserDocument.exists()) {
                        postedByUser = postedByUserDocument.toObject(UserRemoteEntity::class.java)
                            ?.toUserBean()
                        if (postedByUser != null) {
                            usersList.add(postedByUser)
                        }
                    }
                }
                if (postedByUser != null && !postedByUser.blockedUsersList.contains(
                        loggedInUserFirebaseId
                    )
                ) {
                    postsWithUsersList.add(PostWithUserDetails(post, postedByUser))
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

    override suspend fun addLikeOnPost(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        try {
            val currentPostDocument =
                fireStore.collection(FirebaseConstants.POST_KEY).document(postFirebaseId).get()
                    .await()
            if (currentPostDocument.exists()) {
                val postEntity = currentPostDocument.toObject(PostRemoteEntity::class.java)
                if (postEntity != null && !postEntity.whetherDeleted) {
                    val postedByUserDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                        .document(postEntity.createdByUserFirebaseId).get().await()
                    if (postedByUserDocument.exists()) {
                        val postedByUserEntity =
                            postedByUserDocument.toObject(UserRemoteEntity::class.java)
                        return if (postedByUserEntity?.otherUsersStatus?.get(loggedInUserFirebaseId) != StatusWithCurrentUserRemoteEnum.Blocked.name) {
                            fireStore.collection(FirebaseConstants.POST_KEY)
                                .document(postFirebaseId)
                                .update(
                                    PostRemoteEntity::likedBy.name,
                                    FieldValue.arrayUnion(loggedInUserFirebaseId)
                                )
                                .await()
                            ResponseState.success(null)
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
        } catch (exception: Exception) {
            return ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun removeLikeOf(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        try {
            val currentPostDocument =
                fireStore.collection(FirebaseConstants.POST_KEY).document(postFirebaseId).get()
                    .await()
            if (currentPostDocument.exists()) {
                val postEntity = currentPostDocument.toObject(PostRemoteEntity::class.java)
                if (postEntity != null && !postEntity.whetherDeleted) {
                    val postedByUserDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                        .document(postEntity.createdByUserFirebaseId).get().await()
                    if (postedByUserDocument.exists()) {
                        val postedByUserEntity =
                            postedByUserDocument.toObject(UserRemoteEntity::class.java)
                        return if (postedByUserEntity?.otherUsersStatus?.get(loggedInUserFirebaseId) != StatusWithCurrentUserRemoteEnum.Blocked.name) {
                            fireStore.collection(FirebaseConstants.POST_KEY)
                                .document(postFirebaseId)
                                .update(
                                    PostRemoteEntity::likedBy.name,
                                    FieldValue.arrayRemove(loggedInUserFirebaseId)
                                )
                                .await()
                            ResponseState.success(null)
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
        } catch (exception: Exception) {
            return ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getSavedPostsWithUsersFromRemote(
        loggedInUserFirebaseId: String,
        savedPosts: ArrayList<String>
    ): ResponseState<List<PostWithUserDetails>> {
        return getPostDetailsWithUserDetailsFromRemote(loggedInUserFirebaseId, savedPosts)
    }

    override suspend fun deletePostFromRemote(postId: String): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.POST_KEY).document(postId)
                .update(PostRemoteEntity::whetherDeleted.name, true).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            // An error occurred while deleting the post from remote.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addCommentOnRemote(comment: CommentBean): ResponseState<String> {
        return try {
            fireStore.runTransaction { transaction ->
                val postDocument =
                    fireStore.collection(FirebaseConstants.POST_KEY).document(comment.postId)
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
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun deleteCommentOnRemote(
        commentId: String,
        postId: String,
        deleteCount: Int
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                val postDocument =
                    fireStore.collection(FirebaseConstants.POST_KEY).document(postId)
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
        postId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<Pair<MutableMap<CommentBean, ArrayList<CommentBean>>, List<UsersBean>>> {
        return try {
            val commentListResponse = fireStore.collection(FirebaseConstants.COMMENT_KEY)
                .whereEqualTo(CommentRemoteEntity::postId.name, postId)
                .get()
                .await()
            val commentList = arrayListOf<CommentBean>()
            val userList = arrayListOf<UsersBean>()
            val loggedInUserDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                .document(loggedInUserFirebaseId).get().await()
            val loggedInUser = loggedInUserDocument.toObject(UserRemoteEntity::class.java)
            val usersToRemoveFromList = arrayListOf<String>()
            if (loggedInUserDocument != null && loggedInUserDocument.exists() && loggedInUser != null) {
                userList.add(loggedInUser.toUserBean())
                commentListResponse.documents.forEach { document ->
                    if (document.exists()) {
                        val comment = document.toObject(CommentRemoteEntity::class.java)
                        if (comment != null && !comment.whetherDeleted && loggedInUser.otherUsersStatus[comment.commentedBy] != StatusWithCurrentUserRemoteEnum.Blocked.name) {
                            val isUserDetailsAlreadyFetched =
                                userList.find { user -> user.firebaseUserId == comment.commentedBy } != null
                            if (!isUserDetailsAlreadyFetched) {
                                val userDocument =
                                    fireStore.collection(FirebaseConstants.USER_KEY)
                                        .document(comment.commentedBy).get().await()
                                val user = userDocument.toObject(UserRemoteEntity::class.java)
                                if (user != null) {
                                    userList.add(user.toUserBean())
                                    if (user.otherUsersStatus[loggedInUserFirebaseId] != StatusWithCurrentUserRemoteEnum.Blocked.name) {
                                        commentList.add(comment.toCommentBean(document.id))
                                    } else {
                                        usersToRemoveFromList.add(user.firebaseUserId)
                                    }
                                }
                            } else {
                                val user =
                                    userList.find { user -> user.firebaseUserId == comment.commentedBy }
                                if (user != null) {
                                    if (!user.blockedUsersList.contains(loggedInUserFirebaseId)) {
                                        commentList.add(comment.toCommentBean(document.id))
                                    } else {
                                        usersToRemoveFromList.add(user.firebaseUserId)
                                    }
                                }
                            }
                        }
                    }
                }
                commentList.sortBy { it.createdAt }
                val parentCommentList =
                    commentList.filter { comment -> comment.parentCommentId == null }
                val parentChildMap = mutableMapOf<CommentBean, ArrayList<CommentBean>>()
                parentCommentList.forEach { parentComment ->
                    val childCommentsList =
                        commentList.filter { comment -> comment.parentCommentId == parentComment.commentFirebaseId } as ArrayList
                    parentChildMap[parentComment] = childCommentsList
                }
                userList.removeAll { user -> usersToRemoveFromList.contains(user.firebaseUserId) }
                ResponseState.success(Pair(parentChildMap, userList))
            } else {
                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addLikeForComment(
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

    override suspend fun removeLikeForComment(
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

    override suspend fun getPostWithUserFromLocal(savedPostIds: List<String>): ResponseState<List<PostWithUserDetails>> {
        val postWithUsersDetailList = arrayListOf<PostWithUserDetails>()
        return try {
            val postWithUsersList = appDatabase.getPostDao().getSavedPostsAndUsers(savedPostIds)
            postWithUsersList.forEach { postWithUser ->
                if (postWithUser.userDetail != null) {
                    postWithUsersDetailList.add(
                        PostWithUserDetails(
                            postWithUser.postDetail.toPostBean(),
                            postWithUser.userDetail.toUserBean()
                        )
                    )
                }
            }
            ResponseState.success(postWithUsersDetailList)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
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
}