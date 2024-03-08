package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StoriesWithUserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class GetAllStoriesWithUserFormRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Suspended function to retrieve all stories with user details from the remote repository.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing a list of [StoriesWithUserBean] objects representing all stories with user details.
     */
    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<ArrayList<StoriesWithUserBean>> {
        return repository.getAllStoriesWithUserDetailsFromRemote(loggedInUserFirebaseId)
    }
}