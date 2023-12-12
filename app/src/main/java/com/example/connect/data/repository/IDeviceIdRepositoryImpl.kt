package com.example.connect.data.repository

import com.example.connect.common.ErrorCodes
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.repository.IDeviceIdRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IDeviceIdRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val appDatabase: AppDatabase
) : IDeviceIdRepository {


    override suspend fun updateDeviceIdOnRemote(
        fireBaseId: String,
        updatedDeviceId: String
    ): ResponseState<Nothing> {
        // Update the user's device ID on the remote database.
        return try {
            // Get the user's FireStore document reference.
            val documentReference =
                fireStore.collection(FirebaseConstants.UsersKey).document(fireBaseId)

            // Update the user's device ID in the document.
            documentReference.update(
                UserRemoteEntity::currentLoggedInDeviceId.name,
                updatedDeviceId
            )
                .await()

            // Return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // Return an error response if an exception occurs.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun updateDeviceIdOnDb(fireBaseId: String, updatedDeviceId: String): Int {
        // Get the UsersDao object from the AppDatabase object.
        val usersDao = appDatabase.getUsersDao()

        // Update the device ID for the user with the specified Firebase ID.
        return usersDao.updateDeviceId(fireBaseId, updatedDeviceId)
    }


    override suspend fun getDeviceIdFromRemote(firebaseUserId: String): ResponseState<String> {
        // Get the user document from FireStore.
        return try {
            val result =
                fireStore.collection(FirebaseConstants.UsersKey).document(firebaseUserId).get()
                    .await()
            // If the user document exists, get the user model from it.
            if (result.exists()) {
                val userModel = result.toObject(UserRemoteEntity::class.java)
                // If the user model is not null, return the current logged in device ID.
                if (userModel != null) {
                    ResponseState.success(userModel.currentLoggedInDeviceId)
                } else {
                    // If the user model is null, return an error code indicating that no user was found.
                    ResponseState.error(ErrorCodes.NoUserFound)
                }
            } else {
                // If the user document does not exist, return an error code indicating that no user was found.
                ResponseState.error(ErrorCodes.NoUserFound)
            }
        } catch (exception: Exception) {
            // If an exception occurs, return an error response with the exception message.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }
}