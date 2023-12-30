package com.example.connect.domain.useCase.user

import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateFcmTokenOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend fun invoke(currentUserFirebaseId: String, updatedToken: String): Int {
        return repository.updateFCMTokenOnLocal(currentUserFirebaseId, updatedToken)
    }
}