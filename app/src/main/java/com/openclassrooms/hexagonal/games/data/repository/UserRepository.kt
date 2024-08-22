package com.openclassrooms.hexagonal.games.data.repository

import android.content.Context
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.hexagonal.games.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepository @Inject constructor() {

    // TODO Denis : Companion object utile dans un Singleton ?
    companion object {
        private const val COLLECTION_USERS: String = "users"
    }

    // Get the Collection Reference
    private fun getUsersCollection(): CollectionReference {
        // collection() permet de récupérer la référence d'une collection dont le chemin est renseignée en paramètre de la méthode.
        // ici, on récupère tous les users
        return FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
    }


    /**
     * Return current user
     */
    fun getCurrentUser() : FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun isCurrentUserLogged() : Boolean {
        return getCurrentUser()!=null
    }

    fun signOut(context : Context) : Task<Void> {
        return AuthUI.getInstance().signOut(context)
    }

    fun deleteUser(context : Context) : Task<Void> {

        // Il faut supprimer l'utilisateur en base de données avant
        // (pour qu'il soit encore logué au moment de la suppression,
        // sinon les règles de sécurité de la base ne seront pas respectées, seul l'utilisateur peut supprimer son enregistrement de la abse)
        deleteUserFromFirestore()
            ?.addOnSuccessListener {
                Log.d("Debug : ","User supprimé de Firestore")
            }
            ?.addOnFailureListener { exception ->
                Log.d("Debug : ","Erreur : User non supprimé dans Firestore : ${exception.localizedMessage}")
            }

        // Si la suppression de l'utilisateur Firestore est réussie,
        // on supprime également l'utilisateur de l'authentification


        return AuthUI.getInstance().delete(context)

    // TODO Denis : Voir comment je peux remonter l'erreur de Firestore dans la Task renvoyée
    // TODO Denis :  Voir aussi comment exécuter soit les 2 opérations, soit aucune.


    }

    // Create User in Firestore
    fun insertCurrentUserInFirestore() {

        val user = getCurrentUser()
        // Si un utilisateur est connecté
        if (user != null) {

            val uid = user.uid // Récupération de l'ID créé lors de l'authenfication Firebase
            val firstName = user.displayName ?: ""
            val userToCreate = User(uid, firstName)

            // Utilisateur existant dans la base de données ?

            // Si l'utilisateur n'existe pas
            getUserData()?.addOnSuccessListener {

                // document() permet de récupérer la référence d'un document dont le chemin est renseigné en paramètre de la méthode
                // set() effectue le INSERT dans la base
                getUsersCollection().document(uid).set(userToCreate)

            }


        }
    }

    // Get User Data from Firestore
    private fun getUserData(): Task<DocumentSnapshot>? {

        val uidCurrentUser : String? = this.getCurrentUserUID()

        return if (uidCurrentUser != null) {
            getUsersCollection().document(uidCurrentUser).get() // Renvoie l'utilisateur
        } else {
            null
        }

    }


    // Delete the User from Firestore
    private fun deleteUserFromFirestore() : Task<Void>? {

        val uidAuthentication : String? = this.getCurrentUserUID()

        if (uidAuthentication!=null){

            // L'utilisateur doit avoir les droits de supprimer un enregistrement de la BD

//            val col = getUsersCollection()
//            val doc = col.document(uidAuthentication)
//            doc.delete()

            return getUsersCollection().document(uidAuthentication).delete()


        }
        else{
            return null
        }
    }

    private fun getCurrentUserUID(): String? {
        return this.getCurrentUser()?.uid
    }


}