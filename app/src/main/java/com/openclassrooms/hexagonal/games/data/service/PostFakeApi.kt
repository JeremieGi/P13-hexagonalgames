package com.openclassrooms.hexagonal.games.data.service

import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.PostComment
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update

/**
 * This class implements the PostApi interface and provides a fake in-memory data source for Posts.
 * It's intended for testing purposes and simulates a real API.
 */
class PostFakeApi : PostApi {

  private val users = mutableListOf(
    User("1", "Gerry"),
    User("2", "Brenton"),
    User("3", "Wally")
  )
  
  private var posts = MutableStateFlow(
    mutableListOf(
      Post(
        "5",
        "The Secret of the Flowers",
        "Improve your goldfish's physical fitness by getting him a bicycle.",
        null,
        1629858873, // 25/08/2021
        users[0]
      ),
      Post(
        "4",
        "The Door's Game",
        null,
        "https://picsum.photos/id/85/1080/",
        1451638679, // 01/01/2016
        users[2]
      ),
      Post(
        "1",
        "Laughing History",
        "He learned the important lesson that a picnic at the beach on a windy day is a bad idea.",
        "",
        1361696994, // 24/02/2013
        users[0]
      ),
      Post(
        "3",
        "Woman of Years",
        "After fighting off the alligator, Brian still had to face the anaconda.",
        null,
        1346601558, // 02/09/2012
        users[0]
      ),
      Post(
        "2",
        "The Invisible Window",
        null,
        "https://picsum.photos/id/40/1080/",
        1210645031, // 13/05/2008
        users[1]
      ),
    )
  )
  /*
  override fun getPostsOrderByCreationDateDesc(): Flow<List<Post>> =
    posts
  */

  override fun getPostsOrderByCreationDateDesc(): Flow<ResultCustom<List<Post>>> {

    return callbackFlow {

      val list : List<Post>  = posts.value
      trySend(ResultCustom.Success(list))

      // awaitClose : Permet de fermer le listener dès que le flow n'est plus écouté (pour éviter les fuites mémoire)
      awaitClose {

      }
    }

  }

  
  override fun addPost(post: Post) : Flow<ResultCustom<String>> {

    return callbackFlow {

      posts.value.add(0, post)
      trySend(ResultCustom.Success(""))

      // awaitClose : Permet de fermer le listener dès que le flow n'est plus écouté (pour éviter les fuites mémoire)
      awaitClose {

      }

    }

  }

  private fun _loadByID(idPost: String) : Post? {

    return posts.value.find { it.id == idPost }

  }


  override fun loadPostByID(idPost: String): Flow<ResultCustom<Post>> {

    val post = _loadByID(idPost)

    return callbackFlow {

      if (post==null){
        trySend(ResultCustom.Failure("No post find with ID = $idPost"))
      }
      else{
        trySend(ResultCustom.Success(post))
      }

      // awaitClose : Permet de fermer le listener dès que le flow n'est plus écouté (pour éviter les fuites mémoire)
      awaitClose {

      }
    }
  }

  override fun addCommentInPost(postId: String, comment: PostComment): Flow<ResultCustom<String>> {

    var bPostFind = false

    posts.update { currentPosts ->
      currentPosts.map { post ->
        if (post.id == postId) {
          bPostFind = true
          post.copy(
            listComments = post.listComments + comment // Ajouter le nouveau commentaire à la liste
          )
        } else {
          post
        }
      }.toMutableList() // Convertir en liste mutable si nécessaire
    }


    return callbackFlow {

      if (bPostFind){
        trySend(ResultCustom.Success(""))
      }
      else{
        trySend(ResultCustom.Failure("No post find with ID = $postId"))
      }

      // awaitClose : Suspend la coroutine actuelle jusqu'à ce que le canal soit fermé ou annulé et appelle le bloc donné avant de reprendre la coroutine.
      awaitClose {

      }
    }

  }

}
