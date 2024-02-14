package com.teamproject2k.connect.domain.useCase.user

import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class DeleteAllUsersExceptInListFromLocalUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend operator fun invoke(exceptList: List<String>): Int {
        return repository.deleteAllUsersFromLocalExceptInList(exceptList)
    }
}