package com.example.biscataes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameAdapter(private var games: List<Game>) :
    RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameIdTextView: TextView = itemView.findViewById(R.id.textViewGameId)
        val gameTypeTextView: TextView = itemView.findViewById(R.id.textViewGameType)
        val gameStatusTextView: TextView = itemView.findViewById(R.id.textViewGameStatus)
        val gameBeganAtTextView: TextView = itemView.findViewById(R.id.textViewGameBeganAt)
        val gameEndedAtTextView: TextView = itemView.findViewById(R.id.textViewGameEndedAt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_item, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.gameIdTextView.text = "Game ID: ${game.id}"
        holder.gameTypeTextView.text = "Type: ${game.type}"
        holder.gameStatusTextView.text = "Status: ${game.status}"
        holder.gameBeganAtTextView.text = "Began: ${game.began_at}"
        holder.gameEndedAtTextView.text = "Ended: ${game.ended_at ?: "N/A"}"
    }

    override fun getItemCount(): Int = games.size

    fun updateGames(newGames: List<Game>) {
        games = newGames
        notifyDataSetChanged()
    }
}
