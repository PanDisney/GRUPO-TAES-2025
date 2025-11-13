package com.example.biscataes

class Deck {

    // A lista de cartas, que começa vazia
    private var cards = mutableListOf<Card>()

    // O naipe de trunfo do jogo
    var trumpSuit: Suit? = null
        private set // Só pode ser definido por esta classe

    init {
        // Bloco inicializador: é corrido assim que um Deck é criado
        createDeck()
    }

    // Função interna para criar as 40 cartas
    private fun createDeck() {
        cards.clear() // Limpa o baralho caso seja reutilizado

        // Para cada naipe (Suit)
        for (suit in Suit.values()) {
            // Para cada valor (Rank)
            for (rank in Rank.values()) {
                // Adiciona a carta correspondente
                cards.add(Card(suit, rank))
            }
        }
    }

    // Função para baralhar as cartas
    fun shuffle() {
        cards.shuffle()
    }

    // Função para puxar (comprar) a carta do topo
    fun drawCard(): Card? {
        if (cards.isEmpty()) {
            return null // Retorna nulo se o baralho estiver vazio
        }
        return cards.removeAt(0) // Remove e retorna a primeira carta da lista
    }

    // Função para definir o trunfo
    fun setTrump(): Card? {
        // O trunfo é a última carta do baralho baralhado
        val trumpCard = cards.lastOrNull()
        if (trumpCard != null) {
            trumpSuit = trumpCard.suit

            // Marca todas as cartas desse naipe como sendo trunfo
            for (card in cards) {
                if (card.suit == trumpSuit) {
                    card.isTrump = true
                }
            }
        }
        return trumpCard
    }

    // Função para saber quantas cartas restam
    fun cardsRemaining(): Int {
        return cards.size
    }
}