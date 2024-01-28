package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostWithUserDetailsBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetPostDetailsWithUsersFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {

    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        loggedInUserBlockedList: List<String>
    ): ResponseState<List<PostWithUserDetailsBean>> {
        return repository.getPostDetailsWithUsersFromLocal(
            loggedInUserFirebaseId,
            loggedInUserBlockedList
        )
    }

}