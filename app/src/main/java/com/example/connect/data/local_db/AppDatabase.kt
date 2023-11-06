package com.example.connect.data.local_db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.connect.data.local_db.users.IUsersDao
import com.example.connect.data.local_db.users.UserDetails


@Database(entities = [UserDetails::class], version = 1)
abstract class AppDatabase() : RoomDatabase() {

    abstract fun getUsersDao(): IUsersDao
}