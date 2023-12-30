package com.example.connect.presentation.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.connect.R
import com.example.connect.presentation.utils.NotificationsChannelsConstantHelper
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val friendRequestChannel = NotificationChannel(
            NotificationsChannelsConstantHelper.FRIEND_REQUEST_CHANNEL_ID,
            getString(R.string.friend_request_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(
            listOf(
                friendRequestChannel
            )
        )
    }

}
