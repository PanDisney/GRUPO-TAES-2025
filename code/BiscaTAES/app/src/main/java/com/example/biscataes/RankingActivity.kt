package com.example.biscataes

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RankingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        val stats = ScoreManager.getStats(this)
        
        findViewById<TextView>(R.id.textPlayerWins).text = "Vitórias Jogador: ${stats.playerWins}"
        findViewById<TextView>(R.id.textBotWins).text = "Vitórias Bot: ${stats.botWins}"
        findViewById<TextView>(R.id.textDraws).text = "Empates: ${stats.draws}"

        val historyList = findViewById<ListView>(R.id.listViewHistory)
        val history = ScoreManager.getHistory(this)
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, history.map { 
            val scores = it.split(":")
            if (scores.size == 2) {
                "Jogador: ${scores[0]} - Bot: ${scores[1]}"
            } else {
                it
            }
        })
        
        historyList.adapter = adapter
    }
}
