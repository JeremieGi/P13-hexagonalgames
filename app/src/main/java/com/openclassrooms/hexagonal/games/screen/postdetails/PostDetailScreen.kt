package com.openclassrooms.hexagonal.games.screen.postdetails

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.screen.homefeed.ErrorComposable
import com.openclassrooms.hexagonal.games.screen.homefeed.LoadingComposable
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme


@Composable
fun PostDetailScreen(
    modifier: Modifier = Modifier,
    postId : String?,
    viewModel: PostDetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onAddComment : () -> Unit
) {


    if (postId.isNullOrEmpty()){
        ErrorComposable(
            sMessage = "Aucun ID de post passé en paramètre",
            onClickRetryP = {}) {
        }
    }
    else{

        LaunchedEffect(postId) {
            // Coroute exécutée lorsque postId change
            // Coroute exécutée aussi à la rotation de l'écran : Lorsqu'une activité ou un fragment est recomposé en réponse à un changement de configuration, Compose recompose toute l'UI visible.
            viewModel.loadPost(postId)
        }

        // Lecture du post
        val uiStatePost by viewModel.uiStatePostResult.collectAsStateWithLifecycle(
            initialValue = ResultCustom.Loading
        )

        PostDetailScreenScaffold(
            modifier = modifier,
            uiStatePost = uiStatePost,
            onBackClick = onBackClick
        )

    }




}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreenScaffold(
    modifier: Modifier = Modifier,
    uiStatePost: ResultCustom<Post>,
    onBackClick: () -> Unit,
){

    val context = LocalContext.current

    val sTitle = when(uiStatePost){

        is ResultCustom.Loading -> {
            "Post loading"
        }

        is ResultCustom.Success -> {
            uiStatePost.value.title
        }

        is ResultCustom.Failure -> {
            "Error"
        }

    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(sTitle)
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


        when(uiStatePost){

            is ResultCustom.Loading -> {
                LoadingComposable(modifier = modifier)
            }

            is ResultCustom.Success -> {

                DetailPost(
                    modifier = Modifier.padding(contentPadding),
                    postP = uiStatePost.value,
                    onAddCommentClicked = { /*TODO JG*/ }
                )

            }

            is ResultCustom.Failure -> {

                val activity = (context as Activity)
                val error = uiStatePost.errorMessage ?: stringResource(R.string.unknown_error)

                ErrorComposable(
                    modifier=modifier,
                    sMessage = error,
                    onClickRetryP = {},
                    closeActivity = activity::finish
                )

            }

        }


    }

}

@Composable
fun DetailPost(
    modifier: Modifier = Modifier,
    postP: Post,
    onAddCommentClicked: () -> Unit) {

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {

            Text(
                text = stringResource(
                    id = R.string.by,
                    postP.author?.firstname ?: ""
                ),
                style = MaterialTheme.typography.titleSmall
            )


            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                text = postP.title
            )

            if (postP.description != null) {
                Text(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    text = postP.description
                )
            }

            if (postP.photoUrl != null){

                //  l'image est chargée et affichée à l'aide de Coil, une bibliothèque populaire pour le chargement d'images dans Compose.
                SubcomposeAsyncImage(
                    model = postP.photoUrl,
                    loading = {
                        CircularProgressIndicator()
                    },
                    contentDescription = null
                )

            }


        }

    }

}

@Preview
@PreviewScreenSizes
@Composable
private fun PostDetailScreenScaffoldPreview() {

    val post = Post(
        id = "1",
        title = "title",
        description = "description",
        photoUrl = null,
        timestamp = 1,
        author = User(
            id = "1",
            firstname = "firstname"
        )
    )


    HexagonalGamesTheme {

        val uiStatePost = ResultCustom.Success(post)

        PostDetailScreenScaffold(
            uiStatePost = uiStatePost,
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun PostDetailScreenScaffoldLoadingPreview() {


    HexagonalGamesTheme {

        val uiStatePost = ResultCustom.Loading

        PostDetailScreenScaffold(
            uiStatePost = uiStatePost,
            onBackClick = {}
        )
    }
}








