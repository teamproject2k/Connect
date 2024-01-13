package com.example.connect.data.repository

import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.comment.CommentRemoteEntity
import com.example.connect.data.models.post.PostRemoteEntity
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.enums.StatusWithCurrentUserRemoteEnum
import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.domain.utils.VisibilityScopeEnum
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IPostRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val appDatabase: AppDatabase
) : IPostRepository {
    override suspend fun getPostDetailsFromDb(fireBaseId: String): List<PostBean> {
        // Get the post details from the local database.
        return appDatabase.getPostDao().getPostList(fireBaseId).map { it.toPostBean() }
    }

    override suspend fun getPostDetailsFromRemote(
        fireBaseId: String,
        currentUserFirebaseId: String
    ): ResponseState<List<PostBean>> {
        // Get the post details from the server.
        return try {
            val response = fireStore.collection(FirebaseConstants.POST_KEY)
                .whereEqualTo(PostRemoteEntity::createdByUserFirebaseId.name, fireBaseId).get()
                .await()
            val postList = arrayListOf<PostBean>()
            val currentUserDocument =
                fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
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

    override suspend fun addPostToDb(postDetails: PostBean): Long {
        // Add the post details to the local database.
        return appDatabase.getPostDao().insertPost(postDetails.toPostDbEntity())
    }

    override suspend fun addPostListToDb(postDetailList: List<PostBean>): LongArray {
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

    override suspend fun getAllPostsWithUserDetailsFromRemote(currentUserFirebaseId: String): ResponseState<Pair<List<PostBean>, List<UsersBean>>> {
        return try {
            val postListResponse = fireStore.collection(FirebaseConstants.POST_KEY)
                .orderBy(PostRemoteEntity::createdAt.name, Query.Direction.DESCENDING)
                .get().await()
            val postList = arrayListOf<PostBean>()
            val userList = arrayListOf<UsersBean>()
            val currentUserDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                .document(currentUserFirebaseId).get().await()
            val currentUser = currentUserDocument.toObject(UserRemoteEntity::class.java)
            if (currentUserDocument != null && currentUserDocument.exists() && currentUser != null) {
                userList.add(currentUser.toUserBean())
                postListResponse.documents.forEach { document ->
                    if (document.exists()) {
                        val post = document.toObject(PostRemoteEntity::class.java)
                        if (post != null && !post.whetherDeleted) {
                            postList.add(
                                post.toPostBean(
                                    document.id,
                                    currentUser.savedPosts.contains(document.id)
                                )
                            )
                        }
                    }
                }
                postList.forEach { post ->
                    val isUserPresent =
                        userList.find { it.firebaseUserId == post.createdByUserFirebaseId } != null
                    if (!isUserPresent) {
                        val user = fireStore.collection(FirebaseConstants.USER_KEY)
                            .document(post.createdByUserFirebaseId)
                            .get()
                            .await()
                        if (user.exists()) {
                            val userDetails = user.toObject(UserRemoteEntity::class.java)
                            if (userDetails != null) {
                                val whetherShowPost =
                                    (post.createdByUserFirebaseId == currentUser.firebaseUserId)
                                            || (post.postVisibilityScope == VisibilityScopeEnum.Public.name)
                                            || (post.postVisibilityScope == VisibilityScopeEnum.FriendsOnly.name && userDetails.otherUsersStatus[currentUserFirebaseId] == StatusWithCurrentUserRemoteEnum.Friends.name)
                                if (!whetherShowPost) {
                                    postList.remove(post)
                                }
                                userList.add(userDetails.toUserBean())
                            } else {
                                postList.remove(post)
                            }
                        } else {
                            postList.remove(post)
                        }
                    }
                }

                userList.forEach { user ->
                    val isPostPresentForUser =
                        postList.find { it.createdByUserFirebaseId == user.firebaseUserId } != null
                    if (!isPostPresentForUser) {
                        userList.remove(user)
                    }
                }
                ResponseState.success(Pair(postList, userList))
            } else {
                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addLikeOf(
        userFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.POST_KEY).document(postFirebaseId)
                .update(PostRemoteEntity::likedBy.name, FieldValue.arrayUnion(userFirebaseId))
                .await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun removeLikeOf(
        userFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.POST_KEY).document(postFirebaseId)
                .update(PostRemoteEntity::likedBy.name, FieldValue.arrayRemove(userFirebaseId))
                .await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getSavedPostsWithUsersFromRemote(
        loggedInUserFirebaseId: String,
        savedPosts: ArrayList<String>
    ): ResponseState<Pair<ArrayList<PostBean>, ArrayList<UsersBean>>> {
        // Get the post details from the server for the given list of IDs.
        return try {
            val response =
                fireStore.collection(FirebaseConstants.USER_KEY).document(loggedInUserFirebaseId)
                    .get().await()
            if (response != null && response.exists()) {
                val loggedInUser = response.toObject(UserRemoteEntity::class.java)
                if (loggedInUser != null) {
                    val postListDocument = fireStore.collection(FirebaseConstants.POST_KEY).whereIn(
                        FieldPath.documentId(), loggedInUser.savedPosts
                    ).get().await()
                    val postList = arrayListOf<PostBean>()
                    postListDocument.documents.forEach { document ->
                        document.toObject(PostRemoteEntity::class.java)
                            ?.let {
                                if (!it.whetherDeleted) {
                                    postList.add(it.toPostBean(document.id, true))
                                }
                            }
                    }
                    postList.sortByDescending { it.createdAt }

                    val userList = arrayListOf<UsersBean>()
                    postList.forEach { post ->
                        val isUserPresent =
                            userList.find { it.firebaseUserId == post.createdByUserFirebaseId } != null
                        if (!isUserPresent) {
                            val user = fireStore.collection(FirebaseConstants.USER_KEY)
                                .document(post.createdByUserFirebaseId)
                                .get()
                                .await()
                            if (user.exists()) {
                                val userDetails = user.toObject(UserRemoteEntity::class.java)
                                if (userDetails != null) {
                                    val whetherShowPost =
                                        (post.createdByUserFirebaseId == loggedInUserFirebaseId)
                                                || (post.postVisibilityScope == VisibilityScopeEnum.Public.name)
                                                || (post.postVisibilityScope == VisibilityScopeEnum.FriendsOnly.name && userDetails.otherUsersStatus[loggedInUserFirebaseId] == StatusWithCurrentUserRemoteEnum.Friends.name)
                                    if (!whetherShowPost) {
                                        postList.remove(post)
                                    }
                                    userList.add(userDetails.toUserBean())
                                } else {
                                    postList.remove(post)
                                }
                            } else {
                                postList.remove(post)
                            }
                        }
                    }

                    userList.forEach { user ->
                        val isPostPresentForUser =
                            postList.find { it.createdByUserFirebaseId == user.firebaseUserId } != null
                        if (!isPostPresentForUser) {
                            userList.remove(user)
                        }
                    }

                    ResponseState.success(Pair(postList, userList))
                } else {
                    ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                }
            } else {
                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
            }
        } catch (exception: Exception) {
            // An error occurred while getting the post details from the server.
            ResponseState.error(exception.localizedMessage ?: "")
        }
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
                                val userDocument = fireStore.collection(FirebaseConstants.USER_KEY)
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

}