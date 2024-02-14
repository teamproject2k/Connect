package com.teamproject2k.connect.domain.useCase.user

import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateUserDetailsOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Updates the user details on the server.
     *
     * @param fieldsToUpdate The fields to update.
     * @param firebaseUserId The user's Firebase ID.
     * @return A response state containing either the updated user details or an error.
     */
    suspend operator fun invoke(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): ResponseState<Nothing?> {
        return repository.updateUserDetailsOnRemote(fieldsToUpdate, firebaseUserId)
    }
}