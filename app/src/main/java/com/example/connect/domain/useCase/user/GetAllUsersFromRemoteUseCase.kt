package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject


class GetAllUsersNotInListFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend fun invoke(
        excludeUserIdList: List<String>,
        currentUserFirebaseId: String
    ): ResponseState<ArrayList<UsersBean>> {
        return repository.getAllUsersNotInList(excludeUserIdList, currentUserFirebaseId)
    }

}