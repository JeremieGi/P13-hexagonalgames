package com.openclassrooms.hexagonal.games.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.openclassrooms.hexagonal.games.ui.MainActivity


class FirebaseNotificationService : FirebaseMessagingService() {


    companion object {

        // Nom de l'unique channel utilisé dans l'application
        const val CHANNEL_ID_HEXAGONAL : String = "hexagonal_channel_test_ID"
        const val CHANNEL_NAME_HEXAGONAL : String = "Channel de test JG" // Ce nom de channel apparaît dans Android Settings partie Notification

        // Création du channel
        fun createChannel(notificationManager: NotificationManager) {

            val channelName: CharSequence = CHANNEL_NAME_HEXAGONAL
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID_HEXAGONAL, channelName, importance)
            notificationManager.createNotificationChannel(mChannel)

        }

        const val CHANNEL_STATE_NOTIFICATION_DESABLED : String ="Notifications non activées"
        const val CHANNEL_STATE_CHANNEL_DESABLED : String = "Channel inactif"
        const val CHANNEL_STATE_CHANNEL_ENABLED : String = "Channel actif"
        //const val CHANNEL_STATE_EXIST_IMPORTANCE_HIGH : String ="Channel existant - importance haute"
        //const val CHANNEL_STATE_EXIST_IMPORTANCE_NONE : String ="Channel existant - importance faible"
        /**
         * L'unique channel de l'appli est-il créé et actif ?
         */
        fun sChannelEnable(application: Application, channelId: String) : String {

            val sNotificationEnable : String

            if (notificationsAreEnable(application.applicationContext)){

                val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val existingChannel = notificationManager.getNotificationChannel(channelId)
                if (existingChannel == null) {
                    sNotificationEnable = CHANNEL_STATE_CHANNEL_DESABLED
                }
                else{
                    if (existingChannel.importance == NotificationManager.IMPORTANCE_NONE){
                        sNotificationEnable = CHANNEL_STATE_CHANNEL_DESABLED
                    }
                    else{
                        sNotificationEnable = CHANNEL_STATE_CHANNEL_ENABLED
                    }
                }

            }
            else{
                sNotificationEnable = CHANNEL_STATE_NOTIFICATION_DESABLED
            }


            return sNotificationEnable

        }

        // A savoir : On ne peut pas modifier l'autorisation par programmation comme l'utilisateur peut le faire dans les paramètres.
        private fun notificationsAreEnable(context : Context) : Boolean {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    private val NOTIFICATION_ID = 7
    private val NOTIFICATION_TAG = "HEXAGONAL"

    /**
     * Méthode appelé à la réception d'une notification
     */
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

    /**
     * Permet l'affichage à l'écran de la notification
     */
    private fun sendVisualNotification(notification: RemoteMessage.Notification?) {

        // Create an Intent that will be shown when user will click on the Notification
        val intent = Intent(this, MainActivity::class.java )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Build a Notification object
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, CHANNEL_ID_HEXAGONAL)
                .setSmallIcon(com.openclassrooms.hexagonal.games.R.drawable.ic_notifications)
                .setContentTitle(notification!!.title)
                .setContentText(notification.body)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Support Version >= Android 8
 //       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Si le canal n'existe pas
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID_HEXAGONAL)
            if (existingChannel == null) {
                // Il est créé
                createChannel(notificationManager)
            }
            // Si le canal existe déjà, on le touche pas (l'importance peut être paramétrée depuis l'application pour activer les notifs ou pas)

 //       }
//        else{
//            // J'ai mis dans gradle, Oreo 'Android 8' en version minimale
//        }

        // Show notification
        // La notification ne s'affiche pas si l'importance du channel est IMPORTANCE_NONE
        notificationManager.notify(
            NOTIFICATION_TAG,
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }


    // Je prends un warning si je n'implémente pas cette méthode
    override fun onNewToken(token: String) {
        // Mais je ne gère pas les tokens dans ce projet

        /**
         * Le token d'inscription est un identifiant unique attribué à chaque appareil ou instance d'application. Lorsque votre application s'enregistre auprès de Firebase Cloud Messaging, un token unique est généré pour cet appareil. Ce token permet à Firebase d'identifier de manière unique chaque appareil ou utilisateur au sein de votre application.
         */
    }

}