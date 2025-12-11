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
    private const val KEY_COINS = "player_coins" // US-12
    private const val KEY_MATCHES = "matches_played"
    private const val KEY_CAPOTES = "capotes"
    private const val KEY_BANDEIRAS = "bandeiras"
    private const val KEY_ACHIEVEMENTS = "achievements"

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

                // US-12: Award coins based on score
                val currentCoins = prefs.getInt(KEY_COINS, 0)
                val coinsEarned = when {
                    gameLog.playerFinalPoints == 120 -> {
                        editor.putInt(KEY_CAPOTES, currentCapotes + 1)
                        50 // Capote
                    }
                    gameLog.playerFinalPoints > 90 -> {
                        editor.putInt(KEY_BANDEIRAS, currentBandeiras + 1)
                        25   // Good win (Bandeira)
                    }
                    else -> 10                // Normal win
                }
                editor.putInt(KEY_COINS, currentCoins + coinsEarned)
                // Show a toast message to the user
                Toast.makeText(context, "Ganhou $coinsEarned moedas!", Toast.LENGTH_SHORT).show()

                checkAchievements(context, prefs, editor, current + 1, currentCoins + coinsEarned)
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

    private fun checkAchievements(context: Context, prefs: android.content.SharedPreferences, editor: android.content.SharedPreferences.Editor, wins: Int, coins: Int) {
        // Simple mock achievement logic
        var achievements = prefs.getInt(KEY_ACHIEVEMENTS, 0)
        // Example: Achievement for first win
        if (wins == 1 && achievements < 1) {
            achievements++
            Toast.makeText(context, "Conquista Desbloqueada: Primeira Vitória!", Toast.LENGTH_SHORT).show()
        }
        // Example: Achievement for 100 coins
        if (coins >= 100 && achievements < 2) {
            if (achievements == 0 && wins > 1) achievements = 2 // catch up if logic is weird, simplification
            else if (achievements == 1) achievements++

            // Just ensuring it increments, logic can be more complex
        }
        editor.putInt(KEY_ACHIEVEMENTS, achievements)
    }

    data class Stats(
        val playerWins: Int,
        val botWins: Int,
        val draws: Int,
        val matchesPlayed: Int,
        val capotes: Int,
        val bandeiras: Int,
        val coins: Int,
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
            prefs.getInt(KEY_COINS, 0),
            prefs.getInt(KEY_ACHIEVEMENTS, 0)
        )
    }

    // US-12: Function to get coin balance
    fun getCoins(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_COINS, 0)
    }

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

    data class RankingEntry(val name: String, val wins: Int, val coins: Int, val achievements: Int)

    fun getGlobalRankings(context: Context): List<RankingEntry> {
        val stats = getStats(context)
        val playerEntry = RankingEntry("Você", stats.playerWins, stats.coins, stats.achievements)

        // Mock data for other players
        val mockEntries = listOf(
            RankingEntry("Mestre da Bisca", 50, 2500, 10),
            RankingEntry("SuecaKing", 30, 1200, 5),
            RankingEntry("Bot Alpha", 20, 800, 3),
            RankingEntry("Novato123", 2, 50, 0)
        )

        return (mockEntries + playerEntry).sortedByDescending { it.wins }
    }
}
