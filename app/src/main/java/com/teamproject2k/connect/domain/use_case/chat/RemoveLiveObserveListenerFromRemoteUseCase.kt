package com.teamproject2k.connect.domain.use_case.chat

import com.google.firebase.database.ChildEventListener
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class RemoveLiveObserveListenerFromRemoteUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Removes a child event listener from the repository.
     *
     * @param listener The child event listener to be removed.
     */
    operator fun invoke(listener: ChildEventListener) {
        repository.removeEventListener(listener)
    }
}