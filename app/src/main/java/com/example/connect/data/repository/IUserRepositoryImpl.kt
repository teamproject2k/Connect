package com.example.connect.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.data.models.user.UsersDbEntity
import com.example.connect.domain.enums.StatusWithCurrentEnum
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
        // Update the user details on the remote database.
        return try {
            // Get the reference to the user document in the FireStore database.
            val userDocumentReference =
                fireStore.collection(FirebaseConstants.UsersKey).document(firebaseUserId)

            // Update the user details in the document.
            userDocumentReference.update(fieldsToUpdate).await()

            // Return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // Return an error response if an exception occurs.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun updateUserDetailsOnDb(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): Long {

        // Create a string of the set clause, which is the list of fields to update and their new values.
        val setClause = fieldsToUpdate.entries.joinToString(", ") { "${it.key} = ?" }

        // Create the SQL query to update the user details.
        val sql =
            "UPDATE ${UsersDbEntity::class.simpleName} SET $setClause WHERE ${UsersDbEntity::firebaseUserId.name} = ?"

        // Create a list of the bind arguments, which are the values of the fields to update.
        val bindArgs = fieldsToUpdate.values.toMutableList()
        bindArgs.add(firebaseUserId)

        // Create a SimpleSQLiteQuery object with the SQL query and bind arguments.
        val queryToExecute = SimpleSQLiteQuery(sql, bindArgs.toTypedArray())

        // Execute the query and return the number of rows affected.
        return appDatabase.getUsersDao().updateUserDetails(queryToExecute)
    }

    override suspend fun getAllUsersNotInList(
        excludeUserIdList: List<String>,
        currentUserFirebaseId: String
    ): ResponseState<ArrayList<UsersBean>> {
        // Try to get the users from the FireStore database whose id is not in excludeUserIdList.
        return try {
            val usersList = arrayListOf<UsersBean>()
            val result = fireStore.collection(FirebaseConstants.UsersKey)
                .whereNotIn(UserRemoteEntity::firebaseUserId.name, excludeUserIdList).get()
                .await()
            result.documents.forEach { document ->
                val user = document.toObject(UserRemoteEntity::class.java)
                if (user != null) {
                    val statusWithCurrentUser = user.otherUsersStatus[currentUserFirebaseId]
                    if (statusWithCurrentUser != StatusWithCurrentEnum.Blocked.name) {
                        usersList.add(user.toUserBean())
                    }
                }
            }
            // Return a success response with the list of users found.
            ResponseState.success(usersList)
        } catch (exception: Exception) {
            // If there is an exception, return an error response with the exception message.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun sendFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.UsersKey).document(currentUserFirebaseId)
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.UsersKey)
                    .document(requestedUserFirebaseId)
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)
                if (currentUser != null) {
                    currentUser.otherUsersStatus[requestedUserFirebaseId] =
                        StatusWithCurrentEnum.RequestedByCurrentUser.name
                    transaction.update(
                        currentUserDocumentPath,
                        UserRemoteEntity::otherUsersStatus.name,
                        currentUser.otherUsersStatus
                    )
                }
                if (requestUser != null) {
                    requestUser.otherUsersStatus[currentUserFirebaseId] =
                        StatusWithCurrentEnum.RequestedByOtherUser.name
                    transaction.update(
                        requestedUserDocumentPath,
                        UserRemoteEntity::otherUsersStatus.name,
                        requestUser.otherUsersStatus
                    )
                }
            }.await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun acceptFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.UsersKey).document(currentUserFirebaseId)
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.UsersKey)
                    .document(requestedUserFirebaseId)
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)
                if (currentUser != null) {
                    currentUser.otherUsersStatus[requestedUserFirebaseId] =
                        StatusWithCurrentEnum.Friends.name
                    transaction.update(
                        currentUserDocumentPath,
                        UserRemoteEntity::otherUsersStatus.name,
                        currentUser.otherUsersStatus
                    )
                }
                if (requestUser != null) {
                    requestUser.otherUsersStatus[currentUserFirebaseId] =
                        StatusWithCurrentEnum.Friends.name
                    transaction.update(
                        requestedUserDocumentPath,
                        UserRemoteEntity::otherUsersStatus.name,
                        requestUser.otherUsersStatus
                    )
                }
            }.await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun denyFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.UsersKey).document(currentUserFirebaseId)
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.UsersKey)
                    .document(requestedUserFirebaseId)
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)
                if (currentUser != null) {
                    currentUser.otherUsersStatus.remove(requestedUserFirebaseId)
                    transaction.update(
                        currentUserDocumentPath,
                        UserRemoteEntity::otherUsersStatus.name,
                        currentUser.otherUsersStatus
                    )
                }
                if (requestUser != null) {
                    requestUser.otherUsersStatus.remove(currentUserFirebaseId)
                    transaction.update(
                        requestedUserDocumentPath,
                        UserRemoteEntity::otherUsersStatus.name,
                        requestUser.otherUsersStatus
                    )
                }
            }.await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun blockUser(currentUserFirebaseId: String, requestedUserFirebaseId: String) {

    }

    override suspend fun removeFriend(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ) {

    }

    override suspend fun updateOtherUsersStatusOnDb(
        currentUserFirebaseId: String,
        otherUsersStatus: MutableMap<String, String>
    ): Int {
        return appDatabase.getUsersDao()
            .updateOtherUsersStatus(currentUserFirebaseId, otherUsersStatus)
    }
}