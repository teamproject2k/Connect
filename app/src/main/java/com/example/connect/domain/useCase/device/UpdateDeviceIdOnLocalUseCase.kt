package com.example.connect.domain.useCase.device

import com.example.connect.domain.repository.IDeviceIdRepository
import javax.inject.Inject

class UpdateDeviceIdOnLocalUseCase @Inject constructor(private val repository: IDeviceIdRepository) {
    /**
     * Updates the device ID on the local database.
     *
     * @param fireBaseId The Firebase ID of the user.
     * @param updatedDeviceId The updated device ID.
     * @return The number of rows affected.
     */
    suspend operator fun invoke(fireBaseId: String, updatedDeviceId: String): Int {
        return repository.updateDeviceIdOnLocal(fireBaseId, updatedDeviceId)
    }
}