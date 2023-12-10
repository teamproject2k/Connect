package com.example.connect.domain.useCase.user

import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class GetUsersFromNameUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    suspend fun invoke(name: String): ResponseState<Int> {
        return repository.getUsersCountFromNameFromRemote(name)
    }

}