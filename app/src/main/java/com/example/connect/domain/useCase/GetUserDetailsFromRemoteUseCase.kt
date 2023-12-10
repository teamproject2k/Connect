package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class GetUserDetailsFromRemoteUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    suspend fun invoke(userId: String): ResponseState<UserDetails?> {
        return repository.getUserDetails(userId)
    }

}