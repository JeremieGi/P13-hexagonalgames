package com.openclassrooms.hexagonal.games.screen.homefeed

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme


/**
 * Error in DialogAlert
 */
@Composable
fun ErrorComposable(
    modifier: Modifier = Modifier,
    sMessage: String,
    onClickRetryP: () -> Unit,
    closeActivity: () -> Unit
) {

    AlertDialog(
        onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back button.
            closeActivity()
        },
        title = { Text(text = stringResource(R.string.error)) },
        text = { Text(text = sMessage) },
        modifier = modifier,

        dismissButton =  {
            TextButton(
                onClick = {
                    closeActivity()
                }
            ) {
                Text(text = stringResource(R.string.close))
            }
        },


        confirmButton = {

            // Si une lambda est passé en paramètre
            if (onClickRetryP != {}){
                TextButton(onClick = onClickRetryP ) {
                    Text(text = stringResource(R.string.retry))
                }
            }

        }
    )



}

@Preview(showBackground = true)
@Composable
fun ErrorDialogPreview() {

    HexagonalGamesTheme {
        ErrorComposable(
            sMessage = "Message de l'erreur",
            onClickRetryP = {},
            closeActivity = {}
        )
    }

}
