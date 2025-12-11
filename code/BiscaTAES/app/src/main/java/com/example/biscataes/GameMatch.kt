@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.biscataes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameMatch(
    val id: Int,
    val player1: User,
    val player2: User,
    val status: String? = null,
    val total_time: Int? = null, // Add this
    val began_at: String? = null,


    val winner: User? = null,

    val player1_marks: Int? = null,
    val player2_marks: Int? = null,
    val games: List<Game>? = null
)