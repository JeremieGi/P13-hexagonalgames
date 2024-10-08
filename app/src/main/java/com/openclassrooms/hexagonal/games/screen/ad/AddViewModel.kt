package com.openclassrooms.hexagonal.games.screen.ad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * This ViewModel manages data and interactions related to adding new posts in the AddScreen.
 * It utilizes dependency injection to retrieve a PostRepository instance for interacting with post data.
 */
@HiltViewModel
class AddViewModel @Inject constructor(
  private val postRepository: PostRepository,
  private val userRepository: UserRepository
) : ViewModel() {
  
  /**
   * Internal mutable state flow representing the current post being edited.
   */
  private var _post = MutableStateFlow(
    Post(
      id = UUID.randomUUID().toString(),
      title = "",
      description = "",
      photoUrl = null,
      timestamp = System.currentTimeMillis(),
      author = null
    )
  )
  
  /**
   * Public state flow representing the current post being edited.
   * This is immutable for consumers.
   */
  val post : StateFlow<Post>
    get() = _post

  // UI state - Résultat du post
  private val _uiStatePostResult = MutableStateFlow<ResultCustom<String>?>(null)
  val uiStatePostResult: StateFlow<ResultCustom<String>?> = _uiStatePostResult.asStateFlow() // Accès en lecture seule de l'extérieur


  /**
   * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
   */
  val error = post.map { // chaque fois qu'il y a une nouvelle valeur dans _comment, la fonction verifyPost() est appelée.
    verifyPost()
  }.stateIn(
    scope = viewModelScope, // viewModelScope est le scope de coroutine fourni par le ViewModel, garantissant que le StateFlow s'annule lorsque le ViewModel est détruit.
    started = SharingStarted.WhileSubscribed(5_000), //  continue à émettre et à collecter des valeurs pendant qu'il y a des abonnés et se désactive 5000 millisecondes (5 secondes) après que le dernier abonné se soit désinscrit.
    initialValue = null, // valeur initiale de error est null
  )
  
  /**
   * Handles form events like title and description changes.
   *
   * @param formEvent The form event to be processed.
   */
  fun onAction(formEvent: FormEvent) {

    when (formEvent) {
      is FormEvent.DescriptionChanged -> {
        _post.value = _post.value.copy(
          description = formEvent.description
        )
      }
      
      is FormEvent.TitleChanged -> {
        _post.value = _post.value.copy(
          title = formEvent.title
        )
      }



      is FormEvent.PhotoChanged -> {

        // formEvent.photo.toString() => Si null renvoie la chaine "null" => pas bon

        val sPhotoURL : String?
        if (formEvent.photo==null){
          sPhotoURL = null
        }
        else{
          sPhotoURL = formEvent.photo.toString()
        }

        _post.value = _post.value.copy(
          photoUrl = sPhotoURL
        )
      }
    }
  }
  
  /**
   * Attempts to add the current post to the repository after setting the author.
   *
   */
  fun addPost() {

    val userParam : User?
    val userFirebase = userRepository.getCurrentUser()

    userParam = if (userFirebase!=null){
      User(id=userFirebase.uid, firstname = userFirebase.displayName?:"")
    }
    else{
      null // Gérer en amont : Un compte est nécessaire pour ajouter un commentaire
    }

    val postWithAuthor = _post.value.copy(author = userParam)


    viewModelScope.launch {
      postRepository.addPost(postWithAuthor).collect { result ->
        _uiStatePostResult.value = result
      }
    }



  }
  
  /**
   * Verifies mandatory fields of the post
   * and returns a corresponding FormError if so.
   *
   * @return A FormError.TitleError if title is empty, null otherwise.
   */
  private fun verifyPost(): FormError? {

    // Titre obligatoire
    if (_post.value.title.isEmpty()){
      return FormError.TitleError
    }

    // Le post doit contenir une description ou une photo
    if ( _post.value.description.isNullOrEmpty() &&  _post.value.photoUrl.isNullOrEmpty()){
      return FormError.DescriptionOrPictureError
    }

    return null // Pas d'erreur


  }
  
}
