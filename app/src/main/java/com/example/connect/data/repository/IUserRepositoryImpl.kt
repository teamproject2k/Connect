package com.example.connect.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.data.models.user.UsersDbEntity
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IUserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IUserRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val appDatabase: AppDatabase
) :
    IUserRepository {

    override suspend fun getUserDetailsFromRemote(userId: String): ResponseState<UsersBean?> {
        // Try to get the user details from the FireStore database.
        return try {
            val result =
                fireStore.collection(FirebaseConstants.UsersKey).document(userId).get().await()
            // If the document exists, get the user details object and return a success response.
            if (result.exists()) {
                val userModel = result.toObject(UserRemoteEntity::class.java)
                ResponseState.success(userModel?.toUserBean())
            } else {
                // If the document does not exist, return a success response with null.
                ResponseState.success(null)
            }
        } catch (exception: Exception) {
            // If there is an exception, return an error response with the exception message.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getUsersCountFromNameFromRemote(name: String): ResponseState<Int> {
        // Try to get the users from the FireStore database whose name matches the given name.
        return try {
            val result = fireStore.collection(FirebaseConstants.UsersKey)
                .whereEqualTo(UserRemoteEntity::name.name, name).get().await()
            // Return a success response with the number of users found.
            ResponseState.success(result.size())
        } catch (exception: Exception) {
            // If there is an exception, return an error response with the exception message.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addUserToRemote(userDetails: UsersBean): ResponseState<Nothing> {
        // Add the user to the remote database.
        return try {
            // Get the user's FireStore document reference.
            val documentReference =
                fireStore.collection(FirebaseConstants.UsersKey)
                    .document(userDetails.firebaseUserId)

            // Set the user's details in the document.
            documentReference.set(userDetails.toUserRemoteEntity()).await()

            // Return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // Return an error response if an exception occurs.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addUserToDb(userDetails: UsersBean): Long {
        // Add the user to the local database.
        return appDatabase.getUsersDao().insertUser(userDetails.toUserDbEntity())
    }

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

    override suspend fun updateUserDetailsOnRemote(
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

    override suspend fun updateUserDetailsOnDb(
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