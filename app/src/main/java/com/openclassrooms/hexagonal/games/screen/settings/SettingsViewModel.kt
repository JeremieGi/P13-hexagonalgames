package com.openclassrooms.hexagonal.games.screen.settings

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.notification.FirebaseNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel responsible for managing user settings, specifically notification preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor () : ViewModel() {


  // A savoir : On ne peut pas modifier l'autorisation par programmation comme l'utilisateur peut le faire dans les paramètres.

  fun notificationsAreEnable(context : Context) : Boolean {
      return NotificationManagerCompat.from(context).areNotificationsEnabled()
  }



  /**
   * L'unique channel de l'appli est-il créé et actif ?
   */
  fun bChannelEnable(application: Application, channelId: String) : Boolean {

    val bNotificationEnable : Boolean

    val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val existingChannel = notificationManager.getNotificationChannel(channelId)
    if (existingChannel == null) {
      bNotificationEnable = true
    }
    else{
      bNotificationEnable = (existingChannel.importance == NotificationManager.IMPORTANCE_HIGH)
    }

    return bNotificationEnable

  }


  /**
   * Enables notifications for the application.
   */
  fun enableNotifications(application: Application, channelId: String) {

    setNotifications(application,channelId, bEnableP = true)

  }
  
  /**
   * Disables notifications for the application.
   */
  fun disableNotifications(application: Application, channelId: String) {

    setNotifications(application,channelId, bEnableP = false)

  }


  /**
   * TODO Denis prio 3 / JG code à montrer + bug : Si je désactive le channel puis que je le réactive, l'importance reste à IMPORTANCE_NONE
   */
  private fun setNotifications(application: Application, channelId: String, bEnableP : Boolean) {

    viewModelScope.launch(Dispatchers.IO) {

      // service Android utilisé pour gérer les notifications
      val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      // Version supérieure à Android 8 (Oreo)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val existingChannel = notificationManager.getNotificationChannel(channelId)

        // le channel n'existe pas
        if (existingChannel == null){

          if (bEnableP) {
            // Il faut l'activer et le créer
            FirebaseNotificationService.createChannel(notificationManager)
          }

        }
        else{

          // Le channel existe, il faut mettre à jour la priorité
          if (bEnableP) {
            // remettre les notifications en remettant l'importance du canal par défaut
            existingChannel.importance = NotificationManager.IMPORTANCE_HIGH
          }
          else {
            // bloquer les notifications en réduisant l'importance du canal à NotificationManager.IMPORTANCE_NONE, ce qui désactive toutes les notifications de ce canal.
            existingChannel.importance = NotificationManager.IMPORTANCE_NONE
          }

          // createNotificationChannel met à jour l'importance si le channel existe
          notificationManager.createNotificationChannel(existingChannel)

        }

        // TODO Denis : A confirmer avec Denis, le canal a l'air d'être conservé en cache
        // Si je désactive les notifications, en relançant l'appli, le chanel reste désactivé

      } else {
        // Pour les versions inférieures à Oreo, les notifications ne sont pas regroupées en canaux,
        // donc la seule manière de les "bloquer" serait de s'assurer qu'elles ne sont pas envoyées,
        // ce qui doit être géré au niveau de l'application elle-même.

        // J'ai mis dans gradle, Oreo 'Android 8' en version minimale
      }
    }
  }


}
