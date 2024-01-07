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
                .whereEqualTo(PostRemoteEntity::fireBaseUserId.name, fireBaseId).get().await()
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
                if (post != null) {
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
                        if (post != null) {
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
                        userList.find { it.firebaseUserId == post.fireBaseUserId } != null
                    if (!isUserPresent) {
                        val user = fireStore.collection(FirebaseConstants.USER_KEY)
                            .document(post.fireBaseUserId)
                            .get()
                            .await()
                        if (user.exists()) {
                            val userDetails = user.toObject(UserRemoteEntity::class.java)
                            if (userDetails != null) {
                                val whetherShowPost =
                                    (post.fireBaseUserId == currentUser.firebaseUserId)
                                            || (post.postScope == VisibilityScopeEnum.Public.name)
                                            || (post.postScope == VisibilityScopeEnum.FriendsOnly.name && userDetails.otherUsersStatus[currentUserFirebaseId] == StatusWithCurrentUserRemoteEnum.Friends.name)
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
                        postList.find { it.fireBaseUserId == user.firebaseUserId } != null
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

    override suspend fun getSavedPostsFromRemote(savedPosts: ArrayList<String>): ResponseState<List<PostBean>> {
        return ResponseState.success(listOf())
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
        postId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                val postDocument =
                    fireStore.collection(FirebaseConstants.POST_KEY).document(postId)
                transaction.update(
                    postDocument,
                    PostRemoteEntity::commentCount.name,
                    FieldValue.increment(-1)
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
        loggedInUserFireId: String
    ): ResponseState<Pair<MutableMap<CommentBean, ArrayList<CommentBean>>, List<UsersBean>>> {
        return try {
            val commentListResponse = fireStore.collection(FirebaseConstants.COMMENT_KEY)
                .whereEqualTo(CommentRemoteEntity::postId.name, postId)
                .get()
                .await()

            val commentList = arrayListOf<CommentBean>()
            val userList = arrayListOf<UsersBean>()
            val loggedInUserDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                .document(loggedInUserFireId).get().await()
            val loggedInUser = loggedInUserDocument.toObject(UserRemoteEntity::class.java)
            if (loggedInUserDocument != null && loggedInUserDocument.exists() && loggedInUser != null) {
                userList.add(loggedInUser.toUserBean())
                commentListResponse.documents.forEach { document ->
                    if (document.exists()) {
                        val comment = document.toObject(CommentRemoteEntity::class.java)
                        if (comment != null) {
                            commentList.add(comment.toCommentBean(document.id))
                        }
                    }
                }
                commentList.sortByDescending { it.createdAt }

                commentList.forEach { comment ->
                    val isUserPresent =
                        userList.find { it.firebaseUserId == comment.commentedBy } != null
                    if (!isUserPresent) {
                        val user = fireStore.collection(FirebaseConstants.USER_KEY)
                            .document(comment.commentedBy)
                            .get()
                            .await()
                        if (user.exists()) {
                            val userDetails = user.toObject(UserRemoteEntity::class.java)
                            if (userDetails != null) {
                                val whetherShowComment =
                                    (comment.commentedBy == loggedInUser.firebaseUserId)
                                            || ((userDetails.otherUsersStatus[loggedInUserFireId] != StatusWithCurrentUserRemoteEnum.Blocked.name)
                                            && (loggedInUser.otherUsersStatus[userDetails.firebaseUserId] != StatusWithCurrentUserRemoteEnum.Blocked.name))
                                if (!whetherShowComment) {
                                    //  commentList.remove(comment)
                                }
                                userList.add(userDetails.toUserBean())
                            } else {
                                //  commentList.remove(comment)
                            }
                        } else {
                            // commentList.remove(comment)
                        }
                    }
                }

                userList.forEach { user ->
                    val isCommentPresentForUser =
                        commentList.find { it.commentedBy == user.firebaseUserId } != null
                    if (!isCommentPresentForUser) {
                        //userList.remove(user)
                    }
                }

                ResponseState.success(Pair(buildCommentTree(commentList), userList))
            } else {
                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    private fun buildCommentTree(commentList: ArrayList<CommentBean>): MutableMap<CommentBean, ArrayList<CommentBean>> {
        val commentMap = mutableMapOf<CommentBean, ArrayList<CommentBean>>()
        val parentList =
            commentList.filter { it.repliedOnCommentId == null }
        parentList.forEach { parent ->
            val children =
                commentList.filter { comment -> comment.parentCommentId == parent.commentFirebaseId }
            commentMap.getOrPut(parent) { arrayListOf() }.addAll(children.reversed())
        }
        return commentMap
    }
}