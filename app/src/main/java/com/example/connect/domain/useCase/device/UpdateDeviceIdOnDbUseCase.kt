package com.example.connect.domain.useCase.device

import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class UpdateDeviceIdOnDbUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    /**
     * Updates the device ID on the local database.
     *
     * @param fireBaseId The Firebase ID of the user.
     * @param updatedDeviceId The updated device ID.
     * @return The number of rows affected.
     */
    suspend fun invoke(fireBaseId: String, updatedDeviceId: String): Int {
        return repository.updateDeviceIdOnLocal(fireBaseId, updatedDeviceId)
    }
}