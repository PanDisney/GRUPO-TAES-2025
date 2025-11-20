package com.example.biscataes

import android.content.Context
import android.widget.Toast

object ScoreManager {
    private const val PREFS_NAME = "BiscaPrefs"
    private const val KEY_PLAYER_WINS = "player_wins"
    private const val KEY_BOT_WINS = "bot_wins"
    private const val KEY_DRAWS = "draws"
    private const val KEY_HISTORY = "game_history"
    private const val KEY_COINS = "player_coins" // US-12

    fun saveGameResult(context: Context, result: GameEngine.GameResult, playerPoints: Int, botPoints: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Update counters
        when (result) {
            GameEngine.GameResult.PLAYER_WINS -> {
                val current = prefs.getInt(KEY_PLAYER_WINS, 0)
                editor.putInt(KEY_PLAYER_WINS, current + 1)

                // US-12: Award coins based on score
                val currentCoins = prefs.getInt(KEY_COINS, 0)
                val coinsEarned = when {
                    playerPoints == 120 -> 50 // Capote
                    playerPoints > 90 -> 25   // Good win
                    else -> 10                // Normal win
                }
                editor.putInt(KEY_COINS, currentCoins + coinsEarned)
                // Show a toast message to the user
                Toast.makeText(context, "Ganhou $coinsEarned moedas!", Toast.LENGTH_SHORT).show()
            }
            GameEngine.GameResult.BOT_WINS -> {
                val current = prefs.getInt(KEY_BOT_WINS, 0)
                editor.putInt(KEY_BOT_WINS, current + 1)
            }
            GameEngine.GameResult.DRAW -> {
                val current = prefs.getInt(KEY_DRAWS, 0)
                editor.putInt(KEY_DRAWS, current + 1)
            }
            else -> {}
        }

        // Update history (last 10 games)
        // Format: "PlayerPoints:BotPoints"
        val newEntry = "$playerPoints:$botPoints"
        val currentHistory = prefs.getString(KEY_HISTORY, "") ?: ""

        val historyList = if (currentHistory.isEmpty()) {
            mutableListOf()
        } else {
            currentHistory.split(";").toMutableList()
        }

        // Add to front (newest first)
        historyList.add(0, newEntry)

        // Keep only last 10
        if (historyList.size > 10) {
            historyList.removeAt(historyList.lastIndex)
        }

        editor.putString(KEY_HISTORY, historyList.joinToString(";"))
        editor.apply()
    }

    data class Stats(val playerWins: Int, val botWins: Int, val draws: Int)

    fun getStats(context: Context): Stats {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Stats(
            prefs.getInt(KEY_PLAYER_WINS, 0),
            prefs.getInt(KEY_BOT_WINS, 0),
            prefs.getInt(KEY_DRAWS, 0)
        )
    }

    // US-12: Function to get coin balance
    fun getCoins(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_COINS, 0)
    }

    fun getHistory(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyStr = prefs.getString(KEY_HISTORY, "") ?: return emptyList()
        if (historyStr.isEmpty()) return emptyList()
        return historyStr.split(";")
    }
}