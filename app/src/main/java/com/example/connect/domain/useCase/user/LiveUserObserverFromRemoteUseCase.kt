package com.example.connect.domain.useCase.user

import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class LiveUserObserverFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the repository to observe user from remote.
     *
     * @param firebaseUserId The firebase user id.
     * @param userObserverStateFlow The user observer state flow.
     * @return The listener registration.
     */
    suspend operator fun invoke(
        firebaseUserId: String, userObserverStateFlow: MutableStateFlow<ResponseState<UsersBean>>
    ): ListenerRegistration {
        return repository.liveObserveUserFromRemote(firebaseUserId, userObserverStateFlow)
    }
}