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
                val game = Json.decodeFromString(Game.serializer(), gameJson)

                // Display Score
                findViewById<TextView>(R.id.textFinalScore).text =
                    "Pontuação Final: Jogador ${game.player1_points ?: 0} - Bot ${game.player2_points ?: 0}"

                // Display Winner and Status
                findViewById<TextView>(R.id.textGameResult).text =
                    "Status: ${when (game.status) {
                        "E" -> "Terminado"
                        "PL" -> "A ser jogado ou interrompido"
                        else -> game.status ?: "Unknown"
                    }} Vencedor: ${game.winner?.nickname ?: "Em progresso"}"

                // Trump card is not available in the Game object from the API
                findViewById<TextView>(R.id.textTrumpCard).text = "Trunfo: N/A"

                val playerMovesLayout = findViewById<LinearLayout>(R.id.playerMovesLayout)
                val botMovesLayout = findViewById<LinearLayout>(R.id.botMovesLayout)

                // Display Player Moves
                playerMovesLayout.removeAllViews() // Clear previous views
                val playerMovesTitle = TextView(this).apply { text = "Movimentos do Jogador:"; setTypeface(null, android.graphics.Typeface.BOLD) }
                playerMovesLayout.addView(playerMovesTitle)
                game.player1_moves?.forEach { move ->
                    val textView = TextView(this)
                    textView.text = "Turno${move.turn}:${move.card}"
                    playerMovesLayout.addView(textView)
                } ?: run {
                    val textView = TextView(this)
                    textView.text = "No player moves recorded."
                    playerMovesLayout.addView(textView)
                }

                // Display Bot Moves
                botMovesLayout.removeAllViews() // Clear previous views
                val botMovesTitle = TextView(this).apply { text = "Movimentos do Bot:"; setTypeface(null, android.graphics.Typeface.BOLD) }
                botMovesLayout.addView(botMovesTitle)
                game.player2_moves?.forEach { move ->
                    val textView = TextView(this)
                    textView.text = "Turno${move.turn}:${move.card}"
                    botMovesLayout.addView(textView)
                } ?: run {
                    val textView = TextView(this)
                    textView.text = "No bot moves recorded."
                    botMovesLayout.addView(textView)
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Failed to parse game details.", Toast.LENGTH_LONG).show()
                finish()
            }
        } else {
            Toast.makeText(this, "Failed to load game details.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}