package com.example.connect.domain.useCase.chat

import com.example.connect.domain.repository.IChatRepository
import com.google.firebase.database.ChildEventListener
import javax.inject.Inject

class RemoveLiveObserveListenerFromRemoteUseCase @Inject constructor(private val repository: IChatRepository) {

    operator fun invoke(listener: ChildEventListener) {
        repository.removeEventListener(listener)
    }
}