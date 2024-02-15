package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetUsersCountFromNameInitialsFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the repository to get user count from name from remote.
     *
     * @param connectIdFirstPart The user name.
     * @return The response state.
     */
    suspend operator fun invoke(connectIdFirstPart: String): ResponseState<Int> {
        return repository.getUsersCountFromNameInitialsFromRemote(connectIdFirstPart)
    }
}