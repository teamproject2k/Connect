package com.example.connect.data.local_db.chats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.connect.data.models.chats.ChatMetaDataLocalEntity


@Dao
interface IChatMetaDataDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChatMetaData(chatMetaDataLocalEntity: ChatMetaDataLocalEntity): Long

    @Query("UPDATE chat_meta_data SET lastSeenChatId = :lastSeen WHERE chatId = :chatMetaDataId")
    fun updateChatListLastSeen(chatMetaDataId: String, lastSeen: Long)


    @Query("UPDATE chat_meta_data SET isChatDeleted = :isDeleted WHERE chatId = :chatMetaDataId")
    fun updateChatListDeleted(chatMetaDataId: String, isDeleted: Boolean)
}