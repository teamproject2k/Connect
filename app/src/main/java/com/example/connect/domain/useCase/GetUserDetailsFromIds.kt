package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class GetUserDetailsFromIds @Inject constructor(private val repository: IHomeRepository) {

    /**
     * Gets user details from a list of user ids.
     *
     * @param idList The list of user ids.
     * @return A [ResponseState] containing the user details.
     */
    suspend fun invoke(idList: List<String>): ResponseState<List<UserDetails>> {
        return repository.getUserDetailsFromIds(idList)
    }
}