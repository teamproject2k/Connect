package com.teamproject2k.connect.domain.useCase.chat

import com.google.firebase.database.ChildEventListener
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class RemoveLiveObserveListenerFromRemoteUseCase @Inject constructor(private val repository: IChatRepository) {

    operator fun invoke(listener: ChildEventListener) {
        repository.removeEventListener(listener)
    }
}