package com.openclassrooms.hexagonal.games.data.service


import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.domain.model.Post
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.storage.storage
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import kotlinx.coroutines.channels.ChannelResult


class PostFireStoreAPI : PostApi {

    companion object {
        private const val COLLECTION_POSTS : String = "posts"
    }

    // Variable globale de Firebase Storage (pour stocker les images)
    private val _storageRef = Firebase.storage.reference


    private fun getPostCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_POSTS)
    }

    private fun getAllPosts(): Query {
        return this.getPostCollection()
            .orderBy("timestamp", Query.Direction.DESCENDING)
    }


    override fun getPostsOrderByCreationDateDesc(): Flow<ResultCustom<List<Post>>> {

        val queryAllPosts = getAllPosts()

        // Cette méthode crée un Flow qui est basé sur des callbacks, ce qui est idéal pour intégrer des API asynchrones comme Firestore.
        return callbackFlow {

            trySend(ResultCustom.Loading)

            // addSnapshotListener : Ajoute un listener pour écouter les mises à jour en temps réel sur la requête. Chaque fois qu'il y a un changement dans Firestore, ce listener est appelé.
            val listenerRegistration = queryAllPosts.addSnapshotListener { snapshot, firebaseException ->

                if (firebaseException != null) {

                    trySend(ResultCustom.Failure(firebaseException.message))

                    close(firebaseException) // Fermer le flux en cas d'erreur

                    //return@addSnapshotListener // Permet de sortir du bloc .addSnapshotListener{
                }
                else{

                    val result : ChannelResult<Unit>

                    if (snapshot != null && !snapshot.isEmpty) {

                        // Utiliser toObjects necessite un constructeur par défaut pour tous les objets associés (Post et User ici)
                        // J'ai du ajouter des paramètres par défaut aux 2 data class
                        val posts = snapshot.toObjects(Post::class.java)

                        result = trySend(ResultCustom.Success(posts)) // Émettre la liste des posts

                    } else {

                        result = trySend(ResultCustom.Success(emptyList())) // Émettre une liste vide si aucun post n'est trouvé

                    }

                    if (result.isFailure) {
                        trySend(ResultCustom.Failure(result.toString()))
                        close(result.exceptionOrNull())
                    }

                }

            }

            // awaitClose : Permet d'exécuter du code quand le flow n'est plus écouté
            awaitClose {
                listenerRegistration.remove() // Ferme le listener pour éviter une fuite mémoire
            }

        }

    }


    override fun addPost(post: Post) : Flow<ResultCustom<String>> {

        // TODO Denis : En mode avion, je n'ai pas d'erreur => il ne se passe rien
        // mais les posts (sans images) sont ajoutés dès que la connexion revient
        // Comment indiquer çà à l'utilisateur ?

        // Cette méthode crée un Flow qui est basé sur des callbacks, ce qui est idéal pour intégrer des API asynchrones comme Firestore.
        return callbackFlow {

            try {

                // On rentre ici, que si un collector est présent sur le Flow

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
                            trySend(ResultCustom.Failure("Upload failed: ${exception.message}"))

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
                                            trySend(ResultCustom.Success("Post OK"))
                                        }
                                        .addOnFailureListener { firestoreException ->
                                            // Gestion des erreurs lors de l'ajout dans Firestore
                                            trySend(ResultCustom.Failure("Failed to add post to Firestore: ${firestoreException.message}"))
                                        }

                                        .addOnCanceledListener {
                                            trySend(ResultCustom.Failure("addOnCanceledListener"))
                                        }

                                }

                                .addOnFailureListener { urlException ->
                                    // Gestion des erreurs lors de la récupération de l'URL de téléchargement
                                    trySend(ResultCustom.Failure("Failed to get download URL: ${urlException.message}"))
                                }

                                .addOnCanceledListener {
                                    trySend(ResultCustom.Failure("addOnCanceledListener"))
                                }


                        }
                        .addOnCanceledListener {
                            trySend(ResultCustom.Failure("addOnCanceledListener"))
                        }
                        .addOnPausedListener {
                            trySend(ResultCustom.Failure("addOnPausedListener"))
                        }
                        .addOnProgressListener {
                            trySend(ResultCustom.Failure("addOnProgressListener"))
                        }


                }
                else {
                    // Aucune photo

                    // Si aucune photo n'est présente, ajouter directement le post dans Firestore
                    getPostCollection().add(post)
                        .addOnSuccessListener {
                            // Succès de l'ajout dans Firestore
                            trySend(ResultCustom.Success("Post OK")) // TODO Denis : Si je veux utiliser les ressources String ici, je suis obligé de passer le context en paramètre ?
                        }
                        .addOnFailureListener { firestoreException ->
                            // Gestion des erreurs lors de l'ajout dans Firestore
                            trySend(ResultCustom.Failure("Failed to add post to Firestore: ${firestoreException.message}"))
                        }
                        .addOnCanceledListener {
                            trySend(ResultCustom.Failure("addOnCanceledListener"))
                        }


                }


            } catch (e: Exception) {
                trySend(ResultCustom.Failure("Exception occurred: ${e.message}"))
            }



            // awaitClose : Permet d'exécuter du code quand le flow n'est plus écouté
            awaitClose {

            }
        }


    }


}