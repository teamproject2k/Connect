package com.teamproject2k.connect.domain.use_case.device

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IDeviceIdRepository
import javax.inject.Inject

class GetDeviceIdFromRemoteUseCase @Inject constructor(private val repository: IDeviceIdRepository) {
    /**
     * Invokes the repository to get the device ID from the remote server.
     *
     * @param firebaseId The Firebase ID of the user.
     * @return A [ResponseState] containing the device ID or an error.
     */
    suspend operator fun invoke(firebaseId: String): ResponseState<String> {
        return repository.getDeviceIdFromRemote(firebaseId)
    }
}