package com.example.connect.domain.utils

object DomainFunctionHelper {
    fun getSortedChatId(senderId: String, receiverId: String): String {
        return if (senderId < receiverId) senderId + receiverId else receiverId + senderId
    }
}