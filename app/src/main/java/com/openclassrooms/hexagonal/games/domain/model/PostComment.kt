package com.openclassrooms.hexagonal.games.domain.model

import androidx.compose.runtime.Stable

@Stable
data class PostComment (

    /**
     * Unique identifier for the comment.
     */
    val id: String = "",

    val author : User? = null,

    val timestamp: Long = 0,

    val comment : String = ""

)
