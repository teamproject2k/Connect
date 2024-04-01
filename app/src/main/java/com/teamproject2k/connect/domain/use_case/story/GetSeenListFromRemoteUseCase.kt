package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StorySeenTimeWithUserDetailsBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class GetSeenListFromRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Suspended function to retrieve the list of users who have seen a particular story from the remote repository.
     *
     * @param storyId The ID of the story to retrieve the seen list for.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing a list of [StorySeenTimeWithUserDetailsBean] objects representing users who have seen the story.
     */
    suspend operator fun invoke(
        storyId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<StorySeenTimeWithUserDetailsBean>> {
        return repository.getSeenListFromRemote(storyId, loggedInUserFirebaseId)
    }
}
