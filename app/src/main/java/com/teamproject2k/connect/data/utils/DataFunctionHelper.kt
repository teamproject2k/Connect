package com.teamproject2k.connect.data.utils

import com.teamproject2k.connect.domain.enums.MessageDeleteStatusEnum

object DataFunctionHelper {
    /**
     * This function checks if the chat is deleted for everyone, or if it is deleted specifically for the sender or receiver,
     * and whether the logged-in user is the sender or receiver of the chat.
     *
     * @param chatDeletedBy The deletion status of the chat. Possible values are:
     *                      - "DeletedForEveryone": Chat deleted for everyone.
     *                      - "DeletedForSender": Chat deleted for the sender.
     *                      - "DeletedForReceiver": Chat deleted for the receiver.
     * @param senderId The ID of the sender of the chat message.
     * @param receiverId The ID of the receiver of the chat message.
     * @param loggedInUserFirebaseId The ID of the currently logged-in user.
     * @return True if the chat should not be shown to the logged-in user, false otherwise.
     */
    fun whetherNotToShowChatToLoggedInUser(
        chatDeletedBy: String,
        senderId: String,
        receiverId: String,
        loggedInUserFirebaseId: String
    ): Boolean {
        return chatDeletedBy == MessageDeleteStatusEnum.DeletedForEveryone.name
                || (chatDeletedBy == MessageDeleteStatusEnum.DeletedForSender.name && senderId == loggedInUserFirebaseId)
                || (chatDeletedBy == MessageDeleteStatusEnum.DeletedForReceiver.name && receiverId == loggedInUserFirebaseId)
    }
}