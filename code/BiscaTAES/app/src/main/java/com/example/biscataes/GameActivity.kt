package com.example.biscataes

import android.content.Intent
import android.graphics.Color // <-- IMPORTAR CORES
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity // <-- IMPORTAR GRAVIDADE (para centrar o texto)
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.os.Handler
import android.os.Looper


class GameActivity : AppCompatActivity() {

    private lateinit var gameEngine: GameEngine
    private var isUiLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameEngine = GameEngine()

        // Chamar a nossa função de desenhar (agora atualizada)
        drawPlayerHand()
        displayTrumpCard()
        updateScoreboardView()
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

            cardTextView.setBackgroundColor(0xFFFFFFFF.toInt()) // Fundo branco

            // 3. Definir a cor (Vermelho ou Preto)
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

                // 2. Avisar o motor (que ativa o Bot)
                if (gameEngine.isPlayerTurn()) {
                    // --- O JOGADOR LIDERA A VAZA ---
                    gameEngine.playerLeadsTrick(index)
                    updateTableView() // Mostra AMBAS as cartas na mesa
                } else {
                    // --- O JOGADOR RESPONDE AO BOT ---
                    gameEngine.playerResponds(index)
                    updateTableView() // Mostra a carta do jogador (a do bot já lá está)
                }

                // 3. Redesenhar a mão (para a carta jogada desaparecer)
                drawPlayerHand()

                // 4. Redesenhar a mesa (para mostrar as cartas jogadas)
                startTrickResolutionDelay()

                // 5. --- Atraso (Delay) de 1.5 segundos ---

            }


            handLayout.addView(cardTextView)


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

        // Atualizar a carta do jogador
        if (playerCard != null) {
            playerCardView.text = "${getRankSymbol(playerCard.rank)}\n${getSuitSymbol(playerCard.suit)}"
            if (playerCard.suit == Suit.HEARTS || playerCard.suit == Suit.DIAMONDS) {
                playerCardView.setTextColor(Color.RED)
            } else {
                playerCardView.setTextColor(Color.BLACK)
            }
            playerCardView.visibility = TextView.VISIBLE // Tornar visível
        } else {
            playerCardView.visibility = TextView.INVISIBLE // Esconder
        }

        // Atualizar a carta do Bot
        if (botCard != null) {
            botCardView.text = "${getRankSymbol(botCard.rank)}\n${getSuitSymbol(botCard.suit)}"
            if (botCard.suit == Suit.HEARTS || botCard.suit == Suit.DIAMONDS) {
                botCardView.setTextColor(Color.RED)
            } else {
                botCardView.setTextColor(Color.BLACK)
            }
            botCardView.visibility = TextView.VISIBLE // Tornar visível
        } else {
            botCardView.visibility = TextView.INVISIBLE // Esconder
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

            // 4. Definir a cor
            if (trumpCard.suit == Suit.HEARTS || trumpCard.suit == Suit.DIAMONDS) {
                trumpView.setTextColor(Color.RED)
            } else {
                trumpView.setTextColor(Color.BLACK)
            }
        } else {
            // Segurança: se por algum motivo não houver trunfo, esconde
            trumpView.visibility = TextView.GONE
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
            updateScoreboardView() // Atualiza os pontos

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
            // (Aqui podemos mostrar o ecrã de "Fim de Jogo")
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
                isUiLocked = false         // Desbloquear a UI para o jogador responder
            }, 1000) // Bot "pensa" por 1 segundo

        } else {
            // É a vez do jogador, desbloqueia a UI
            isUiLocked = false
        }
    }
}

