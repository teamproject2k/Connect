package com.teamproject2k.connect.data.models.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.teamproject2k.connect.data.local_db.TableNames
import com.teamproject2k.connect.domain.enums.StatusWithCurrentUserRemoteEnum
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.presentation.utils.FunctionHelper

@Entity(tableName = TableNames.USERS_TABLE_NAME)
data class UsersLocalEntity(
    @PrimaryKey
    val firebaseUserId: String,
    val connectUserId: String,
    val fcmToken: String,
    val mobileNumber: String,
    val name: String,
    val gender: String,
    val dateOfBirth: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val currentLoggedInDeviceId: String,
    val bio: String,
    val profilePhoto: String? = null,
    val coverPhoto: String? = null,
    val otherUsersStatus: MutableMap<String, String> = mutableMapOf(),
    val genderVisibility: String,
    val dobVisibility: String,
    val friendListVisibility: String,
    val savedPosts: ArrayList<String> = arrayListOf(),
    val lastActiveAt: Long
) {
    fun toUserBean(): UserBean {
        val friendList = mutableListOf<String>()
        val requestedFriendRequestList = mutableListOf<String>()
        val receivedFriendRequestList = mutableListOf<String>()
        val blockedUsersList = mutableListOf<String>()
        otherUsersStatus.forEach { entry ->
            when (entry.value) {
                StatusWithCurrentUserRemoteEnum.Friends.name -> {
                    friendList.add(entry.key)
                }

                StatusWithCurrentUserRemoteEnum.RequestedByCurrentUser.name -> {
                    requestedFriendRequestList.add(entry.key)
                }

                StatusWithCurrentUserRemoteEnum.RequestedByOtherUser.name -> {
                    receivedFriendRequestList.add(entry.key)
                }

                StatusWithCurrentUserRemoteEnum.Blocked.name -> {
                    blockedUsersList.add(entry.key)
                }
            }
        }
        return UserBean(
            firebaseUserId,
            connectUserId,
            fcmToken,
            mobileNumber,
            FunctionHelper.getFormattedDisplayName(name),
            gender,
            dateOfBirth,
            createdAt,
            modifiedAt,
            currentLoggedInDeviceId,
            bio,
            profilePhoto,
            coverPhoto,
            friendList,
            requestedFriendRequestList,
            receivedFriendRequestList,
            blockedUsersList,
            genderVisibility,
            dobVisibility,
            friendListVisibility,
            savedPosts,
            lastActiveAt
        )
    }
}