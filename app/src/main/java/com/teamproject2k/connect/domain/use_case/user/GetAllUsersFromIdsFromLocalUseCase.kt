package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetAllUsersFromIdsFromLocalUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend operator fun invoke(userIdList: List<String>): List<UserBean> {
        return repository.getAllUsersFromIdsFromLocal(userIdList)
    }
}