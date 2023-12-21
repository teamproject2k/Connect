package com.example.connect.data.models.chats

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatDbEntity(
    @PrimaryKey
    val id: String,
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val postScope: String,
    val postType: String
) {
//    fun toChatBean(): ChatDbBean {
//        return PostBean(
//            id,
//            fireBaseUserId,
//            mediaUrl,
//            caption,
//            createdAt,
//            postScope,
//            postType
//        )
//    }
}