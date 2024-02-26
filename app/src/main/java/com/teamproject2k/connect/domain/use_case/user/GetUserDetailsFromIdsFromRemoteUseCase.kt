package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetUserDetailsFromIdsFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Gets user details from a list of user ids.
     *
     * @param idList The list of user ids.
     * @return A [ResponseState] containing the user details.
     */
    suspend operator fun invoke(idList: List<String>): ResponseState<List<UserBean>> {
        return repository.getUserDetailsFromIdsFromRemote(idList)
    }
}