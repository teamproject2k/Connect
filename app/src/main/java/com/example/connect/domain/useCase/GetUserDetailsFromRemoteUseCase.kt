package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class GetUserDetailsFromRemoteUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    /**
     * Invokes the repository to get user details.
     *
     * @param userId The user ID.
     * @return A [ResponseState] containing the user details or an error.
     */
    suspend fun invoke(userId: String): ResponseState<UserDetails?> {
        return repository.getUserDetails(userId)
    }

}