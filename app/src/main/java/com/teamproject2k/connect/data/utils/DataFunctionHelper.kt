package com.teamproject2k.connect.data.utils

import com.teamproject2k.connect.domain.enums.MessageDeleteStatusEnum

object DataFunctionHelper {

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