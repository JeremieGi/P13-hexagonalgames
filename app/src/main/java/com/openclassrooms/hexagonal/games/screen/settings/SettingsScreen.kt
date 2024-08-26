package com.openclassrooms.hexagonal.games.screen.settings


import android.app.Application
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

    Settings(
      modifier = Modifier.padding(contentPadding),
      bNotificationEnableP = viewModel.bChannelEnable(application,FirebaseNotificationService.CHANNEL_ID_HEXAGONAL),
      bNotificationEnableInAndroidSettingsP = viewModel.notificationsAreEnable(context),
      onNotificationDisabledClicked = {
        viewModel.disableNotifications(application,FirebaseNotificationService.CHANNEL_ID_HEXAGONAL)
      },
      onNotificationEnabledClicked = {
        viewModel.enableNotifications(application,FirebaseNotificationService.CHANNEL_ID_HEXAGONAL)
      }
    )
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Settings(
  modifier: Modifier = Modifier,
  onNotificationEnabledClicked: () -> Unit,
  onNotificationDisabledClicked: () -> Unit,
  bNotificationEnableInAndroidSettingsP : Boolean,
  bNotificationEnableP : Boolean
) {

  val notificationsPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    rememberPermissionState(
      android.Manifest.permission.POST_NOTIFICATIONS
    )
  } else {
    null
  }

  var bEnable by remember { mutableStateOf(bNotificationEnableP) }
  
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

    if (bNotificationEnableInAndroidSettingsP){
      Text("Notification activée dans les paramètres Android")
    }
    else{
      Text("Notification désactivée dans les paramètres Android. A activer manuellement.")
    }

    if (bEnable){
      Text("Notification activée dans le channel")
    }
    else{
      Text("Notification désactivée dans le channel")
    }

    Button(
      onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          if (notificationsPermissionState?.status?.isGranted == false) {
            notificationsPermissionState.launchPermissionRequest()
          }
        }
        
        onNotificationEnabledClicked()

        bEnable = true
      }
    ) {
      Text(text = stringResource(id = R.string.notification_enable))
    }
    Button(
      onClick = {
        onNotificationDisabledClicked()
        bEnable = false
      }
    ) {
      Text(text = stringResource(id = R.string.notification_disable))
    }
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun SettingsPreview() {
  HexagonalGamesTheme {
    Settings(
      onNotificationEnabledClicked = { },
      onNotificationDisabledClicked = { },
      bNotificationEnableP = true,
      bNotificationEnableInAndroidSettingsP = true,
    )
  }
}