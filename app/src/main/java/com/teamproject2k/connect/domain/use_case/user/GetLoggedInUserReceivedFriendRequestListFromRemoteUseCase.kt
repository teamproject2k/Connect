package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetLoggedInUserReceivedFriendRequestListFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<Pair<UserBean, ArrayList<UserBean>>> {
        return repository.getLoggedInUserReceivedFriendRequestListFromRemote(loggedInUserFirebaseId)
    }
}