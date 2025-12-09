package com.example.biscataes

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class GameDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_details)

        val serializedGameLog = intent.getStringExtra("GAME_HISTORY_ITEM")

        if (serializedGameLog != null) {
            val gameLog = Json.decodeFromString<GameLog>(serializedGameLog)

            findViewById<TextView>(R.id.textFinalScore).text =
                "Pontuação Final: Jogador ${gameLog.playerFinalPoints} - Bot ${gameLog.botFinalPoints}"
            findViewById<TextView>(R.id.textTrumpCard).text = "Trunfo: ${gameLog.trumpCard ?: "N/A"}"
            findViewById<TextView>(R.id.textGameResult).text = "Resultado: ${
                when (gameLog.gameResult) {
                    GameEngine.GameResult.PLAYER_WINS -> "Jogador Venceu"
                    GameEngine.GameResult.BOT_WINS -> "Bot Venceu"
                    GameEngine.GameResult.DRAW -> "Empate"
                    else -> "Indefinido"
                }
            }"

            val playerMovesLayout = findViewById<LinearLayout>(R.id.playerMovesLayout)
            val botMovesLayout = findViewById<LinearLayout>(R.id.botMovesLayout)

            // Display Player Moves
            for (move in gameLog.playerMoves) {
                val textView = TextView(this)
                textView.text = "Turn ${move.turn}: ${move.card}"
                playerMovesLayout.addView(textView)
            }

            // Display Bot Moves
            for (move in gameLog.botMoves) {
                val textView = TextView(this)
                textView.text = "Turn ${move.turn}: ${move.card}"
                botMovesLayout.addView(textView)
            }
        } else {
            findViewById<TextView>(R.id.textGameDetails).text = "Nenhum detalhe do jogo disponível."
        }
    }
}
