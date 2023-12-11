package com.example.connect.domain.useCase.user

import com.example.connect.common.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class GetUserDetailsFromRemoteUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    /**
     * Invokes the repository to get user details from remote.
     *
     * @param userId The user id.
     * @return The response state.
     */
    suspend fun invoke(userId: String): ResponseState<UsersBean?> {
        return repository.getUserDetailsFromRemote(userId)
    }
}