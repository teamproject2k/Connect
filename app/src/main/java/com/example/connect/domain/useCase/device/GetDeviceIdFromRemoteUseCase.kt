package com.example.connect.domain.useCase.device

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IDeviceIdRepository
import javax.inject.Inject

class GetDeviceIdFromRemoteUseCase @Inject constructor(private val repository: IDeviceIdRepository) {

    suspend fun invoke(firebaseId: String): ResponseState<String> {
        return repository.getDeviceIdFromRemote(firebaseId)
    }
}