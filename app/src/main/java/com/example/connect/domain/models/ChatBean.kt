package com.example.connect.domain.models

import com.example.connect.data.models.chats.ChatLocalEntity
import com.example.connect.data.models.chats.ChatRemoteEntity

data class ChatBean(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val sentAt: Long,
    val modifiedAt: String,
    val messageStatus: String,
    val deletedBy: String
) {
    fun toChatRemoteEntity(): ChatRemoteEntity {
        return ChatRemoteEntity(
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

    fun toChatLocalEntity(): ChatLocalEntity {
        return ChatLocalEntity(
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