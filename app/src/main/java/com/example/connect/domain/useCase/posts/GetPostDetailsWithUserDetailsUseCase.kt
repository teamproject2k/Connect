package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetPostDetailsWithUserDetailsUseCase @Inject constructor(private val repository: IPostRepository) {

    suspend fun invoke(currentUserFirebaseId: String): ResponseState<Pair<List<PostBean>, List<UsersBean>>> {
        return repository.getAllPostsWithUserDetailsFromRemote(currentUserFirebaseId)
    }
}