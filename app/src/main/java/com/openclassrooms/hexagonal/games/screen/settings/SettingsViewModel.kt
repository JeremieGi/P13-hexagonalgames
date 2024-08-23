package com.openclassrooms.hexagonal.games.screen.settings

import androidx.lifecycle.ViewModel


/**
 * ViewModel responsible for managing user settings, specifically notification preferences.
 */
class SettingsViewModel : ViewModel() {

/*
  fun notificationsAreEnable(context : Context) : Boolean {
      return NotificationManagerCompat.from(context).areNotificationsEnabled()
  }
*/
  /**
   * Enables notifications for the application.
   * TODO JG: Implement the logic to enable notifications, likely involving interactions with a notification manager.
   */
  fun enableNotifications() {

    // TODO JG : Je ne comprends pas où il faut que je désactive les notifications à part faire une SharedPreference que j'utilise dans FirebaseNotificationService
    // On ne peut pas modifier l'autorisation par programmation comme l'utilisateur peut le faire dans les paramètres.
    // Peut -être un moyen avec les canaux ?

  }
  
  /**
   * Disables notifications for the application.
   * TODO JG: Implement the logic to disable notifications, likely involving interactions with a notification manager.
   */
  fun disableNotifications() {
    //TODO JG
  }
  
}
