package com.example.connect.domain.useCase

import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class UpdateDeviceIdOnDbUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    suspend fun updateDeviceIdOnLocal(fireBaseId: String, updatedDeviceId: String) =
        repository.updateDeviceIdOnLocal(fireBaseId, updatedDeviceId)
}