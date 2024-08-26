package com.openclassrooms.hexagonal.games.screen.addComment


import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.screen.homefeed.ErrorComposable
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommentScreen(
    modifier: Modifier = Modifier,
    postId : String?,
    viewModel: AddCommentViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {

    if (postId.isNullOrEmpty()){

        ErrorComposable(
            sMessage = "Aucun ID de post passé en paramètre",
            onClickRetryP = {}) {
        }
    }
    else{

        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(id = R.string.add_comment))
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

            val currentComment by viewModel.comment.collectAsStateWithLifecycle()
            val error by viewModel.error.collectAsStateWithLifecycle()

            // Obtenir le résultat de l'enregistrement du commentaire
            val postResult by viewModel.uiStatePostCommentResult.collectAsStateWithLifecycle()

            CreateComment(
                modifier = Modifier.padding(contentPadding),
                error = error,
                currentComment = currentComment.comment,
                onCommentChanged = { viewModel.onPostCommentChanged( it ) },
                stateResultSave = postResult,
                onSaveCommentClicked = { viewModel.addCommentToPost(postId) },
                onBackAfterSaveClick = onBackClick
            )
        }

    }

}

@Composable
private fun CreateComment(
    modifier: Modifier = Modifier,
    currentComment: String,
    onCommentChanged: (String) -> Unit,
    onSaveCommentClicked: () -> Unit,
    error: String?,
    stateResultSave: ResultCustom<String>?,
    onBackAfterSaveClick: () -> Unit
) {

    val context = LocalContext.current

    val scrollState = rememberScrollState()

    // Gérer les différents états du résultat d'ajout d'un post
    when (stateResultSave) {

        null -> {
            // Au 1er appel
        }

        is ResultCustom.Loading -> {
            // N'arrivera pas
        }

        is ResultCustom.Success -> {
            // Afficher un message de succès et fermer l'activity
            Toast
                .makeText(context, stateResultSave.value, Toast.LENGTH_SHORT)
                .show()

            onBackAfterSaveClick()
        }

        is ResultCustom.Failure -> {
            // Afficher un message d'erreur suite à l'envoi du Post
            val errorMessage = stateResultSave.errorMessage ?: context.getString(R.string.unknowError)
            Toast
                .makeText(context, errorMessage, Toast.LENGTH_SHORT)
                .show()
        }


    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                value = currentComment,
                isError = !error.isNullOrEmpty(),
                onValueChange = { onCommentChanged(it) },
                label = { Text(stringResource(id = R.string.Comment)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            if (!error.isNullOrEmpty()) {
                Text(
                    text = stringResource(id = R.string.CommentEmpty),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Button(
            enabled = (error == null),
            onClick = {
                onSaveCommentClicked()
            }
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(id = R.string.action_save)
            )
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun CreatePostPreview() {
    HexagonalGamesTheme {
        CreateComment(
            currentComment = "",
            onCommentChanged = {},
            onSaveCommentClicked = { },
            error = null,
            stateResultSave = ResultCustom.Success(""),
            onBackAfterSaveClick = {}
        )
    }
}