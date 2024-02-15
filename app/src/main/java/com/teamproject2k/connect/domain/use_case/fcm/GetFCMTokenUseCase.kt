package com.teamproject2k.connect.domain.use_case.fcm

import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IFCMRepository
import javax.inject.Inject

class GetFCMTokenUseCase @Inject constructor(private val repository: IFCMRepository) {

    /**
     * Invokes the repository's getFCMToken method and returns the result as a ResponseState.
     *
     * @return A ResponseState containing the fcm token or error.
     */
    suspend operator fun invoke(): ResponseState<String> {
        return repository.getFCMToken()
    }
}