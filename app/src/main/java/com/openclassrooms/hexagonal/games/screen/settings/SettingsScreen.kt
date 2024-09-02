package com.openclassrooms.hexagonal.games.screen.settings


import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.notification.FirebaseNotificationService
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.screen.homefeed.ErrorComposable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: SettingsViewModel = hiltViewModel(),
  onBackClick: () -> Unit
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(id = R.string.action_settings))
        },
        navigationIcon = {
          IconButton(onClick = {
            onBackClick()
          }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(id = R.string.contentDescription_go_back)
            )
          }
        }
      )
    }
  ) { contentPadding ->


    val context = LocalContext.current
    val application = context.applicationContext as Application

    val operationState by viewModel.uiStateNotifCallback.collectAsState()

    SettingsComposable(
      modifier = Modifier.padding(contentPadding),
      operationStateP = operationState,
      onNotificationDisabledClicked = {
        viewModel.disableNotifications(application,FirebaseNotificationService.CHANNEL_ID_HEXAGONAL)
      },
      onNotificationEnabledClicked = {
        viewModel.enableNotifications(application,FirebaseNotificationService.CHANNEL_ID_HEXAGONAL)
      },
      bChannelAlreadyExistP = {
        viewModel.bChannelAlreadyExist(application,FirebaseNotificationService.CHANNEL_ID_HEXAGONAL)
      }

    )
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun SettingsComposable(
  modifier: Modifier = Modifier,
  onNotificationEnabledClicked: () -> Unit,
  onNotificationDisabledClicked: () -> Unit,
  operationStateP: ResultCustom<String>?,
  bChannelAlreadyExistP : () -> Boolean
) {


  val notificationsPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    rememberPermissionState(
      android.Manifest.permission.POST_NOTIFICATIONS
    )
  } else {
    null
  }

  val context = LocalContext.current
  val application = context.applicationContext as Application


  var sInfoNotifChannel by remember { mutableStateOf(FirebaseNotificationService.sChannelEnable(application,FirebaseNotificationService.CHANNEL_ID_HEXAGONAL)) }

  when (operationStateP){
      null -> {
        // Premier appel
      }

    is ResultCustom.Failure -> {
      ErrorComposable(
        sMessage = operationStateP.errorMessage?:"Unknow error",
        onClickRetryP = { }) {
      }
    }

    is ResultCustom.Loading -> {
      // N'arrivera pas
    }

    is ResultCustom.Success -> {
      sInfoNotifChannel = FirebaseNotificationService.sChannelEnable(application,FirebaseNotificationService.CHANNEL_ID_HEXAGONAL)
    }
  }


  // Callback de la fenêtre de préférence (Settings) des notficiation pour l'application
  val notificationSettingsLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    // Callback au retour de la fenêtre de notification
//    if (result.resultCode == android.app.Activity.RESULT_OK) {
//
//    } else {
//
//    }
    sInfoNotifChannel = FirebaseNotificationService.sChannelEnable(application,FirebaseNotificationService.CHANNEL_ID_HEXAGONAL)
  }

  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceEvenly
  ) {

    Icon(
      modifier = Modifier.size(200.dp),
      painter = painterResource(id = R.drawable.ic_notifications),
      tint = MaterialTheme.colorScheme.onSurface,
      contentDescription = stringResource(id = R.string.contentDescription_notification_icon)
    )

    if (sInfoNotifChannel == FirebaseNotificationService.CHANNEL_STATE_NOTIFICATION_ENABLE){

      // Premier lancement, il faut activer les notifications manuellement

      Text("Notification désactivée dans les paramètres Android. A activer manuellement.")
      ButtonLaunchPreferences(
        sTextLabelButton = "First, enable Android Notification for the application",
        bModeChannelP = false, // Préférence de l'application
        notificationSettingsLauncher
      )


    }
    else{

      Text("Notification activée dans les paramètres Android")

      if (sInfoNotifChannel == FirebaseNotificationService.CHANNEL_STATE_EXIST_IMPORTANCE_HIGH){

        Text("Notification activée pour le channel ${FirebaseNotificationService.CHANNEL_NAME_HEXAGONAL}")

        Button(
          onClick = {

            onNotificationDisabledClicked()


          }
        ) {
          Text(text = stringResource(id = R.string.notification_disable))
        }

      }
      else{

        Text("Notification désactivée pour le channel ${FirebaseNotificationService.CHANNEL_NAME_HEXAGONAL} : ${sInfoNotifChannel}")



        if (bChannelAlreadyExistP()){
          // On ne pourra pas désactiver par programmation, il faudra ouvrir les préférences utilisateur

          Text("Une limitation Android empêche de monter le niveau d'importance d'un channel existant. " +
                  "Cette solution va donc necessiter l'ouverture des Préférences de l'application.")

          ButtonLaunchPreferences(
            sTextLabelButton = "Il faut ici ouvrir les préférences",
            bModeChannelP = true,
            notificationSettingsLauncher = notificationSettingsLauncher
          )

        }
        else{

          // La premier activation est faisable par programmation

          Button(
            onClick = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (notificationsPermissionState?.status?.isGranted == false) {
                  notificationsPermissionState.launchPermissionRequest()
                }
              }

              onNotificationEnabledClicked()

              }
          ) {
            Text(text = stringResource(id = R.string.notification_enable))
          }
        }

      }

    }

  }
}



@Composable
fun ButtonLaunchPreferences(
  sTextLabelButton: String,
  bModeChannelP: Boolean,
  notificationSettingsLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
  ){

  val context = LocalContext.current



  Button(onClick = {

    val intent : Intent

    if (bModeChannelP) {
      // Créez un Intent pour ouvrir les paramètres de notification sur le channel
      intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        putExtra(Settings.EXTRA_CHANNEL_ID, FirebaseNotificationService.CHANNEL_ID_HEXAGONAL)
      }
    }
    else{
      // Créez un Intent pour ouvrir les paramètres généraux de notification
      intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
      }
    }

    // Lancez l'activité pour les paramètres de notification
    notificationSettingsLauncher.launch(intent)
  }) {
    Text(text = sTextLabelButton)
  }


}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun SettingsPreview() {
  HexagonalGamesTheme {

    val mockChannelAlreadyExists = { false }

    SettingsComposable(
      onNotificationEnabledClicked = { },
      onNotificationDisabledClicked = { },
      bChannelAlreadyExistP = mockChannelAlreadyExists,
      operationStateP = null
    )
  }
}

