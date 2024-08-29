package com.openclassrooms.hexagonal.games.screen.addComment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.PostComment
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

@HiltViewModel
class AddCommentViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    /**
     * Internal mutable state flow representing the current comment being edited.
     */
    private var _comment = MutableStateFlow(
        PostComment(
            id = UUID.randomUUID().toString(),
            comment = "",
            timestamp = System.currentTimeMillis(),
            author = null
        )
    )

    /**
     * Public state flow representing the current comment being edited.
     * This is immutable for consumers.
     */
    val comment: StateFlow<PostComment>
        get() = _comment



    // UI state - Résultat de l'ajout du commentaire
    private val _uiStatePostCommentResult = MutableStateFlow<ResultCustom<String>?>(null)
    val uiStatePostCommentResult: StateFlow<ResultCustom<String>?> = _uiStatePostCommentResult.asStateFlow() // Accès en lecture seule de l'extérieur


    /**
     * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
     */
    val error = _comment.map {
        verifyPost()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    /**
     * Handles form events like title and description changes.
     *
     * @param commentInput : current comment value in the UI
     */
    fun onPostCommentChanged(commentInput : String) {

        _comment.value = _comment.value.copy(
            comment = commentInput
        )

    }

    /**
     * Attempts to add the current post to the repository after setting the author.
     *
     */
    fun addCommentToPost(postID : String) {

        val userParam : User?
        val userFirebase = userRepository.getCurrentUser()

        userParam = if (userFirebase!=null){
            User(id=userFirebase.uid, firstname = userFirebase.displayName?:"")
        }
        else{
            null // Gérer en amont : Un compte est nécessaire pour ajouter un commentaire
        }

        val commentWithAuthor = _comment.value.copy(author = userParam)


        viewModelScope.launch {
            postRepository.addCommentInPost(postID, commentWithAuthor).collect { result ->
                _uiStatePostCommentResult.value = result
            }
        }



    }

    /**
     * Verifies mandatory fields of the post
     * and returns a corresponding FormError if so.
     *
     * @return A FormError.TitleError if title is empty, null otherwise.
     */
    private fun verifyPost(): String? {

        // Titre obligatoire
        if (_comment.value.comment.isEmpty()){
            return "Commentaire non valide" // TODO Denis / JG Trad : Comment not valid => bonne pratique ?

        }

        return null // Pas d'erreur

    }

}