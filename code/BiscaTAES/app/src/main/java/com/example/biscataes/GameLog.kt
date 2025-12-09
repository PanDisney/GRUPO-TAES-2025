package com.example.biscataes

import kotlinx.serialization.Serializable

@Serializable
data class GameLog(
    val playerFinalPoints: Int,
    val botFinalPoints: Int,
    val playerMoves: List<MoveData>,
    val botMoves: List<MoveData>,
    val trumpCard: String?, // Make it nullable as it might not be set in some edge cases
    val gameResult: GameEngine.GameResult
)
