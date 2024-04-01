package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class DeleteStoryInRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Suspended function to delete a story from the remote repository.
     *
     * @param storyId The ID of the story to be deleted.
     * @return A [ResponseState] representing the result of the deletion operation.
     */
    suspend operator fun invoke(storyId: String): ResponseState<Nothing> {
        return repository.deleteStoryInRemote(storyId)
    }
}