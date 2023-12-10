package com.example.connect.data.repository

import com.example.connect.common.ErrorCodes
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IAuthenticationRepository
import com.example.connect.presentation.ui.auth.AuthenticationActivity
import com.example.connect.presentation.utils.ConstantsHelper
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class IAuthenticationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val appDatabase: AppDatabase
) :
    IAuthenticationRepository {
    override suspend fun sendOtp(
        countryCode: String,
        mobileNumber: String,
        responseStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>>
    ) {

        // Create a callback object to handle the verification process.
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // If the verification is completed, check if the user is already logged in.
                if (firebaseAuth.currentUser?.uid != null) {
                    // If the user is already logged in, send a success response with the user's ID.
                    responseStateFlow.value = ResponseState.success(
                        Pair(
                            FirebaseConstants.AutoLogin,
                            firebaseAuth.currentUser!!.uid
                        )
                    )
                } else {
                    // If the user is not logged in, send an error response.
                    responseStateFlow.value = ResponseState.error(ErrorCodes.NoUserFound)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // If the verification fails, send an error response with the error message.
                responseStateFlow.value = ResponseState.error(e.localizedMessage ?: "")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // If the code is sent, send a success response with the verification ID.
                responseStateFlow.value = ResponseState.success(Pair("", verificationId))
            }

        }

        // Create a PhoneAuthOptions object to configure the verification process.
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(countryCode + mobileNumber)
            .setTimeout(ConstantsHelper.OTPTimeOutTime, TimeUnit.SECONDS)
            .setCallbacks(callbacks)

        // If the AuthenticationActivity is not null, set the activity to the options object.
        if (AuthenticationActivity.Instance != null) {
            options.setActivity(AuthenticationActivity.Instance!!)
        }

        // Verify the phone number using the PhoneAuthProvider.
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    override suspend fun verifyOtp(
        verificationId: String,
        otp: String
    ): ResponseState<FirebaseUser> {
        // Create a PhoneAuthCredential object using the verification ID and the OTP.
        val credentials = PhoneAuthProvider.getCredential(verificationId, otp)
        // Try to sign in the user with the credentials.
        return try {
            val result = firebaseAuth.signInWithCredential(credentials).await()
            // If the sign in is successful, return a success response with the user object.
            if (result.user != null) {
                ResponseState.success(result.user!!)
            } else {
                // If the sign in fails, sign out the user and return an error response.
                firebaseAuth.signOut()
                ResponseState.error(ErrorCodes.NoUserFound)
            }
        } catch (exception: Exception) {
            // If there is an exception, return an error response with the exception message.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getUserDetailsFromRemote(userId: String): ResponseState<UsersBean?> {
        // Try to get the user details from the Firestore database.
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

    override suspend fun getUsersCountFromName(name: String): ResponseState<Int> {
        // Try to get the users from the Firestore database whose name matches the given name.
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
            // Get the user's Firestore document reference.
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

    override suspend fun addUserToLocalDb(userDetails: UsersBean): Long {
        // Add the user to the local database.
        return appDatabase.getUsersDao().insertUser(userDetails.toUserDbEntity())
    }

    override suspend fun updateDeviceIdOnRemote(
        fireBaseId: String,
        updatedDeviceId: String
    ): ResponseState<Nothing> {
        // Update the user's device ID on the remote database.
        return try {
            // Get the user's Firestore document reference.
            val documentReference =
                fireStore.collection(FirebaseConstants.UsersKey).document(fireBaseId)

            // Update the user's device ID in the document.
            documentReference.update(UserRemoteEntity::currentLoggedInDeviceId.name, updatedDeviceId)
                .await()

            // Return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // Return an error response if an exception occurs.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun updateDeviceIdOnLocal(fireBaseId: String, updatedDeviceId: String): Int {
        // Get the UsersDao object from the AppDatabase object.
        val usersDao = appDatabase.getUsersDao()

        // Update the device ID for the user with the specified Firebase ID.
        return usersDao.updateDeviceId(fireBaseId, updatedDeviceId)
    }
}