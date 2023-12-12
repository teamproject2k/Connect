package com.example.connect.data.repository

import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.user.UsersDbEntity
import com.example.connect.domain.repository.IHomeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class IHomeRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val fireStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : IHomeRepository {




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

}