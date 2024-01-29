package com.example.connect.data.models.chats

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connect.domain.models.ChatBean

@Entity
data class ChatLocalEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val sentAt: Long,
    val modifiedAt: String,
    val messageStatus: String,
    val deletedBy: String
) {
    fun toChatBean(): ChatBean {
        return ChatBean(
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