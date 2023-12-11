package com.example.connect.domain.useCase.user

import com.example.connect.common.ResponseState
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class GetUserDetailsFromIdsFromRemote @Inject constructor(private val repository: IHomeRepository) {

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