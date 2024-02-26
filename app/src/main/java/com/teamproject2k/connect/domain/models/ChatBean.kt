package com.teamproject2k.connect.domain.models

import android.os.Parcelable
import com.teamproject2k.connect.data.models.chats.ChatLocalEntity
import com.teamproject2k.connect.data.models.chats.ChatRemoteEntity
import com.teamproject2k.connect.domain.utils.DomainFunctionHelper
import kotlinx.parcelize.Parcelize


@Parcelize
data class ChatBean(
    val firebaseId: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val sentAt: Long,
    val modifiedAt: Long,
    var deletedBy: String,
    val mediaUrl: String,
    val mediaType: String,
    val repliedOnChatId: String?
) : Parcelable {
    fun toChatRemoteEntity(): ChatRemoteEntity {
        return ChatRemoteEntity(
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

    fun toChatLocalEntity(): ChatLocalEntity {
        return ChatLocalEntity(
            firebaseId,
            DomainFunctionHelper.getSortedChatId(senderId, receiverId),
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