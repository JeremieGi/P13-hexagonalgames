package com.openclassrooms.hexagonal.games.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.openclassrooms.hexagonal.games.ui.MainActivity


class FirebaseNotificationService : FirebaseMessagingService() {

    // TODO JG / Denis : Revoir ce code (Je reçois bien les notifs)

    private val NOTIFICATION_ID = 7
    private val NOTIFICATION_TAG = "HEXAGONAL"

    override fun onMessageReceived(message: RemoteMessage) {

        super.onMessageReceived(message)

        if (message.notification != null) {
            // Get message sent by Firebase
            val notification = message.notification

            // Affiche une notification visuelle
            Log.d("NOTIF", notification?.body.toString())
            sendVisualNotification(notification)
        }


    }

    private fun sendVisualNotification(notification: RemoteMessage.Notification?) {

        // Create an Intent that will be shown when user will click on the Notification
        val intent = Intent(this, MainActivity::class.java )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        // FLAG_ONE_SHOT

        // Create a Channel (Android 8)
        val channelId = "default_notification_channel_id"

        // Build a Notification object
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(com.openclassrooms.hexagonal.games.R.drawable.ic_notifications)
                .setContentTitle(notification!!.title)
                .setContentText(notification.body)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName: CharSequence = "Firebase Messages"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(mChannel)
        }

        // Show notification
        notificationManager.notify(
            NOTIFICATION_TAG,
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }

}