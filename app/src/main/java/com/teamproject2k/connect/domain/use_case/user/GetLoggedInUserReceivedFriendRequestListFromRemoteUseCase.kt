package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetLoggedInUserReceivedFriendRequestListFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Suspended function to retrieve the list of friend requests received by the logged-in user from the remote repository.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing a pair of the logged-in user's information and a list of received friend requests.
     */
    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<Pair<UserBean, ArrayList<UserBean>>> {
        return repository.getLoggedInUserReceivedFriendRequestListFromRemote(loggedInUserFirebaseId)
    }
}