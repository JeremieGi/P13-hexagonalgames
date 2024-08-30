package com.openclassrooms.hexagonal.games.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openclassrooms.hexagonal.games.screen.Screen
import com.openclassrooms.hexagonal.games.screen.ad.AddScreen
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedScreen
import com.openclassrooms.hexagonal.games.screen.postdetails.PostDetailScreen
import com.openclassrooms.hexagonal.games.screen.settings.SettingsScreen
import com.openclassrooms.hexagonal.games.screen.userinfoscreen.UserInfoScreen
import com.openclassrooms.hexagonal.games.screen.addComment.AddCommentScreen
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the application. This activity serves as the entry point and container for the navigation
 * fragment. It handles setting up the toolbar, navigation controller, and action bar behavior.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)


    setContent {
      val navController = rememberNavController()
      
      HexagonalGamesTheme {
        HexagonalGamesNavHost(navHostController = navController)
      }
    }
  }


}

private const val idPostArg = "postId"
private const val refresh_comments = "refresh_comments"


@Composable
fun HexagonalGamesNavHost(navHostController: NavHostController) {
  NavHost(
    navController = navHostController,
    startDestination = Screen.Homefeed.route
  ) {
    composable(route = Screen.Homefeed.route) {
      HomefeedScreen(
        onPostClick = { post ->
          //Le clic sur un Post, ouvre le Post
          navHostController.navigate(Screen.PostDetail.createRoute(post.id))
        },
        onSettingsClick = {
          navHostController.navigate(Screen.Settings.route)
        },
        onFABClick = {
          navHostController.navigate(Screen.AddPost.route)
        },
        onMyAccountClickWithConnectedUser = {
          navHostController.navigate(Screen.UserInfo.route)
        }
      )
    }
    composable(route = Screen.AddPost.route) {
      AddScreen(
        onBackClick = { navHostController.navigateUp() }
      )
    }
    composable(route = Screen.Settings.route) {
      SettingsScreen(
        onBackClick = { navHostController.navigateUp() }
      )
    }
    composable(route = Screen.UserInfo.route) {
      UserInfoScreen(
        onBackClick = { navHostController.navigateUp() }
      )
    }

    composable(route = Screen.PostDetail.route) { backStackEntry ->

      // Extraire le postId de l'entrée de la pile
      val postId = backStackEntry.arguments?.getString(idPostArg)

      // Les commentaires doivent être rafraichis ? (Ajout d'un commentaire depuis AddCommentToPost)
      val refreshComments = backStackEntry.savedStateHandle?.get<Boolean>(refresh_comments)

      PostDetailScreen(
        postId = postId,
        onBackClick = { navHostController.navigateUp() },
        onFABAddCommentClick = { idPost ->
          // L'ID du post est transféré
          navHostController.navigate(Screen.AddCommentToPost.createRoute(idPost))
        },
        reloadPost = refreshComments
      )
    }

    composable(route = Screen.AddCommentToPost.route) { backStackEntry ->

      // Extraire le postId de l'entrée de la pile
      val postId = backStackEntry.arguments?.getString(idPostArg)

      val savedStateHandle = navHostController.previousBackStackEntry?.savedStateHandle


      Log.d("Debug","AddCommentScreen => ouverture") // TODO : Prio 2 - On dirait que NavHost fait pleins de redessin

      AddCommentScreen(
        postId = postId,
        onBackClick = { navHostController.navigateUp() },
        onCommentAdded = {
          savedStateHandle?.set(refresh_comments, true) // Permet d'indiquer à la fenêtre parente (PostDetail) qu'un commentaire a été ajouté
          navHostController.navigateUp()
        }
      )

    }

  }
}


