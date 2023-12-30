package com.example.connect.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.data.models.user.UsersDbEntity
import com.example.connect.domain.enums.StatusWithCurrentUserRemoteEnum
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.flow.MutableStateFlow
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
                fireStore.collection(FirebaseConstants.USER_KEY).document(userId).get().await()
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
            val result = fireStore.collection(FirebaseConstants.USER_KEY)
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
                fireStore.collection(FirebaseConstants.USER_KEY)
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
            val response = fireStore.collection(FirebaseConstants.USER_KEY)
                .whereIn(UserRemoteEntity::firebaseUserId.name, idList).get().await()
            val usersList = arrayListOf<UsersBean>()
            response.documents.forEach { document ->
                document.toObject(UserRemoteEntity::class.java)
                    ?.let { usersList.add(it.toUserBean()) }
            }
            usersList.sortByDescending { it.createdAt }
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
                fireStore.collection(FirebaseConstants.USER_KEY).document(firebaseUserId)

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
            val result = fireStore.collection(FirebaseConstants.USER_KEY)
                .whereNotIn(UserRemoteEntity::firebaseUserId.name, excludeUserIdList)
                .get()
                .await()
            result.documents.forEach { document ->
                val user = document.toObject(UserRemoteEntity::class.java)
                if (user != null) {
                    val statusWithCurrentUser = user.otherUsersStatus[currentUserFirebaseId]
                    if (statusWithCurrentUser != StatusWithCurrentUserRemoteEnum.Blocked.name) {
                        usersList.add(user.toUserBean())
                    }
                }
            }
            usersList.sortByDescending { it.createdAt }
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
                // Get the documents for the current user and the requested user.
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.USER_KEY)
                    .document(requestedUserFirebaseId)
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)

                // If both users exist, we check their status with each other.
                if (currentUser != null && requestUser != null) {
                    val currentUserStatus =
                        currentUser.otherUsersStatus.getOrDefault(
                            requestedUserFirebaseId,
                            StatusWithCurrentUserRemoteEnum.NotFriends.name
                        )
                    val requestUserStatus = requestUser.otherUsersStatus.getOrDefault(
                        currentUserFirebaseId,
                        StatusWithCurrentUserRemoteEnum.NotFriends.name
                    )

                    // If both users are not friends, we update their status with each other.
                    if (currentUserStatus == StatusWithCurrentUserRemoteEnum.NotFriends.name && requestUserStatus == StatusWithCurrentUserRemoteEnum.NotFriends.name) {
                        currentUser.otherUsersStatus[requestedUserFirebaseId] =
                            StatusWithCurrentUserRemoteEnum.RequestedByCurrentUser.name
                        transaction.update(
                            currentUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            currentUser.otherUsersStatus
                        )
                        requestUser.otherUsersStatus[currentUserFirebaseId] =
                            StatusWithCurrentUserRemoteEnum.RequestedByOtherUser.name
                        transaction.update(
                            requestedUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            requestUser.otherUsersStatus
                        )
                    } else {
                        // If the users are already friends or have a pending request, we throw an exception.
                        throw Exception(FirebaseErrorCodes.UPDATE_ACCOUNT)
                    }
                } else {
                    // If either user does not exist, we throw an exception.
                    throw Exception(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }.await()
            // If the transaction is successful, we return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // If the transaction fails, we return an error response.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun withdrawFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.USER_KEY)
                    .document(requestedUserFirebaseId)
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)
                if (currentUser != null && requestUser != null) {
                    // Get the current user's status with the requested user.
                    val currentUserStatus =
                        currentUser.otherUsersStatus.getOrDefault(
                            requestedUserFirebaseId,
                            StatusWithCurrentUserRemoteEnum.NotFriends.name
                        )
                    // Get the requested user's status with the current user.
                    val requestUserStatus = requestUser.otherUsersStatus.getOrDefault(
                        currentUserFirebaseId,
                        StatusWithCurrentUserRemoteEnum.NotFriends.name
                    )
                    // Check if the current user has requested the requested user and the requested user has requested the current user.
                    if (currentUserStatus == StatusWithCurrentUserRemoteEnum.RequestedByCurrentUser.name && requestUserStatus == StatusWithCurrentUserRemoteEnum.RequestedByOtherUser.name) {
                        // Remove the requested user from the current user's other users status map.
                        currentUser.otherUsersStatus.remove(requestedUserFirebaseId)
                        // Update the current user's document with the updated other users status map.
                        transaction.update(
                            currentUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            currentUser.otherUsersStatus
                        )
                        // Remove the current user from the requested user's other users status map.
                        requestUser.otherUsersStatus.remove(currentUserFirebaseId)
                        // Update the requested user's document with the updated other users status map.
                        transaction.update(
                            requestedUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            requestUser.otherUsersStatus
                        )
                    } else {
                        // Throw an exception if the current user's status with the requested user or the requested user's status with the current user is not correct.
                        throw Exception(FirebaseErrorCodes.UPDATE_ACCOUNT)
                    }
                } else {
                    // Throw an exception if the current user or the requested user is not found.
                    throw Exception(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }.await()
            // Return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // Return an error response if an exception occurs.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun acceptFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            // Run a transaction to update the data in the database.
            fireStore.runTransaction { transaction ->
                // Get the documents for the current user and the requested user.
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.USER_KEY)
                    .document(requestedUserFirebaseId)
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)

                // Check if the current user and the requested user exist.
                if (currentUser != null && requestUser != null) {
                    // Get the status of the current user and the requested user.
                    val currentUserStatus =
                        currentUser.otherUsersStatus.getOrDefault(
                            requestedUserFirebaseId,
                            StatusWithCurrentUserRemoteEnum.NotFriends.name
                        )
                    val requestUserStatus = requestUser.otherUsersStatus.getOrDefault(
                        currentUserFirebaseId,
                        StatusWithCurrentUserRemoteEnum.NotFriends.name
                    )

                    // Check if the current user and the requested user have both requested each other.
                    if (currentUserStatus == StatusWithCurrentUserRemoteEnum.RequestedByOtherUser.name && requestUserStatus == StatusWithCurrentUserRemoteEnum.RequestedByCurrentUser.name) {
                        // Update the status of the current user and the requested user to "Friends".
                        currentUser.otherUsersStatus[requestedUserFirebaseId] =
                            StatusWithCurrentUserRemoteEnum.Friends.name
                        transaction.update(
                            currentUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            currentUser.otherUsersStatus
                        )
                        requestUser.otherUsersStatus[currentUserFirebaseId] =
                            StatusWithCurrentUserRemoteEnum.Friends.name
                        transaction.update(
                            requestedUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            requestUser.otherUsersStatus
                        )
                    } else {
                        // Throw an exception if the current user and the requested user have not both requested each other.
                        throw Exception(FirebaseErrorCodes.UPDATE_ACCOUNT)
                    }
                } else {
                    // Throw an exception if the current user or the requested user does not exist.
                    throw Exception(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }.await()

            // Return a ResponseState object with the success status.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // Return a ResponseState object with the error status.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun removeFriendRequest(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                // We get the documents for the current user and the requested user.
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.USER_KEY)
                    .document(requestedUserFirebaseId)
                // We get the UserRemoteEntity objects for the current user and the requested user.
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)
                // If the current user and the requested user are not null, we continue.
                if (currentUser != null && requestUser != null) {
                    // We get the status of the current user for the requested user.
                    val currentUserStatus =
                        currentUser.otherUsersStatus.getOrDefault(
                            requestedUserFirebaseId,
                            StatusWithCurrentUserRemoteEnum.NotFriends.name
                        )
                    // We get the status of the requested user for the current user.
                    val requestUserStatus = requestUser.otherUsersStatus.getOrDefault(
                        currentUserFirebaseId,
                        StatusWithCurrentUserRemoteEnum.NotFriends.name
                    )
                    // If the current user status is "RequestedByOtherUser" and the requested user status is "RequestedByCurrentUser", we continue.
                    if (currentUserStatus == StatusWithCurrentUserRemoteEnum.RequestedByOtherUser.name && requestUserStatus == StatusWithCurrentUserRemoteEnum.RequestedByCurrentUser.name) {
                        // We remove the requested user from the current user's other users status map.
                        currentUser.otherUsersStatus.remove(requestedUserFirebaseId)
                        // We update the current user document in the database.
                        transaction.update(
                            currentUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            currentUser.otherUsersStatus
                        )
                        // We remove the current user from the requested user's other users status map.
                        requestUser.otherUsersStatus.remove(currentUserFirebaseId)
                        // We update the requested user document in the database.
                        transaction.update(
                            requestedUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            requestUser.otherUsersStatus
                        )
                    } else {
                        // If the current user status or the requested user status is not correct, we throw an exception.
                        throw Exception(FirebaseErrorCodes.UPDATE_ACCOUNT)
                    }
                } else {
                    // If the current user or the requested user is null, we throw an exception.
                    throw Exception(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }.await()
            // If the transaction is successful, we return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // If the transaction fails, we return an error response.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun blockUser(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                // We get the current user's document from the database.
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)

                // We get the requested user's document from the database.
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.USER_KEY)
                    .document(requestedUserFirebaseId)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)

                // If both users exist, we update their otherUsersStatus fields.
                if (currentUser != null && requestUser != null) {
                    currentUser.otherUsersStatus[requestedUserFirebaseId] =
                        StatusWithCurrentUserRemoteEnum.Blocked.name
                    transaction.update(
                        currentUserDocumentPath,
                        UserRemoteEntity::otherUsersStatus.name,
                        currentUser.otherUsersStatus
                    )
                    requestUser.otherUsersStatus.remove(currentUserFirebaseId)
                    transaction.update(
                        requestedUserDocumentPath,
                        UserRemoteEntity::otherUsersStatus.name,
                        requestUser.otherUsersStatus
                    )

                } else {
                    // If either user does not exist, we throw an exception.
                    throw Exception(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }.await()
            // If the transaction is successful, we return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // If the transaction fails, we return an error response.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun unBlockUser(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                // Get the current user's document from the database.
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)

                // Get the requested user's document from the database.
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.USER_KEY)
                    .document(requestedUserFirebaseId)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)

                // If both users exist, we can proceed with unblocking the user.
                if (currentUser != null && requestUser != null) {
                    // Get the current user's status for the requested user.
                    val currentUserStatus =
                        currentUser.otherUsersStatus.getOrDefault(
                            requestedUserFirebaseId,
                            StatusWithCurrentUserRemoteEnum.NotFriends.name
                        )

                    // Get the requested user's status for the current user.
                    val requestUserStatus = requestUser.otherUsersStatus.getOrDefault(
                        currentUserFirebaseId,
                        StatusWithCurrentUserRemoteEnum.NotFriends.name
                    )

                    // If the current user has blocked the requested user and the requested user is not friends with the current user, we can proceed with unblocking the user.
                    if (currentUserStatus == StatusWithCurrentUserRemoteEnum.Blocked.name && requestUserStatus == StatusWithCurrentUserRemoteEnum.NotFriends.name) {
                        // Remove the requested user from the current user's blocked list.
                        currentUser.otherUsersStatus.remove(requestedUserFirebaseId)

                        // Update the current user's document in the database.
                        transaction.update(
                            currentUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            currentUser.otherUsersStatus
                        )
                    } else {
                        // If the current user has not blocked the requested user or the requested user is friends with the current user, we throw an exception.
                        throw Exception(FirebaseErrorCodes.UPDATE_ACCOUNT)
                    }
                } else {
                    // If either user does not exist, we throw an exception.
                    throw Exception(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }.await()

            // If the transaction is successful, we return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // If the transaction fails, we return an error response.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun unFriendUser(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                // We get the documents of the two users from the database.
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.USER_KEY)
                    .document(requestedUserFirebaseId)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)

                // If both users exist, we check if they are friends.
                if (currentUser != null && requestUser != null) {
                    val currentUserStatus =
                        currentUser.otherUsersStatus.getOrDefault(
                            requestedUserFirebaseId,
                            StatusWithCurrentUserRemoteEnum.NotFriends.name
                        )
                    val requestUserStatus = requestUser.otherUsersStatus.getOrDefault(
                        currentUserFirebaseId,
                        StatusWithCurrentUserRemoteEnum.NotFriends.name
                    )

                    // If they are friends, we remove them from each other's friends lists.
                    if (currentUserStatus == StatusWithCurrentUserRemoteEnum.Friends.name && requestUserStatus == StatusWithCurrentUserRemoteEnum.Friends.name) {
                        currentUser.otherUsersStatus.remove(requestedUserFirebaseId)
                        transaction.update(
                            currentUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            currentUser.otherUsersStatus
                        )
                        requestUser.otherUsersStatus.remove(currentUserFirebaseId)
                        transaction.update(
                            requestedUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            requestUser.otherUsersStatus
                        )
                    } else {
                        // If they are not friends, we throw an exception.
                        throw Exception(FirebaseErrorCodes.UPDATE_ACCOUNT)
                    }
                } else {
                    // If one or both users do not exist, we throw an exception.
                    throw Exception(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }.await()
            // If the transaction is successful, we return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // If the transaction fails, we return an error response.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun unFriendAndBlockUser(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.runTransaction { transaction ->
                // Get the current user document from the database.
                val currentUserDocumentPath =
                    fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                val currentUser =
                    transaction.get(currentUserDocumentPath).toObject(UserRemoteEntity::class.java)

                // Get the requested user document from the database.
                val requestedUserDocumentPath = fireStore.collection(FirebaseConstants.USER_KEY)
                    .document(requestedUserFirebaseId)
                val requestUser = transaction.get(requestedUserDocumentPath)
                    .toObject(UserRemoteEntity::class.java)

                // If both users exist, we can proceed.
                if (currentUser != null && requestUser != null) {
                    // Get the current user's status with the requested user.
                    val currentUserStatus =
                        currentUser.otherUsersStatus.getOrDefault(
                            requestedUserFirebaseId,
                            StatusWithCurrentUserRemoteEnum.NotFriends.name
                        )

                    // Get the requested user's status with the current user.
                    val requestUserStatus = requestUser.otherUsersStatus.getOrDefault(
                        currentUserFirebaseId,
                        StatusWithCurrentUserRemoteEnum.NotFriends.name
                    )

                    // If both users are friends, we can unfriend and block them.
                    if (currentUserStatus == StatusWithCurrentUserRemoteEnum.Friends.name && requestUserStatus == StatusWithCurrentUserRemoteEnum.Friends.name) {
                        // Set the current user's status with the requested user to Blocked.
                        currentUser.otherUsersStatus[requestedUserFirebaseId] =
                            StatusWithCurrentUserRemoteEnum.Blocked.name

                        // Update the current user document in the database.
                        transaction.update(
                            currentUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            currentUser.otherUsersStatus
                        )

                        // Remove the current user from the requested user's otherUsersStatus map.
                        requestUser.otherUsersStatus.remove(currentUserFirebaseId)

                        // Update the requested user document in the database.
                        transaction.update(
                            requestedUserDocumentPath,
                            UserRemoteEntity::otherUsersStatus.name,
                            requestUser.otherUsersStatus
                        )
                    } else {
                        // If the users are not friends, we throw an exception.
                        throw Exception(FirebaseErrorCodes.UPDATE_ACCOUNT)
                    }
                } else {
                    // If either user does not exist, we throw an exception.
                    throw Exception(FirebaseErrorCodes.NO_USER_FOUND)
                }
            }.await()

            // If the transaction is successful, we return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // If the transaction fails, we return an error response.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun updateOtherUsersStatusOnDb(
        currentUserFirebaseId: String,
        otherUsersStatus: MutableMap<String, String>
    ): Int {
        // Get the UsersDao object from the AppDatabase object.
        val usersDao = appDatabase.getUsersDao()

        // Call the updateOtherUsersStatus() method on the UsersDao object.
        return usersDao.updateOtherUsersStatus(currentUserFirebaseId, otherUsersStatus)
    }

    override suspend fun liveObserveUserFromRemote(
        firebaseUserId: String,
        userObserverStateFlow: MutableStateFlow<ResponseState<UsersBean>>
    ): ListenerRegistration {
        // Get a reference to the user document in the Firestore database.
        val userDocumentReference = fireStore.collection(FirebaseConstants.USER_KEY)
            .document(firebaseUserId)

        // Add a snapshot listener to the user document.
        return userDocumentReference.addSnapshotListener(MetadataChanges.EXCLUDE) { document, error ->
            // If there is no error and the document exists, get the user data from the document.
            if (error == null && document != null && document.exists()) {
                val requiredUser = document.toObject(UserRemoteEntity::class.java)

                // If the user data is not null, convert it to a UsersBean object and emit it to the user observer state flow.
                if (requiredUser != null) {
                    userObserverStateFlow.value =
                        ResponseState.success(requiredUser.toUserBean())
                }
            }
        }
    }

    override suspend fun savePost(
        currentUserFirebaseId: String,
        postId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                .update(
                    UserRemoteEntity::savedPosts.name,
                    FieldValue.arrayUnion(postId)
                )
                .await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun unSavePost(
        currentUserFirebaseId: String,
        postId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                .update(
                    UserRemoteEntity::savedPosts.name,
                    FieldValue.arrayRemove(postId)
                )
                .await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun updateFCMTokenOnRemote(
        currentUserFirebaseId: String,
        fcmToken: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.USER_KEY).document(currentUserFirebaseId)
                .update(UserRemoteEntity::fcmToken.name, fcmToken).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun updateFCMTokenOnLocal(
        currentUserFirebaseId: String,
        updatedToken: String
    ): Int {
        return appDatabase.getUsersDao()
            .updateFCMTokenOnLocal(currentUserFirebaseId, fcmToken = updatedToken)
    }
}