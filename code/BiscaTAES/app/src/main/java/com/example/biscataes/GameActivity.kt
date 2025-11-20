package com.example.biscataes

import android.content.Intent
import android.graphics.Color // <-- IMPORTAR CORES
import android.graphics.drawable.GradientDrawable // <-- Importar GradientDrawable para design melhor
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity // <-- IMPORTAR GRAVIDADE (para centrar o texto)
import android.view.View // <-- IMPORTAR VIEW para VISIBLE/INVISIBLE
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import android.widget.Toast


class GameActivity : AppCompatActivity() {

    private lateinit var gameEngine: GameEngine
    private var isUiLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        gameEngine = GameEngine()

        // Chamar a nossa função de desenhar (agora atualizada)
        drawPlayerHand()
        drawBotHand() // <-- Desenhar a mão do Bot
        displayTrumpCard()
        updateScoreboardView()
        updateDeckView() // <-- Atualiza o baralho
    }

    //
    // --- FUNÇÃO drawPlayerHand ATUALIZADA ---
    //
    private fun drawPlayerHand() {
        val hand = gameEngine.getPlayerHand()
        val handLayout = findViewById<LinearLayout>(R.id.playerHandLayout)

        handLayout.removeAllViews()

        for ((index, card) in hand.withIndex()) {
            val cardTextView = TextView(this)

            // --- MUDANÇA AQUI ---
            // 1. Obter os símbolos
            val rankSymbol = getRankSymbol(card.rank) // "A", "K", "7", "2"
            val suitSymbol = getSuitSymbol(card.suit) // "♥", "♦", "♣", "♠"

            // 2. Definir o texto formatado
            cardTextView.text = "$rankSymbol\n$suitSymbol" // "A" em cima, "♥" em baixo

            // --- Melhorar o Estilo ---
            cardTextView.textSize = 20f // Texto maior
            cardTextView.gravity = Gravity.CENTER // Centrar o texto (vertical e horizontal)
            cardTextView.setPadding(16, 16, 16, 16)

            // Definir o tamanho da "carta"
            val cardWidth = 120 // Largura em pixels
            val cardHeight = 180 // Altura em pixels
            val params = LinearLayout.LayoutParams(cardWidth, cardHeight)
            params.setMargins(8, 0, 8, 0)
            cardTextView.layoutParams = params

            // Design das cartas do Jogador (Frente)
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(Color.WHITE)
            shape.setStroke(2, Color.BLACK)
            shape.cornerRadius = 15f // Cantos arredondados
            cardTextView.background = shape

            // 3. Definir a cor do texto (Vermelho ou Preto)
            if (card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS) {
                cardTextView.setTextColor(Color.RED)
            } else {
                cardTextView.setTextColor(Color.BLACK)
            }
            // --- Fim do estilo ---

            // --- Listener de clique (igual a antes) ---

            cardTextView.setOnClickListener {
                // Se a UI estiver bloqueada (à espera do delay), não faz nada
                if (isUiLocked) return@setOnClickListener

                // 1. Bloquear a UI para evitar cliques duplos
                isUiLocked = true

                Log.d("GameActivity", "Clicou na carta: $card (índice: $index)")

                // 2. Avisar o motor
                if (gameEngine.isPlayerTurn()) {
                    // --- O JOGADOR LIDERA A VAZA ---
                    gameEngine.playerLeadsTrick(index)
                    updateTableView() // Mostra AMBAS as cartas na mesa

                    // Redesenhar a mão e resolver a vaza
                    drawPlayerHand()
                    drawBotHand() // Atualizar mão do bot (jogou carta)
                    startTrickResolutionDelay()
                } else {
                    // --- O JOGADOR RESPONDE AO BOT ---
                    val moveWasValid = gameEngine.playerResponds(index)
                    if (moveWasValid) {
                        updateTableView() // Mostra a carta do jogador
                        drawPlayerHand()
                        drawBotHand() // Atualizar mão do bot (sincronizar)
                        startTrickResolutionDelay()
                    } else {
                        // Jogada inválida!
                        Toast.makeText(this, "Jogada inválida! Deve assistir (jogar o mesmo naipe).", Toast.LENGTH_SHORT).show()
                        isUiLocked = false // Desbloquear a UI para o jogador tentar de novo
                    }
                }
            }


            handLayout.addView(cardTextView)


        }
    }

    // --- NOVA FUNÇÃO: Desenhar a mão do Bot (Costas da carta) ---
    private fun drawBotHand() {
        val handSize = gameEngine.getBotHandSize()
        val handLayout = findViewById<LinearLayout>(R.id.botHandLayout)

        handLayout.removeAllViews()

        for (i in 0 until handSize) {
            val cardView = TextView(this)

            // Estilo "Verso da Carta" - USANDO IMAGEM
            cardView.setBackgroundResource(R.drawable.back_card_red) // <-- MUDANÇA AQUI PARA USAR A IMAGEM .PNG
            cardView.text = "" // Sem texto

            // Definir o tamanho da "carta" (EXATAMENTE igual ao do player)
            val cardWidth = 120
            val cardHeight = 180
            val params = LinearLayout.LayoutParams(cardWidth, cardHeight)
            params.setMargins(8, 0, 8, 0)
            cardView.layoutParams = params

            handLayout.addView(cardView)
        }
    }

    //
    // --- FUNÇÕES AJUDANTES (NOVAS) ---
    //
    // --- NOVA FUNÇÃO ---
    // Atualiza as cartas na "mesa"
    private fun updateTableView() {
        // Encontrar os TextViews da mesa
        val playerCardView = findViewById<TextView>(R.id.playerPlayedCardView)
        val botCardView = findViewById<TextView>(R.id.botPlayedCardView)

        // Apanhar as cartas da mesa a partir do motor
        val playerCard = gameEngine.playerCardOnTable
        val botCard = gameEngine.botCardOnTable

        // Helper para criar o shape das cartas jogadas
        fun setupPlayedCardStyle(view: TextView) {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(Color.WHITE)
            shape.setStroke(2, Color.BLACK)
            shape.cornerRadius = 15f
            view.background = shape
        }

        // Atualizar a carta do jogador
        if (playerCard != null) {
            playerCardView.text = "${getRankSymbol(playerCard.rank)}\n${getSuitSymbol(playerCard.suit)}"
            setupPlayedCardStyle(playerCardView)
            if (playerCard.suit == Suit.HEARTS || playerCard.suit == Suit.DIAMONDS) {
                playerCardView.setTextColor(Color.RED)
            } else {
                playerCardView.setTextColor(Color.BLACK)
            }
            playerCardView.visibility = View.VISIBLE // Tornar visível
        } else {
            playerCardView.visibility = View.INVISIBLE // Esconder
        }

        // Atualizar a carta do Bot
        if (botCard != null) {
            botCardView.text = "${getRankSymbol(botCard.rank)}\n${getSuitSymbol(botCard.suit)}"
            setupPlayedCardStyle(botCardView)
            if (botCard.suit == Suit.HEARTS || botCard.suit == Suit.DIAMONDS) {
                botCardView.setTextColor(Color.RED)
            } else {
                botCardView.setTextColor(Color.BLACK)
            }
            botCardView.visibility = View.VISIBLE // Tornar visível
        } else {
            botCardView.visibility = View.INVISIBLE // Esconder
        }
    }
    // --- NOVA FUNÇÃO ---
