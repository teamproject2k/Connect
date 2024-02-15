package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class AddUserListToLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend operator fun invoke(userList: List<UsersBean>): LongArray {
        return repository.addUserListToLocal(userList)
    }
}