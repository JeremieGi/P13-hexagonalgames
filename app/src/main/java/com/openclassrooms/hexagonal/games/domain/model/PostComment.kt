package com.openclassrooms.hexagonal.games.domain.model

data class PostComment (

    /**
     * Unique identifier for the comment.
     */
    val id: String = "",

    val author : User? = null,

    val timestamp: Long = 0,

    val comment : String = ""

)
