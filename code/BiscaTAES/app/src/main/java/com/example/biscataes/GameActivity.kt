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

class GameActivity : AppCompatActivity() {

    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameEngine = GameEngine()

        // Chamar a nossa função de desenhar (agora atualizada)
        drawPlayerHand()
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
                Log.d("GameActivity", "Clicou na carta: $card (íice: $index)")
                gameEngine.playerPlaysCard(index)
                drawPlayerHand() // Redesenha a mão
                updateTableView()
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
}

