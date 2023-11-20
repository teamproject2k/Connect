package com.example.connect.data.local_db.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery

@Dao
interface IUsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userDetails: UserDetails): Long

    @Query("UPDATE UserDetails SET currentLoggedInDeviceId = :updatedDeviceId WHERE firebaseUserId = :fireBaseId")
    fun updateDeviceId(fireBaseId: String, updatedDeviceId: String): Int

    @Query("SELECT * FROM UserDetails WHERE firebaseUserId = :fireBaseId")
    fun getUserDetails(fireBaseId: String): UserDetails?

    @RawQuery
    fun updateUserDetails(queryToExecute: SimpleSQLiteQuery): Long
}