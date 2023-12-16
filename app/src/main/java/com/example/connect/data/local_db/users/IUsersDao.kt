package com.example.connect.data.local_db.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.connect.data.models.user.UsersDbEntity

@Dao
interface IUsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userDetails: UsersDbEntity): Long

    @Query("UPDATE UsersDbEntity SET currentLoggedInDeviceId = :updatedDeviceId WHERE firebaseUserId = :fireBaseId")
    fun updateDeviceId(fireBaseId: String, updatedDeviceId: String): Int

    @Query("SELECT * FROM UsersDbEntity WHERE firebaseUserId = :fireBaseId")
    fun getUserDetails(fireBaseId: String): UsersDbEntity?


    @Query("UPDATE UsersDbEntity SET otherUsersStatus = :otherUsersStatus WHERE firebaseUserId = :currentUserFirebaseId")
    fun updateOtherUsersStatus(
        currentUserFirebaseId: String,
        otherUsersStatus: MutableMap<String, String>
    ): Int

    @RawQuery
    fun updateUserDetails(queryToExecute: SimpleSQLiteQuery): Long

}