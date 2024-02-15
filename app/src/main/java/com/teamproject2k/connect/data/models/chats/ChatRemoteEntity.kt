package com.teamproject2k.connect.data.models.chats

import com.teamproject2k.connect.domain.models.ChatBean


@Suppress("unused")
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
    constructor() : this(
        senderId = "",
        receiverId = "",
        message = "",
        sentAt = 0,
        modifiedAt = 0,
        deletedBy = "",
        mediaUrl = "",
        mediaType = ""
    )

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