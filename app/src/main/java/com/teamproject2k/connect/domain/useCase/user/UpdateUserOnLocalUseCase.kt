package com.teamproject2k.connect.domain.useCase.user

import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateUserOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend operator fun invoke(userDetails: UsersBean): Int {
        return repository.updateUserOnLocal(userDetails)
    }
}