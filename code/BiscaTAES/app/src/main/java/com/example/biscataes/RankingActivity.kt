@file:OptIn(InternalSerializationApi::class)

package com.example.biscataes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Transaction(
    val id: Int,
    val type: String,
    val euros: Int? = null,
    val amount: Int? = null,
    val transaction_date: String? = null
)

// No wrapper class needed if the API returns a list directly: [ {...}, {...} ]
// If it returns { "data": [ ... ] }, keep the wrapper. 
// Based on CoinController: return response()->json($transactions); which is a list.

class RankingActivity : AppCompatActivity() {

    private lateinit var layoutPersonal: ConstraintLayout
    private lateinit var layoutGlobal: LinearLayout
    private lateinit var btnPersonal: Button
    private lateinit var btnGlobal: Button

    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = getAuthToken()
                        if (token != null) {
                            BearerTokens(token, "")
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        layoutPersonal = findViewById(R.id.layoutPersonal)
        layoutGlobal = findViewById(R.id.layoutGlobal)
        btnPersonal = findViewById(R.id.btnPersonal)
        btnGlobal = findViewById(R.id.btnGlobal)

        btnPersonal.setOnClickListener { showPersonalStats() }
        btnGlobal.setOnClickListener { showGlobalRankings() }

        // Load Personal Stats by default
        showPersonalStats()
    }

    private fun showPersonalStats() {
        layoutPersonal.visibility = View.VISIBLE
        layoutGlobal.visibility = View.GONE
        btnPersonal.isEnabled = false
        btnGlobal.isEnabled = true

        fetchPersonalStats()
    }

    private fun fetchPersonalStats() {
        lifecycleScope.launch {
            try {
                // Fetch user data for coins
                val userResponse: ApiResponse = client.get("http://10.0.2.2:8000/api/user").body()
                val currentUser = userResponse.data
                val currentUserEmail = currentUser.email
                val currentCoins = currentUser.coins

                // Fetch matches for game stats
                val matchesResponse: MatchHistoryResponse = client.get("http://10.0.2.2:8000/api/matches").body()
                val matches = matchesResponse.data

                var matchesPlayed = matches.size
                var wins = 0
                var capotes = 0
                var bandeiras = 0

                matches.forEach { match ->
                    if (match.winner?.email == currentUserEmail) {
                        wins++
                        val myPoints = if (match.player1.email == currentUserEmail) match.player1_marks else match.player2_marks
                        if (myPoints != null) {
                             if (myPoints == 120) capotes++
                             else if (myPoints > 90) bandeiras++
                        }
                    }
                }

                // Fetch transactions for coin stats
                var coinsPurchased = 0
                var coinsEarnedPlaying = 0

                try {
                     // CoinController returns a direct list, not wrapped in "data"
                     val transactions: List<Transaction> = client.get("http://10.0.2.2:8000/api/user/transactions").body()
                     
                     transactions.forEach { transaction ->
                         val amount = transaction.amount ?: 0
                         
                         // Check types based on CoinController/Seeder
                         // CoinController sets type='purchase' or 'deduction'
                         // Seeder suggests 'Coin purchase', 'Game payout', etc.
                         // We will check for multiple possibilities to be safe
                         
                         if (transaction.type == "purchase" || transaction.type == "Coin purchase") {
                             coinsPurchased += amount
                         } else if (transaction.type == "Game payout" || transaction.type == "Match payout" || transaction.type == "Bonus") {
                             coinsEarnedPlaying += amount
                         }
                     }
                } catch (e: Exception) {
                    Log.e("RankingActivity", "Error fetching transactions", e)
                }

                updatePersonalStatsUI(matchesPlayed, wins, capotes, bandeiras, currentCoins, coinsPurchased, coinsEarnedPlaying)

            } catch (e: Exception) {
                Log.e("RankingActivity", "Error fetching stats", e)
                Toast.makeText(this@RankingActivity, "Failed to load stats.", Toast.LENGTH_SHORT).show()
                 val localStats = ScoreManager.getStats(this@RankingActivity)
                 updatePersonalStatsUI(localStats.matchesPlayed, localStats.playerWins, localStats.capotes, localStats.bandeiras, localStats.coins, 0, 0)
            }
        }
    }

    private fun updatePersonalStatsUI(matchesPlayed: Int, wins: Int, capotes: Int, bandeiras: Int, currentCoins: Int, purchasedCoins: Int, earnedCoins: Int) {
        findViewById<TextView>(R.id.textMatchesPlayed).text = "Jogos Disputados: $matchesPlayed"
        findViewById<TextView>(R.id.textPlayerWins).text = "Vitórias: $wins"

        val winRate = if (matchesPlayed > 0) {
            (wins.toFloat() / matchesPlayed.toFloat()) * 100
        } else {
            0f
        }
        findViewById<TextView>(R.id.textWinRate).text = "Taxa de Vitória: %.1f%%".format(winRate)

        findViewById<TextView>(R.id.textCapotes).text = "Capotes: $capotes"
        findViewById<TextView>(R.id.textBandeiras).text = "Bandeiras: $bandeiras"

        findViewById<TextView>(R.id.textCurrentCoins).text = "Moedas Atuais: $currentCoins"
        findViewById<TextView>(R.id.textCoinsEarned).text = "Moedas Ganhas (Jogo): $earnedCoins"
        findViewById<TextView>(R.id.textCoinsPurchased).text = "Moedas Compradas: $purchasedCoins"
    }

    private fun showGlobalRankings() {
        layoutPersonal.visibility = View.GONE
        layoutGlobal.visibility = View.VISIBLE
        btnPersonal.isEnabled = true
        btnGlobal.isEnabled = false

        val globalRankings = ScoreManager.getGlobalRankings(this)
        val globalList = findViewById<ListView>(R.id.listViewGlobal)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, globalRankings.mapIndexed { index, entry ->
            "${index + 1}. ${entry.name} - ${entry.wins} Vitórias - ${entry.coins} Moedas - ${entry.achievements} Conquistas"
        })
        globalList.adapter = adapter
    }

    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }
}
