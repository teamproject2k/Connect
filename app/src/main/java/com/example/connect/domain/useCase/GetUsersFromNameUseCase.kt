package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class GetUsersFromNameUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    /**
     * Invokes the repository to get users from name.
     *
     * @param name The name of the user.
     * @return A [ResponseState] containing the size of users with same name or an error.
     */
    suspend fun invoke(name: String): ResponseState<Int> {
        return repository.getUsersFromName(name)
    }

}