package com.example.connect.domain.useCase.device

import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class GetDeviceIdFromRemoteUseCase @Inject constructor(private val repository: IHomeRepository) {

    suspend fun invoke(firebaseId: String): ResponseState<String> {
        return repository.getDeviceIdFromRemote(firebaseId)
    }
}