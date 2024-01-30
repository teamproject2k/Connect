package com.example.connect.domain.useCase.chat

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.repository.IChatRepository
import com.google.firebase.database.ChildEventListener
import javax.inject.Inject

class LiveObserveChatListOnRemoteUseCase @Inject constructor(private val repository: IChatRepository) {
    operator fun invoke(
        loggedInUserFirebaseId: String,
        otherUserFirebaseId: String,
        chatListState: SnapshotStateList<ChatBean>,
        onError: (errorMessage: String) -> Unit
    ): ChildEventListener {
        return repository.liveObserveChat(
            loggedInUserFirebaseId,
            otherUserFirebaseId,
            chatListState,
            onError
        )
    }
}