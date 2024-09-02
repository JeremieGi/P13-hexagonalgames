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
import com.google.accompanist.permissions.rememberPermissionState
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.notification.FirebaseNotificationService
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.google.accompanist.permissions.isGranted
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


    // Récupération des évènements de création / modification de channel
    val operationState by viewModel.uiStateNotifCallback.collectAsState()

    SettingsComposable(
      modifier = Modifier.padding(contentPadding),
      operationStateP = operationState,
      createChannelP = {
        viewModel.createChannel()
      },
      bChannelAlreadyExistP = {
        viewModel.bChannelAlreadyExist()
      },
      bChannelIsEnabledP = {
        viewModel.bChannelEnabled()
      }

    )
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun SettingsComposable(
  modifier: Modifier = Modifier,
  createChannelP: () -> Unit,
  operationStateP: ResultCustom<String>?,
  bChannelAlreadyExistP: () -> Boolean,
  bChannelIsEnabledP: () -> Boolean
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
  ) { _ ->
    // Callback au retour de la fenêtre de notification
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

    // Aucun notification autorisée
    if (sInfoNotifChannel == FirebaseNotificationService.CHANNEL_STATE_NOTIFICATION_DESABLED){

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

      // Channel existant
      if (bChannelAlreadyExistP()){
        // On ne pourra pas activer / désactiver par programmation, il faudra ouvrir les préférences utilisateur
        ChannelPreferenceComposable(
          notificationSettingsLauncher = notificationSettingsLauncher,
          bChannelIsEnabledP = bChannelIsEnabledP
        )
      }
      else{

        // Channel inexistant

        // Première activation possible par programmation

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = Color.LightGray), // Couleur de fond grise
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ){

          Text("Channel <${FirebaseNotificationService.CHANNEL_NAME_HEXAGONAL}> non créé.")
          Text( "Notifications non activées.")

          // La premiere activation est faisable par programmation
          Button(
            onClick = {

              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (notificationsPermissionState?.status?.isGranted == false) {
                  notificationsPermissionState.launchPermissionRequest()
                }
              }

              createChannelP()

            }
          ) {
            Text(text = "Create and active channel")
          }

        }

      }

    }

  }
}

@Composable
fun ChannelPreferenceComposable(
  notificationSettingsLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
  bChannelIsEnabledP: () -> Boolean
) {

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = Color.LightGray), // Couleur de fond grise
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ){

    if (bChannelIsEnabledP()){
      Text(stringResource(R.string.channel_activ))
    }
    else{
      Text(stringResource(R.string.channel_d_sactiv))
    }

    Text(
      modifier = Modifier.padding(
        top = 6.dp
      ),
      text = stringResource(R.string.limitation_android) +
              stringResource(R.string.info_channel)
    )

    ButtonLaunchPreferences(
      sTextLabelButton = stringResource(R.string.preferences_du_channel),
      bModeChannelP = true,
      notificationSettingsLauncher = notificationSettingsLauncher
    )
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

// Préviews non gérées
@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun SettingsPreview() {
  HexagonalGamesTheme {

    val mockFalse = { false }

    SettingsComposable(
      createChannelP = { },
      operationStateP = ResultCustom.Success("test"),
      bChannelAlreadyExistP = mockFalse,
      bChannelIsEnabledP = mockFalse

    )
  }
}


