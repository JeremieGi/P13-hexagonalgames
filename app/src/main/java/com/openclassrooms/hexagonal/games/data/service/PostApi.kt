package com.openclassrooms.hexagonal.games.data.service

import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.PostComment
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines the contract for interacting with Post data from a data source.
 * It outlines the methods for retrieving and adding Posts, abstracting the underlying
 * implementation details of fetching and persisting data.
 */
interface PostApi {
  /**
   * Retrieves a list of Posts ordered by their creation date in descending order.
   *
   * @return A list of Posts sorted by creation date (newest first).
   */
  fun getPostsOrderByCreationDateDesc(): Flow<ResultCustom<List<Post>>> // Nouvelle signature pour gestion des erreurs

  /**
   * Adds a new Post to the data source.
   *
   * @param post The Post object to be added.
   */
  fun addPost(post: Post): Flow<ResultCustom<String>>

  /**
   * Load a post
   */
  fun loadPostByID(idPost: String): Flow<ResultCustom<Post>>


  /**
   * Adds a new comment in a Post to the data source.
   *
   * @param postId : ID post
   * @param comment : comment to add
   */
  fun addCommentInPost(postId: String, comment: PostComment): Flow<ResultCustom<String>>

}
