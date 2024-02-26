package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject


class GetAllUsersNotInListFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Gets all users that are not in the exclude list.
     *
     * @param excludeUserIdList The list of user ids to exclude.
     * @param loggedInUserFirebaseId The current user's firebase id.
     * @return A response state containing the list of users.
     */
    suspend operator fun invoke(
        excludeUserIdList: List<String>,
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<UserBean>> {
        return repository.getAllUsersNotInListFromRemote(excludeUserIdList, loggedInUserFirebaseId)
    }
}