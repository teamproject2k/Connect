package com.example.connect.domain.useCase.user

import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class AddUserListToLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend fun invoke(userList: List<UsersBean>): LongArray {
        return repository.addUserListTLocal(userList)
    }
}