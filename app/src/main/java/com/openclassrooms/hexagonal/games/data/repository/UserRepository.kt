package com.openclassrooms.hexagonal.games.data.repository

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepository @Inject constructor() {

    // Companion object pertinent même dans un Singleton pour stocker les constantes indépendantes de l'instance
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

    /**
     * @return : True => The current user is logged, else False
     */
    fun isCurrentUserLogged() : Boolean {
        return getCurrentUser()!=null
    }

    /**
     * Log out current user
     */
    fun signOut(context : Context) : Task<Void> {
        return AuthUI.getInstance().signOut(context)
    }

    /**
     * Delete user of the application's user
     */
    fun deleteUser(context : Context) : Flow<ResultCustom<String>> {

        val uidAuthentication : String? = this.getCurrentUserUID()

        return callbackFlow {

            // L'utilisateur doit être authentifié
            if (uidAuthentication!=null){

                // Il faut supprimer l'utilisateur en base de données avant
                // (pour qu'il soit encore logué au moment de la suppression,
                // sinon les règles de sécurité de la base ne seront pas respectées, seul l'utilisateur peut supprimer son enregistrement de la abse)
                 deleteUserFromFirestore(uidAuthentication)
                    .addOnSuccessListener {

                        // Suppression dans les données OK

                        // Suppression dans l'Authentification Firebase
                        AuthUI.getInstance().delete(context)
                            .addOnSuccessListener {
                                trySend(ResultCustom.Success(context.getString(R.string.deleteaccount_ok),))
                            }
                            .addOnFailureListener { exception ->
                                trySend(ResultCustom.Failure(exception.localizedMessage))
                            }

                    }
                    .addOnFailureListener { exception ->
                        // La suppression dans l'Authentification a échoué
                        trySend(ResultCustom.Failure(exception.localizedMessage))
                    }

            }
            else{
                trySend(ResultCustom.Failure(context.getString(R.string.nologinuser)))
            }

            // awaitClose : Permet de fermer le listener dès que le flow n'est plus écouté (pour éviter les fuites mémoire)
            awaitClose {

            }
        }

        
    }

    /**
     * Create User in Firestore (database)
     */
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

    /**
     * Get User Data from Firestore
     */
    private fun getUserData(): Task<DocumentSnapshot>? {

        val uidCurrentUser : String? = this.getCurrentUserUID()

        return if (uidCurrentUser != null) {
            getUsersCollection().document(uidCurrentUser).get() // Renvoie l'utilisateur
        } else {
            null
        }

    }


    /**
     * Delete the current User from Firestore, return null if user is not logged
     */
    private fun deleteUserFromFirestore(uidAuthenticationP : String) : Task<Void> {

        // L'utilisateur doit avoir les droits de supprimer un enregistrement de la BD
        return getUsersCollection().document(uidAuthenticationP).delete()

    }

    private fun getCurrentUserUID(): String? {
        return this.getCurrentUser()?.uid
    }


}