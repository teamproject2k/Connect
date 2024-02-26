package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddUserToSeenListInRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(
        storyId: String,
        storySeenBy: String,
        storySeenAt: Long
    ): ResponseState<Nothing> {
        return repository.addUserToSeenListInRemote(storyId, storySeenBy, storySeenAt)
    }
}