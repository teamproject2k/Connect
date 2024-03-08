package com.teamproject2k.connect.domain.repository

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.database.ChildEventListener
import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.models.ChatMetaDataBean
import com.teamproject2k.connect.domain.models.ChatWithUserAndCountBean
import com.teamproject2k.connect.domain.models.UserWithChatListBean
import com.teamproject2k.connect.domain.network_utils.ResponseState

interface IChatRepository {
    /**
     * Retrieves a list of chats for the logged-in user from the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing an array list of [UserWithChatListBean] objects representing
     *         users along with their chat lists. If successful, returns the list; otherwise, contains an error message.
     */
    suspend fun getChatListFromRemote(loggedInUserFirebaseId: String): ResponseState<ArrayList<UserWithChatListBean>>

    /**
     * Deletes all chats from the local database.
     *
     * @return The number of chats deleted from the local database.
     */
    suspend fun deleteAllChats(): Int

    /**
     * Deletes a chat from the local database.
     *
     * @param chatBean The chat to be deleted.
     * @return The number of chats deleted from the local database.
     */
    suspend fun deleteChat(chatBean: ChatBean): Int

    /**
     * Sets up a live observation of chat messages between the logged-in user and another user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param otherUserFirebaseId The Firebase ID of the other user in the chat.
     * @param chatListState The state list where chat messages will be observed.
     * @param onError Callback function invoked in case of an error with the error message as the parameter.
     * @return The child event listener used for observing chat messages.
     */
    fun liveObserveChat(
        loggedInUserFirebaseId: String,
        otherUserFirebaseId: String,
        chatListState: SnapshotStateList<ChatBean>,
        onError: (errorMessage: String) -> Unit
    ): ChildEventListener

    /**
     * Removes a child event listener from the database reference.
     *
     * @param eventListener The child event listener to be removed.
     */
    fun removeEventListener(eventListener: ChildEventListener)

    /**
     * Sends a chat message to the remote server.
     *
     * @param message The chat message to be sent.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     *         otherwise, contains an error message.
     */
    suspend fun sendChatMessageOnRemote(message: ChatBean): ResponseState<Nothing>

    /**
     * Deletes a message on the remote server.
     *
     * @param deletedBy The Firebase ID of the user who initiated the message deletion.
     * @param senderId The Firebase ID of the message sender.
     * @param receiverId The Firebase ID of the message receiver.
     * @param messageId The ID of the message to be deleted.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     *         otherwise, contains an error message.
     */
    suspend fun deleteMessageOnRemote(
        deletedBy: String,
        senderId: String,
        receiverId: String,
        messageId: String
    ): ResponseState<Nothing>

    /**
     * Adds a list of chat metadata to the local database.
     *
     * @param chatMetaDataList The list of chat metadata to be added to the local database.
     * @return An array of long values representing the IDs of the inserted chat metadata in the local database.
     *         The order of IDs corresponds to the order of chat metadata in the input list.
     */
    suspend fun addChatMetaDataToLocal(chatMetaDataList: List<ChatMetaDataBean>): LongArray

    /**
     * Adds a list of chats to the local database.
     *
     * @param chatList The list of chats to be added to the local database.
     * @return An array of long values representing the IDs of the inserted chats in the local database.
     *         The order of IDs corresponds to the order of chats in the input list.
     */
    suspend fun addChatListToLocal(chatList: List<ChatBean>): LongArray

    /**
     * Retrieves a list of users with their last message and unread message count for the logged-in user from the local database.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A list of [ChatWithUserAndCountBean] objects containing user details, their last message, and unread message count.
     */
    suspend fun getUserWithLastMessageWithUnreadCount(loggedInUserFirebaseId: String): List<ChatWithUserAndCountBean>

    /**
     * Updates the last seen timestamp for a chat in the local database.
     *
     * @param chatId The ID of the chat for which the last seen timestamp is being updated.
     * @param lastSeenAt The new last seen timestamp.
     * @return The number of chats updated in the local database.
     */
    suspend fun updateLastSeenAtOnLocal(
        chatId: String,
        lastSeenAt: Long
    ): Int

    /**
     * Updates a chat in the local database.
     *
     * @param chatBean The chat to be updated.
     * @return The number of chats updated in the local database.
     */
    suspend fun updateChatOnLocal(chatBean: ChatBean): Int
}