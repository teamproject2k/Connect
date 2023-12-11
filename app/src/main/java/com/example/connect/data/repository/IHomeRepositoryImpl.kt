package com.example.connect.data.repository

import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.connect.common.ErrorCodes
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.post.PostRemoteEntity
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.data.models.user.UsersDbEntity
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IHomeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class IHomeRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val fireStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
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

    override suspend fun getPostDetailsFromDb(fireBaseId: String): List<PostBean> {
        // Get the post details from the local database.
        return appDatabase.getPostDao().getPostList(fireBaseId).map { it.toPostBean() }
    }

    override suspend fun getPostDetailsFromRemote(fireBaseId: String): ResponseState<List<PostBean>> {
        // Get the post details from the server.
        return try {
            val response = fireStore.collection(FirebaseConstants.PostsKey)
                .whereEqualTo(PostRemoteEntity::fireBaseUserId.name, fireBaseId).get().await()
            val postList = arrayListOf<PostBean>()
            response.documents.forEach { document ->
                val post = document.toObject(PostRemoteEntity::class.java)
                if (post != null) {
                    postList.add(post.toPostBean(document.id))
                }
            }
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
            val response = fireStore.collection(FirebaseConstants.MediaKey).document(fireBaseId)
                .collection(FirebaseConstants.PostsKey).add(postDetails.toPostRemoteEntity())
                .await()
            ResponseState.success(response.id)
        } catch (exception: Exception) {
            // An error occurred while uploading the post details to the server.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun uploadFileToRemote(url: Uri, path: String): ResponseState<String> {
        // Upload the file to the specified path in Firebase Storage.
        return try {
            val downloadUrl = firebaseStorage.reference.child(path).putFile(url)
                .await().storage.downloadUrl.await()
            // Return the download URL as a success state.
            ResponseState.success(downloadUrl.toString())
        } catch (exception: Exception) {
            // Return an error state with the exception message.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun updateUserDetailsOnServer(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): ResponseState<Nothing?> {
        return try {
            fireStore.collection(FirebaseConstants.UsersKey).document(firebaseUserId)
                .update(fieldsToUpdate).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getUsersFromName(name: Any): ResponseState<Int> {
        return ResponseState.error("")
    }

    override suspend fun updateImageOnRemoteStorage(
        imageUri: Uri?,
        firebaseUserId: String,
        parameterToUpdate: String
    ): ResponseState<String> {
        return try {
            val downloadUrl =
                firebaseStorage.reference.child(FirebaseConstants.UsersKey).child(firebaseUserId)
                    .child(parameterToUpdate).putFile(imageUri!!)
                    .await().storage.downloadUrl.await()

            ResponseState.success(downloadUrl.toString())
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun updateUserDetailsOnLocal(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): Long {

        val setClause = fieldsToUpdate.entries.joinToString(", ") { "${it.key} = ?" }
        val sql =
            "UPDATE ${UsersDbEntity::class.simpleName} SET $setClause WHERE ${UsersDbEntity::firebaseUserId.name} = ?"
        val bindArgs = fieldsToUpdate.values.toMutableList()
        bindArgs.add(firebaseUserId)

        val queryToExecute = SimpleSQLiteQuery(sql, bindArgs.toTypedArray())

        return appDatabase.getUsersDao().updateUserDetails(queryToExecute)
    }

    override suspend fun getDeviceIdFromRemote(firebaseUserId: String): ResponseState<String> {
        return try {
            val result =
                fireStore.collection(FirebaseConstants.UsersKey).document(firebaseUserId).get()
                    .await()
            if (result.exists()) {
                val userModel = result.toObject(UserRemoteEntity::class.java)
                if (userModel != null) {
                    ResponseState.success(userModel.currentLoggedInDeviceId)
                } else {
                    ResponseState.error(ErrorCodes.NoUserFound)
                }
            } else {
                ResponseState.error(ErrorCodes.NoUserFound)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }
}