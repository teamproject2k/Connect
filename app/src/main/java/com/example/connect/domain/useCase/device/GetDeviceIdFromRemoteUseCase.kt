package com.example.connect.domain.useCase.device

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IDeviceIdRepository
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