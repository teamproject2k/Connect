package com.example.connect.data.models.chats

import com.example.connect.domain.models.ChatBean

data class ChatRemoteEntity(
    val senderId: String,
    val receiverId: String,
    val message: String,
    val sentAt: Long,
    val modifiedAt: Long,
    val deletedBy: String,
    val mediaUrl: String,
    val mediaType: String,
    val repliedOnChatId: String? = null
) {
    constructor() : this("", "", "", 0, 0, "", "", "")

    fun toChatBean(firebaseId: String): ChatBean {
        return ChatBean(
            firebaseId,
            senderId,
            receiverId,
            message,
            sentAt,
            modifiedAt,
            deletedBy,
            mediaUrl,
            mediaType,
            repliedOnChatId
        )
    }
}