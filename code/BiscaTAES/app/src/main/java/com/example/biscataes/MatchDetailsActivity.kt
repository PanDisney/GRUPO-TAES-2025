@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.biscataes

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json

class MatchDetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private var games = mutableListOf<Game>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_details)

        val matchJson = intent.getStringExtra("MATCH_JSON")

        if (matchJson != null) {
            val match = Json.decodeFromString(GameMatch.serializer(), matchJson)
            displayMatchDetails(match)
            setupRecyclerView(match.games ?: emptyList())
        } else {
            Toast.makeText(this, "Failed to load match details.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun displayMatchDetails(match: GameMatch) {
        findViewById<TextView>(R.id.text_match_details_title).text = "Match #${match.id}"
        findViewById<TextView>(R.id.text_match_players).text = "Players: ${match.player1.nickname} vs ${match.player2.nickname}"
        findViewById<TextView>(R.id.text_match_score).text = "Score: ${match.player1_marks ?: 0} - ${match.player2_marks ?: 0}"
        findViewById<TextView>(R.id.text_match_winner).text = "Winner: ${match.winner?.nickname ?: "In Progress"}"

        val matchTimeTextView = findViewById<TextView>(R.id.text_match_time)
        match.total_time?.let { totalTimeSeconds ->
            val minutes = totalTimeSeconds / 60
            val seconds = totalTimeSeconds % 60
            matchTimeTextView.text = "Tempo da Partida: %02d:%02d".format(minutes, seconds)
        } ?: run {
            matchTimeTextView.text = "Tempo da Partida: N/A"
        }

        val matchDateTextView = findViewById<TextView>(R.id.text_match_date)
        match.began_at?.let {
            // Extract date part from "YYYY-MM-DD HH:MM:SS"
            val date = it.substring(0, 10)
            matchDateTextView.text = "Data: $date"
        } ?: run {
            matchDateTextView.text = "Data: N/A"
        }
    }

    private fun setupRecyclerView(fetchedGames: List<Game>) {
        recyclerView = findViewById(R.id.recycler_view_games)
        recyclerView.layoutManager = LinearLayoutManager(this)
        games.addAll(fetchedGames)
        gameAdapter = GameAdapter(games) { game ->
            // Handle click on a game item
            Toast.makeText(this, "Clicked on Game ID: ${game.id}", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, GameDetailsActivity::class.java)
            val gameJson = Json.encodeToString(Game.serializer(), game)
            intent.putExtra("GAME_JSON", gameJson)
            startActivity(intent)
        }
        recyclerView.adapter = gameAdapter
    }
}