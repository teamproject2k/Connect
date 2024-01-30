package com.example.connect.data.models.chats

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connect.domain.models.ChatBean

@Entity
data class ChatLocalEntity(
    @PrimaryKey
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
    fun toChatBean(): ChatBean {
        return ChatBean(
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