package com.example.biscataes

import android.os.Bundle
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RankingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        val stats = ScoreManager.getStats(this)
        
        findViewById<TextView>(R.id.textPlayerWins).text = "Vitórias Jogador: ${stats.playerWins}"
        findViewById<TextView>(R.id.textBotWins).text = "Vitórias Bot: ${stats.botWins}"
        findViewById<TextView>(R.id.textDraws).text = "Empates: ${stats.draws}"

        val history = ScoreManager.getHistory(this)
        val historyList = findViewById<ListView>(R.id.listViewHistory)
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, history.map { gameLog ->
            val resultText = when (gameLog.gameResult) {
                GameEngine.GameResult.PLAYER_WINS -> "Ganhou"
                GameEngine.GameResult.BOT_WINS -> "Perdeu"
                GameEngine.GameResult.DRAW -> "Empatou"
                else -> "Indefinido"
            }
            "Resultado: $resultText - Pts Jogador: ${gameLog.playerFinalPoints} - Pts Bot: ${gameLog.botFinalPoints}"
        })
        
        historyList.adapter = adapter

        historyList.setOnItemClickListener { parent, view, position, id ->
            val selectedGameLog = history[position]
            val serializedGameLog = Json.encodeToString(selectedGameLog)
            val intent = Intent(this, GameDetailsActivity::class.java)
            intent.putExtra("GAME_HISTORY_ITEM", serializedGameLog)
            startActivity(intent)
        }
    }
}
