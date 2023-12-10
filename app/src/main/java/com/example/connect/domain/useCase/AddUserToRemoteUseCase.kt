package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class AddUserToRemoteUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    /**
     * Invokes the repository to add a user to the remote.
     *
     * @param userDetails The user details to add.
     * @return A response state containing the result of the operation.
     */
    suspend fun invoke(userDetails: UserDetails): ResponseState<Nothing> {
        return repository.addUserToRemote(userDetails)
    }
}