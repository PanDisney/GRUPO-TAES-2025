package com.example.biscataes

import android.util.Log

class GameEngine(startMode: String? = null) {

    enum class GameResult{
        PLAYER_WINS,
        BOT_WINS,
        DRAW,
        UNDEFINED
    }

    var gameResult: GameResult = GameResult.UNDEFINED
        private set
    private lateinit var player: Player
    private lateinit var bot: Player
    private lateinit var deck: Deck

    private var isPlayerTurnToLead = true
    var trumpCard: Card? = null
        private set

    var isGameRunning = false
        private set

    var playerPoints = 0
        private set
    var botPoints = 0
        private set

    var playerGamesWon = 0
        private set
    var botGamesWon = 0
        private set

    init {
        Log.d("GameEngine", "Motor de Jogo Criado. A iniciar nova partida...")
        startNewMatch(startMode)
    }

    fun startNewMatch(startMode: String? = null) {
        playerGamesWon = 0
        botGamesWon = 0
        startNewGame(true, startMode)
    }

    fun startNewGame(isFirstGame: Boolean = false, startMode: String? = null) {
        gameResult = GameResult.UNDEFINED
        playerPoints = 0
        botPoints = 0

        player = Player("Humano", isBot = false)
        bot = Player("Bot", isBot = true)

        deck = Deck()
        val shouldShuffle = startMode != "NO_SHUFFLE" && startMode != "DEBUG_DEAL"
        deck.shuffleIfNeeded(shouldShuffle)
        trumpCard = deck.setTrump()

        if (trumpCard == null) {
            Log.e("GameEngine", "Erro: O baralho está vazio, não foi possível definir trunfo.")
            return
        }

        if (startMode == "NO_SHUFFLE") {
            val allCards = deck.getCards().toMutableList()
            val highValueCards = allCards.filter { it.rank == Rank.ACE || it.rank == Rank.SEVEN }
            highValueCards.forEach { player.drawToHand(it) }
            allCards.removeAll(highValueCards)

            val remainingPlayerHand = 9 - player.getHand().size
            for (i in 0 until remainingPlayerHand) {
                if (allCards.isNotEmpty()) {
                    player.drawToHand(allCards.removeAt(0))
                }
            }
            for (i in 1..9) {
                if (allCards.isNotEmpty()) {
                    bot.drawToHand(allCards.removeAt(0))
                }
            }
            deck.clear()
            allCards.forEach{ deck.getCards().add(it)}

        } else if (startMode == "DEBUG_DEAL") {
            val trumpSuit = trumpCard!!.suit
            val otherSuit = Suit.values().first { it != trumpSuit }
            val playerHandCards = deck.getCards().filter { it.suit == trumpSuit || it.suit == otherSuit }
            val botHandCards = deck.getCards().filter { it.suit != trumpSuit && it.suit != otherSuit }

            playerHandCards.forEach { player.drawToHand(it) }
            botHandCards.forEach { bot.drawToHand(it) }
            deck.clear()

        } else {
            for (i in 1..9) {
                deck.drawCard()?.let { player.drawToHand(it) }
                deck.drawCard()?.let { bot.drawToHand(it) }
            }
        }

        isGameRunning = true
        if (isFirstGame) {
            isPlayerTurnToLead = when (startMode) {
                "DEBUG_DEAL" -> true
                else -> true
            }
        } else {
            isPlayerTurnToLead = true
        }

        Log.d("GameEngine", "--- Jogo Iniciado ---")
        Log.d("GameEngine", "TRUNFO: $trumpCard")
        Log.d("GameEngine", "Mão do Jogador: ${player.getHand()}")
        Log.d("GameEngine", "Mão do Bot: ${bot.getHand()}")
        Log.d("GameEngine", "Cartas no baralho: ${deck.cardsRemaining()}")
    }

    var playerCardOnTable: Card? = null
    var botCardOnTable: Card? = null

    fun playerLeadsTrick(cardIndex: Int) {
        if (!isPlayerTurnToLead) return

        val playerCard = player.playCard(cardIndex)
        playerCardOnTable = playerCard
        Log.d("GameEngine", "O jogador (Humano) LIDEROU com: $playerCard")

        val botCard = findBestCardForBot(playerCard)
        val botCardIndex = bot.getHand().indexOf(botCard)
        botCardOnTable = bot.playCard(botCardIndex)
        Log.d("GameEngine", "O Bot (IA) RESPONDEU com: $botCardOnTable")
    }

    fun botLeadsTrick() {
        if (isPlayerTurnToLead) return

        val hand = bot.getHand()
        val cardToPlay = hand.minWithOrNull(compareBy({ it.rank.points }, { it.rank.strength }))!!
        val cardIndex = hand.indexOf(cardToPlay)

        botCardOnTable = bot.playCard(cardIndex)
        Log.d("GameEngine", "Bot (IA) LIDEROU com: $botCardOnTable")
    }

