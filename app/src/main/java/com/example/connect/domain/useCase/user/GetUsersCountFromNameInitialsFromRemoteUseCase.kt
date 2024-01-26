package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetUsersCountFromNameInitialsFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the repository to get user count from name from remote.
     *
     * @param connectIdFirstPart The user name.
     * @return The response state.
     */
    suspend operator fun invoke(connectIdFirstPart: String): ResponseState<Int> {
        return repository.getUsersCountFromNameInitialsFromRemote(connectIdFirstPart)
    }
}