package com.example.biscataes

class Deck {

    private var cards = mutableListOf<Card>()

    var trumpSuit: Suit? = null
        private set

    init {
        createDeck()
    }

    private fun createDeck() {
        cards.clear()

        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                cards.add(Card(suit, rank))
            }
        }
    }

    fun shuffle() {
        cards.shuffle()
    }

    fun shuffleIfNeeded(shouldShuffle: Boolean) {
        if (shouldShuffle) {
            cards.shuffle()
        }
    }

    fun drawCard(): Card? {
        if (cards.isEmpty()) {
            return null
        }
        return cards.removeAt(0)
    }

    fun setTrump(): Card? {
        val trumpCard = cards.lastOrNull()
        if (trumpCard != null) {
            trumpSuit = trumpCard.suit

            for (card in cards) {
                if (card.suit == trumpSuit) {
                    card.isTrump = true
                }
            }
        }
        return trumpCard
    }

    fun cardsRemaining(): Int {
        return cards.size
    }

    fun getCards(): MutableList<Card> {
        return cards
    }

    fun clear() {
        cards.clear()
    }
}