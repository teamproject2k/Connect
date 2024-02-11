package com.example.connect.data.local_db.chats

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.connect.data.models.chats.ChatLocalEntity


@Dao
interface IChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatMessagesList(chatMessagesList: List<ChatLocalEntity>): LongArray


    @Query("DELETE FROM chat")
    fun deleteAllChats(): Int

    @Delete
    fun deleteChat(chatLocalEntity: ChatLocalEntity): Int

    @Query("SELECT * From chat WHERE chatId = :chatId ORDER BY sentAt DESC LIMIT 1")
    fun getLastMessage(chatId: String): ChatLocalEntity?

    @Query("SELECT COUNT(*) FROM chat WHERE chatId = :chatId AND sentAt > :lastSeenChatAt")
    fun getUnreadMessageCount(
        chatId: String,
        lastSeenChatAt: Long,
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: ChatLocalEntity): Long

    @Update
    fun updateMessage(message: ChatLocalEntity): Int
}