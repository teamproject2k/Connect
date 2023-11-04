package com.example.connect.data.repository

import com.example.connect.common.ErrorCodes
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import com.example.connect.presentation.ui.auth.AuthenticationActivity
import com.example.connect.presentation.utils.ConstantsHelper
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class IAuthenticationRepositoryImpl @Inject constructor(private val firebaseAuth: FirebaseAuth) :
    IAuthenticationRepository {
    override suspend fun sendOtp(
        countryCode: String,
        mobileNumber: String,
        responseState: MutableStateFlow<ResponseState<String>>
    ) {

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                responseState.value = ResponseState.success(FirebaseConstants.AutoLogin)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                responseState.value = ResponseState.error(e.localizedMessage ?: "")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                responseState.value = ResponseState.success(verificationId)
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
    ): ResponseState<FirebaseUser?> {
        val credentials = PhoneAuthProvider.getCredential(verificationId, otp)
        return try {
            val result = firebaseAuth.signInWithCredential(credentials).await()
            if (result.user != null) {
                ResponseState.success(result.user)
            } else {
                firebaseAuth.signOut()
                ResponseState.error(ErrorCodes.NoUserFound)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getUserDetails(userId: String) {

    }
}