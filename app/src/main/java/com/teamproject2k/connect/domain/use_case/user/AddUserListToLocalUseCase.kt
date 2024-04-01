package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class AddUserListToLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Suspended function to add a list of users to the local repository.
     *
     * @param userList The list of [UserBean] objects to add to the local repository.
     * @return An array of IDs representing the newly added users.
     */
    suspend operator fun invoke(userList: List<UserBean>): LongArray {
        return repository.addUserListToLocal(userList)
    }
}