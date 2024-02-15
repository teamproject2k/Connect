package com.teamproject2k.connect.domain.models

import android.os.Parcelable
import com.teamproject2k.connect.data.models.user.UserRemoteEntity
import com.teamproject2k.connect.data.models.user.UsersLocalEntity
import com.teamproject2k.connect.domain.enums.StatusWithCurrentUserRemoteEnum
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsersBean(
    val firebaseUserId: String,
    val connectUserId: String,
    val fcmToken: String,
    val mobileNumber: String,
    var name: String,
    var gender: String,
    var dateOfBirth: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val currentLoggedInDeviceId: String,
    var bio: String,
    var profilePhoto: String? = null,
    var coverPhoto: String? = null,
    val friendList: MutableList<String> = mutableListOf(),
    val requestedFriendRequestList: MutableList<String> = mutableListOf(),
    val receivedFriendRequestList: MutableList<String> = mutableListOf(),
    val blockedUsersList: MutableList<String> = mutableListOf(),
    var genderVisibility: String,
    var dobVisibility: String,
    var friendListVisibility: String,
    val savedPosts: ArrayList<String> = arrayListOf(),
    var lastActiveAt: Long
) : Parcelable {
    fun toUserRemoteEntity(): UserRemoteEntity {
        val otherUsersStatus: MutableMap<String, String> = mutableMapOf()
        friendList.forEach { key ->
            otherUsersStatus[key] = StatusWithCurrentUserRemoteEnum.Friends.name
        }
        requestedFriendRequestList.forEach { key ->
            otherUsersStatus[key] = StatusWithCurrentUserRemoteEnum.RequestedByCurrentUser.name
        }
        receivedFriendRequestList.forEach { key ->
            otherUsersStatus[key] = StatusWithCurrentUserRemoteEnum.RequestedByOtherUser.name
        }
        blockedUsersList.forEach { key ->
            otherUsersStatus[key] = StatusWithCurrentUserRemoteEnum.Blocked.name
        }
        return UserRemoteEntity(
            firebaseUserId,
            connectUserId,
            fcmToken,
            mobileNumber,
            name,
            gender,
            dateOfBirth,
            createdAt,
            modifiedAt,
            currentLoggedInDeviceId,
            bio,
            profilePhoto,
            coverPhoto,
            otherUsersStatus,
            genderVisibility,
            dobVisibility,
            friendListVisibility,
            savedPosts,
            lastActiveAt
        )
    }

    fun toUserLocalEntity(): UsersLocalEntity {
        val otherUsersStatus: MutableMap<String, String> = mutableMapOf()
        friendList.forEach { key ->
            otherUsersStatus[key] = StatusWithCurrentUserRemoteEnum.Friends.name
        }
        requestedFriendRequestList.forEach { key ->
            otherUsersStatus[key] = StatusWithCurrentUserRemoteEnum.RequestedByCurrentUser.name
        }
        receivedFriendRequestList.forEach { key ->
            otherUsersStatus[key] = StatusWithCurrentUserRemoteEnum.RequestedByOtherUser.name
        }
        blockedUsersList.forEach { key ->
            otherUsersStatus[key] = StatusWithCurrentUserRemoteEnum.Blocked.name
        }
        return UsersLocalEntity(
            firebaseUserId,
            connectUserId,
            fcmToken,
            mobileNumber,
            name,
            gender,
            dateOfBirth,
            createdAt,
            modifiedAt,
            currentLoggedInDeviceId,
            bio,
            profilePhoto,
            coverPhoto,
            otherUsersStatus,
            genderVisibility,
            dobVisibility,
            friendListVisibility,
            savedPosts,
            lastActiveAt
        )
    }
}