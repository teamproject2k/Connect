package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class DeleteAllUsersExceptInListFromLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Suspended function to delete all users from the local repository except those in the provided list of user IDs.
     *
     * @param exceptList The list of user IDs to exclude from deletion.
     * @return The number of users deleted from the local repository.
     */
    suspend operator fun invoke(exceptList: List<String>): Int {
        return repository.deleteAllUsersFromLocalExceptInList(exceptList)
    }
}