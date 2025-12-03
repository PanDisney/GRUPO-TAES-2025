package com.example.biscataes

import kotlinx.serialization.Serializable

@Serializable
data class GameListResponse(
    val data: List<Game>
)