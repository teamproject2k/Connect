package com.example.connect.data.models.chats

import com.example.connect.domain.models.ChatsBean

data class ChatsRemoteEntity(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val sentAt: Long,
    val modifiedAt: String,
    val messageStatus: String,
    val deletedBy: String
) {
    fun toChatsBean(): ChatsBean {
        return ChatsBean(
            id,
            senderId,
            receiverId,
            message,
            sentAt,
            modifiedAt,
            messageStatus,
            deletedBy
        )
    }
}