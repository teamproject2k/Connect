package com.example.connect.domain.useCase.user

import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject


class GetAllUsersNotInListFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Gets all users that are not in the exclude list.
     *
     * @param excludeUserIdList The list of user ids to exclude.
     * @param loggedInUserFirebaseId The current user's firebase id.
     * @return A response state containing the list of users.
     */
    suspend operator fun invoke(
        excludeUserIdList: List<String>,
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<UsersBean>> {
        return repository.getAllUsersNotInListFromRemote(excludeUserIdList, loggedInUserFirebaseId)
    }
}