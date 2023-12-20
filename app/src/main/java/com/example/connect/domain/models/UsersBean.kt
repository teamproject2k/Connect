package com.example.connect.domain.models

import android.os.Parcelable
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.data.models.user.UsersDbEntity
import com.example.connect.domain.enums.StatusWithCurrentUserRemoteEnum
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsersBean(
    val firebaseUserId: String,
    val connectUserId: String,
    val name: String,
    val gender: String,
    val dateOfBirth: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val currentLoggedInDeviceId: String,
    val bio: String,
    val profilePhoto: String? = null,
    val coverPhoto: String? = null,
    val friendList: MutableList<String> = mutableListOf(),
    val requestedFriendRequestList: MutableList<String> = mutableListOf(),
    val receivedFriendRequestList: MutableList<String> = mutableListOf(),
    val blockedUsersList: MutableList<String> = mutableListOf(),
    var genderVisibility: String,
    var dobVisibility: String,
    var friendListVisibility: String
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
            friendListVisibility
        )
    }

    fun toUserDbEntity(): UsersDbEntity {
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
        return UsersDbEntity(
            firebaseUserId,
            connectUserId,
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
            friendListVisibility
        )
    }
}