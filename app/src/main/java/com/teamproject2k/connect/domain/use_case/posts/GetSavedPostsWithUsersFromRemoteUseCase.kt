package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.PostWithUserDetailsBean
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetSavedPostsWithUsersFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {

    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        savedPosts: ArrayList<String>
    ): ResponseState<List<PostWithUserDetailsBean>> {
        return repository.getSavedPostsWithUsersFromRemote(loggedInUserFirebaseId, savedPosts)
    }
}