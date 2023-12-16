package com.example.connect.domain.useCase.user

import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateOtherUserStatusUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend fun invoke(
        currentUserFirebaseId: String,
        otherUsersStatus: MutableMap<String, String>
    ): Int {
        return repository.updateOtherUsersStatusOnDb(currentUserFirebaseId, otherUsersStatus)
    }
}