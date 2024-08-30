package com.openclassrooms.hexagonal.games.domain.model

import androidx.compose.runtime.Stable
import java.io.Serializable

// Tous les éléments ont des paramètres par défauts pour pouvoir utiliser la fonction toObjects

/**
 * This class represents a Post data object. It holds information about a post, including its
 * ID, title, description, photo URL, creation timestamp, and the author (User object).
 * The class implements Serializable to allow for potential serialization needs.
 */
@Stable // Marking a class as @Stable indicates that the class's instances are stable and their properties will not change unexpectedly.
data class Post(
  /**
   * Unique identifier for the Post.
   */
  val id: String = "",
  
  /**
   * Title of the Post.
   */
  val title: String = "",
  
  /**
   * Optional description for the Post.
   */
  val description: String? = null,
  
  /**
   * En lecture, URL : https://firebasestorage.googleapis.com/v0/b/hexagonal-games.appspot.com/o/images%2FpostIDd8681ac9-1dbe-4efd-806d-3d4a33ffee2f.jpg?alt=media&token=58cb3373-8e77-4763-a484-88f31979d755"
   * Lors de la saisie, ce membre contient le chemin renvoyé par le picker : content://media/picker/0/com.android.providers.media.photopicker/media/1000000035
   */
  val photoUrl: String? = null,
  
  /**
   * Timestamp representing the creation date and time of the Post in milliseconds since epoch.
   */
  // @ServerTimestamp => java.lang.IllegalArgumentException: Field timestamp is annotated with @ServerTimestamp but is long instead of Date or Timestamp
  val timestamp: Long = 0,
  
  /**
   * User object representing the author of the Post.
   */
  val author: User? = null,

  /**
   * List of comments
   */
  val listComments: List<PostComment> = emptyList()

) : Serializable {

  // Accesseur pour obtenir la liste des commentaires triés par timestamp décroissant
  fun listCommentsRecentsFirst() : List<PostComment> {
    return listComments.sortedByDescending { it.timestamp }
  }

}
