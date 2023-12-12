package com.example.connect.domain.useCase.user

import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetUsersFromNameUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend fun invoke(name: String): ResponseState<Int> {
        return repository.getUsersCountFromNameFromRemote(name)
    }

}