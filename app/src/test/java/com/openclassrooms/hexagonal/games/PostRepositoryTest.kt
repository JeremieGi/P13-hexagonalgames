package com.openclassrooms.hexagonal.games

import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.data.service.PostFakeApi
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.PostComment
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/**
 * Après coup, pas très pertinent de tester PostRepository qui joue uniquement le rôle de passe-plat vers l'API
 */

@OptIn(ExperimentalCoroutinesApi::class)
class PostRepositoryTest {

    private lateinit var postFakeAPI : PostFakeApi              // Pas de mock, je réutilise la FakeAPI initialement livré avec le projet
    private lateinit var cutPostRepository : PostRepository     //Class Under Test

    @Before
    fun createRepository() {

        postFakeAPI = PostFakeApi() // API reuse for Test
        cutPostRepository = PostRepository(postFakeAPI)

    }


    @Test
    fun test_loadAllPost() = runTest {

        // Créer le collecteur du flow du repository
        val resultList = mutableListOf<ResultCustom<List<Post>>>()
        val job = launch {
            cutPostRepository.flowPost.collect { result ->
                resultList.add(result)
            }
        }

        //when => Test réel de la fonction
        run {
            cutPostRepository.loadAllPosts()
        }

        // Attend que toutes les couroutines en attente s'executent
        advanceUntilIdle()

        // On attend 1 valeurs dans le flow du repository
        assertEquals(1, resultList.size)

        if (resultList.isNotEmpty()) {

            // Première valeur => La liste des posts
            val result = resultList[0]

            if (result is ResultCustom.Success){

                val listResult = result.value
                assertEquals("Nombre de post renvoyés",5,listResult.size)

            }
            else{
                assertFalse( "Le résultat devrait être de type ResultCustom.Success",false)
            }

        }
        else{
            assertFalse( "Flow vide",false)
        }

        // Cancel the collection job
        job.cancel()

    }

    @Test
    fun test_loadPostByID() = runTest {

        val idUser = "1"

        // Créer le collecteur du flow du repository
        val resultFlow = mutableListOf<ResultCustom<Post>>()
        val job = launch {
            cutPostRepository.loadPostByID(idUser).collect { result ->
                resultFlow.add(result)
            }
        }

        //when => Test réel de la fonction
        run {
            cutPostRepository.loadPostByID(idUser)
        }

        // Attend que toutes les couroutines en attente s'executent
        advanceUntilIdle()


        // On attend 1 valeur dans le flow
        assertEquals(1, resultFlow.size)

        if (resultFlow.size==1){

            val postResult = resultFlow[0]

            if (postResult is ResultCustom.Success){

                // l'utilisateur chargé a bien le même ID que celui recherché
                assertEquals("Vérification de l'ID", idUser, postResult.value.id)

            }
            else{
                assertFalse( "Le résultat devrait être de type ResultCustom.Success",false)
            }

        }
        else{
            assertFalse( "Une seule valeur attendue dans le Flow",false)
        }

        // Cancel the collection job
        job.cancel()

    }

    @Test
    fun test_addPost() = runTest {

        val post = Post(
            id = "1",
            title = "title",
            description = "description",
            photoUrl = null,
            timestamp = 1,
            author = User(
                id = "1",
                firstname = "firstname"
            )
        )

            // Créer le collecteur du flow du repository
        val resultAddFlow = mutableListOf<ResultCustom<String>>()
        val job = launch {
            cutPostRepository.addPost(post).collect { result ->
                resultAddFlow.add(result)
            }
        }

        //when => Test réel de la fonction
        run {
            cutPostRepository.addPost(post)
        }

        // Attend que toutes les couroutines en attente s'executent
        advanceUntilIdle()


        // On attend 1 valeur dans le flow
        assertEquals(1, resultAddFlow.size)

        if (resultAddFlow.size==1){

            val postResult = resultAddFlow[0]

            if (postResult is ResultCustom.Success){

                val actualFlow = cutPostRepository.flowPost.first()
                if (actualFlow is ResultCustom.Success){
                   assertEquals("Vérif un post supplémentaire",6,actualFlow.value.size)
                }

            }
            else{
                assertFalse( "Le résultat devrait être de type ResultCustom.Success",false)
            }

        }
        else{
            assertFalse( "Une seule valeur attendue dans le Flow",false)
        }

        // Cancel the collection job
        job.cancel()

    }


    @Test
    fun test_addComment() = runTest {

        val postID = "1"
        val author = User("2", "Brenton")
        val postComment =  PostComment(id="123",author = author, comment = "Mon commentaire")

        // Créer le collecteur du flow du repository
        val resultFlow = mutableListOf<ResultCustom<String>>()
        val job = launch {
            cutPostRepository.addCommentInPost(postID,postComment).collect { result ->
                resultFlow.add(result)
            }
        }

        //when => Test réel de la fonction
        run {
            cutPostRepository.addCommentInPost(postID,postComment)
        }

        // Attend que toutes les couroutines en attente s'executent
        advanceUntilIdle()


        // On attend 1 valeur dans le flow
        assertEquals(1, resultFlow.size)

        if (resultFlow.size==1){

            val postResult = resultFlow[0]

            if (postResult is ResultCustom.Success){
                // OK
            }
            else{
                assertFalse( "Le résultat devrait être de type ResultCustom.Success",false)
            }

        }
        else{
            assertFalse( "Une seule valeur attendue dans le Flow",false)
        }

        // Cancel the collection job
        job.cancel()

    }



}