    private fun findBestCardForBot(playerCard: Card): Card {
        val botHand = bot.getHand()
        val leadingSuit = playerCard.suit

        val mustFollowSuit = deck.cardsRemaining() == 0
        val availableCards: List<Card>

        if (mustFollowSuit) {
            val cardsOfLeadingSuit = botHand.filter { it.suit == leadingSuit }
            if (cardsOfLeadingSuit.isNotEmpty()) {
                availableCards = cardsOfLeadingSuit
                Log.d("GameEngine", "Bot tem de assistir com o naipe $leadingSuit. Cartas disponíveis: $availableCards")
            } else {
                availableCards = botHand
                Log.d("GameEngine", "Bot não tem o naipe $leadingSuit para assistir. Joga qualquer carta.")
            }
        } else {
            availableCards = botHand
        }

        val winningTrumps = availableCards.filter {
            it.isTrump && (!playerCard.isTrump || it.rank.strength > playerCard.rank.strength)
        }
        if (winningTrumps.isNotEmpty()) {
            return winningTrumps.minByOrNull { it.rank.strength }!!
        }

        val winningSameSuit = availableCards.filter {
            !it.isTrump && it.suit == playerCard.suit && it.rank.strength > playerCard.rank.strength
        }
        if (winningSameSuit.isNotEmpty()) {
            return winningSameSuit.maxByOrNull { it.rank.strength }!!
        }

        val nonTrumps = availableCards.filter { !it.isTrump }
        if (nonTrumps.isNotEmpty()) {
            return nonTrumps.minWithOrNull(compareBy({ it.rank.points }, { it.rank.strength }))!!
        }

        return availableCards.minByOrNull { it.rank.strength }!!
    }

    private fun isMoveValid(cardPlayed: Card, leadingCard: Card, playerHand: List<Card>): Boolean {
        if (deck.cardsRemaining() > 0) {
            return true
        }

        val leadingSuit = leadingCard.suit
        val playerHasSuit = playerHand.any { it.suit == leadingSuit }

        if (!playerHasSuit) {
            return true
        }

        return cardPlayed.suit == leadingSuit
    }

    fun playerResponds(playerCardIndex: Int): Boolean {
        if (isPlayerTurnToLead) return false

        val playerCard = player.getHand()[playerCardIndex]
        val leadingCard = botCardOnTable!!

        if (!isMoveValid(playerCard, leadingCard, player.getHand())) {
            Log.w("GameEngine", "Jogada inválida! O jogador deve assistir (jogar o mesmo naipe).")
            return false
        }

        player.playCard(playerCardIndex)
        playerCardOnTable = playerCard
        Log.d("GameEngine", "Jogador (Humano) RESPONDEU com: $playerCard")
        return true
    }

    fun resolveCurrentTrick() {
        if (playerCardOnTable == null || botCardOnTable == null) {
            Log.e("GameEngine", "Erro: Tentou resolver a vaza mas as cartas não estão na mesa.")
            return
        }

        val playerCard = playerCardOnTable!!
        val botCard = botCardOnTable!!

        val winningCard: Card
        if (playerCard.isTrump && !botCard.isTrump) {
            winningCard = playerCard
        } else if (!playerCard.isTrump && botCard.isTrump) {
            winningCard = botCard
        } else if (playerCard.isTrump && botCard.isTrump) {
            winningCard = if (playerCard.rank.strength > botCard.rank.strength) playerCard else botCard
        } else if (playerCard.suit == botCard.suit) {
            winningCard = if (playerCard.rank.strength > botCard.rank.strength) playerCard else botCard
        } else {
            winningCard = if (isPlayerTurnToLead) playerCard else botCard
        }

        val winner: Player
        val loser: Player

        if (winningCard == playerCard) {
            winner = player
            loser = bot
            isPlayerTurnToLead = true
        } else {
            winner = bot
            loser = player
            isPlayerTurnToLead = false
        }

        winner.addWonCards(listOf(playerCard, botCard))

        playerPoints = player.calculatePoints()
        botPoints = bot.calculatePoints()
        Log.d("GameEngine", "Vaza ganha por ${winner.name}. Pontos: P $playerPoints - B $botPoints")

        if (deck.cardsRemaining() > 0) {
            deck.drawCard()?.let { winner.drawToHand(it) }
        }
        if (deck.cardsRemaining() > 0) {
            deck.drawCard()?.let { loser.drawToHand(it) }
        }

        if (deck.cardsRemaining() == 0) {
            Log.d("GameEngine", "Baralho vazio! As regras de 'assistir' estão agora ativas.")
        }

        playerCardOnTable = null
        botCardOnTable = null

        if (player.isHandEmpty() && deck.cardsRemaining() == 0) {
            isGameRunning = false
            Log.d("GameEngine", "--- FIM DE JOGO ---")
            Log.d("GameEngine", "Resultado Final: Jogador $playerPoints - Bot $botPoints")

            if (playerPoints > botPoints) {
                gameResult = GameResult.PLAYER_WINS
                Log.d("GameEngine", "Vencedor: Jogador!")
                when {
                    playerPoints == 120 -> {
                        playerGamesWon = 4
                        Log.d("GameEngine", "CAPOTE! Jogador vence a partida.")
                    }
                    playerPoints > 90 -> {
                        playerGamesWon += 2
                        Log.d("GameEngine", "Boa vitória! Jogador ganha 2 jogos.")
                    }
                    else -> {
                        playerGamesWon++
                    }
                }
            } else if (botPoints > playerPoints) {
                gameResult = GameResult.BOT_WINS
                Log.d("GameEngine", "Vencedor: Bot!")
                when {
                    botPoints == 120 -> {
                        botGamesWon = 4
                        Log.d("GameEngine", "CAPOTE! Bot vence a partida.")
                    }
                    botPoints > 90 -> {
                        botGamesWon += 2
                        Log.d("GameEngine", "Boa vitória! Bot ganha 2 jogos.")
                    }
                    else -> {
                        botGamesWon++
                    }
                }
            } else {
                gameResult = GameResult.DRAW
                Log.d("GameEngine", "Empate!")
            }
        }
    }

    fun getPlayerHand(): List<Card> {
        return player.getHand()
    }

    fun isPlayerTurn(): Boolean = isPlayerTurnToLead

    fun getBotHandSize(): Int {
        return bot.getHand().size
    }

    fun getCardsRemaining(): Int {
        return if (::deck.isInitialized) deck.cardsRemaining() else 0
    }
}
