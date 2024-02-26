package com.teamproject2k.connect.domain.use_case.device

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IDeviceIdRepository
import javax.inject.Inject

class UpdateDeviceIdOnRemoteUseCase @Inject constructor(private val repository: IDeviceIdRepository) {
    /**
     * Invokes the updateDeviceIdOnRemote method on the repository.
     *
     * @param fireBaseId The Firebase ID of the user.
     * @param updatedDeviceId The updated device ID of the user.
     * @return A ResponseState object containing the result of the update.
     */
    suspend operator fun invoke(
        fireBaseId: String,
        updatedDeviceId: String
    ): ResponseState<Nothing> {
        return repository.updateDeviceIdOnRemote(fireBaseId, updatedDeviceId)
    }
}