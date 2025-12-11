@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.biscataes

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json

class GameDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_details)

        val gameJson = intent.getStringExtra("GAME_JSON")

        if (gameJson != null) {
            try {
                val game = Json { ignoreUnknownKeys = true }.decodeFromString(Game.serializer(), gameJson)

                // Display Score
                findViewById<TextView>(R.id.textFinalScore).text =
                    "Pontuação Final: ${game.player1?.nickname ?: "Player 1"} ${game.player1_points ?: 0} - ${game.player2?.nickname ?: "Bot"} ${game.player2_points ?: 0}"

                // Display Winner and Status
                findViewById<TextView>(R.id.textGameResult).text =
                    "Status: ${when (game.status) {
                        "E" -> "Terminado"
                        "PL" -> "A ser jogado ou interrompido"
                        else -> game.status ?: "Unknown"
                    }} Vencedor: ${game.winner?.nickname ?: "Em progresso"}"

                // Display Trump Card
                val trumpCardView = findViewById<TextView>(R.id.trump_card_view)
                val trumpCardString = game.trump_card
                if (trumpCardString != null) {
                    val trumpCard = parseCard(trumpCardString)
                    if (trumpCard != null) {
                        styleCardTextView(trumpCardView, trumpCard)
                        trumpCardView.visibility = android.view.View.VISIBLE
                    } else {
                        trumpCardView.visibility = android.view.View.GONE
                    }
                } else {
                    trumpCardView.visibility = android.view.View.GONE
                }

                val tricksLayout = findViewById<android.widget.TableLayout>(R.id.tricksLayout)
                // Clear all views except the header row
                tricksLayout.removeViews(1, tricksLayout.childCount - 1)


                game.tricks?.forEach { trick ->
                    val trickRow = layoutInflater.inflate(R.layout.trick_row, null) as android.widget.TableRow

                    val trickNumberTextView = trickRow.findViewById<TextView>(R.id.trick_number_textview)
                    val player1NameTextView = trickRow.findViewById<TextView>(R.id.player1_name_textview)
                    val player1CardTextView = trickRow.findViewById<TextView>(R.id.player1_card_textview)
                    val player2NameTextView = trickRow.findViewById<TextView>(R.id.player2_name_textview)
                    val player2CardTextView = trickRow.findViewById<TextView>(R.id.player2_card_textview)
                    val winnerLabelPlayer1 = trickRow.findViewById<TextView>(R.id.winner_label_player1)
                    val winnerLabelPlayer2 = trickRow.findViewById<TextView>(R.id.winner_label_player2)

                    trickNumberTextView.text = "Trick ${trick.trick_number}:"
                    player1NameTextView.text = game.player1?.nickname ?: "Player 1"
                    player2NameTextView.text = game.player2?.nickname ?: "Bot"

                    val player1Card = parseCard(trick.player1_card)
                    val player2Card = parseCard(trick.player2_card)

                    if (player1Card != null) {
                        styleCardTextView(player1CardTextView, player1Card)
                    }
                    if (player2Card != null) {
                        styleCardTextView(player2CardTextView, player2Card)
                    }

                    when (trick.winner_id) {
                        game.player1?.id -> winnerLabelPlayer1.visibility = android.view.View.VISIBLE
                        game.player2?.id -> winnerLabelPlayer2.visibility = android.view.View.VISIBLE
                    }

                    tricksLayout.addView(trickRow)

                } ?: run {
                    val noTricksRow = android.widget.TableRow(this)
                    val textView = TextView(this)
                    textView.text = "No tricks recorded."
                    noTricksRow.addView(textView)
                    tricksLayout.addView(noTricksRow)
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Failed to parse game details: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        } else {
            Toast.makeText(this, "Failed to load game details.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun parseCard(cardString: String): Card? {
        return try {
            val parts = cardString.split("_OF_")
            val rank = Rank.valueOf(parts[0])
            val suit = Suit.valueOf(parts[1])
            Card(suit, rank)
        } catch (e: Exception) {
            null
        }
    }

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

    private fun getSuitSymbol(suit: Suit): String {
        return when (suit) {
            Suit.HEARTS -> "♥"
            Suit.DIAMONDS -> "♦"
            Suit.CLUBS -> "♣"
            Suit.SPADES -> "♠"
        }
    }

    private fun styleCardTextView(cardTextView: TextView, card: Card) {
        val rankSymbol = getRankSymbol(card.rank)
        val suitSymbol = getSuitSymbol(card.suit)
        cardTextView.text = "$rankSymbol\n$suitSymbol"

        val shape = android.graphics.drawable.GradientDrawable()
        shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        shape.setColor(android.graphics.Color.WHITE)
        shape.setStroke(2, android.graphics.Color.BLACK)
        shape.cornerRadius = 15f
        cardTextView.background = shape

        if (card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS) {
            cardTextView.setTextColor(android.graphics.Color.RED)
        } else {
            cardTextView.setTextColor(android.graphics.Color.BLACK)
        }
    }
}