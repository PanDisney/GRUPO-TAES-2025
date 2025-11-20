package com.example.biscataes

import android.util.Log // Vamos usar isto para "imprimir" o estado do jogo

class GameEngine {

    enum class GameResult{
        PLAYER_WINS,
        BOT_WINS,
        DRAW,
        UNDEFINED
    }

    // --- 1. Propriedades do Jogo ---
    var gameResult: GameResult = GameResult.UNDEFINED
        private set
    private lateinit var player: Player
    private lateinit var bot: Player
    private lateinit var deck: Deck

    // true = Humano joga primeiro, false = Bot joga primeiro
    private var isPlayerTurnToLead = true
    // A carta de trunfo que fica virada para cima
    var trumpCard: Card? = null
        private set

    // Estado do jogo
    var isGameRunning = false
        private set

    var playerPoints = 0
        private set
    var botPoints = 0
        private set

    // --- US-10: Propriedades da Partida ---
    var playerGamesWon = 0
        private set
    var botGamesWon = 0
        private set
    // --- Fim US-10 ---

    // --- 2. Função de Setup (Início) ---
    init {
        // Esta função é chamada assim que o GameEngine é criado
        Log.d("GameEngine", "Motor de Jogo Criado. A iniciar nova partida...")
        startNewMatch()
    }

    // --- US-10: Iniciar uma nova partida (zera tudo) ---
    fun startNewMatch() {
        playerGamesWon = 0
        botGamesWon = 0
        startNewGame()
    }


    // Função para preparar um novo jogo (ronda)
    fun startNewGame() {
        gameResult = GameResult.UNDEFINED
        playerPoints = 0 // Zerar pontos do jogo
        botPoints = 0

        // Criar os participantes
        player = Player("Humano", isBot = false)
        bot = Player("Bot", isBot = true)

        // Criar e preparar o baralho
        deck = Deck()
        deck.shuffle()
        trumpCard = deck.setTrump() // Define o trunfo

        // Verificar se o trunfo foi definido corretamente
        if (trumpCard == null) {
            Log.e("GameEngine", "Erro: O baralho está vazio, não foi possível definir trunfo.")
            return
        }

        // Distribuir as 9 cartas iniciais
        for (i in 1..9) {
            deck.drawCard()?.let { player.drawToHand(it) }
            deck.drawCard()?.let { bot.drawToHand(it) }
        }

        isGameRunning = true
        isPlayerTurnToLead = true // Jogador começa sempre o primeiro jogo da partida

        // --- Debug: Imprimir o estado inicial ---
        Log.d("GameEngine", "--- Jogo Iniciado ---")
        Log.d("GameEngine", "TRUNFO: $trumpCard")
        Log.d("GameEngine", "Mão do Jogador: ${player.getHand()}")
        Log.d("GameEngine", "Mão do Bot: ${bot.getHand()}")
        Log.d("GameEngine", "Cartas no baralho: ${deck.cardsRemaining()}")
    }

    // --- 3. Funções de Ação (para mais tarde) ---
    var playerCardOnTable: Card? = null
    var botCardOnTable: Card? = null

    // Esta função será chamada quando o jogador humano jogar uma carta
    fun playerLeadsTrick(cardIndex: Int) {
        if (!isPlayerTurnToLead) return // Segurança

        // 1. O jogador joga a carta
        val playerCard = player.playCard(cardIndex)
        playerCardOnTable = playerCard
        Log.d("GameEngine", "O jogador (Humano) LIDEROU com: $playerCard")

        // 2. O Bot reage (usamos a lógica antiga)
        val botCard = findBestCardForBot(playerCard)
        val botCardIndex = bot.getHand().indexOf(botCard)
        botCardOnTable = bot.playCard(botCardIndex)
        Log.d("GameEngine", "O Bot (IA) RESPONDEU com: $botCardOnTable")
    }

