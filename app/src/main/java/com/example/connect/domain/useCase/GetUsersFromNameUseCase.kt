package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class GetUsersFromNameUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    suspend fun invoke(name: String): ResponseState<Int> {
        return repository.getUsersFromName(name)
    }

}