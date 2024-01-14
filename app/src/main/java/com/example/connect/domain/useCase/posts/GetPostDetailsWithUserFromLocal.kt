package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostWithUserDetails
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetPostDetailsWithUserFromLocal @Inject constructor(private val repository: IPostRepository) {
    suspend fun invoke(savedPostIds: List<String>): ResponseState<List<PostWithUserDetails>> {
        return repository.getPostWithUserFromLocal(savedPostIds)
    }
}