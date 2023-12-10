package com.example.connect.data.repository

import com.example.connect.common.ErrorCodes
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IHomeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class IHomeRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val fireStore: FirebaseFirestore
) : IHomeRepository {

    override suspend fun getUserDetailsFromLocal(fireBaseId: String): UserDetails? {
        // Get the user details from the local database.
        return appDatabase.getUsersDao().getUserDetails(fireBaseId)
    }

    override suspend fun getUserDetailsFromServer(fireBaseId: String): ResponseState<UserDetails> {
        // Get the user details from the server.
        return try {
            val result =
                fireStore.collection(FirebaseConstants.UsersKey).document(fireBaseId).get().await()
            val user = result.toObject(UserDetails::class.java)
            if (result.exists() && user != null) {
                // The user details were found on the server.
                ResponseState.success(user)
            } else {
                // The user details were not found on the server.
                ResponseState.error(ErrorCodes.NoUserFound)
            }
        } catch (exception: Exception) {
            // An error occurred while getting the user details from the server.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addUserToLocalDb(userDetails: UserDetails): Long {
        // Add the user details to the local database.
        return appDatabase.getUsersDao().insertUser(userDetails)
    }

    override suspend fun getUserDetailsFromIds(idList: List<String>): ResponseState<List<UserDetails>> {
        // Get the user details from the server for the given list of IDs.
        return try {
            val response = fireStore.collection(FirebaseConstants.UsersKey)
                .whereIn(UserDetails::firebaseUserId.name, idList).get().await()
            val usersList = arrayListOf<UserDetails>()
            response.documents.forEach { document ->
                document.toObject(UserDetails::class.java)?.let { usersList.add(it) }
            }
            ResponseState.success(usersList)
        } catch (exception: Exception) {
            // An error occurred while getting the user details from the server.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getPostDetailsFromLocal(fireBaseId: String): List<PostDetails>? {
        // Get the post details from the local database.
        return appDatabase.getPostDao().getPostList(fireBaseId)
    }

    override suspend fun getPostDetailsFromServer(fireBaseId: String): ResponseState<List<PostDetails>> {
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

    override suspend fun addPostToLocal(postDetails: PostDetails): Long {
        // Add the post details to the local database.
        return appDatabase.getPostDao().insertPost(postDetails)
    }

    override suspend fun addPostListToLocal(postDetailList: List<PostDetails>): LongArray {
        // Add the post details to the local database.
        return appDatabase.getPostDao().insertPostList(postDetailList)
    }

    override suspend fun uploadPostToServer(
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