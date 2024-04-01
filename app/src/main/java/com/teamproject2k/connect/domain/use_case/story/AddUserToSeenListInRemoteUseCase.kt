package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddUserToSeenListInRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Suspended function to add a user to the seen list of a story in the remote repository.
     *
     * @param storyId The ID of the story.
     * @param storySeenBy The ID of the user who has seen the story.
     * @param storySeenAt The timestamp when the story was seen.
     * @return A [ResponseState] representing the result of the operation.
     */
    suspend operator fun invoke(
        storyId: String,
        storySeenBy: String,
        storySeenAt: Long
    ): ResponseState<Nothing> {
        return repository.addUserToSeenListInRemote(storyId, storySeenBy, storySeenAt)
    }
}