package com.example.connect.domain.repository

import com.example.connect.common.ResponseState
import com.example.connect.domain.models.UsersBean
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow

interface IAuthenticationRepository {
    /**
     * Sends an OTP to the given mobile number.
     *
     * @param countryCode The country code of the mobile number.
     * @param mobileNumber The mobile number.
     * @param responseStateFlow The state flow that will be updated with the response.
     */
    suspend fun sendOtp(
        countryCode: String,
        mobileNumber: String,
        responseStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>>
    )

    /**
     * Verifies the OTP and returns the FirebaseUser if successful.
     *
     * @param verificationId The verification ID.
     * @param otp The OTP.
     * @return The response state.
     */
    suspend fun verifyOtp(verificationId: String, otp: String): ResponseState<FirebaseUser>

    /**
     * Gets the user details from the remote database.
     *
     * @param userId The user ID.
     * @return The response state.
     */
    suspend fun getUserDetailsFromRemote(userId: String): ResponseState<UsersBean?>

    /**
     * Gets the number of users with the given name from remote.
     *
     * @param name The name.
     * @return The response state.
     */
    suspend fun getUsersCountFromNameFromRemote(name: String): ResponseState<Int>

    /**
     * Adds the user to the remote database.
     *
     * @param userDetails The user details.
     * @return The response state.
     */
    suspend fun addUserToRemote(userDetails: UsersBean): ResponseState<Nothing>

    /**
     * Adds the user to the local database.
     *
     * @param userDetails The user details.
     * @return The row ID of the inserted row.
     */
    suspend fun addUserToDb(userDetails: UsersBean): Long

    /**
     * Updates the device ID on the remote database.
     *
     * @param fireBaseId The Firebase ID.
     * @param updatedDeviceId The updated device ID.
     * @return The response state.
     */
    suspend fun updateDeviceIdOnRemote(
        fireBaseId: String,
        updatedDeviceId: String
    ): ResponseState<Nothing>

    /**
     * Updates the device ID on the local database.
     *
     * @param fireBaseId The Firebase ID.
     * @param updatedDeviceId The updated device ID.
     * @return The number of rows affected.
     */
    suspend fun updateDeviceIdOnDb(fireBaseId: String, updatedDeviceId: String): Int

}