package com.example.connect.domain.useCase.user

import com.example.connect.common.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class GetUserDetailsFromRemoteUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    suspend fun invoke(userId: String): ResponseState<UsersBean?> {
        return repository.getUserDetailsFromRemote(userId)
    }

}