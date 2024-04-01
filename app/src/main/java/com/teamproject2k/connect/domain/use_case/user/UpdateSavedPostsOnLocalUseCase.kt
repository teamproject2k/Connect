package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateSavedPostsOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the function to update the list of saved posts for the logged-in user in the local database.
     *
     * @param loggedInUserFireBaseId The Firebase ID of the logged-in user.
     * @param savedPostList The updated list of post Firebase IDs to be saved for the logged-in user.
     * @return The number of saved posts updated in the local database.
     */
    suspend operator fun invoke(loggedInUserFireBaseId: String, savedPostList: List<String>): Int {
        return repository.updateSavedPostOnLocal(loggedInUserFireBaseId, savedPostList)
    }
}