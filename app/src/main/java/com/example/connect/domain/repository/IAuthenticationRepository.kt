package com.example.connect.domain.repository

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow

interface IAuthenticationRepository {
    suspend fun sendOtp(
        countryCode: String,
        mobileNumber: String,
        responseStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>>
    )

    suspend fun verifyOtp(verificationId: String, otp: String): ResponseState<FirebaseUser>

    suspend fun getUserDetails(userId: String): ResponseState<UserDetails?>


    suspend fun getUsersFromName(name: String): ResponseState<Int>

    suspend fun addUserToRemote(userDetails: UserDetails): ResponseState<Nothing>

    suspend fun addUserToLocalDb(userDetails: UserDetails): Long

}