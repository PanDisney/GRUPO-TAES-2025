package com.example.biscataes

import android.util.Log // Vamos usar isto para "imprimir" o estado do jogo

class GameEngine {

    // --- 1. Propriedades do Jogo ---
    private lateinit var player: Player
    private lateinit var bot: Player
    private lateinit var deck: Deck

    // A carta de trunfo que fica virada para cima
    var trumpCard: Card? = null
        private set

    // Estado do jogo
    var isGameRunning = false
        private set

    // --- 2. Função de Setup (Início) ---
    init {
        // Esta função é chamada assim que o GameEngine é criado
        Log.d("GameEngine", "Motor de Jogo Criado. A iniciar novo jogo...")
        startNewGame()
    }

    // Função para preparar um novo jogo
    fun startNewGame() {
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

        // --- Debug: Imprimir o estado inicial ---
        Log.d("GameEngine", "--- Jogo Iniciado ---")
        Log.d("GameEngine", "TRUNFO: $trumpCard")
        Log.d("GameEngine", "Mão do Jogador: ${player.getHand()}")
        Log.d("GameEngine", "Mão do Bot: ${bot.getHand()}")
        Log.d("GameEngine", "Cartas no baralho: ${deck.cardsRemaining()}")
    }

    // --- 3. Funções de Ação (para mais tarde) ---

    // Esta função será chamada quando o jogador humano jogar uma carta
    fun playerPlaysCard(cardIndex: Int) {
        if (!isGameRunning || cardIndex < 0 || cardIndex >= player.getHand().size) {
            Log.w("GameEngine", "Tentativa de jogar carta inválida (índice: $cardIndex)")
            return
        }

        // Lógica para jogar a carta...
        // Lógica para o Bot responder...
        // Lógica para ver quem ganhou a vaza...
        // Lógica para comprar cartas do baralho...
        // Lógica para verificar o fim do jogo...

        val playedCard = player.playCard(cardIndex)
        Log.d("GameEngine", "O jogador (Humano) jogou: $playedCard")

        // --- PRÓXIMOS PASSOS (Ainda não implementados) ---
        // 2. Lógica para o Bot responder...
        // 3. Lógica para ver quem ganhou a vaza...
        // 4. Lógica para comprar cartas do baralho...
        // 5. Lógica para verificar o fim do jogo...

    }

    // Função para obter a mão do jogador (para a UI)
    fun getPlayerHand(): List<Card> {
        return player.getHand()
    }

}