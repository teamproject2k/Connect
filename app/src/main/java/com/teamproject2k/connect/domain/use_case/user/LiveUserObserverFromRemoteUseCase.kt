package com.teamproject2k.connect.domain.use_case.user

import com.google.firebase.firestore.ListenerRegistration
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
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
        firebaseUserId: String, userObserverStateFlow: MutableStateFlow<ResponseState<UserBean>>
    ): ListenerRegistration {
        return repository.liveObserveUserFromRemote(firebaseUserId, userObserverStateFlow)
    }
}