@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.biscataes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.InternalSerializationApi

class GameAdapter(
    private val games: List<Game>,
    private val listener: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameId: TextView = itemView.findViewById(R.id.text_game_id)
        val gameStatusWinner: TextView = itemView.findViewById(R.id.text_game_status) // This will now include status and winner
        val gameScore: TextView = itemView.findViewById(R.id.text_game_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.gameId.text = "Game ${position + 1}:" // Displaying game number

        val winnerText = game.winner?.nickname ?: "In Progress"

        holder.gameStatusWinner.text = "Status: ${
            when (game.status) {
                "E" -> "Ended"
                "PL" -> "Being played or interrupted"
                else -> game.status ?: "Unknown"
            }
        } Winner: $winnerText" // Combine status and winner
        holder.gameScore.text = "Score: ${game.player1_points ?: 0} - ${game.player2_points ?: 0}"
        holder.itemView.setOnClickListener { listener(game) }
    }

    override fun getItemCount(): Int = games.size
}