package com.example.connect.data.repository

import com.example.connect.common.ErrorCodes
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.local_db.users.UserDetails
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

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                if (firebaseAuth.currentUser?.uid != null) {
                    responseStateFlow.value = ResponseState.success(
                        Pair(
                            FirebaseConstants.AutoLogin,
                            firebaseAuth.currentUser!!.uid
                        )
                    )
                } else {
                    responseStateFlow.value = ResponseState.error(ErrorCodes.NoUserFound)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                responseStateFlow.value = ResponseState.error(e.localizedMessage ?: "")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                responseStateFlow.value = ResponseState.success(Pair("", verificationId))
            }

        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(countryCode + mobileNumber)
            .setTimeout(ConstantsHelper.OTPTimeOutTime, TimeUnit.SECONDS)
            .setCallbacks(callbacks)
        if (AuthenticationActivity.Instance != null) {
            options.setActivity(AuthenticationActivity.Instance!!)
        }
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    override suspend fun verifyOtp(
        verificationId: String,
        otp: String
    ): ResponseState<FirebaseUser> {
        val credentials = PhoneAuthProvider.getCredential(verificationId, otp)
        return try {
            val result = firebaseAuth.signInWithCredential(credentials).await()
            if (result.user != null) {
                ResponseState.success(result.user!!)
            } else {
                firebaseAuth.signOut()
                ResponseState.error(ErrorCodes.NoUserFound)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getUserDetails(userId: String): ResponseState<UserDetails?> {
        return try {
            val result =
                fireStore.collection(FirebaseConstants.UsersKey).document(userId).get().await()
            if (result.exists()) {
                val userModel = result.toObject(UserDetails::class.java)
                ResponseState.success(userModel)
            } else {
                ResponseState.success(null)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getUsersFromName(name: String): ResponseState<Int> {
        return try {
            val result = fireStore.collection(FirebaseConstants.UsersKey)
                .whereEqualTo(UserDetails::name.name, name).get().await()
            ResponseState.success(result.size())
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addUserToRemote(userDetails: UserDetails): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.UsersKey).document(userDetails.firebaseUserId)
                .set(userDetails).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addUserToLocalDb(userDetails: UserDetails): Long {
        return appDatabase.getUsersDao().insertUser(userDetails)
    }

    override suspend fun updateDeviceIdOnRemote(
        fireBaseId: String,
        updatedDeviceId: String
    ): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.UsersKey).document(fireBaseId)
                .update(UserDetails::currentLoggedInDeviceId.name, updatedDeviceId).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun updateDeviceIdOnLocal(fireBaseId: String, updatedDeviceId: String): Int {
        return appDatabase.getUsersDao().updateDeviceId(fireBaseId, updatedDeviceId)
    }
}