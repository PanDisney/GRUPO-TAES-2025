@file:OptIn(InternalSerializationApi::class)

package com.example.biscataes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json@InternalSerializationApi


@Serializable
data class CardFace(
    val id: Int,
    val name: String,
    val cost: Int,
    val image_name: String,
    val is_owned: Boolean
)
