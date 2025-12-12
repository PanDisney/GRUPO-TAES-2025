package com.example.biscataes

import android.content.Context
import android.widget.Toast
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ScoreManager {
    private const val PREFS_NAME = "BiscaPrefs"
    private const val KEY_PLAYER_WINS = "player_wins"
    private const val KEY_BOT_WINS = "bot_wins"
    private const val KEY_DRAWS = "draws"
    private const val KEY_HISTORY = "game_history"
    private const val KEY_MATCHES = "matches_played"
    private const val KEY_CAPOTES = "capotes"
    private const val KEY_BANDEIRAS = "bandeiras"
    private const val KEY_ACHIEVEMENTS_COUNT = "achievements"
    private const val KEY_UNLOCKED_ACHIEVEMENTS = "unlocked_achievements_set"

    private val json = Json { ignoreUnknownKeys = true }

    fun saveGameResult(context: Context, gameLog: GameLog) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Update matches played
        val currentMatches = prefs.getInt(KEY_MATCHES, 0)
        editor.putInt(KEY_MATCHES, currentMatches + 1)

        // Update counters
        when (gameLog.gameResult) {
            GameEngine.GameResult.PLAYER_WINS -> {
                val current = prefs.getInt(KEY_PLAYER_WINS, 0)
                editor.putInt(KEY_PLAYER_WINS, current + 1)

                val currentCapotes = prefs.getInt(KEY_CAPOTES, 0)
                val currentBandeiras = prefs.getInt(KEY_BANDEIRAS, 0)

                checkAchievements(context, prefs, editor, gameLog.playerFinalPoints)
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
        // Serialize the GameLog object to a JSON string
        val newEntry = json.encodeToString(gameLog)
        val currentHistory = prefs.getString(KEY_HISTORY, "") ?: ""

        val historyList = if (currentHistory.isEmpty()) {
            mutableListOf()
        } else {
            currentHistory.split("---END_GAME_LOG---").toMutableList().apply { removeAll { it.isBlank() } }
        }

        // Add to front (newest first)
        historyList.add(0, newEntry)

        // Keep only last 10
        if (historyList.size > 10) {
            historyList.removeAt(historyList.lastIndex)
        }

        editor.putString(KEY_HISTORY, historyList.joinToString("---END_GAME_LOG---"))
        editor.apply()
    }

    private fun checkAchievements(context: Context, prefs: android.content.SharedPreferences, editor: android.content.SharedPreferences.Editor, playerPoints: Int) {
        // Retrieve current unlocked achievements
        val unlockedSet = prefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        var newUnlock = false

        // Achievement: Bandeira Victory (Points > 90)
        if (playerPoints > 90 && !unlockedSet.contains("ACH_BANDEIRA")) {
            unlockedSet.add("ACH_BANDEIRA")
            Toast.makeText(context, "Conquista Desbloqueada: Mestre da Bandeira!", Toast.LENGTH_LONG).show()
            newUnlock = true
        }

        // Achievement: Capote Victory (Points == 120)
        if (playerPoints == 120 && !unlockedSet.contains("ACH_CAPOTE")) {
            unlockedSet.add("ACH_CAPOTE")
            Toast.makeText(context, "Conquista Desbloqueada: Rei do Capote!", Toast.LENGTH_LONG).show()
            newUnlock = true
        }

        if (newUnlock) {
            editor.putStringSet(KEY_UNLOCKED_ACHIEVEMENTS, unlockedSet)
            editor.putInt(KEY_ACHIEVEMENTS_COUNT, unlockedSet.size)
        }
    }

    data class Stats(
        val playerWins: Int,
        val botWins: Int,
        val draws: Int,
        val matchesPlayed: Int,
        val capotes: Int,
        val bandeiras: Int,
        val achievements: Int
    )

    fun getStats(context: Context): Stats {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Stats(
            prefs.getInt(KEY_PLAYER_WINS, 0),
            prefs.getInt(KEY_BOT_WINS, 0),
            prefs.getInt(KEY_DRAWS, 0),
            prefs.getInt(KEY_MATCHES, 0),
            prefs.getInt(KEY_CAPOTES, 0),
            prefs.getInt(KEY_BANDEIRAS, 0),
            prefs.getInt(KEY_ACHIEVEMENTS_COUNT, 0)
        )
    }

    // US-12: Function to get coin balance
    fun getHistory(context: Context): List<GameLog> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyStr = prefs.getString(KEY_HISTORY, "") ?: return emptyList()
        if (historyStr.isEmpty()) return emptyList()

        return historyStr.split("---END_GAME_LOG---")
            .filter { it.isNotBlank() }
            .mapNotNull {
                try {
                    json.decodeFromString<GameLog>(it)
                } catch (e: Exception) {
                    // Log the error or handle corrupted data
                    null
                }
            }
    }
}
