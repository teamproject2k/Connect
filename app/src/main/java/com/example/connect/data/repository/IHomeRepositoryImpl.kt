package com.example.connect.data.repository

import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.connect.common.ErrorCodes
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IHomeRepository
import com.example.connect.presentation.utils.FunctionHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class IHomeRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val fireStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : IHomeRepository {
    override suspend fun getUserDetailsFromLocal(fireBaseId: String): UserDetails? {
        return appDatabase.getUsersDao().getUserDetails(fireBaseId)
    }

    override suspend fun getUserDetailsFromServer(fireBaseId: String): ResponseState<UserDetails> {
        return try {
            val result =
                fireStore.collection(FirebaseConstants.UsersKey).document(fireBaseId).get().await()
            val user = result.toObject(UserDetails::class.java)
            if (result.exists() && user != null) {
                ResponseState.success(user)
            } else {
                ResponseState.error(ErrorCodes.NoUserFound)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addUserToLocalDb(userDetails: UserDetails): Long {
        return appDatabase.getUsersDao().insertUser(userDetails)
    }

    override suspend fun getUserDetailsFromIds(idList: List<String>): ResponseState<List<UserDetails>> {
        return try {
            val response = fireStore.collection(FirebaseConstants.UsersKey)
                .whereIn(UserDetails::firebaseUserId.name, idList).get().await()
            val usersList = arrayListOf<UserDetails>()
            response.documents.forEach { document ->
                document.toObject(UserDetails::class.java)?.let { usersList.add(it) }
            }
            ResponseState.success(usersList)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getPostDetailsFromLocal(fireBaseId: String): List<PostDetails>? {
        return appDatabase.getPostDao().getPostList(fireBaseId)
    }

    override suspend fun getPostDetailsFromServer(fireBaseId: String): ResponseState<List<PostDetails>> {
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
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addPostToLocal(postDetails: PostDetails): Long {
        return appDatabase.getPostDao().insertPost(postDetails)
    }

    override suspend fun addPostListToLocal(postDetailList: List<PostDetails>): LongArray {
        return appDatabase.getPostDao().insertPostList(postDetailList)
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
        return FunctionHelper.getUsersFromName(fireStore, name.toString())
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
            "UPDATE ${UserDetails::class.simpleName} SET $setClause WHERE ${UserDetails::firebaseUserId.name} = ?"
        val bindArgs = fieldsToUpdate.values.toMutableList()
        bindArgs.add(firebaseUserId)

        val queryToExecute = SimpleSQLiteQuery(sql, bindArgs.toTypedArray())

        return appDatabase.getUsersDao().updateUserDetails(queryToExecute)
    }

}