@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.biscataes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Game(
    val id: Int,
    val type: String,
    val status: String,
    val player1_points: Int? = null,
    val player2_points: Int? = null,
    val player1_moves: List<MoveData>? = null,
    val player2_moves: List<MoveData>? = null,
    val total_time: Int? = null,
    val winner: User? = null // Assuming API will provide this
)
