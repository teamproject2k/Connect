package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostWithUserDetails
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetSavedPostDetailsWithUserFromLocal @Inject constructor(private val repository: IPostRepository) {
    suspend fun invoke(savedpostFirebaseIds: List<String>): ResponseState<List<PostWithUserDetails>> {
        return repository.getPostWithUserFromLocal(savedpostFirebaseIds)
    }
}