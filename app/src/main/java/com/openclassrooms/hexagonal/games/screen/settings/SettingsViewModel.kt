package com.openclassrooms.hexagonal.games.screen.settings

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
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
    private val _application : Application
) : AndroidViewModel(_application) { // Utilisation d'AndroidViewModel car j'ai besoin du contexte de l'application dans beaucoup de méthode

  // UI state - Résultat des modifications faites au channel
  private val _uiStateNotifCallback = MutableStateFlow<ResultCustom<String>?>(null)
  val uiStateNotifCallback : StateFlow<ResultCustom<String>?> = _uiStateNotifCallback.asStateFlow() // Accès en lecture seule de l'extérieur

  // Channel ID utilisé toujours le même dans ce viewModel
  private val _channelId = FirebaseNotificationService.CHANNEL_ID_HEXAGONAL


  /**
   * limitation des channels : Si je désactive le channel puis que je le réactive, l'importance reste à IMPORTANCE_NONE
   * https://stackoverflow.com/questions/60820163/android-notification-importance-cannot-be-changed
   * On ne peut pas jouer avec l'importance du channel après sa création.
  */
  fun createChannel() {

    viewModelScope.launch(Dispatchers.IO) {

      try {

        // service Android utilisé pour gérer les notifications
        val notificationManager = _application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Version supérieure à Android 8 (Oreo)
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val existingChannel = notificationManager.getNotificationChannel(_channelId)

        // le channel n'existe pas
        if (existingChannel == null){

            // Il faut l'activer et le créer
            FirebaseNotificationService.createChannel(notificationManager)
            _uiStateNotifCallback.value = ResultCustom.Success("Channel créé")

        }
        else{

          _uiStateNotifCallback.value = ResultCustom.Success("Channel already exist")

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
  fun bChannelAlreadyExist(): Boolean{

    val notificationManager = _application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.getNotificationChannel(_channelId) != null

  }

  /**
   * Retourne Vrai si le channel existe et est actif
   */
  fun bChannelEnabled(): Boolean{

    // Vérifiez si l'API utilisée supporte les canaux de notification

      val notificationManager = _application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val channel = notificationManager.getNotificationChannel(_channelId)

      // Si le canal existe et que son importance n'est pas IMPORTANCE_NONE, il est autorisé
      return channel?.importance != NotificationManager.IMPORTANCE_NONE

  }



}
