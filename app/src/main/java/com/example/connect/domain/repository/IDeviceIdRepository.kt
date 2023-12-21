package com.example.connect.domain.repository

import com.example.connect.domain.network_request_response.ResponseState

interface IDeviceIdRepository {
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

    /**
     * Gets the device ID from the remote server.
     *
     * @param firebaseUserId The user's Firebase ID.
     * @return A [ResponseState] containing the device ID or an error.
     */
    suspend fun getDeviceIdFromRemote(firebaseUserId: String): ResponseState<String>
}