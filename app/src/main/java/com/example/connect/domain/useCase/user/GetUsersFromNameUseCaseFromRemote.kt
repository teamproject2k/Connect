package com.example.connect.domain.useCase.user

import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class GetUsersFromNameUseCaseFromRemote @Inject constructor(private val repository: IAuthenticationRepository) {

    /**
     * Invokes the repository to get user count from name from remote.
     *
     * @param name The user name.
     * @return The response state.
     */
    suspend fun invoke(name: String): ResponseState<Int> {
        return repository.getUsersCountFromNameFromRemote(name)
    }

}