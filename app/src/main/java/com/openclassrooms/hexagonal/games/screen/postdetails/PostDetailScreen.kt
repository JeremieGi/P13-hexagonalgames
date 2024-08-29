package com.openclassrooms.hexagonal.games.screen.postdetails

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import com.openclassrooms.hexagonal.games.domain.model.PostComment
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.screen.homefeed.CommentCell
import com.openclassrooms.hexagonal.games.screen.homefeed.ErrorComposable
import com.openclassrooms.hexagonal.games.screen.homefeed.LoadingComposable
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme


@Composable
fun PostDetailScreen(
    modifier: Modifier = Modifier,
    postId : String?,
    viewModel: PostDetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onFABAddCommentClick : (idPost : String) -> Unit
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
            postId = postId,
            bUserConnectedP = viewModel.isCurrentUserLogged(),
            uiStatePost = uiStatePost,
            onFABAddCommentClick = onFABAddCommentClick,
            onBackClick = onBackClick
        )

    }




}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreenScaffold(
    modifier: Modifier = Modifier,
    postId : String,
    bUserConnectedP : Boolean,
    uiStatePost: ResultCustom<Post>,
    onFABAddCommentClick : (idPost : String) -> Unit,
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
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {

                    if (bUserConnectedP){
                        onFABAddCommentClick(postId)
                    }
                    else{
                        Toast
                            .makeText(context, context.getString(R.string.needlogin), Toast.LENGTH_SHORT)
                            .show()
                    }


                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.description_button_add)
                )
            }
        }
    ) { contentPadding ->


        when(uiStatePost){

            is ResultCustom.Loading -> {
                LoadingComposable(modifier = modifier)
            }

            is ResultCustom.Success -> {

                DetailPost(
                    modifier = Modifier.padding(contentPadding),
                    postP = uiStatePost.value
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
    postP: Post) {

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Display the Post
        ElevatedCard(
            modifier = Modifier.wrapContentSize(),
        ){
            Column(
                modifier = Modifier
                    //.fillMaxSize()
                    //.weight(1f)
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

        // Liste des commentaires
        if (postP.listComments.isEmpty()){
            Text(
                modifier = Modifier
                    .padding(top = 16.dp),
                text = stringResource(R.string.no_comments)
            )
        }
        else{
            Text(
                modifier = Modifier
                    .padding(top = 16.dp),
                text = stringResource(R.string.comments, postP.listComments.size)
            )

            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(postP.listComments) { comment ->
                    CommentCell(
                        commentP = comment
                    )
                }

            }
        }


    }

}



@Preview
@PreviewScreenSizes
@Composable
private fun PostDetailScreenScaffoldPreview() {

    val author = User("1", "JG")

    val listPostComment = listOf(
        PostComment(id="1",author = author, comment = "Mon commentaire"),
        PostComment(id="2",author = author, comment = "Mon commentaire2"),
        PostComment(id="3",author = author, comment = "Mon commentaire\nsur 2 lignes")
    )

    val post = Post(
        id = "1",
        title = "title",
        description = "description",
        photoUrl = null,
        timestamp = 1,
        author = User(
            id = "1",
            firstname = "firstname"
        ),
        listComments = listPostComment

    )


    HexagonalGamesTheme {

        val uiStatePost = ResultCustom.Success(post)

        PostDetailScreenScaffold(
            postId = post.id,
            uiStatePost = uiStatePost,
            bUserConnectedP = true,
            onFABAddCommentClick = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun PostDetailScreenScaffoldLoadingPreview() {


    HexagonalGamesTheme {

        val uiStatePost = ResultCustom.Loading

        PostDetailScreenScaffold(
            postId = "",
            uiStatePost = uiStatePost,
            bUserConnectedP = true,
            onFABAddCommentClick = {},
            onBackClick = {}
        )
    }
}








