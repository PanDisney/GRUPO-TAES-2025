@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.biscataes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MatchHistoryAdapter(
    private val matches: List<GameMatch>,
    private val listener: (GameMatch) -> Unit
) : RecyclerView.Adapter<MatchHistoryAdapter.MatchViewHolder>() {

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val matchId: TextView = itemView.findViewById(R.id.text_match_id)
        val players: TextView = itemView.findViewById(R.id.text_players)
        val score: TextView = itemView.findViewById(R.id.text_score)
        val winner: TextView = itemView.findViewById(R.id.text_winner)
        val status: TextView = itemView.findViewById(R.id.text_status)
        val layout: LinearLayout = itemView.findViewById(R.id.match_item_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_history, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        holder.matchId.text = "Match ID: ${match.id}"
        holder.players.text = "Players: ${match.player1.nickname} vs ${match.player2.nickname}"
        holder.score.text = "Score: ${match.player1_marks ?: 0} - ${match.player2_marks ?: 0}"
        holder.winner.text = "Winner: ${match.winner?.nickname ?: "In Progress"}"
        holder.status.text = "Status: ${when (match.status) {
            "E" -> "Ended"
            "PL" -> "Being played or interrupted"
            else -> match.status ?: "Unknown"
        }}"

        holder.layout.setOnClickListener { listener(match) }
    }

    override fun getItemCount(): Int = matches.size
}