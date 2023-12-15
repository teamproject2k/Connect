package com.example.connect.domain.useCase.user

import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class AcceptFriendRequestUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend fun invoke(
        currentUserFirebaseId: String,
        requestUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.acceptFriendRequest(currentUserFirebaseId, requestUserFirebaseId)
    }
}