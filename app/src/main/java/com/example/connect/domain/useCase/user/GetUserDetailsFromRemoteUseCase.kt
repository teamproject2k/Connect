package com.example.connect.domain.useCase.user

import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetUserDetailsFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the repository to get user details from remote.
     *
     * @param userId The user id.
     * @return The response state of the user details.
     */
    suspend operator fun invoke(userId: String): ResponseState<UsersBean?> {
        return repository.getUserDetailsFromRemote(userId)
    }
}