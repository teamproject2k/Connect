package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetUserDetailsFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the repository to get user details from remote.
     *
     * @param userId The user id.
     * @return The response state of the user details.
     */
    suspend operator fun invoke(userId: String): ResponseState<UserBean?> {
        return repository.getUserDetailsFromRemote(userId)
    }
}