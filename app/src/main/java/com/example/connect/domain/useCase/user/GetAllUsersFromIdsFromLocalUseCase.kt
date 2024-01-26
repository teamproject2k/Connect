package com.example.connect.domain.useCase.user

import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetAllUsersFromIdsFromLocalUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend operator fun invoke(userIdList: List<String>): List<UsersBean> {
        return repository.getAllUsersFromIdsFromLocal(userIdList)
    }
}