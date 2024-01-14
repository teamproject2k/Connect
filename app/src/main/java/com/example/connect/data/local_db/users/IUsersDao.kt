package com.example.connect.data.local_db.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.connect.data.models.user.UsersDbEntity

@Dao
interface IUsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userDetails: UsersDbEntity): Long

    @Query("UPDATE users SET currentLoggedInDeviceId = :updatedDeviceId WHERE firebaseUserId = :fireBaseId")
    fun updateDeviceId(fireBaseId: String, updatedDeviceId: String): Int

    @Query("SELECT * FROM users WHERE firebaseUserId = :fireBaseId")
    fun getUserDetails(fireBaseId: String): UsersDbEntity?


    @Query("UPDATE users SET otherUsersStatus = :otherUsersStatus WHERE firebaseUserId = :currentUserFirebaseId")
    fun updateOtherUsersStatus(
        currentUserFirebaseId: String,
        otherUsersStatus: MutableMap<String, String>
    ): Int

    @RawQuery
    fun updateUserDetails(queryToExecute: SimpleSQLiteQuery): Long

    @Query("UPDATE users SET fcmToken = :fcmToken WHERE firebaseUserId = :currentUserFirebaseId")
    fun updateFCMTokenOnLocal(currentUserFirebaseId: String, fcmToken: String): Int

    @Query("UPDATE users SET savedPosts = :savedPostList WHERE firebaseUserId = :currentUserFirebaseId")
    fun updateSavedPostOnLocal(
        savedPostList: List<String>,
        currentUserFirebaseId: String
    ): Int


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserList(userList: List<UsersDbEntity>): LongArray


    @Update
    fun updateUsersDetails(userDetails: UsersDbEntity): Int

}