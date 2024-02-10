package com.example.connect.data.local_db.chats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.connect.data.models.chats.ChatLocalEntity
import com.example.connect.domain.models.ChatBean


@Dao
interface IChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatMessagesList(chatMessagesList: List<ChatLocalEntity>): LongArray

    @Query("SELECT * From chat WHERE chatId = :chatId AND deletedBy NOT LIKE '%' || :loggedInUserFirebaseId || '%'  ORDER BY sentAt DESC LIMIT 1")
    fun getLastMessage(chatId: String, loggedInUserFirebaseId: String): ChatLocalEntity?

    @Query("SELECT COUNT(*) FROM chat WHERE chatId = :chatId AND sentAt > :lastSeenChatAt ")
    fun getUnreadMessageCount(chatId: String, lastSeenChatAt: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: ChatLocalEntity): Long

}