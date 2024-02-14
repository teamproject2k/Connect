package com.teamproject2k.connect.data.models.chats

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.teamproject2k.connect.domain.models.ChatBean

@Entity(tableName = "chat")
data class ChatLocalEntity(
    @PrimaryKey
    val firebaseId: String,
    val chatId: String,
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
    fun toChatBean(): ChatBean {
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