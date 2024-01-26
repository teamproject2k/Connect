package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetPostDetailsFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Gets the post details from the server.
     *
     * @param fireBaseId The fire base id of the post.
     * @return A [ResponseState] containing the list of post details.
     */
    suspend operator fun invoke(
        fireBaseId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<List<PostBean>> {
        return repository.getPostDetailsFromRemote(fireBaseId, loggedInUserFirebaseId)
    }
}