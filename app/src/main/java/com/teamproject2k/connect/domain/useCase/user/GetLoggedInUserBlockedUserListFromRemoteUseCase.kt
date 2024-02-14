package com.teamproject2k.connect.domain.useCase.user

import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetLoggedInUserBlockedUserListFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<Pair<UsersBean, ArrayList<UsersBean>>> {
        return repository.getLoggedInUserBlockedListFromRemote(loggedInUserFirebaseId)
    }
}