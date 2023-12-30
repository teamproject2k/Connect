package com.example.connect.presentation.services.fcm

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.connect.R
import com.example.connect.presentation.ui.home.base_screen.HomeActivity
import com.example.connect.presentation.utils.NotificationsConstantHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
@AndroidEntryPoint
open class MessagingService : FirebaseMessagingService() {


    @Inject
    protected lateinit var firebaseAuth: FirebaseAuth


    // no need to handle onNewToken as the firebase user is null after clear data so it will not send the token


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val dataMessage = remoteMessage.data
        if (dataMessage.isNotEmpty()) {
            when (val type = dataMessage[NotificationTypesEnum::name.name]) {
                NotificationTypesEnum.FriendRequestReceived.name -> {
                    val message =
                        getString(
                            R.string.send_you_friend_request,
                            dataMessage[NotificationsConstantHelper.MESSAGE]
                        )
                    showNotification(
                        NotificationsConstantHelper.FRIEND_REQUEST_CHANNEL_ID,
                        null,
                        message,
                        type
                    )
                }

                NotificationTypesEnum.FriendRequestAccepted.name -> {
                    val message =
                        getString(
                            R.string.accepted_your_friend_request,
                            dataMessage[NotificationsConstantHelper.MESSAGE]
                        )
                    showNotification(
                        NotificationsConstantHelper.FRIEND_REQUEST_CHANNEL_ID,
                        null,
                        message,
                        type
                    )
                }

                else -> {
                    val title = dataMessage[NotificationsConstantHelper.TITLE]
                    val message = dataMessage[NotificationsConstantHelper.MESSAGE]
                    showNotification(
                        NotificationsConstantHelper.DEFAULT_CHANNEL_ID,
                        title,
                        message ?: "",
                        null
                    )
                }
            }
        }
    }

    private fun showNotification(
        channelId: String,
        title: String?,
        message: String,
        notificationType: String?
    ) {
        val intent = Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(NotificationTypesEnum::class.simpleName, notificationType)
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

}