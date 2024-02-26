package com.teamproject2k.connect.data.local_db.chats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.teamproject2k.connect.data.models.chats.ChatMetaDataLocalEntity


@Dao
interface IChatMetaDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChatMetaDataList(chatMetaDataList: List<ChatMetaDataLocalEntity>): LongArray

    @Query("SELECT * FROM chat_meta_data WHERE isChatDeleted=0 ORDER BY lastSeenChatAt DESC")
    fun getAllChatMetaDatList(): List<ChatMetaDataLocalEntity>

    @Query("UPDATE chat_meta_data SET lastSeenChatAt = :lastSeen WHERE chatId = :chatMetaDataId")
    fun updateChatListLastSeen(chatMetaDataId: String, lastSeen: Long): Int

    @Query("UPDATE chat_meta_data SET isChatDeleted = :isDeleted WHERE chatId = :chatMetaDataId")
    fun updateChatListDeleted(chatMetaDataId: String, isDeleted: Boolean)


    @Query("DELETE FROM chat_meta_data")
    fun deleteAllChatMetaData()

}