package com.openclassrooms.hexagonal.games.data.service

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.storage.storage


class PostFireStoreAPI : PostApi {

    // TODO Denis : Companion object utile dans un Singleton ?
    companion object {
        private const val COLLECTION_POSTS : String = "posts"
    }


    private val _storageRef = Firebase.storage.reference

    private fun getPostCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_POSTS)
    }

    private fun getAllPosts(): Query {
        return this.getPostCollection()
            .orderBy("timestamp", Query.Direction.DESCENDING)
    }

    // TODO Denis : Revue de getPostsOrderByCreationDateDesc

    override fun getPostsOrderByCreationDateDesc(): Flow<List<Post>> {

        val queryAllPosts = getAllPosts()

        // Cette méthode crée un Flow qui est basé sur des callbacks, ce qui est idéal pour intégrer des API asynchrones comme Firestore.
        return callbackFlow {

            // TODO Denis : Si je mets le tél en mode avion, j'ai pas d'erreur

            // addSnapshotListener : Ajoute un listener pour écouter les mises à jour en temps réel sur la requête. Chaque fois qu'il y a un changement dans Firestore, ce listener est appelé.
            val listenerRegistration = queryAllPosts.addSnapshotListener { snapshot, firebaseException ->

                if (firebaseException != null) {
                    close(firebaseException) // Fermer le flux en cas d'erreur
                    return@addSnapshotListener
                }
/*
                if (snapshot != null && !snapshot.isEmpty) {

                    // Marche pas car il me faut un contructeur sans paramètre pour Post
                    val posts = snapshot.toObjects(Post::class.java)

                    trySend(posts).isSuccess // Émettre la liste des posts
                } else {
                    trySend(emptyList()).isSuccess // Émettre une liste vide si aucun post n'est trouvé
                }
*/

                // Traiter les documents de la snapshot
                val posts = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val id = document.id
                        val title = document.getString("title") ?: ""
                        val desc = document.getString("description") ?: ""
                        val photoURL = document.getString("photoUrl") ?: ""
                        val timestamp = document.getLong("timestamp") ?: 0L

                        val userAuthor: User? = document.get("author")?.let { authorMap ->     // let permet de travailler sur l'auteur que si la valeur est renseignée
                            // TODO Denis : Warning sur la ligne ci-dessous
                            val mapAuthor = authorMap as? Map<String, Any>                     // 3 éléments indicés par le nom de la rubrique
                            mapAuthor?.let {
                                val idUser = it["id"] as? String ?: ""
                                val firstnameUser = it["firstname"] as? String ?: ""
                                User(idUser, firstnameUser, "")
                            }
                        }

                        Post(id, title, desc, photoURL, timestamp, userAuthor)

                    } catch (ex: Exception) {
                        // Gérer les erreurs de conversion ici
                        null
                    }
                } ?: emptyList()

                // Émettre la liste des posts (Equivalent de emit dans un callbackFlow)
                val result = trySend(posts)
                if (result.isFailure) {
                    close(result.exceptionOrNull())
                }

            }

            // TODO Denis : explication de awaitClose (Si je l'enlève l'appli plante) : java.lang.IllegalStateException: 'awaitClose { yourCallbackOrListener.cancel() }' should be used in the end of callbackFlow block.
            // Cette fonction est appelée lorsque le Flow est annulé
            awaitClose {
                listenerRegistration.remove() // Suppression du listener mais comment le remettre ensuite ?
            }
        }

    }


    override fun addPost(post: Post) {

        // TODO Denis : Voir comment bien gérer les cas d'exceptions

        // Si une photo est présente, il faut l'uploader
        if (post.photoUrl != null){

            // Récupération du content (content://media/picker/0/com.android.providers.media.photopicker/media/1000000035)
            // dans une URI
            val uri = Uri.parse(post.photoUrl)

            // Référence vers le fichier distant
            val storageRefImage = _storageRef.child("images/postID${post.id}.jpg")

            // Upload
            val uploadTask = storageRefImage.putFile(uri)

            // Observer les résultats de l'upload
            uploadTask
                .addOnFailureListener { exception ->

                    // Gestion des erreurs lors de l'upload
                    println("Upload failed: ${exception.message}")

                }
                .addOnSuccessListener { taskSnapshot ->

                    // Récupérer l'URL de téléchargement de l'image
                    storageRefImage.downloadUrl
                        .addOnSuccessListener { uri ->

                            // Mettre à jour l'objet Post avec l'URL de l'image
                            val updatedPost = post.copy(photoUrl = uri.toString())

                            // Ajouter le post dans Firestore
                            getPostCollection().add(updatedPost)
                                .addOnSuccessListener {
                                    // Succès de l'ajout dans Firestore
                                    println("Post added successfully to Firestore")
                                }
                                .addOnFailureListener { firestoreException ->
                                    // Gestion des erreurs lors de l'ajout dans Firestore
                                    println("Failed to add post to Firestore: ${firestoreException.message}")
                                }

                        }

                        .addOnFailureListener { urlException ->
                            // Gestion des erreurs lors de la récupération de l'URL de téléchargement
                            println("Failed to get download URL: ${urlException.message}")
                        }
                }

        }
        else {
            // Aucune photo

            // Si aucune photo n'est présente, ajouter directement le post dans Firestore
            getPostCollection().add(post)
                .addOnSuccessListener {
                    // Succès de l'ajout dans Firestore
                    println("Post added successfully to Firestore")
                }
                .addOnFailureListener { firestoreException ->
                    // Gestion des erreurs lors de l'ajout dans Firestore
                    println("Failed to add post to Firestore: ${firestoreException.message}")
                }

        }





    }


}