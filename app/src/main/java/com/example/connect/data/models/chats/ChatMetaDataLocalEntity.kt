package com.example.connect.data.models.chats

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("chat_meta_data")
data class ChatMetaDataLocalEntity(
    @PrimaryKey
    val chatId: String,
    val lastSeenChatId: Long,
    val isChatDeleted: Boolean = false
)