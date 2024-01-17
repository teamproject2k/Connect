package com.example.connect.domain.models

import com.example.connect.data.models.chats.ChatsDbEntity
import com.example.connect.data.models.chats.ChatsRemoteEntity

data class ChatsBean(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val sentAt: Long,
    val modifiedAt: String,
    val messageStatus: String,
    val deletedBy: String
) {
    fun toChatsRemoteEntity(): ChatsRemoteEntity {
        return ChatsRemoteEntity(
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

    fun toChatsDbEntity(): ChatsDbEntity {
        return ChatsDbEntity(
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