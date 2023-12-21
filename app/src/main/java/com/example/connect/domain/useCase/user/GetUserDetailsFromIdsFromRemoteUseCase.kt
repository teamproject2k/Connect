package com.example.connect.domain.useCase.user

import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetUserDetailsFromIdsFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Gets user details from a list of user ids.
     *
     * @param idList The list of user ids.
     * @return A [ResponseState] containing the user details.
     */
    suspend fun invoke(idList: List<String>): ResponseState<List<UsersBean>> {
        return repository.getUserDetailsFromIdsFromRemote(idList)
    }
}