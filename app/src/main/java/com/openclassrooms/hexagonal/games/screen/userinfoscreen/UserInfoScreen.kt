package com.openclassrooms.hexagonal.games.screen.userinfoscreen

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.firebase.ui.auth.AuthUI
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: UserInfoViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.action_myaccount))
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
        UserInfo(
            modifier = Modifier.padding(contentPadding),
            userDisplayNameP = viewModel.user?.displayName,
            userEmailP = viewModel.user?.email
        )
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun UserInfo(
    modifier: Modifier = Modifier,
    userDisplayNameP : String?,
    userEmailP : String ?
) {

    val context = LocalContext.current


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "User Info", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Name: ${userDisplayNameP ?: "N/A"}")
        Text(text = "Email: ${userEmailP ?: "N/A"}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener {
                    // méthode qui permet de spécifier une action à exécuter une fois que l'opération signOut() est terminée.

                    Toast
                        .makeText(context, context.getString(R.string.deconnexion_ok), Toast.LENGTH_SHORT)
                        .show()

                }
                .addOnFailureListener {

                }
        }) {
            Text(stringResource(id = R.string.SignOut))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // TODO JG : Delete Account
        }) {
            Text(stringResource(id = R.string.DeleteAccount))
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun UserInfoPreview() {

    HexagonalGamesTheme {
        UserInfo(
            userDisplayNameP = "Jérémie",
            userEmailP = "jeremie.neotic@free.fr"
        )
    }
}