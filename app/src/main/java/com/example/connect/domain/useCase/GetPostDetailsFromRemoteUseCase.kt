package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class GetPostDetailsFromRemoteUseCase @Inject constructor(private val repository: IHomeRepository) {

    /**
     * Gets the post details from the server.
     *
     * @param fireBaseId The fire base id of the post.
     * @return A [ResponseState] containing the list of post details.
     */
    suspend fun invoke(fireBaseId: String): ResponseState<List<PostDetails>> {
        return repository.getPostDetailsFromServer(fireBaseId)
    }

}