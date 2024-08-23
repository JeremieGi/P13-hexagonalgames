package com.openclassrooms.hexagonal.games.screen.homefeed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme


/**
 * Composable qui s'affiche lors du chargement
 */

@Composable
fun LoadingComposable(modifier: Modifier = Modifier){

    val currentContext = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = 100.dp // Espace en haut
            )
            // "En chargement" soit annoncé par talkBack
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                contentDescription =
                    currentContext.getString(R.string.loading)
            }
        ,
        //.background(Color.Red),
        //verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.loading),
            modifier = modifier
        )

        CircularProgressIndicator(
            modifier = modifier.padding(
                top = 10.dp
            )
        )

    }

}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    HexagonalGamesTheme {
        LoadingComposable()
    }
}
