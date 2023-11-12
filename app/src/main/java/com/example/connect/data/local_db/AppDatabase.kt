package com.example.connect.data.local_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.connect.data.local_db.posts.IPostDao
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.IUsersDao
import com.example.connect.data.local_db.users.UserDetails


@Database(entities = [UserDetails::class, PostDetails::class], version = 1)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getUsersDao(): IUsersDao


    abstract fun getPostDao(): IPostDao
}