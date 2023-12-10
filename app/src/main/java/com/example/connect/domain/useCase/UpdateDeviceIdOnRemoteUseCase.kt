package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class UpdateDeviceIdOnRemoteUseCase @Inject constructor(private val repository: IAuthenticationRepository) {
    suspend fun invoke(
        fireBaseId: String,
        updatedDeviceId: String
    ): ResponseState<Nothing> {
        return repository.updateDeviceIdOnRemote(fireBaseId, updatedDeviceId)
    }

}