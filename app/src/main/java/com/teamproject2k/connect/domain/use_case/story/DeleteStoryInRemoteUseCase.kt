package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class DeleteStoryInRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(storyId: String): ResponseState<Nothing> {
        return repository.deleteStoryInRemote(storyId)
    }
}