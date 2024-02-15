package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetPostDetailsFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Gets the post details from the server.
     *
     * @param postFireBaseId The fire base id of the post.
     * @return A [ResponseState] containing the list of post details.
     */
    suspend operator fun invoke(
        postFireBaseId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<List<PostBean>> {
        return repository.getPostDetailsFromRemote(postFireBaseId, loggedInUserFirebaseId)
    }
}