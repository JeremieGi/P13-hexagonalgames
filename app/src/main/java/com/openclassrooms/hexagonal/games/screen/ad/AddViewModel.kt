package com.openclassrooms.hexagonal.games.screen.ad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
  val post: StateFlow<Post>
    get() = _post
  
  /**
   * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
   */
  val error = post.map {
    verifyPost()
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = null,
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
        _post.value = _post.value.copy(
          photoUrl = formEvent.photo.toString()
        )
      }
    }
  }
  
  /**
   * Attempts to add the current post to the repository after setting the author.
   *
   */
  fun addPost() {

    // TODO JG : Ne pas permettre la saisie du post si l'utilisateur n'est pas loggué

    // Retrieve the current user

    val userParam : User?
    val userFirebase = userRepository.getCurrentUser()

    userParam = if (userFirebase!=null){
      User(id=userFirebase.uid, firstname = userFirebase.displayName?:"", lastname = "")
    }
    else{
      null
    }

    postRepository.addPost(
      _post.value.copy(
        //author = User("1", "Gerry", "Ariella")
        author = userParam
      )
    )
  }
  
  /**
   * Verifies mandatory fields of the post
   * and returns a corresponding FormError if so.
   *
   * @return A FormError.TitleError if title is empty, null otherwise.
   */
  private fun verifyPost(): FormError? {
    return if (_post.value.title.isEmpty()) {
      FormError.TitleError
    } else {
      null
    }
  }
  
}
