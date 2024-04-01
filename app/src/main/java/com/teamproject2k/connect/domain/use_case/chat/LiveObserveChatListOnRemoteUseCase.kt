package com.teamproject2k.connect.domain.use_case.chat

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.database.ChildEventListener
import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class LiveObserveChatListOnRemoteUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Sets up a live observation of chat messages between the logged-in user and another user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param otherUserFirebaseId The Firebase ID of the other user in the chat.
     * @param chatListState The state list where chat messages will be observed.
     * @param onError Callback function invoked in case of an error with the error message as the parameter.
     * @return The child event listener used for observing chat messages.
     */
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