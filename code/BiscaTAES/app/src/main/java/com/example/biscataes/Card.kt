package com.example.biscataes

// Enum para os Naipes (Suits)
enum class Suit {
    HEARTS, // Copas
    DIAMONDS, // Ouros
    CLUBS, // Paus
    SPADES // Espadas
}

// Enum para os Valores (Ranks)
// Guardamos o nome, os pontos e a ordem de força
enum class Rank(val points: Int, val strength: Int) {
    TWO(0, 1),
    THREE(0, 2),
    FOUR(0, 3),
    FIVE(0, 4),
    SIX(0, 5),
    QUEEN(2, 6), // Dama [cite: 84]
    JACK(3, 7), // Valete [cite: 84]
    KING(4, 8), // Rei [cite: 83]
    SEVEN(10, 9), // Sete (Bisca) [cite: 83]
    ACE(11, 10) // Ás [cite: 82]
}

// A Data Class que representa uma carta individual
data class Card(
    val suit: Suit,
    val rank: Rank,
    var isTrump: Boolean = false // Vamos usar isto para saber se é um trunfo
) {
    override fun toString(): String {
        return "${rank}_OF_${suit}"
    }
    // Função útil para mais tarde
    fun getPoints(): Int = rank.points
}