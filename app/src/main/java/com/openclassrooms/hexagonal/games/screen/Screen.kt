package com.openclassrooms.hexagonal.games.screen

//import androidx.navigation.NamedNavArgument

sealed class Screen(
  val route: String,
  //val navArguments: List<NamedNavArgument> = emptyList()
) {

  companion object {
    const val ID_POST_ARG = "postId"
  }

  data object Homefeed : Screen("homefeed")
  
  data object AddPost : Screen("addPost")
  
  data object Settings : Screen("settings")

  data object UserInfo : Screen("userInfo")

  data object PostDetail : Screen("postDetail/{$ID_POST_ARG}"){
    // Configurer la Route avec des Arguments
    fun createRoute(postId: String) = "postDetail/$postId"
  }

  data object AddCommentToPost : Screen("addCommentToPost/{$ID_POST_ARG}"){
    // Configurer la Route avec des Arguments
    fun createRoute(postId: String) = "addCommentToPost/$postId"
  }
}