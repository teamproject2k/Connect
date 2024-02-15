package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class SavePostOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.savePostOnRemote(loggedInUserFirebaseId, postFirebaseId)
    }
}