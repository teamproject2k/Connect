package com.teamproject2k.connect.presentation.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.teamproject2k.connect.R
import com.teamproject2k.connect.presentation.utils.NotificationsConstantHelper
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Firebase.crashlytics.setUserId(Firebase.auth.uid ?: "")
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val friendRequestChannel = NotificationChannel(
            NotificationsConstantHelper.FRIEND_REQUEST_CHANNEL_ID,
            getString(R.string.friend_request_channel),
            NotificationManager.IMPORTANCE_HIGH
        )
        val defaultNotificationChannel = NotificationChannel(
            NotificationsConstantHelper.DEFAULT_CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(
            listOf(
                friendRequestChannel,
                defaultNotificationChannel
            )
        )
    }

}
