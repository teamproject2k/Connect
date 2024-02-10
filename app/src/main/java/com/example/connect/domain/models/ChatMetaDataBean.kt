package com.example.connect.domain.models

import com.example.connect.data.models.chats.ChatMetaDataLocalEntity


data class ChatMetaDataBean(
    val chatId: String,
    val lastSeenChatAt: Long,
    val isChatDeleted: Boolean = false
) {
    fun toChatMetaDataLocalEntity(): ChatMetaDataLocalEntity {
        return ChatMetaDataLocalEntity(chatId, lastSeenChatAt, isChatDeleted)
    }
}