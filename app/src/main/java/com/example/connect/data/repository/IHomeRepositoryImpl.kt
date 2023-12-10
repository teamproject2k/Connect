package com.example.connect.data.repository

import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IHomeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class IHomeRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val fireStore: FirebaseFirestore
) : IHomeRepository {

    override suspend fun getUserDetailsFromDb(fireBaseId: String): UsersBean? {
        // Get the user details from the local database.
        return appDatabase.getUsersDao().getUserDetails(fireBaseId)?.toUserBean()
    }

    override suspend fun getUserDetailsFromIdsFromRemote(idList: List<String>): ResponseState<List<UsersBean>> {
        // Get the user details from the server for the given list of IDs.
        return try {
            val response = fireStore.collection(FirebaseConstants.UsersKey)
                .whereIn(UserRemoteEntity::firebaseUserId.name, idList).get().await()
            val usersList = arrayListOf<UsersBean>()
            response.documents.forEach { document ->
                document.toObject(UserRemoteEntity::class.java)
                    ?.let { usersList.add(it.toUserBean()) }
            }
            ResponseState.success(usersList)
        } catch (exception: Exception) {
            // An error occurred while getting the user details from the server.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getPostDetailsFromDb(fireBaseId: String): List<PostDetails>? {
        // Get the post details from the local database.
        return appDatabase.getPostDao().getPostList(fireBaseId)
    }

    override suspend fun getPostDetailsFromRemote(fireBaseId: String): ResponseState<List<PostDetails>> {
        // Get the post details from the server.
        return try {
            val response = fireStore.collection(FirebaseConstants.PostsKey)
                .whereEqualTo(PostDetails::fireBaseUserId.name, fireBaseId).get().await()
            val postList = arrayListOf<PostDetails>()
            response.documents.forEach { document ->
                val post = document.toObject(PostDetails::class.java)
                if (post != null) {
                    postList.add(post)
                }
            }
            ResponseState.success(postList)
        } catch (exception: Exception) {
            // An error occurred while getting the post details from the server.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addPostToDb(postDetails: PostDetails): Long {
        // Add the post details to the local database.
        return appDatabase.getPostDao().insertPost(postDetails)
    }

    override suspend fun addPostListToDb(postDetailList: List<PostDetails>): LongArray {
        // Add the post details to the local database.
        return appDatabase.getPostDao().insertPostList(postDetailList)
    }

    override suspend fun uploadPostToRemote(
        postDetails: PostDetails,
        fireBaseId: String
    ): ResponseState<String> {
        // Upload the post details to the server.
        return try {
            val response = fireStore.collection(FirebaseConstants.MediaKey).document(fireBaseId)
                .collection(FirebaseConstants.PostsKey).add(postDetails)
                .await()
            ResponseState.success(response.id)
        } catch (exception: Exception) {
            // An error occurred while uploading the post details to the server.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

}