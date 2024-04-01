package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetAllUsersFromIdsFromLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Suspended function to retrieve user information for a list of user IDs from the local repository.
     *
     * @param userIdList The list of user IDs to fetch user information for.
     * @return A list of [UserBean] objects corresponding to the provided user IDs.
     */
    suspend operator fun invoke(userIdList: List<String>): List<UserBean> {
        return repository.getAllUsersFromIdsFromLocal(userIdList)
    }
}