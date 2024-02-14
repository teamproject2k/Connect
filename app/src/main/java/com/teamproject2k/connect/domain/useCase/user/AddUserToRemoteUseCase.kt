package com.teamproject2k.connect.domain.useCase.user

import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class AddUserToRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the repository to add a user to the remote.
     *
     * @param userDetails The user details to add.
     * @return A response state containing the result of the operation.
     */
    suspend operator fun invoke(userDetails: UsersBean): ResponseState<Nothing> {
        return repository.addUserToRemote(userDetails)
    }
}