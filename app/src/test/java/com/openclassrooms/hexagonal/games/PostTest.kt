package com.openclassrooms.hexagonal.games

import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.PostComment
import org.junit.Assert.assertEquals
import org.junit.Test

class PostTest {


    @Test
    fun test_listCommentsRecentsFirst() {

        val listComment = listOf(
            PostComment("1",null,1451638679,"Commentaire 1"),// 01/01/2016,
            PostComment("2",null,1629858873,"Commentaire 2") // 25/08/2021
        )

        val post = Post(listComments = listComment)

        val resultSorted = post.listCommentsRecentsFirst()

        assertEquals("Commentaire le plus récent en premier","2",resultSorted[0].id)
        assertEquals("Commentaire le plus vieux en dernier","1",resultSorted[1].id)


    }

}