    // --- NOVA FUNÇÃO ---
    // O Bot escolhe uma carta para liderar a vaza
    fun botLeadsTrick() {
        if (isPlayerTurnToLead) return // Segurança

        // Lógica simples: jogar a carta de menor valor (pontos, depois força)
        val hand = bot.getHand()
        val cardToPlay = hand.minWithOrNull(compareBy({ it.rank.points }, { it.rank.strength }))!!
        val cardIndex = hand.indexOf(cardToPlay)

        botCardOnTable = bot.playCard(cardIndex) // Remove da mão, põe na mesa
        Log.d("GameEngine", "Bot (IA) LIDEROU com: $botCardOnTable")
    }
    // --- LÓGICA DO BOT ---
    // Esta função decide qual a melhor carta para o Bot jogar
    private fun findBestCardForBot(playerCard: Card): Card {
        val botHand = bot.getHand()
        val leadingSuit = playerCard.suit

        // --- NOVA LÓGICA DA REGRA DE ASSISTIR ---
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
        // --- FIM DA NOVA LÓGICA ---

        // 1. Tentar ganhar com Trunfos (a partir das cartas disponíveis)
        val winningTrumps = availableCards.filter {
            it.isTrump && (!playerCard.isTrump || it.rank.strength > playerCard.rank.strength)
        }
        if (winningTrumps.isNotEmpty()) {
            // Joga o trunfo mais baixo que ganha
            return winningTrumps.minByOrNull { it.rank.strength }!!
        }

        // 2. Tentar ganhar com o mesmo naipe (a partir das cartas disponíveis)
        val winningSameSuit = availableCards.filter {
            !it.isTrump && it.suit == playerCard.suit && it.rank.strength > playerCard.rank.strength
        }
        if (winningSameSuit.isNotEmpty()) {
            // Joga a carta mais alta (para garantir) do mesmo naipe
            return winningSameSuit.maxByOrNull { it.rank.strength }!!
        }

        // 3. Não pode ganhar. Tentar jogar a carta mais baixa que não seja trunfo (das disponíveis)
        val nonTrumps = availableCards.filter { !it.isTrump }
        if (nonTrumps.isNotEmpty()) {
            // Joga a carta de menor valor (pontos primeiro, depois força)
            return nonTrumps.minWithOrNull(compareBy({ it.rank.points }, { it.rank.strength }))!!
        }

        // 4. Se só tiver trunfos (ou se for forçado a jogar de um naipe que só tem trunfos), joga a mais baixa
        return availableCards.minByOrNull { it.rank.strength }!!
    }

