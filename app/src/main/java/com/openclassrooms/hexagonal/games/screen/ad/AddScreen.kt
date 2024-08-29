package com.openclassrooms.hexagonal.games.screen.ad

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.screen.homefeed.LoadingComposable
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
  modifier: Modifier = Modifier,
  viewModel: AddViewModel = hiltViewModel(),
  onBackClick: () -> Unit
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(id = R.string.add_fragment_label))
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
    val post by viewModel.post.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // Obtenir le résultat de l'enregistrement du post
    val postResult by viewModel.uiStatePostResult.collectAsStateWithLifecycle()



    CreatePost(
      modifier = Modifier.padding(contentPadding),
      error = error,
      title = post.title,
      onTitleChanged = { viewModel.onAction(FormEvent.TitleChanged(it)) },
      description = post.description ?: "",
      onDescriptionChanged = { viewModel.onAction(FormEvent.DescriptionChanged(it)) },
      onPhotoChanged = { viewModel.onAction(FormEvent.PhotoChanged(it)) },
      stateResultSave = postResult,
      onSaveClicked = { viewModel.addPost() },
      onBackAfterSaveClick = onBackClick
    )
  }
}

@Composable
private fun CreatePost(
  modifier: Modifier = Modifier,
  title: String,
  onTitleChanged: (String) -> Unit,
  description: String,
  onDescriptionChanged: (String) -> Unit,
  onPhotoChanged: (Uri?) -> Unit,
  onSaveClicked: () -> Unit,
  error: FormError?,
  stateResultSave: ResultCustom<String>?,
  onBackAfterSaveClick: () -> Unit
) {

  val context = LocalContext.current

  val scrollState = rememberScrollState()


  // Gérer les différents états du résultat d'ajout d'un post
  when (stateResultSave) {

    is ResultCustom.Loading -> {
      LoadingComposable() // N'arrivera pas
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

    null -> {
      // Ne rien afficher => pas d'ajout de post en cours'
      //Log.d("test","test")
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
        value = title,
        isError = error is FormError.TitleError,
        onValueChange = { onTitleChanged(it) },
        label = { Text(stringResource(id = R.string.hint_title)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = true
      )
      if (error is FormError.TitleError) {
        Text(
          text = stringResource(id = error.messageRes),
          color = MaterialTheme.colorScheme.error,
        )
      }


      var selectedImageUri by rememberSaveable() { mutableStateOf<Uri?>(null) }

      // Callback du mediaPicker (Android 11 et supérieur
      val pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedImageUri = uri
        onPhotoChanged(selectedImageUri)
      }

      // Callback du image launcher (Android 10 et inférieur)
      val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        onPhotoChanged(selectedImageUri)
      }



      OutlinedTextField(
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(),
        value = description,
        isError = error is FormError.DescriptionOrPictureError,
        onValueChange = { onDescriptionChanged(it) },
        label = { Text(stringResource(id = R.string.hint_description)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
      )
      if (error is FormError.DescriptionOrPictureError) {
        Text(
          text = stringResource(id = error.messageRes),
          color = MaterialTheme.colorScheme.error,
        )
      }


      Button(
        modifier = Modifier.padding(top = 16.dp),
        onClick = {

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+
            // Lancement du media picker (que les images)
            pickMediaLauncher.launch(PickVisualMediaRequest(ImageOnly))
          } else { // Versions inférieures
            pickImageLauncher.launch("image/*")
          }
        }
      ) {
        Text(
          modifier = Modifier.padding(8.dp),
          text = stringResource(id = R.string.action_selectPhoto)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Si une image est sélectionnée
      selectedImageUri?.let { uri ->
        Image(
          painter = rememberAsyncImagePainter(uri), //  l'image est chargée et affichée à l'aide de Coil, une bibliothèque populaire pour le chargement d'images dans Compose.
          contentDescription = null,
          modifier = Modifier.size(200.dp),
          contentScale = ContentScale.Crop
        )
      }


    }
    Button(
      enabled = (error == null),
      onClick = {
        onSaveClicked()
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
    CreatePost(
      title = "test",
      onTitleChanged = { },
      description = "description",
      onDescriptionChanged = { },
      onPhotoChanged = { },
      onSaveClicked = { },
      error = null,
      stateResultSave = null,
      onBackAfterSaveClick = {}
    )
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun CreatePostErrorPreview() {
  HexagonalGamesTheme {
    CreatePost(
      title = "test",
      onTitleChanged = { },
      description = "description",
      onDescriptionChanged = { },
      onPhotoChanged = { },
      onSaveClicked = { },
      error = FormError.TitleError,
      stateResultSave = null,
      onBackAfterSaveClick = {}
    )
  }
}