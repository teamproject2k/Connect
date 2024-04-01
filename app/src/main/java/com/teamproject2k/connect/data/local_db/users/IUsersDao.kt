package com.teamproject2k.connect.data.local_db.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import com.teamproject2k.connect.data.models.user.UsersLocalEntity

@Dao
interface IUsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userDetails: UsersLocalEntity): Long

    @Query("UPDATE users SET currentLoggedInDeviceId = :updatedDeviceId WHERE firebaseUserId = :fireBaseId")
    fun updateDeviceId(fireBaseId: String, updatedDeviceId: String): Int

    @Query("SELECT * FROM users WHERE firebaseUserId = :fireBaseId")
    fun getUserDetails(fireBaseId: String): UsersLocalEntity?

    @Query("UPDATE users SET otherUsersStatus = :otherUsersStatus WHERE firebaseUserId = :loggedInUserFirebaseId")
    fun updateOtherUsersStatus(
        loggedInUserFirebaseId: String,
        otherUsersStatus: MutableMap<String, String>
    ): Int

    @RawQuery
    fun updateUserDetails(queryToExecute: SimpleSQLiteQuery): Long

    @Query("UPDATE users SET fcmToken = :fcmToken WHERE firebaseUserId = :loggedInUserFirebaseId")
    fun updateFCMTokenOnLocal(loggedInUserFirebaseId: String, fcmToken: String): Int

    @Query("UPDATE users SET savedPosts = :savedPostList WHERE firebaseUserId = :loggedInUserFirebaseId")
    fun updateSavedPostOnLocal(
        savedPostList: List<String>,
        loggedInUserFirebaseId: String
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserList(userList: List<UsersLocalEntity>): LongArray

    @Update
    fun updateUsersDetails(userDetails: UsersLocalEntity): Int

    @Query("SELECT * FROM users WHERE firebaseUserId IN (:userIdList) ORDER BY createdAt DESC")
    fun getAllUserFromIds(userIdList: List<String>): List<UsersLocalEntity>

    @Query("DELETE FROM users WHERE firebaseUserId NOT IN (:exceptList)")
    fun deleteAllUsersExcept(exceptList: List<String>): Int

    @Query("DELETE FROM users")
    fun deleteAllUsers()

}