    private fun isMoveValid(cardPlayed: Card, leadingCard: Card, playerHand: List<Card>): Boolean {
        // A regra de "assistir" só se aplica quando não há mais cartas para comprar
        if (deck.cardsRemaining() > 0) {
            return true
        }

        val leadingSuit = leadingCard.suit
        val playerHasSuit = playerHand.any { it.suit == leadingSuit }

        // Se o jogador não tem o naipe, pode jogar qualquer carta
        if (!playerHasSuit) {
            return true
        }

        // Se o jogador tem o naipe, a carta jogada tem de ser desse naipe
        return cardPlayed.suit == leadingSuit
    }
    fun playerResponds(playerCardIndex: Int): Boolean {
        if (isPlayerTurnToLead) return false // Segurança

        val playerCard = player.getHand()[playerCardIndex]
        val leadingCard = botCardOnTable!!

        if (!isMoveValid(playerCard, leadingCard, player.getHand())) {
            Log.w("GameEngine", "Jogada inválida! O jogador deve assistir (jogar o mesmo naipe).")
            return false // Indicate invalid move
        }

        player.playCard(playerCardIndex)
        playerCardOnTable = playerCard
        Log.d("GameEngine", "Jogador (Humano) RESPONDEU com: $playerCard")
        return true
    }
    // --- NOVA FUNÇÃO ---
    // Chamada depois de ambas as cartas estarem na mesa
    fun resolveCurrentTrick() {
        if (playerCardOnTable == null || botCardOnTable == null) {
            Log.e("GameEngine", "Erro: Tentou resolver a vaza mas as cartas não estão na mesa.")
            return
        }

        val playerCard = playerCardOnTable!!
        val botCard = botCardOnTable!!

        // --- 1. Lógica de Quem Ganhou ---
        val winningCard: Card
        if (playerCard.isTrump && !botCard.isTrump) {
            winningCard = playerCard
        } else if (!playerCard.isTrump && botCard.isTrump) {
            winningCard = botCard
        } else if (playerCard.isTrump && botCard.isTrump) {
            // Ambas são trunfo, ganha a mais forte
            winningCard = if (playerCard.rank.strength > botCard.rank.strength) playerCard else botCard
        } else if (playerCard.suit == botCard.suit) {
            // Mesmo naipe (sem trunfo), ganha a mais forte
            winningCard = if (playerCard.rank.strength > botCard.rank.strength) playerCard else botCard
        } else {
            // Naipes diferentes (sem trunfo), ganha quem jogou primeiro
            winningCard = if (isPlayerTurnToLead) playerCard else botCard
        }

        // --- 2. Dar Cartas ao Vencedor ---
        val winner: Player
        val loser: Player

        if (winningCard == playerCard) {
            winner = player
            loser = bot
            isPlayerTurnToLead = true // Vencedor joga primeiro na próxima
        } else {
            winner = bot
            loser = player
            isPlayerTurnToLead = false // Vencedor joga primeiro na próxima
        }

        winner.addWonCards(listOf(playerCard, botCard))

        // Atualizar contagem de pontos
        playerPoints = player.calculatePoints()
        botPoints = bot.calculatePoints()
        Log.d("GameEngine", "Vaza ganha por ${winner.name}. Pontos: P $playerPoints - B $botPoints")

        // --- 3. "Biscar" (Comprar Cartas) ---
        // O vencedor compra primeiro, só se houver cartas no baralho
        if (deck.cardsRemaining() > 0) {
            deck.drawCard()?.let { winner.drawToHand(it) }
        }
        if (deck.cardsRemaining() > 0) {
            deck.drawCard()?.let { loser.drawToHand(it) }
        }

        // --- 4. Verificar se existem cartas no deck ---
        if (deck.cardsRemaining() == 0) {
            Log.d("GameEngine", "Baralho vazio! As regras de 'assistir' estão agora ativas.")
        }

        // --- 5. Limpar a Mesa ---
        playerCardOnTable = null
        botCardOnTable = null

        // --- 6. Verificar Fim de Jogo ---
        if (player.isHandEmpty() && deck.cardsRemaining() == 0) {
            isGameRunning = false
            Log.d("GameEngine", "--- FIM DE JOGO ---")
            Log.d("GameEngine", "Resultado Final: Jogador $playerPoints - Bot $botPoints")

            // Determina quem é o vencedor
            if (playerPoints > botPoints) {
                gameResult = GameResult.PLAYER_WINS
                Log.d("GameEngine", "Vencedor: Jogador!")
                // US-12: Lógica de Capote
                when {
                    playerPoints == 120 -> {
                        playerGamesWon = 4 // Vence a partida inteira
                        Log.d("GameEngine", "CAPOTE! Jogador vence a partida.")
                    }
                    playerPoints > 90 -> { // 91 a 119
                        playerGamesWon += 2 // Ganha 2 jogos
                        Log.d("GameEngine", "Boa vitória! Jogador ganha 2 jogos.")
                    }
                    else -> {
                        playerGamesWon++ // Vitória normal
                    }
                }
            } else if (botPoints > playerPoints) {
                gameResult = GameResult.BOT_WINS
                Log.d("GameEngine", "Vencedor: Bot!")
                // US-12: Lógica de Capote para o Bot
                when {
                    botPoints == 120 -> {
                        botGamesWon = 4 // Vence a partida inteira
                        Log.d("GameEngine", "CAPOTE! Bot vence a partida.")
                    }
                    botPoints > 90 -> { // 91 a 119
                        botGamesWon += 2 // Ganha 2 jogos
                        Log.d("GameEngine", "Boa vitória! Bot ganha 2 jogos.")
                    }
                    else -> {
                        botGamesWon++ // Vitória normal
                    }
                }
            } else { // Empate (60-60)
                gameResult = GameResult.DRAW
                Log.d("GameEngine", "Empate!")
                // Ninguém ganha jogos num empate
            }
        }
    }
    // Função para obter a mão do jogador (para a UI)
    fun getPlayerHand(): List<Card> {
        return player.getHand()
    }
    // Adicione esta função para a Activity poder perguntar
    fun isPlayerTurn(): Boolean = isPlayerTurnToLead

    // Função para obter o tamanho da mão do Bot (para desenhar cartas viradas)
    fun getBotHandSize(): Int {
        return bot.getHand().size
    }

    // Função para saber quantas cartas restam (para a UI)
    fun getCardsRemaining(): Int {
        return if (::deck.isInitialized) deck.cardsRemaining() else 0
    }
}