// Mostra a carta de trunfo no ecrã
    private fun displayTrumpCard() {
        // 1. Ir buscar a carta de trunfo ao motor
        val trumpCard = gameEngine.trumpCard

        // 2. Encontrar o TextView no layout
        val trumpView = findViewById<TextView>(R.id.trumpCardView)

        if (trumpCard != null) {
            // 3. Usar as funções helper que já temos
            trumpView.text = "${getRankSymbol(trumpCard.rank)}\n${getSuitSymbol(trumpCard.suit)}"

            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(Color.WHITE)
            shape.setStroke(2, Color.BLACK)
            shape.cornerRadius = 15f
            trumpView.background = shape

            // 4. Definir a cor
            if (trumpCard.suit == Suit.HEARTS || trumpCard.suit == Suit.DIAMONDS) {
                trumpView.setTextColor(Color.RED)
            } else {
                trumpView.setTextColor(Color.BLACK)
            }
            // GARANTIR QUE ESTÁ VISÍVEL
            trumpView.visibility = View.VISIBLE
        } else {
            // Segurança: se por algum motivo não houver trunfo, esconde
            trumpView.visibility = View.GONE
        }
    }

    private fun updateScoreboardView() {
        // 1. Encontrar os TextViews
        val playerScoreView = findViewById<TextView>(R.id.playerScoreView)
        val botScoreView = findViewById<TextView>(R.id.botScoreView)

        // 2. Ir buscar os pontos ao motor de jogo
        val playerPoints = gameEngine.playerPoints
        val botPoints = gameEngine.botPoints

        // 3. Atualizar o texto
        playerScoreView.text = "Tu: $playerPoints"
        botScoreView.text = "Bot: $botPoints"
    }

    // --- NOVA FUNÇÃO: Atualiza o Deck ---
    private fun updateDeckView() {
        val deckView = findViewById<TextView>(R.id.deckView)
        val remaining = gameEngine.getCardsRemaining()

        if (remaining > 0) {
            deckView.text = remaining.toString()
            deckView.visibility = View.VISIBLE
        } else {
            deckView.visibility = View.INVISIBLE
        }
    }

    // Converte um Rank num símbolo de texto
    private fun getRankSymbol(rank: Rank): String {
        return when (rank) {
            Rank.ACE -> "A"
            Rank.KING -> "K"
            Rank.JACK -> "J"
            Rank.QUEEN -> "Q"
            Rank.SEVEN -> "7"
            Rank.SIX -> "6"
            Rank.FIVE -> "5"
            Rank.FOUR -> "4"
            Rank.THREE -> "3"
            Rank.TWO -> "2"
        }
    }

    // Converte um Suit num emoji/símbolo
    private fun getSuitSymbol(suit: Suit): String {
        return when (suit) {
            Suit.HEARTS -> "♥"
            Suit.DIAMONDS -> "♦"
            Suit.CLUBS -> "♣"
            Suit.SPADES -> "♠"
        }
    }

    // --- NOVA FUNÇÃO (extraída) ---
    private fun startTrickResolutionDelay() {
        // Inicia o delay para resolver a vaza
        Handler(Looper.getMainLooper()).postDelayed({

            gameEngine.resolveCurrentTrick() // Resolve a vaza

            // Atualiza toda a UI
            updateTableView()      // Limpa a mesa
            drawPlayerHand()       // Mostra a mão com a nova carta
            drawBotHand()          // <-- Atualiza mão do Bot (compra de cartas)
            updateScoreboardView() // Atualiza os pontos
            updateDeckView()       // <-- Atualiza o deck (cartas compradas)

            // --- A GRANDE MUDANÇA ---
            // Verifica se o Bot joga a seguir
            checkIfBotPlaysFirst()

        }, 1500) // 1.5s de delay
    }

    // --- NOVA FUNÇÃO ---
    // Verifica se é a vez do Bot jogar primeiro
    private fun checkIfBotPlaysFirst() {
        // Se o jogo acabou, não faz nada
        if (!gameEngine.isGameRunning) {
            isUiLocked = false
            showEndGameDialog()
            return
        }

        if (!gameEngine.isPlayerTurn()) {
            // É a vez do Bot liderar!
            Log.d("GameActivity", "É a vez do Bot liderar a vaza.")
            isUiLocked = true // Manter a UI bloqueada

            // Simular o "pensamento" do Bot
            Handler(Looper.getMainLooper()).postDelayed({
                gameEngine.botLeadsTrick() // Bot joga a sua carta
                updateTableView()          // Mostrar a carta do Bot na mesa
                drawBotHand()              // <-- Atualiza mão do Bot
                isUiLocked = false         // Desbloquear a UI para o jogador responder
            }, 1000) // Bot "pensa" por 1 segundo

        } else {
            // É a vez do jogador, desbloqueia a UI
            isUiLocked = false
        }
    }

    private fun showEndGameDialog() {
        val result = gameEngine.gameResult
        val title: String
        val message: String

        when (result) {
            GameEngine.GameResult.PLAYER_WINS -> {
                title = "Vitória!"
                message = "Parabéns, você ganhou o jogo!"
            }
            GameEngine.GameResult.BOT_WINS -> {
                title = "Derrota"
                message = "O Bot ganhou desta vez. Tente novamente!"
            }
            GameEngine.GameResult.DRAW -> {
                title = "Empate"
                message = "O jogo terminou em empate."
            }
            else -> return // Não faz nada se o resultado for indefinido
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)

        builder.setPositiveButton("Jogar Novamente") { _, _ ->
            gameEngine.startNewGame()
            drawPlayerHand()
            drawBotHand() // <-- Reiniciar mão do bot
            displayTrumpCard()
            updateScoreboardView()
            updateTableView() // Limpa as cartas da mesa da jogada anterior
            updateDeckView()  // <-- Atualiza o deck
        }

        builder.setNegativeButton("Voltar ao Menu") { _, _ ->
            finish() // Fecha a GameActivity
        }

        val dialog = builder.create()
        dialog.show()
    }
}
