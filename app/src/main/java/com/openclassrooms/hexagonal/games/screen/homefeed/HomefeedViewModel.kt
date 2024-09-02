package com.openclassrooms.hexagonal.games.screen.homefeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing data and events related to the Homefeed.
 * This ViewModel retrieves posts from the PostRepository and exposes them as a Flow<List<Post>>,
 * allowing UI components to observe and react to changes in the posts data.
 */
@HiltViewModel
class HomefeedViewModel @Inject constructor(
  private val postRepository: PostRepository,
  private val userRepository: UserRepository
) :
  ViewModel() {
  
  private val _postsStateFlow : MutableStateFlow<ResultCustom<List<Post>>> = MutableStateFlow(ResultCustom.Loading)
  val postsStateFlow: StateFlow<ResultCustom<List<Post>>> = _postsStateFlow.asStateFlow() // Accès en lecture seule de l'extérieur

  /**
   * Returns a Flow observable containing the list of posts fetched from the repository.
   *
   * @return A Flow<List<Post>> object that can be observed for changes.
   */
  //val postsStateFlow: StateFlow<ResultCustom<List<Post>>>
  //  get() = _postsStateFlow

  //private val _uiStateUserDeleteResult = MutableStateFlow<ResultCustom<String>?>(null)



  init {
    viewModelScope.launch {
      postRepository.flowPost.collect {
        _postsStateFlow.value = it
      }
    }
  }

  fun loadPost() {
    return postRepository.loadAllPosts()
  }

  fun isCurrentUserLogged() : Boolean {
    return userRepository.isCurrentUserLogged()
  }

  fun insertCurrentUserInFirestore() {
    return userRepository.insertCurrentUserInFirestore()
  }
  
}
