package com.openclassrooms.hexagonal.games.screen.settings

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.notification.FirebaseNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel responsible for managing user settings, specifically notification preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor (

) : ViewModel() {

  // UI state - Résultat des modifications faites au channel
  private val _uiStateNotifCallback = MutableStateFlow<ResultCustom<String>?>(null)
  val uiStateNotifCallback : StateFlow<ResultCustom<String>?> = _uiStateNotifCallback.asStateFlow() // Accès en lecture seule de l'extérieur


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
   * limitation des channels : Si je désactive le channel puis que je le réactive, l'importance reste à IMPORTANCE_NONE
   * https://stackoverflow.com/questions/60820163/android-notification-importance-cannot-be-changed
  */
  private fun setNotifications(application: Application, channelId: String, bEnableP : Boolean) {

    viewModelScope.launch(Dispatchers.IO) {

      try {

        // service Android utilisé pour gérer les notifications
        val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Version supérieure à Android 8 (Oreo)
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val existingChannel = notificationManager.getNotificationChannel(channelId)

        // le channel n'existe pas
        if (existingChannel == null){

          if (bEnableP) {
            // Il faut l'activer et le créer
            FirebaseNotificationService.createChannel(notificationManager)
            _uiStateNotifCallback.value = ResultCustom.Success("Channel créé")
          }

        }
        else{

          // Le channel existe, il faut mettre à jour la priorité
          if (bEnableP) {
            // remettre les notifications en remettant l'importance du canal par défaut

            // On ne peut pas remonter l'importance du channel
            existingChannel.importance = NotificationManager.IMPORTANCE_HIGH

            _uiStateNotifCallback.value = ResultCustom.Success("Importance High")

          }
          else {
            // bloquer les notifications en réduisant l'importance du canal à NotificationManager.IMPORTANCE_NONE, ce qui désactive toutes les notifications de ce canal.
            existingChannel.importance = NotificationManager.IMPORTANCE_NONE

            _uiStateNotifCallback.value = ResultCustom.Success("Importance None")
          }

          // createNotificationChannel met à jour l'importance si le channel existe
          notificationManager.createNotificationChannel(existingChannel)

        }


        // le canal est conservé en cache
        // Si je désactive les notifications, en relançant l'appli, le chanel reste désactivé

        //} else {
        // Pour les versions inférieures à Oreo, les notifications ne sont pas regroupées en canaux,
        // donc la seule manière de les "bloquer" serait de s'assurer qu'elles ne sont pas envoyées,
        // ce qui doit être géré au niveau de l'application elle-même.

        // J'ai mis dans gradle, Oreo 'Android 8' en version minimale
        // Sinon on pourrait utiliser les SharedPreferences
        //}

      } catch (e: Exception) {

        // En cas d'erreur
        _uiStateNotifCallback.value = ResultCustom.Failure(e.message)
      }


    }
  }

  /**
   * Retourne Vrai si le channel existe déjà
   */
  fun bChannelAlreadyExist(application: Application, channelId: String): Boolean{

    val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.getNotificationChannel(channelId) != null

  }



}
