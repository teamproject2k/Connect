package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetLoggedInUserRequestedUserListFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Suspended function to fetch requested user list from remote repository.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing a pair of logged-in user information and a list of requested users.
     */
    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<Pair<UserBean, ArrayList<UserBean>>> {
        return repository.getRequestedByLoggedInUserListFromRemoteFromRemote(loggedInUserFirebaseId)
    }
}