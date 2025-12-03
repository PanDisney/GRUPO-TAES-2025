package com.example.biscataes

import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: Int,
    val player1_id: Int,
    val player2_id: Int,
    val winner_id: Int?,
    val type: String,
    val status: String,
    val began_at: String,
    val ended_at: String?,
    val total_time: Int?,
    val player1_moves: Int?,
    val player2_moves: Int?
)
