package com.teamproject2k.connect.data.repository

import com.teamproject2k.connect.data.local_db.AppDatabase
import com.teamproject2k.connect.domain.repository.IAppLocalRepository
import javax.inject.Inject

class IAppLocalRepositoryImpl @Inject constructor(private val appDatabase: AppDatabase) :
    IAppLocalRepository {
    override suspend fun deleteAllTables() {
        appDatabase.getChatDao().deleteAllChats()
        appDatabase.getPostDao().deleteAllPosts()
        appDatabase.getStoryDao().getAllStories()
        appDatabase.getUsersDao().deleteAllUsers()
        appDatabase.getChatMetaDataDao().deleteAllChatMetaData()
    }
}