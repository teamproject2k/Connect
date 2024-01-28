package com.example.connect.domain.useCase.user

import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetLoggedInUserRequestedUserListFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<Pair<UsersBean, ArrayList<UsersBean>>> {
        return repository.getRequestedByLoggedInUserListFromRemoteFromRemote(loggedInUserFirebaseId)
    }
}