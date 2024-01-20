package com.example.connect.domain.useCase.user

import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class DeleteAllUserFromLocalExceptInList @Inject constructor(private val repository: IUserRepository) {

    suspend fun invoke(exceptList: List<String>): Int {
        return repository.deleteAllUsersFromLocalExcept(exceptList)
    }
}