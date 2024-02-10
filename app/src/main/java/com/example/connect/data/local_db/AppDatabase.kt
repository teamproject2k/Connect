package com.example.connect.data.local_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.connect.data.local_db.chats.IChatDao
import com.example.connect.data.local_db.chats.IChatMetaDataDao
import com.example.connect.data.local_db.posts.IPostDao
import com.example.connect.data.local_db.stories.IStoryDao
import com.example.connect.data.local_db.users.IUsersDao
import com.example.connect.data.models.chats.ChatLocalEntity
import com.example.connect.data.models.chats.ChatMetaDataLocalEntity
import com.example.connect.data.models.post.PostLocalEntity
import com.example.connect.data.models.story.StoryLocalEntity
import com.example.connect.data.models.user.UsersLocalEntity

@Database(
    entities = [UsersLocalEntity::class, PostLocalEntity::class, StoryLocalEntity::class, ChatMetaDataLocalEntity::class, ChatLocalEntity::class],
    version = 1
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getUsersDao(): IUsersDao
    abstract fun getPostDao(): IPostDao
    abstract fun getStoryDao(): IStoryDao

    abstract fun getChatMetaDataDao(): IChatMetaDataDao


    abstract fun getChatDao(): IChatDao
}