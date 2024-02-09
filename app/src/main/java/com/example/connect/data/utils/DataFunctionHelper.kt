package com.example.connect.data.utils

object DataFunctionHelper {
    fun getSortedChatId(senderId: String, receiverId: String): String {
        return if (senderId < receiverId) senderId + receiverId else receiverId + senderId
    }
}