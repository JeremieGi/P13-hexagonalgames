package com.openclassrooms.hexagonal.games.screen.homefeed

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.util.DebugLogger
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomefeedScreen(
  modifier: Modifier = Modifier,
  viewModel: HomefeedViewModel = hiltViewModel(),
  onPostClick: (Post) -> Unit = {},
  onSettingsClick: () -> Unit = {},
  onFABClick: () -> Unit = {},
  onMyAccountClickWithConnectedUser : () -> Unit = {}

) {

  val context = LocalContext.current

  var showMenu by rememberSaveable { mutableStateOf(false) }


  val signInLauncher = rememberLauncherForActivityResult(
    contract = FirebaseAuthUIActivityResultContract()
  ) { result ->
    // Callback avec le résultat de la connexion

    val response = result.idpResponse
    if (result.resultCode == RESULT_OK) {

      // Successfully signed in
      Toast
        .makeText(context, context.getString(R.string.connexion_ok), Toast.LENGTH_SHORT)
        .show()

      // Insertion de l'utilisateur dans la base de données
      viewModel.insertCurrentUserInFirestore()

      showMenu = false

      // ...
    } else {

      response?.error?.errorCode?.let {
        Toast
          .makeText(context, it, Toast.LENGTH_SHORT)
          .show()
      }
      ?: run {
        // Si errorCode est null, afficher un message d'erreur générique
        Toast.makeText(context, context.getString(R.string.loginCancel), Toast.LENGTH_SHORT).show()
      }


    }

  }


  
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(id = R.string.homefeed_fragment_label))
        },
        actions = {
          IconButton(onClick = { showMenu = !showMenu }) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = stringResource(id = R.string.contentDescription_more)
            )
          }
          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              onClick = {
                onSettingsClick()
              },
              text = {
                Text(
                  text = stringResource(id = R.string.action_settings)
                )
              }
            )
            DropdownMenuItem(
              onClick = {


                if (!viewModel.isCurrentUserLogged()){

                  // Si l’utilisateur n’est pas connecté, redirige vers l’écran de création de compte / connexion

                  // Pour avoir l'écran de login, il faut paramétrer dans Firebase, Authentication, Settings, User actions, => décocher Email enumerattion protection

                  // Ici : Authenfication mail / mot de passe
                  val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                  )

                  // Create and launch sign-in intent
                  val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    //.setAlwaysShowSignInMethodScreen(true) // Affiche la fenêtre Sign in with (même si ici on a que le provider email/password ...)
                    .build()

                  signInLauncher.launch(signInIntent)

                }
                else{

                  // Si l’utilisateur est connecté, redirige vers l’écran de gestion du compte
                  onMyAccountClickWithConnectedUser()

                }


              },
              text = {
                Text(
                  text = stringResource(id = R.string.action_myaccount)
                )
              }
            )
            DropdownMenuItem(
              onClick = {
                throw RuntimeException("Test Crashlytics") // Force a crash
              },
              text = {
                Text(
                  text = "Test Crashlytics (plante l'appli)"
                )
              }
            )
          }
        }
      )
    },
    floatingActionButtonPosition = FabPosition.End,
    floatingActionButton = {
      FloatingActionButton(
        onClick = {

          if (viewModel.isCurrentUserLogged()){
            onFABClick()
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

    val postsStateFlow by viewModel.postsStateFlow.collectAsStateWithLifecycle()

    when(val result = postsStateFlow){

      is ResultCustom.Loading -> {
        LoadingComposable(modifier = modifier)
      }

      is ResultCustom.Success -> {

        HomefeedList(
          modifier = modifier.padding(contentPadding),
          posts = result.value,
          onPostClick = onPostClick
        )

      }

      is ResultCustom.Failure -> {

        val activity = (context as Activity)

        val error = result.errorMessage ?: stringResource(R.string.unknown_error)

        ErrorComposable(
          modifier=modifier,
          sMessage = error,
          onClickRetryP = { viewModel.loadPost() },
          closeActivity = activity::finish
        )

      }

    }


  }
}




@Composable
private fun HomefeedList(
  modifier: Modifier = Modifier,
  posts: List<Post>,
  onPostClick: (Post) -> Unit,
) {

  val context = LocalContext.current

  LazyColumn(
    modifier = modifier.padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {

    items(posts, key = {it.id}) { post ->
      HomefeedCell(
        post = post,
        onPostClick = onPostClick
      )
    }

  }

  if (posts.isEmpty()){
    Toast
      .makeText(context, stringResource(R.string.no_posts), Toast.LENGTH_SHORT)
      .show()
  }


}

@Composable
private fun HomefeedCell(
  post: Post,
  onPostClick: (Post) -> Unit,
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    onClick = {
      onPostClick(post)
    }) {
    Column(
      modifier = Modifier.padding(8.dp),
    ) {
      Text(
        text = stringResource(
          id = R.string.by,
          post.author?.firstname ?: ""
        ),
        style = MaterialTheme.typography.titleSmall
      )
      Text(
        text = post.title,
        style = MaterialTheme.typography.titleLarge
      )
      if (!post.photoUrl.isNullOrEmpty()) {

        AsyncImage(
          modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .aspectRatio(ratio = 16 / 9f),
          model = post.photoUrl,
          imageLoader = LocalContext.current.imageLoader.newBuilder()
            .logger(DebugLogger())
            .build(),
          placeholder = ColorPainter(Color.DarkGray),
          contentDescription = "image",
          contentScale = ContentScale.Crop,
        )

      }
      if (!post.description.isNullOrEmpty()) {
        Text(
          modifier = Modifier
            .padding(top = 8.dp),
          text = post.description,
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun HomefeedCellPreview() {
  HexagonalGamesTheme {
    HomefeedCell(
      post = Post(
        id = "1",
        title = "title",
        description = "description",
        photoUrl = null,
        timestamp = 1,
        author = User(
          id = "1",
          firstname = "firstname"
        )
      ),
      onPostClick = {}
    )
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun HomefeedCellImagePreview() {
  HexagonalGamesTheme {
    HomefeedCell(
      post = Post(
        id = "1",
        title = "title",
        description = null,
        photoUrl = "https://picsum.photos/id/85/1080/",
        timestamp = 1,
        author = User(
          id = "1",
          firstname = "firstname"
        )
      ),
      onPostClick = {}
    )
  }
}