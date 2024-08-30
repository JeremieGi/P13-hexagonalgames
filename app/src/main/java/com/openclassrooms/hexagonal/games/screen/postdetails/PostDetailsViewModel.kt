package com.openclassrooms.hexagonal.games.screen.postdetails

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

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
) : ViewModel() {


    // UI state - Résultat du post
    private val _uiStatePostResult = MutableStateFlow<ResultCustom<Post>>(ResultCustom.Loading)
    val uiStatePostResult: StateFlow<ResultCustom<Post>> = _uiStatePostResult.asStateFlow() // Accès en lecture seule de l'extérieur


    fun loadPost(idPost : String){

        viewModelScope.launch {
            postRepository.loadPostByID(idPost).collect { result ->
                // Je ne fais pas de classe UIState dédié
                _uiStatePostResult.value = result
            }
        }

    }

    fun isCurrentUserLogged(): Boolean {
        return userRepository.isCurrentUserLogged()
    }


}