package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetAllCommentsUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend fun invoke(
        postId: String,
        loggedInUserFireId: String
    ): ResponseState<Pair<List<CommentBean>, List<UsersBean>>> {
        return repository.getAllCommentsFromRemote(postId, loggedInUserFireId)
    }
}