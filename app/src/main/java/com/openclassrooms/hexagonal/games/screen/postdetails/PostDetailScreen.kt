package com.openclassrooms.hexagonal.games.screen.postdetails

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.screen.homefeed.ErrorComposable
import com.openclassrooms.hexagonal.games.screen.homefeed.LoadingComposable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    modifier: Modifier = Modifier,
    postId : String?,
    viewModel: PostDetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {

    if (postId.isNullOrEmpty()){
        ErrorComposable(
            sMessage = "Aucun ID de post passé en paramètre",
            onClickRetryP = {}) {
        }
    }
    else{

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
                    post = uiStatePost.value,
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
    modifier: Modifier,
    post: Post,
    onAddCommentClicked: () -> Unit) {



}





