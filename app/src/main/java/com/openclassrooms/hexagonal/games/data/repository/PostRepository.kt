package com.openclassrooms.hexagonal.games.data.repository

import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.service.PostApi
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.PostComment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class provides a repository for accessing and managing Post data.
 * It utilizes dependency injection to retrieve a PostApi instance for interacting
 * with the data source. The class is marked as a Singleton using @Singleton annotation,
 * ensuring there's only one instance throughout the application.
 */
@Singleton
class PostRepository @Inject constructor(
  private val postApi: PostApi,
  private val injectedContext: InjectedContext // Contexte connu par injection de dépendance (Permet de vérifier l'accès à Internet et aussi d'accéder aux ressources chaines)
) {
  
  /**
   * Retrieves a Flow object containing a list of Posts ordered by creation date
   * in descending order.
   *
   * @return Flow containing a list of Posts.
   */
  private var _flowPost : Flow<ResultCustom<List<Post>>> = postApi.getPostsOrderByCreationDateDesc()
  val flowPost: Flow<ResultCustom<List<Post>>>
    get() = _flowPost
  
  /**
   * Adds a new Post to the data source using the injected PostApi.
   *
   * @param post The Post object to be added.
   * @return
   */
  fun addPost(post: Post): Flow<ResultCustom<String>> {

    if (!injectedContext.isInternetAvailable()){
      return flow {
        emit(ResultCustom.Failure(injectedContext.getInjectedContext().getString(R.string.no_network)))
      }
    }
    else{
      return postApi.addPost(post)
    }

  }

  fun loadAllPosts() {

    _flowPost = if (!injectedContext.isInternetAvailable()) {
      // Créer un flux d'erreur si Internet n'est pas disponible
      flow {
        emit(ResultCustom.Failure(injectedContext.getInjectedContext().getString(R.string.no_network)))
      }
    } else {
      // Assigner le flux retourné par l'API si Internet est disponible
      postApi.getPostsOrderByCreationDateDesc()
    }

  }

  fun loadPostByID(idPost : String) : Flow<ResultCustom<Post>> {

    if (!injectedContext.isInternetAvailable()) {
      return flow {
        emit(
          ResultCustom.Failure(
            injectedContext.getInjectedContext().getString(R.string.no_network)
          )
        )
      }
    } else {
      return postApi.loadPostByID(idPost)
    }
  }

  fun addCommentInPost(postId: String, comment: PostComment): Flow<ResultCustom<String>> {

    if (!injectedContext.isInternetAvailable()){
      return flow {
        emit(ResultCustom.Failure(injectedContext.getInjectedContext().getString(R.string.no_network)))
      }
    }
    else{
      return postApi.addCommentInPost(postId,comment)
    }

  }
  
}
