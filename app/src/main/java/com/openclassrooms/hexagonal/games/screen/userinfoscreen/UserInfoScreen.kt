package com.openclassrooms.hexagonal.games.screen.userinfoscreen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.screen.homefeed.LoadingComposable
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: UserInfoViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {


    val context = LocalContext.current

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


        // Obtenir le résultat de la suppression de l'utilisateur
        val deleteUserResult = produceState<ResultCustom<String>?>(initialValue = null, key1 = context) {
            viewModel.deleteUser(context).collect { result ->
                value = result
            }
        }

        UserInfo(
            modifier = Modifier.padding(contentPadding),
            userDisplayNameP = viewModel.getCurrentUser()?.displayName,
            userEmailP = viewModel.getCurrentUser()?.email,
            onClickSignOut = viewModel::signOut,
            resultUiStateDeleteUser = deleteUserResult.value,
            onClickDeleteUser = { viewModel.deleteUser(context) },
            onBackClick = onBackClick
        )
    }
}


@Composable
private fun UserInfo(
    modifier: Modifier = Modifier,
    userDisplayNameP: String?,
    userEmailP: String?,
    onClickSignOut: (Context) -> Task<Void>,
    onClickDeleteUser: () -> Unit,
    onBackClick: () -> Unit,
    resultUiStateDeleteUser: ResultCustom<String>?
) {

    val context = LocalContext.current

    var sErrorSignOut by rememberSaveable { mutableStateOf("") }
    var sErrorDeleteAccount by rememberSaveable { mutableStateOf("") }

    // Gérer les différents états du résultat de suppression de l'utilisateur
    when (val result = resultUiStateDeleteUser) {

        is ResultCustom.Loading -> {
            LoadingComposable()
        }

        is ResultCustom.Success -> {
            // Afficher un message de succès et fermer l'activity
            Toast
                .makeText(context, result.value, Toast.LENGTH_SHORT)
                .show()

            onBackClick()
        }

        is ResultCustom.Failure -> {
            // Afficher un message d'erreur
            val errorMessage =  result.errorMessage ?: context.getString(R.string.unknowError)
            sErrorDeleteAccount = errorMessage
        }

        null -> {
            // Ne rien afficher => pas de suppression en cours
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        //Text(text = "User Info", style = MaterialTheme.typography.headlineMedium)
        //Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(R.string.name, userDisplayNameP ?: "N/A"))
        Text(text = stringResource(R.string.email, userEmailP ?: "N/A"))

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {

            onClickSignOut(context)
                .addOnCompleteListener {
                    // méthode qui permet de spécifier une action à exécuter une fois que l'opération signOut() est terminée.

                    Toast
                        .makeText(context, context.getString(R.string.deconnexion_ok), Toast.LENGTH_SHORT)
                        .show()

                    onBackClick()

                }
                .addOnFailureListener { exception ->
                    // Erreur lors de la déconnexion

                    val errorMessage = exception.localizedMessage ?: context.getString(R.string.unknowError)

                    sErrorSignOut = errorMessage

                    Toast
                        .makeText(context, errorMessage, Toast.LENGTH_SHORT)
                        .show()

                }

        }) {
            Text(stringResource(id = R.string.SignOut))
        }

        if (sErrorSignOut.isNotEmpty()){
            Text(
                text = sErrorSignOut,
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {

            // Suppression du compte
            onClickDeleteUser() // Le résultat est géré par callback


        }) {
            Text(stringResource(id = R.string.DeleteAccount))
        }

        if (sErrorDeleteAccount.isNotEmpty()){
            Text(
                text = sErrorDeleteAccount,
                color = Color.Red
            )
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun UserInfoPreview() {

    val mock : (Context) -> Task<Void> = { context ->
        // Simulate a successful sign-out task
        Tasks.forResult(null)
    }


    HexagonalGamesTheme {
        UserInfo(
            userDisplayNameP = "Jérémie",
            userEmailP = "jeremie.neotic@free.fr",
            onClickSignOut = mock,
            onClickDeleteUser = {},
            onBackClick = {},
            resultUiStateDeleteUser = null
        )
    }

}