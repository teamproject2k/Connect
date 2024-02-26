package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateUserOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend operator fun invoke(userDetails: UserBean): Int {
        return repository.updateUserOnLocal(userDetails)
    }
}