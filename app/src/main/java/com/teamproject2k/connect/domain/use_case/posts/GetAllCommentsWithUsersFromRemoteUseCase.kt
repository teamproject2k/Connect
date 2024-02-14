package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.CommentWithUserBean
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetAllCommentsWithUsersFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(
        postFirebaseId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<MutableMap<CommentWithUserBean, ArrayList<CommentWithUserBean>>> {
        return repository.getAllCommentsWithUsersFromRemote(postFirebaseId, loggedInUserFirebaseId)
    }
}