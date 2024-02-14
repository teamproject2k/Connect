package com.teamproject2k.connect.data.repository

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IAuthenticationRepository
import com.teamproject2k.connect.domain.utils.FirebaseConstants
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.ui.auth.AuthenticationActivity
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class IAuthenticationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
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
                val firebaseUserId = firebaseAuth.currentUser?.uid
                if (firebaseUserId != null) {
                    // If the user is already logged in, send a success response with the user's ID.
                    responseStateFlow.value = ResponseState.success(
                        Pair(FirebaseConstants.AUTO_LOGIN, firebaseUserId)
                    )
                } else {
                    // If the user is not logged in, send an error response.
                    responseStateFlow.value = ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
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
            .setTimeout(ConstantsHelper.OTP_TIMEOUT_TIME, TimeUnit.SECONDS)
            .setCallbacks(callbacks)

        // If the AuthenticationActivity is not null, set the activity to the options object.
        val authenticationActivityInstance = AuthenticationActivity.Instance
        if (authenticationActivityInstance != null) {
            options.setActivity(authenticationActivityInstance)
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
            val user = result.user
            if (user != null) {
                ResponseState.success(user)
            } else {
                // If the sign in fails, sign out the user and return an error response.
                firebaseAuth.signOut()
                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
            }
        } catch (exception: Exception) {
            // If there is an exception, return an error response with the exception message.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }
}

