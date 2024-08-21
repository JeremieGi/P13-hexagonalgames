package com.openclassrooms.hexagonal.games.data.service

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.domain.model.Post
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class PostFireStoreAPI : PostApi {

    // TODO Denis : Companion object utile dans un Singleton ?
    companion object {
        private const val COLLECTION_POSTS : String = "posts"
    }

    private fun getPostCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_POSTS)
    }

    private fun getAllPosts(): Query {
        return this.getPostCollection()
//            .document(chat)
//            .collection(MESSAGE_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
 //           .limit(50)
    }


    override fun getPostsOrderByCreationDateDesc(): Flow<List<Post>> {

        val queryAllPosts = getAllPosts()

        // Cette méthode crée un Flow qui est basé sur des callbacks, ce qui est idéal pour intégrer des API asynchrones comme Firestore.
        return callbackFlow {

            // addSnapshotListener : Ajoute un listener pour écouter les mises à jour en temps réel sur la requête. Chaque fois qu'il y a un changement dans Firestore, ce listener est appelé.
            val listenerRegistration = queryAllPosts.addSnapshotListener { snapshot, firebaseException ->

                if (firebaseException != null) {
                    close(firebaseException) // Fermer le flux en cas d'erreur
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val posts = snapshot.toObjects(Post::class.java)
                    trySend(posts).isSuccess // Émettre la liste des posts
                } else {
                    trySend(emptyList()).isSuccess // Émettre une liste vide si aucun post n'est trouvé
                }
            }

            // Cette fonction est appelée lorsque le Flow est annulé
            awaitClose {
                listenerRegistration.remove()
            }
        }

    }


    override fun addPost(post: Post) {

        // document() permet de récupérer la référence d'un document dont le chemin est renseigné en paramètre de la méthode
        // set() effectue le INSERT dans la base
        getPostCollection().add(post)


    }


}