package com.example.biscataes

class Player(val name: String, val isBot: Boolean = false) {

    // A mão do jogador
    private val hand = mutableListOf<Card>()

    // As cartas que o jogador ganhou
    private val wonCards = mutableListOf<Card>()

    // Função para adicionar uma carta à mão (quando se compra do baralho)
    fun drawToHand(card: Card) {
        hand.add(card)
    }

    // Função para jogar uma carta da mão
    // Recebe o índice da carta na mão
    fun playCard(cardIndex: Int): Card {
        // Remove a carta da mão e retorna-a
        return hand.removeAt(cardIndex)
    }

    // Função para adicionar as cartas ganhas numa vaza
    fun addWonCards(cards: List<Card>) {
        wonCards.addAll(cards)
    }

    // Função para calcular o total de pontos
    fun calculatePoints(): Int {
        var totalPoints = 0
        for (card in wonCards) {
            totalPoints += card.getPoints() // Usa a função que criámos na classe Card
        }
        return totalPoints
    }

    // Função para obter a mão atual do jogador
    fun getHand(): List<Card> {
        return hand
    }

    // Função para verificar se a mão está vazia
    fun isHandEmpty(): Boolean {
        return hand.isEmpty()
    }
}