package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.CommentWithUser
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetAllCommentsWithUsersFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend fun invoke(
        postFirebaseId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<MutableMap<CommentWithUser, ArrayList<CommentWithUser>>> {
        return repository.getAllCommentsWithUsersFromRemote(postFirebaseId, loggedInUserFirebaseId)
    }
}