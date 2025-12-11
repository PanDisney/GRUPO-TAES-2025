@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.biscataes

import kotlinx.serialization.Serializable

@Serializable
data class Trick(
    val trick_number: Int,
    val leader_id: Int?, // Make this nullable,
    val player1_card: String,
    val player2_card: String,
    val winner_id: Int? // Make this nullable
)

@Serializable
data class Game(
    val id: Int,
    val type: String,
    val status: String,
    val player1: User? = null,
    val player2: User? = null,
    val player1_points: Int? = null,
    val player2_points: Int? = null,
    @Deprecated("Use tricks instead") val player1_moves: List<MoveData>? = null,
    @Deprecated("Use tricks instead") val player2_moves: List<MoveData>? = null,
    val total_time: Int? = null,
    val winner: User? = null, // Assuming API will provide this
    val trump_card: String? = null,
    val tricks: List<Trick>? = null
)

