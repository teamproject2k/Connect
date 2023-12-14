package com.example.connect.data.local_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.connect.data.local_db.posts.IPostDao
import com.example.connect.data.local_db.users.IUsersDao
import com.example.connect.data.models.post.PostDbEntity
import com.example.connect.data.models.user.UsersDbEntity


@Database(entities = [UsersDbEntity::class, PostDbEntity::class], version = 1)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getUsersDao(): IUsersDao

    abstract fun getPostDao(): IPostDao
}