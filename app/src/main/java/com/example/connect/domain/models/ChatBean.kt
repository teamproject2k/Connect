package com.example.connect.domain.models

import com.example.connect.data.models.chats.ChatLocalEntity
import com.example.connect.data.models.chats.ChatRemoteEntity

data class ChatBean(
    val firebaseId: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val sentAt: Long,
    val modifiedAt: Long,
    val messageStatus: String,
    val deletedBy: String,
    val mediaUrl: String,
    val mediaType: String
) {
    fun toChatRemoteEntity(): ChatRemoteEntity {
        return ChatRemoteEntity(
            senderId,
            receiverId,
            message,
            sentAt,
            modifiedAt,
            messageStatus,
            deletedBy,
            mediaUrl,
            mediaType
        )
    }

    fun toChatLocalEntity(): ChatLocalEntity {
        return ChatLocalEntity(
            firebaseId,
            senderId,
            receiverId,
            message,
            sentAt,
            modifiedAt,
            messageStatus,
            deletedBy,
            mediaUrl,
            mediaType
        )
    }
}