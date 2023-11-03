package com.example.connect.data.repository

import com.example.connect.common.ErrorCodes
import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IAuthenticationRepositoryImpl @Inject constructor(private val firebaseAuth: FirebaseAuth) : IAuthenticationRepository {
    override suspend fun sendOtp(mobileNumber: String) {
    }

    override suspend fun verifyOtp(verificationId: String, otp: String): ResponseState<FirebaseUser?> {
        val credentials = PhoneAuthProvider.getCredential(verificationId, otp)
        return try {
            val result = firebaseAuth.signInWithCredential(credentials).await()
            if (result.user != null) {
                ResponseState.success(result.user)
            } else {
                ResponseState.error(ErrorCodes.NoUserFound)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getUserDetails(userId: String) {

    }
}